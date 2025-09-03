package no.uio.ifi.in2000.team33.lumo.data.pvgis

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.team33.lumo.data.pvgis.model.PvgisResponse
import java.util.concurrent.ConcurrentHashMap

/**
 * Data source for fetching solar radiation and production data from the PVGIS API.
 *
 * This class is responsible for accessing two endpoints:
 * - Returns hourly solar irradiance and temperature data.
 * - Returns estimated annual and daily energy production in kWh.
 *
 * Uses a Ktor HTTP client with Gson support for JSON deserialization.
 */
class PvgisDataSource {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            gson()
        }
    }

    // Simple caching system
    private val hourlyDataCache = ConcurrentHashMap<String, PvgisResponse>()
    private val productionDataCache = ConcurrentHashMap<String, PvgisResponse>()

    /**
     * Fetches hourly solar radiation and temperature data (e.g., G(i) and T2m)
     * from the PVGIS API. Uses a caching mechanism to avoid redundant requests
     * for the same parameters.
     *
     * Example request:
     * https://re.jrc.ec.europa.eu/api/v5_2/seriescalc
     *   ?lat=59.91&lon=10.75
     *   &angle=35&aspect=180
     *   &mountingplace=building
     *   &pvtechchoice=crystSi
     *   &optimalinclination=0
     *   &startyear=2018&endyear=2018
     *   &outputformat=json
     *
     * @param lat Latitude of the panel location.
     * @param lon Longitude of the panel location.
     * @param angle Tilt angle of the solar panel.
     * @param aspect Orientation direction.
     * @return PvgisResponse containing hourly data, or null if the request fails.
     */
    suspend fun fetchHourlyData(
        lat: Double,
        lon: Double,
        angle: Int = 35,
        aspect: Int = 180
    ): PvgisResponse? = withContext(Dispatchers.IO) {
        val cacheKey = "series-$lat-$lon-$angle-$aspect"
        hourlyDataCache[cacheKey]?.let { cachedResponse ->
            println("PVGIS Cache hit for hourly data: $cacheKey")
            return@withContext cachedResponse
        }

        val url = buildPvgisUrl(
            endpoint = "seriescalc",
            lat = lat,
            lon = lon,
            angle = angle,
            aspect = aspect
        )

        println("PVGIS seriescalc request:\n$url")
        try {
            client.get(url).body<PvgisResponse>().also { response ->
                hourlyDataCache[cacheKey] = response
                return@withContext response
            }
        } catch (e: Exception) {
            println("Error during seriescalc request: ${e.message}")
            return@withContext null
        }
    }

    /**
     * Fetches estimated annual and daily solar energy production (E_y and E_d) from the PVGIS API.
     *
     * This function:
     * - Constructs a query to PVGIS with panel and location parameters.
     * - Uses a simple cache to avoid redundant network calls for the same input.
     * - Returns a parsed PVGIS response containing production estimates.
     *
     * Example request:
     * https://re.jrc.ec.europa.eu/api/v5_2/PVcalc
     *   ?lat=59.91&lon=10.75
     *   &peakpower=5.0
     *   &loss=14
     *   &angle=35&aspect=180
     *   &mountingplace=building
     *   &pvtechchoice=crystSi
     *   &optimalinclination=0
     *   &outputformat=json
     *
     * @param lat Latitude of the panel location.
     * @param lon Longitude of the panel location.
     * @param peakPower Installed peak power capacity in kWp. Default is 5.0 kWp.
     * @param loss System loss percentage (e.g., inverter losses, temperature losses). Default is 14%.
     * @param angle Tilt angle of the solar panel (0 = horizontal, 90 = vertical).
     * @param aspect Orientation angle (0 = South, 90 = West, 180 = North, 270 = East).
     * @return PvgisResponse object if the request succeeds; null otherwise.
     */
    suspend fun fetchProductionData(
        lat: Double,
        lon: Double,
        peakPower: Double = 5.0,
        loss: Int = 14,
        angle: Int,
        aspect: Int
    ): PvgisResponse? = withContext(Dispatchers.IO) {
        val cacheKey = "pvcalc-$lat-$lon-$peakPower-$loss-$angle-$aspect"
        productionDataCache[cacheKey]?.let { cachedResponse ->
            println("PVGIS Cache hit for production data: $cacheKey")
            return@withContext cachedResponse
        }

        val url = buildString {
            append("https://re.jrc.ec.europa.eu/api/v5_2/PVcalc")
            append("?lat=$lat&lon=$lon&peakpower=$peakPower&loss=$loss")
            append("&angle=$angle&aspect=$aspect&mountingplace=building")
            append("&pvtechchoice=crystSi&optimalinclination=0&outputformat=json")
        }

        println("PVGIS PVcalc request:\n$url")

        try {
            client.get(url).body<PvgisResponse>().also { response ->
                productionDataCache[cacheKey] = response
                return@withContext response
            }
        } catch (e: Exception) {
            println("Error during PVcalc request: ${e.message}")
            return@withContext null
        }
    }

    /**
     * Utility method for constructing a PVGIS API URL
     */
    private fun buildPvgisUrl(
        endpoint: String,
        lat: Double,
        lon: Double,
        angle: Int,
        aspect: Int,
        peakPower: Double? = null,
        loss: Int? = null
    ): String = buildString {
        append("https://re.jrc.ec.europa.eu/api/v5_2/$endpoint")
        append("?lat=$lat&lon=$lon")
        if (peakPower != null) append("&peakpower=$peakPower")
        if (loss != null) append("&loss=$loss")
        append("&angle=$angle&aspect=$aspect&mountingplace=building")
        append("&pvtechchoice=crystSi&optimalinclination=0")
        if (endpoint == "seriescalc") append("&startyear=2018&endyear=2018")
        append("&outputformat=json")
    }

}