package no.uio.ifi.in2000.team33.lumo

import kotlinx.coroutines.runBlocking
import no.uio.ifi.in2000.team33.lumo.data.electricity.ElectricityPriceDataSource
import org.junit.Assert.*
import org.junit.Test

class ElectricityPriceDataSourceUnitTest {

    //TDD - Relatert til API
    @Test
    fun `fetchQuarterlyPricesSSB should return non-empty list with positive values`() = runBlocking {
        // Arrange
        val dataSource = ElectricityPriceDataSource()

        // Act
        val result = dataSource.fetchQuarterlyPricesSSB()

        // Assert
        assertTrue("Result should not be empty", result.isNotEmpty())
        assertTrue("All values should be positive", result.all { it.second > 0 })
    }
}