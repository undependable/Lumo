package no.uio.ifi.in2000.team33.lumo.data.address

import no.uio.ifi.in2000.team33.lumo.data.address.model.AddressResponse
import no.uio.ifi.in2000.team33.lumo.data.address.model.Adresser

class AddressRepository {
    // Datasource for getting the address
    private val dataSource = AddressDataSource()

    // Get Addresser object from address
    suspend fun getAddressInformation(address: String): Adresser? {
        val response: AddressResponse = dataSource.getAddressResponse(address)
        // If the response is valid, return it
        try {
            if (response.adresser.isEmpty()) return null
            return response.adresser[0]
            // If the response is invalid, return an error message
        } catch (e: Exception) {
            return null
        }
    }
    // Get Addresser object from coordinates
    suspend fun getAddressInformationFromPoint(lat: Double, lon: Double): Adresser? {
        val response: AddressResponse = dataSource.getAddressFromPoint(lat, lon)
        // If the response is valid, return it
        try {
            if (response.adresser.isEmpty()) return null
            return response.adresser[0]
            // If the response is invalid, return an error message
        } catch (e: Exception) {
            return null
        }
    }
    // Get all addresses from given address
    suspend fun getAllAddresses(address: String): List<Adresser>? {
        val response: AddressResponse = dataSource.getAddressResponse(address)
        // If the response is valid, return it
        try {
            if (response.adresser.isEmpty()) return null
            return response.adresser
        } catch (e: Exception) {
            return null
        }
    }
}
