package no.uio.ifi.in2000.team33.lumo.data.address

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.gson.gson
import no.uio.ifi.in2000.team33.lumo.data.address.model.AddressResponse


class AddressDataSource {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            gson()
        }
    }
    // Cache for address data
    private val addressCache = mutableMapOf<String, AddressResponse>()
    private val pointCache = mutableMapOf<String, AddressResponse>()

    // Get AddressResponse from API
    suspend fun getAddressResponse(address: String): AddressResponse {
        val cacheKey = address.lowercase().trim()
        addressCache[cacheKey]?.let {
            println("Returning cached address data for '$address'")
            return it
        }

        val newAddress = address.replace(" ", "%20") // Encodes spaces in address
        val url =
            "https://ws.geonorge.no/adresser/v1/sok?sok=$newAddress&fuzzy=true&utkoordsys=4258&treffPerSide=5&side=0&asciiKompatibel=true"
        val response = client.get(url)

        val addressResponse = response.body<AddressResponse>()
        addressCache[cacheKey] = addressResponse

        return addressResponse
    }
    // Get AddressResponse from coordinates
    suspend fun getAddressFromPoint(lat: Double, lon: Double): AddressResponse {
        val cacheKey = "${lat}_${lon}"

        pointCache[cacheKey]?.let {
            println("Returning cached point data for coordinates ($lat, $lon)")
            return it
        }
        val url =
            "https://ws.geonorge.no/adresser/v1/punktsok?radius=100&lat=${lat}&lon=${lon}&treffPerSide=1"
        val response = client.get(url)
        val addressResponse = response.body<AddressResponse>()

        // Update the cache with the new data
        pointCache[cacheKey] = addressResponse

        return addressResponse
    }
}