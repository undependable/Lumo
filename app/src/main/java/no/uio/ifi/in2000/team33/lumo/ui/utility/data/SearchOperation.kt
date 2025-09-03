package no.uio.ifi.in2000.team33.lumo.ui.utility.data

// Sealed class for search operations
sealed class SearchOperation {
    object Idle : SearchOperation()
    object SearchingAddresses : SearchOperation()
    object SearchingCoordinates : SearchOperation()
    object LoadingLocation : SearchOperation()
}