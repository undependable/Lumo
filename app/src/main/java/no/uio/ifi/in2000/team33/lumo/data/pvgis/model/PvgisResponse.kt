package no.uio.ifi.in2000.team33.lumo.data.pvgis.model

// Full response model from PVGIS
data class PvgisResponse(
    val inputs: Inputs,
    val outputs: SeriesOutputs?
)

// Request input metadata
data class Inputs(
    val location: Location
)

// Geographic location details
data class Location(
    val latitude: Double,
    val longitude: Double
)

// All output returned by PVGIS
data class SeriesOutputs(
    val hourly: List<HourlyData>?,     // Hourly data points
    val totals: Totals?,               // Aggregated total values
    val monthly: MonthlyWrapper? = null // Monthly estimates
)

// Total production output block
data class Totals(
    val fixed: FixedTotals?
)

// Fixed system production estimates
data class FixedTotals(
    val E_d: Double,  // Average daily production (kWh/day)
    val E_y: Double   // Estimated annual production (kWh/year)
)

// Monthly production data wrapper
data class MonthlyWrapper(
    val fixed: List<MonthlyData>
)

// Single month's production estimate
data class MonthlyData(
    val month: Int,   // Month number (1 to 12)
    val E_m: Double   // Energy produced in that month (kWh)
)
