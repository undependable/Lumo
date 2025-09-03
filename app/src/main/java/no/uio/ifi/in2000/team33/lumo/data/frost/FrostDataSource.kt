package no.uio.ifi.in2000.team33.lumo.data.frost

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.team33.lumo.data.address.model.Representasjonspunkt
import no.uio.ifi.in2000.team33.lumo.data.frost.model.observation.ObservationResponse
import no.uio.ifi.in2000.team33.lumo.data.frost.model.station.WeatherStation

class FrostDataSource {
    // Simple cache for API responses
    private val observationsCache = mutableMapOf<String, ObservationResponse>()
    private val stationsCache = mutableMapOf<String, List<String>>()

    private val client = HttpClient(CIO) {
        install(Auth) {
            basic {
                credentials {
                    BasicAuthCredentials(
                        username = "da233aef-bbba-4b48-a159-a57551e69316",
                        password = "fa3ab9b1-7258-4dee-b3b5-265d84e85fce"
                    )
                }
                sendWithoutRequest { true }
            }
        }
        install(ContentNegotiation) {
            gson()
        }
    }
    // Get station for multiple weather types
    suspend fun getWeatherStation(representationPoint: Representasjonspunkt): WeatherStation =
        withContext(Dispatchers.IO) {
            val lon: Double = representationPoint.lon
            val lat: Double = representationPoint.lat

            val url =
                "https://frost.met.no/sources/v0.jsonld?types=SensorSystem&elements=mean(air_temperature%20P1M)%2C%20mean(cloud_area_fraction%20P1M)%2C%20mean(snow_coverage_type%20P1M)&geometry=nearest(POINT($lon%20$lat))&validtime=2023-01-01%2F2023-12-31"

            client.get(url).body()
        }

    // Get station for temperature
    suspend fun getTemperatureStation(lon: Double, lat: Double): String =
        withContext(Dispatchers.IO) {
            // Check cache first
            val cacheKey = "temp-$lon-$lat-single"
            stationsCache[cacheKey]?.firstOrNull()?.let { return@withContext it }

            println("Finding nearest temperature station for coordinates ($lon, $lat)...")
            val url =
                "https://frost.met.no/sources/v0.jsonld?types=SensorSystem&elements=mean(air_temperature%20P1M)&geometry=nearest(POINT($lon%20$lat))&nearestmaxcount=1"

            val response = client.get(url).body<WeatherStation>()
            val sourceId = response.data.firstOrNull()?.id ?: ""

            if (sourceId.isNotEmpty()) {
                stationsCache[cacheKey] = listOf(sourceId)
            }
            sourceId
        }

    // Get station for cloud coverage
    suspend fun getCloudStation(lon: Double, lat: Double): String = withContext(Dispatchers.IO) {
        // Check cache first
        val cacheKey = "cloud-$lon-$lat-single"
        stationsCache[cacheKey]?.firstOrNull()?.let { return@withContext it }

        println("Finding nearest cloud station for coordinates ($lon, $lat)...")
        val url =
            "https://frost.met.no/sources/v0.jsonld?types=SensorSystem&elements=mean(cloud_area_fraction%20P1M)&geometry=nearest(POINT($lon%20$lat))&nearestmaxcount=1"

        val response = client.get(url).body<WeatherStation>()
        val sourceId = response.data.firstOrNull()?.id ?: ""

        if (sourceId.isNotEmpty()) {
            stationsCache[cacheKey] = listOf(sourceId)
        }
        sourceId
    }

    // Get station for snow coverage
    suspend fun getSnowStation(lon: Double, lat: Double): String = withContext(Dispatchers.IO) {
        // Check cache first
        val cacheKey = "snow-$lon-$lat-single"
        stationsCache[cacheKey]?.firstOrNull()?.let { return@withContext it }

        println("Finding nearest snow station for coordinates ($lon, $lat)...")
        val url =
            "https://frost.met.no/sources/v0.jsonld?types=SensorSystem&elements=mean(snow_coverage_type%20P1M)&geometry=nearest(POINT($lon%20$lat))&nearestmaxcount=1"

        val response = client.get(url).body<WeatherStation>()
        val sourceId = response.data.firstOrNull()?.id ?: ""

        if (sourceId.isNotEmpty()) {
            stationsCache[cacheKey] = listOf(sourceId)
        }
        sourceId
    }
    // Get ObservationResponse for Weather type and station
    suspend fun getObservations(
        stationId: String,
        element: String,
        timeResolution: String,
        startTime: String,
        endTime: String
    ): ObservationResponse = withContext(Dispatchers.IO) {
        // Check cache first
        val cacheKey = "$stationId-$element-$timeResolution-$startTime-$endTime"
        observationsCache[cacheKey]?.let { return@withContext it }

        val url =
            "https://frost.met.no/observations/v0.jsonld?sources=$stationId&elements=$element&timeresolutions=$timeResolution&referencetime=$startTime%2F$endTime&timeoffsets=default&levels=default"

        val response = client.get(url).body<ObservationResponse>()

        // Cache the result
        observationsCache[cacheKey] = response
        response
    }

    suspend fun getCurrentWeather(weatherStation: String): ObservationResponse =
        withContext(Dispatchers.IO) {
            val url =
                "https://frost.met.no/observations/v0.jsonld?sources=$weatherStation&referencetime=latest&elements=air_temperature"

            client.get(url).body()
        }
}