package no.uio.ifi.in2000.team33.lumo.ui.mappoint

import no.uio.ifi.in2000.team33.lumo.data.electricity.SavingsResult
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.MapPoint

data class MapPointUIState(
    // Core data
    val mapPoints: MutableList<MapPoint> = mutableListOf(),
    val currentMapPoint: MapPoint? = null,
    val selectedTakflateIndex: Int = 0,

    // Production and savings data
    val monthlyProduction: List<Pair<Int, Double>>? = null,
    val chartDataString: String = "",
    val monthlySavings: List<Pair<Int, Double>>? = null,
    val profitability: SavingsResult? = null
) {
    val hasMonthlyData: Boolean get() = !monthlyProduction.isNullOrEmpty()

}