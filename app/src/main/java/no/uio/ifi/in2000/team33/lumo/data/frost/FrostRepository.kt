package no.uio.ifi.in2000.team33.lumo.data.frost

import android.util.Log
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import no.uio.ifi.in2000.team33.lumo.data.address.model.Representasjonspunkt
import no.uio.ifi.in2000.team33.lumo.data.frost.model.observation.ObservationResponse
import no.uio.ifi.in2000.team33.lumo.data.frost.model.station.WeatherType
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class FrostRepository(private val dataSource: FrostDataSource = FrostDataSource()) {
    private val temperatureCache = mutableMapOf<String, Double>()
    private val lastTemperatureFetchTime = mutableMapOf<String, LocalDateTime>()

    suspend fun getStationId(representationPoint: Representasjonspunkt): String =
        dataSource.getWeatherStation(representationPoint).data.firstOrNull()?.id
            ?: "her skjedde det noe galt"

    suspend fun getCurrentTemperature(stationId: String): Double {

        val now = LocalDateTime.now()
        val lastFetchTime = lastTemperatureFetchTime[stationId]

        // Check if we have a cached value that's less than an hour old
        if (lastFetchTime != null &&
            ChronoUnit.HOURS.between(lastFetchTime, now) < 1 &&
            temperatureCache.containsKey(stationId)) {
            Log.i("GETCURRENTTEMP", "CACHE HIT")
            return temperatureCache[stationId] ?: 0.0
        }

        val observationResponse: ObservationResponse = dataSource.getCurrentWeather(stationId)
        val temperature = observationResponse.data.firstOrNull()?.observations?.firstOrNull()?.value ?: 0.0

        temperatureCache[stationId] = temperature
        lastTemperatureFetchTime[stationId] = now
        return temperature
    }
    // Get full map of weather data for multiple weather types
    suspend fun getWeatherData(lon: Double, lat: Double): Map<WeatherType, List<Double>> =
        coroutineScope {
            val timeResolution = "P1M"
            val startTime = "2023-01-01"
            val endTime = "2023-12-31"

            // Define all weather types we need data for
            val allWeatherTypes = listOf(
                WeatherType.TEMPERATURE,
                WeatherType.CLOUD,
                WeatherType.SNOW
            )

            // Create a map to hold the default values (12 months of zeros for each type)
            val defaultValues = List(12) { 0.0 }
            val defaultResults = allWeatherTypes.associateWith { defaultValues }

            // Results storage
            val weatherResults = mutableMapOf<WeatherType, Deferred<List<Double>?>>()
            val usedStations = mutableMapOf<WeatherType, String>()

            // Fetch element-specific stations in parallel
            val temperatureStationDef = async { dataSource.getTemperatureStation(lon, lat) }
            val cloudStationDef = async { dataSource.getCloudStation(lon, lat) }
            val snowStationDef = async { dataSource.getSnowStation(lon, lat) }

            // Wait for all stations to be retrieved
            val temperatureStation = temperatureStationDef.await()
            val cloudStation = cloudStationDef.await()
            val snowStation = snowStationDef.await()

            // Process temperature data
            weatherResults[WeatherType.TEMPERATURE] = async {
                val element = "mean(air_temperature%20P1M)"
                processStationData(
                    temperatureStation,
                    element,
                    timeResolution,
                    startTime,
                    endTime,
                    WeatherType.TEMPERATURE,
                    usedStations
                )
            }

            // Process cloud data
            weatherResults[WeatherType.CLOUD] = async {
                val element = "mean(cloud_area_fraction%20P1M)"
                processStationData(
                    cloudStation,
                    element,
                    timeResolution,
                    startTime,
                    endTime,
                    WeatherType.CLOUD,
                    usedStations
                )
            }

            // Process snow data
            weatherResults[WeatherType.SNOW] = async {
                val element = "mean(snow_coverage_type%20P1M)"
                processStationData(
                    snowStation,
                    element,
                    timeResolution,
                    startTime,
                    endTime,
                    WeatherType.SNOW,
                    usedStations
                )
            }

            // Start with default values for all weather types
            val finalResults = defaultResults.toMutableMap()

            // Update with actual values where available
            weatherResults.forEach { (weatherType, deferredResult) ->
                deferredResult.await()?.let { values ->
                    finalResults[weatherType] = values
                }
            }

            // Log which stations were used and which had default values
            println("\nStations used for weather data:")
            allWeatherTypes.forEach { type ->
                if (type in usedStations) {
                    println("${type.name}: Station ${usedStations[type]}")
                } else {
                    println("${type.name}: USING DEFAULT VALUES (no data found)")
                }
            }

            finalResults
        }
    // Process station data for a specific weather type
    private suspend fun processStationData(
        stationId: String,
        element: String,
        timeResolution: String,
        startTime: String,
        endTime: String,
        weatherType: WeatherType,
        usedStations: MutableMap<WeatherType, String>
    ): List<Double>? {
        if (stationId.isEmpty()) {
            println("No station found for ${weatherType.name}")
            return null
        }

        try {
            val observations = dataSource.getObservations(
                stationId, element, timeResolution, startTime, endTime
            )

            // Verify we have 12 values (complete monthly data)
            val totalValues = observations.data.sumOf { it.observations.size }

            if (observations.data.isNotEmpty() && totalValues == 12) {
                // Found valid data for this element
                usedStations[weatherType] = stationId

                // Extract monthly values
                return observations.data.flatMap { data ->
                    data.observations.map { it.value }
                }
            } else {
                println("Insufficient data for $element from station $stationId: found $totalValues values, expected 12")
            }
        } catch (e: Exception) {
            val errorMsg = if (e is io.ktor.client.call.NoTransformationFoundException)
                "412 Precondition Failed: On fetching $element from $stationId"
            else
                "Error: On fetching $element from $stationId - ${e.message}"

            println(errorMsg)
        }
        return null
    }

}