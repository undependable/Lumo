package no.uio.ifi.in2000.team33.lumo

import kotlinx.coroutines.runBlocking
import no.uio.ifi.in2000.team33.lumo.data.address.model.Representasjonspunkt
import no.uio.ifi.in2000.team33.lumo.data.frost.FrostRepository
import no.uio.ifi.in2000.team33.lumo.data.frost.model.station.WeatherType
import org.junit.Test

import org.junit.Assert.*

class FrostRepositoryUnitTest {

    // Oslo coordinates
    private val osloLon = 10.7522
    private val osloLat = 59.9139
    private val osloPoint = Representasjonspunkt("", osloLon, osloLat)

    // Kautokeino coordinates
    private val kautokeinoLon = 23.0412
    private val kautokeinoLat = 68.9962
    private val kautokeinoPoint = Representasjonspunkt("", kautokeinoLon, kautokeinoLat)

    @Test
    fun getStationId_returnsValidValue() = runBlocking {
        // Arrange
        val repository = FrostRepository()

        // Act
        val stationId = repository.getStationId(osloPoint)

        // Assert
        assertNotNull(stationId)
        assertFalse(stationId.isEmpty())
    }

    @Test
    fun getCurrentTemperature_returnsNumericValue() = runBlocking {
        // Arrange
        val repository = FrostRepository()
        val osloStationId = repository.getStationId(osloPoint)

        // Act
        val temperature = repository.getCurrentTemperature(osloStationId)

        // Assert
        // Temperature should be a reasonable value (e.g., between -50 and +50)
        assertTrue(temperature >= -50.0 && temperature <= 50.0)
    }

    @Test
    fun getWeatherData_returnsCompleteDataMap() = runBlocking {
        // Arrange
        val repository = FrostRepository()

        // Act
        val result = repository.getWeatherData(osloLon, osloLat)

        // Assert
        assertNotNull(result)
        assertEquals(3, result.size)
        assertTrue(result.containsKey(WeatherType.TEMPERATURE))
        assertTrue(result.containsKey(WeatherType.CLOUD))
        assertTrue(result.containsKey(WeatherType.SNOW))
        assertEquals(12, result[WeatherType.TEMPERATURE]?.size)
        assertEquals(12, result[WeatherType.CLOUD]?.size)
        assertEquals(12, result[WeatherType.SNOW]?.size)
    }
    @Test
    fun getStationId_withKautokeinoCoordinates_returnsValidValue() = runBlocking {
        // Arrange
        val repository = FrostRepository()

        // Act
        val stationId = repository.getStationId(kautokeinoPoint)

        // Assert
        assertNotNull(stationId)
        assertFalse(stationId.isEmpty())
    }

    @Test
    fun getCurrentTemperature_withKautokeinoStation_returnsNumericValue() = runBlocking {
        // Arrange
        val repository = FrostRepository()
        val kautokeinoStationId = repository.getStationId(kautokeinoPoint)

        // Act
        val temperature = repository.getCurrentTemperature(kautokeinoStationId)

        // Assert
        // Temperature should be a reasonable value for Kautokeino (e.g., between -50 and +30)
        assertTrue(temperature >= -50.0 && temperature <= 30.0)
    }

    @Test
    fun getWeatherData_withKautokeinoCoordinates_returnsCompleteDataMap() = runBlocking {
        // Arrange
        val repository = FrostRepository()

        // Act
        val result = repository.getWeatherData(kautokeinoLon, kautokeinoLat)

        // Assert
        assertNotNull(result)
        assertEquals(3, result.size)
        assertTrue(result.containsKey(WeatherType.TEMPERATURE))
        assertTrue(result.containsKey(WeatherType.CLOUD))
        assertTrue(result.containsKey(WeatherType.SNOW))
        assertEquals(12, result[WeatherType.TEMPERATURE]?.size)
        assertEquals(12, result[WeatherType.CLOUD]?.size)
        assertEquals(12, result[WeatherType.SNOW]?.size)

        // Kautokeino typically has colder temperatures than Oslo
        val avgTemperature = result[WeatherType.TEMPERATURE]?.average() ?: 0.0
        // Typically average annual temperature is below 5°C in Kautokeino
        assertTrue("Average temperature in Kautokeino should be below 5°C", avgTemperature < 5.0)

        // Kautokeino typically has more snow than Oslo
        val avgSnow = result[WeatherType.SNOW]?.average() ?: 0.0
        // Verify that there's significant snow data
        assertTrue("Snow data should be significant in Kautokeino", avgSnow > 0.0)
    }
}