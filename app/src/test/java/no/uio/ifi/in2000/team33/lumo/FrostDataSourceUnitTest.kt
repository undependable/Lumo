package no.uio.ifi.in2000.team33.lumo

import org.junit.Assert.*
import org.junit.Test
import kotlinx.coroutines.runBlocking
import no.uio.ifi.in2000.team33.lumo.data.address.model.Representasjonspunkt
import no.uio.ifi.in2000.team33.lumo.data.frost.FrostDataSource

import kotlin.system.measureTimeMillis

class FrostDataSourceUnitTest {

    // Oslo coordinates
    private val osloLon = 10.7522
    private val osloLat = 59.9139
    private val osloPoint = Representasjonspunkt("", osloLon, osloLat)

    @Test
    fun getWeatherStation_returnsValidStation() = runBlocking {
        // Arrange
        val dataSource = FrostDataSource()

        // Act
        val weatherStation = dataSource.getWeatherStation(osloPoint)

        // Assert
        assertNotNull(weatherStation)
        assertNotNull(weatherStation.data)
        assertFalse(weatherStation.data.isEmpty())
    }

    @Test
    fun getStationMethods_returnValidIds() = runBlocking {
        // Arrange
        val dataSource = FrostDataSource()

        // Act
        val tempStationId = dataSource.getTemperatureStation(osloLon, osloLat)
        val cloudStationId = dataSource.getCloudStation(osloLon, osloLat)
        val snowStationId = dataSource.getSnowStation(osloLon, osloLat)

        // Assert
        assertFalse(tempStationId.isEmpty())
        assertFalse(cloudStationId.isEmpty())
        assertFalse(snowStationId.isEmpty())
    }

    @Test
    fun getCurrentWeather_returnsValidData() = runBlocking {
        // Arrange
        val dataSource = FrostDataSource()
        val stationId = dataSource.getTemperatureStation(osloLon, osloLat)

        // Act
        val observations = dataSource.getCurrentWeather(stationId)

        // Assert
        assertNotNull(observations)
        assertNotNull(observations.data)
        assertFalse(observations.data.isEmpty())
    }

    @Test
    fun stationCache_worksProperly() = runBlocking {
        // Arrange
        val dataSource = FrostDataSource()

        // Act - First call should take longer (no cache)
        val firstCallTime = measureTimeMillis {
            dataSource.getTemperatureStation(osloLon, osloLat)
        }

        // Second call should be faster (cached)
        val secondCallTime = measureTimeMillis {
            dataSource.getTemperatureStation(osloLon, osloLat)
        }

        // Assert
        assertTrue("Second call should be faster due to caching", secondCallTime < firstCallTime)
    }

    @Test
    fun observationsCache_worksProperly() = runBlocking {
        // Arrange
        val dataSource = FrostDataSource()
        val stationId = dataSource.getTemperatureStation(osloLon, osloLat)
        val element = "mean(air_temperature%20P1M)"
        val timeResolution = "P1M"
        val startTime = "2023-01-01"
        val endTime = "2023-12-31"

        // Act - First call
        val firstCallTime = measureTimeMillis {
            dataSource.getObservations(stationId, element, timeResolution, startTime, endTime)
        }

        // Second call (should use cache)
        val secondCallTime = measureTimeMillis {
            dataSource.getObservations(stationId, element, timeResolution, startTime, endTime)
        }

        // Assert
        assertTrue("Second call should be faster due to caching", secondCallTime < firstCallTime)
    }
}