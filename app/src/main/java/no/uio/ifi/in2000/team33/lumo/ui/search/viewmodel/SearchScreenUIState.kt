package no.uio.ifi.in2000.team33.lumo.ui.search.viewmodel

import no.uio.ifi.in2000.team33.lumo.data.address.model.Adresser
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.ErrorType
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.MapPoint

data class SearchScreenUIState(
    // Address search
    val suggestedAddresses: List<Adresser> = emptyList(),
    val showSuggestions: Boolean = false,
    val searchBarText: String = "",

    // Map interaction
    val selectedMapPoint: MapPoint? = null,
    val showAddressBottomSheet: Boolean = false,
    val showTakflateBottomSheet: Boolean = false,
    val showInfoModal: Boolean = false,
    val mapStyle2D: Boolean = false,

    // Location permission
    val showLocationPermissionDialog: Boolean = false,

    // Error handling
    val showError: Boolean = false,
    val error: ErrorType? = null
) {
    // Computed properties
    val hasAddressSuggestions: Boolean get() = suggestedAddresses.isNotEmpty()
    val hasSelectedPoint: Boolean get() = selectedMapPoint != null
    val hasError: Boolean get() = showError && error != null
}