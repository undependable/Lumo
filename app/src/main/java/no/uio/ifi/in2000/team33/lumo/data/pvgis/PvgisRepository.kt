package no.uio.ifi.in2000.team33.lumo.data.pvgis

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import no.uio.ifi.in2000.team33.lumo.data.address.model.Representasjonspunkt
import no.uio.ifi.in2000.team33.lumo.data.frost.FrostRepository
import no.uio.ifi.in2000.team33.lumo.data.frost.model.station.WeatherType
import java.util.concurrent.ConcurrentHashMap

/**
 * Repository class that combines solar data from the PVGIS API with weather data from the Frost API.
 *
 * This repository calculates:
 * - Estimated incoming solar energy (Wh/m2/year)
 * - Estimated annual electricity production (kWh/year), adjusted for cloud cover, snow cover, and temperature
 *
 * Results are cached to improve performance and avoid redundant API calls.
 */
class PvgisRepository {
    private val dataSource = PvgisDataSource()
    private val frostRepository = FrostRepository()

    private val productionCache = ConcurrentHashMap<String, Double>()
    private val monthlyProductionCache = ConcurrentHashMap<String, List<Pair<Int, Double>>>()

    companion object {
        const val HOURS_IN_YEAR = 8760.0
        const val CLOUD_FACTOR = 0.75
        const val SNOW_FACTOR = 0.9
        const val TEMP_COEFF = -0.004
        const val REFERENCE_TEMP = 25.0
    }

    /**
     * Adjusts incoming solar irradiance (Wh/m2) based on cloud and snow cover.
     *
     * @param influx Incoming solar irradiance (Wh/m2)
     * @param cloudCover Cloud coverage as a value between 0.0 and 1.0
     * @param snowCover Snow coverage as a value between 0.0 and 1.0
     * @return Adjusted irradiance
     */
    inline fun adjustIrradianceForCloudAndSnow(
        influx: Double,
        cloudCover: Double,
        snowCover: Double
    ): Double {
        val cloudMultiplier = 1 - cloudCover * CLOUD_FACTOR
        val snowMultiplier = 1 - snowCover * SNOW_FACTOR
        return influx * cloudMultiplier * snowMultiplier
    }

    /**
     * Adjusts power output based on ambient temperature.
     *
     * @param basePower Raw power output before temperature correction
     * @param temperature Current ambient temperature (°C)
     * @param tempCoeff Temperature coefficient (default -0.004)
     * @param referenceTemp Reference temperature for solar panels (°C, default 25.0)
     * @return Temperature-corrected power output
     */
    inline fun adjustPowerWithTemperature(
        basePower: Double,
        temperature: Double,
        tempCoeff: Double = TEMP_COEFF,
        referenceTemp: Double = REFERENCE_TEMP
    ): Double {
        val correctionFactor = 1 + tempCoeff * (temperature - referenceTemp)
        return basePower * correctionFactor
    }

    /**
     * Calculates adjusted annual electricity production (kWh/year).
     *
     * @param lat Latitude of the location
     * @param lon Longitude of the location
     * @param area Panel area in square meters
     * @param efficiency Solar panel efficiency (0.0 - 1.0)
     * @param angle Tilt angle of the solar panel (°)
     * @param aspect Panel orientation angle (0 = South, 90 = West, -90 = East)
     * @return Adjusted annual electricity production in kWh
     */
    suspend fun calculateAdjustedAnnualProduction(
        lat: Double,
        lon: Double,
        area: Double,
        efficiency: Double,
        angle: Int,
        aspect: Int
    ): Double? = coroutineScope {
        val cacheKey = "prod-$lat-$lon-$area-$efficiency-$angle-$aspect"
        productionCache[cacheKey]?.let {
            println("Cache hit for adjusted production: $cacheKey")
            return@coroutineScope it
        }

        val timesJob = async { dataSource.fetchHourlyData(lat, lon, angle, aspect) }
        val productionJob = async {
            dataSource.fetchProductionData(
                lat = lat,
                lon = lon,
                peakPower = area * efficiency,
                loss = 14,
                angle = angle,
                aspect = aspect
            )
        }
        val weatherJob = async { frostRepository.getWeatherData(lon, lat) }

        val timesResponse = timesJob.await()
        val productionResponse = productionJob.await()
        val weatherData = weatherJob.await()

        if (timesResponse == null || productionResponse == null) return@coroutineScope null

        val gIValues = timesResponse.outputs?.hourly?.mapNotNull { it.Gi }
        if (gIValues.isNullOrEmpty()) return@coroutineScope null

        val averageInflux = gIValues.sum() / gIValues.size
        val rawProduction =
            productionResponse.outputs?.totals?.fixed?.E_y ?: return@coroutineScope null

        val cloudCover = (weatherData[WeatherType.CLOUD]?.average() ?: 0.0) / 100.0
        val snowCover = (weatherData[WeatherType.SNOW]?.average() ?: 0.0) / 100.0
        val temperature = weatherData[WeatherType.TEMPERATURE]?.average() ?: 10.0

        val weatherAdjusted =
            rawProduction * adjustIrradianceForCloudAndSnow(1.0, cloudCover, snowCover)
        val tempAdjusted = adjustPowerWithTemperature(weatherAdjusted, temperature)

        productionCache[cacheKey] = tempAdjusted
        return@coroutineScope tempAdjusted
    }

    /**
     * Calculates adjusted incoming solar irradiation (Wh/m²/year).
     *
     * @param lat Latitude of the location
     * @param lon Longitude of the location
     * @param point Geographic representation point (used for reference)
     * @return Adjusted annual incoming solar energy in Wh/m²/year
     */
    suspend fun calculateAdjustedIrradiation(
        lat: Double,
        lon: Double,
        point: Representasjonspunkt
    ): Double? = coroutineScope {
        val timesJob = async { dataSource.fetchHourlyData(lat, lon) }
        val weatherJob = async { frostRepository.getWeatherData(lon, lat) }

        val timesResponse = timesJob.await()
        val weatherData = weatherJob.await()

        val gIValues =
            timesResponse?.outputs?.hourly?.mapNotNull { it.Gi } ?: return@coroutineScope null
        val averageInflux = if (gIValues.isNotEmpty()) gIValues.sum() / gIValues.size else 0.0

        val cloud = (weatherData[WeatherType.CLOUD]?.average() ?: 0.0) / 100.0
        val snow = (weatherData[WeatherType.SNOW]?.average() ?: 0.0) / 100.0

        val adjusted = adjustIrradianceForCloudAndSnow(averageInflux, cloud, snow)
        return@coroutineScope adjusted * HOURS_IN_YEAR
    }

    /**
     * Calculates adjusted monthly production (kWh/month).
     *
     * @param lat Latitude of the location
     * @param lon Longitude of the location
     * @param area Panel area (m²)
     * @param efficiency Efficiency of the panel (0.0 - 1.0)
     * @param angle Tilt angle (degrees)
     * @param aspect Orientation (0 = south, 90 = west, -90 = east)
     * @return List of (month, adjusted kWh) pairs
     */
    suspend fun calculateAdjustedMonthlyProduction(
        lat: Double,
        lon: Double,
        area: Double,
        efficiency: Double,
        angle: Int,
        aspect: Int
    ): List<Pair<Int, Double>>? = coroutineScope {
        val cacheKey = "maaned-$lat-$lon-$area-$efficiency-$angle-$aspect"
        monthlyProductionCache[cacheKey]?.let {
            println("Cache hit for monthly production: $cacheKey")
            return@coroutineScope it
        }

        val weatherJob = async { frostRepository.getWeatherData(lon, lat) }
        val productionJob = async {
            dataSource.fetchProductionData(
                lat = lat,
                lon = lon,
                peakPower = area * efficiency,
                loss = 14,
                angle = angle,
                aspect = aspect
            )
        }

        val weatherData = weatherJob.await()
        val productionResponse = productionJob.await()

        val clouds = weatherData[WeatherType.CLOUD] ?: return@coroutineScope null
        val snow = weatherData[WeatherType.SNOW] ?: return@coroutineScope null
        val temps = weatherData[WeatherType.TEMPERATURE] ?: return@coroutineScope null
        val baseMonthly = productionResponse?.outputs?.monthly?.fixed ?: return@coroutineScope null

        val result = ArrayList<Pair<Int, Double>>(12)

        for (index in baseMonthly.indices) {
            val base = baseMonthly[index].E_m
            val cloud = clouds.getOrNull(index) ?: 0.0
            val snowVal = snow.getOrNull(index) ?: 0.0
            val temp = temps.getOrNull(index) ?: 10.0

            val weatherFactor = adjustIrradianceForCloudAndSnow(1.0, cloud / 100.0, snowVal / 100.0)
            val tempFactor = adjustPowerWithTemperature(1.0, temp)
            val adjusted = base * weatherFactor * tempFactor

            result.add(index + 1 to adjusted)
        }

        monthlyProductionCache[cacheKey] = result
        return@coroutineScope result
    }
}