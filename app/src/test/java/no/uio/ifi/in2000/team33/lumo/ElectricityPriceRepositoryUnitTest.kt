package no.uio.ifi.in2000.team33.lumo

import kotlinx.coroutines.runBlocking
import no.uio.ifi.in2000.team33.lumo.data.electricity.ElectricityPriceRepository
import org.junit.Assert.*
import org.junit.Test

class ElectricityPriceRepositoryUnitTest {

    //TDD - Relatert til repository
    @Test
    fun `fetchAverageSSBPriceFor2024 should return valid formatted price`() = runBlocking {
        // Arrange
        val repository = ElectricityPriceRepository()

        // Act
        val result = repository.fetchAverageSSBPriceFor2024()

        // Assert
        println("Result: $result")

        // Sjekk at resultatet ikke er en feilmelding
        assertFalse("Resultatet indikerer tom liste fra SSB", result.contains("Ingen"))

        // Sjekk at den inneholder noe tallverdi
        val numberRegex = Regex("""\d+(\.\d+)?""")
        assertTrue("Resultatet inneholder ikke tall", numberRegex.containsMatchIn(result))

        // Sjekk at den slutter med forventet tekst
        assertTrue("Resultatet slutter ikke med forventet tekst", result.contains("SSB snitt 2024"))
    }
}