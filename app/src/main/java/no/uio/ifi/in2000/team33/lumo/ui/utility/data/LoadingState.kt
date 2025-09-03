package no.uio.ifi.in2000.team33.lumo.ui.utility.data

// Consolidated loading states
sealed class LoadingState {
    object Idle : LoadingState()
    object LoadingMapPoints : LoadingState()
    object LoadingTemperature : LoadingState()
    object LoadingEstimates : LoadingState()
    object LoadingMonthlyData : LoadingState()
    object SavingTakflate : LoadingState()
}