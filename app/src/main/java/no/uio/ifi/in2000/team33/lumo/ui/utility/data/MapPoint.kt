package no.uio.ifi.in2000.team33.lumo.ui.utility.data

import no.uio.ifi.in2000.team33.lumo.ui.home.ViewModel.RoofData

/**
 * Immutable data class representing a map point with solar panel calculations
 * Always create new instances instead of mutating existing ones to ensure proper StateFlow emissions
 */
data class MapPoint(
    val name: String,
    val lat: Double,
    val lon: Double,
    val closestStationID: String,
    val areaCode: String,
    val areaPlace: String,
    val isHouse: Boolean,
    val region: String,
    val registeredRoofs: MutableList<RoofData> = mutableListOf(),
    val isFavorite: Boolean = false,
    val annualEstimate: Double = 0.0,
    val temperature: Double = 10000.0, // Default value indicating not loaded
    val priceThisHour: String = "",
    val annualPrice: String = ""
) {
    // Computed properties
    val hasTemperature: Boolean get() = temperature != 10000.0
    val hasPrice: Boolean get() = priceThisHour.isNotEmpty()
    val hasAnnualEstimate: Boolean get() = annualEstimate > 0.0


}