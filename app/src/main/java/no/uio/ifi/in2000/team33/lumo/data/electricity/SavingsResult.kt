package no.uio.ifi.in2000.team33.lumo.data.electricity

/**
 * Data class representing the result of a profitability calculation
 *
 * @property annualSavings The total amount of money (in NOK) saved per year
 * @property paybackTime The number of years required to pay back the installation cost
 */
data class SavingsResult(
    val annualSavings: Double,
    val paybackTime: Double
)

/**
 * Calculates the monetary savings for each month based on monthly production and electricity prices
 *
 * @param monthlyProduction A list of pairs containing (monthNumber, kWh produced)
 * @param monthlyPrices A map of month names to electricity prices in kr/kWh
 * @param consumptionRate The proportion of produced electricity that is consumed by the user
 * @param sellPrice The price (in kr/kWh) received for selling unused electricity back to the grid
 * @return A list of pairs where each pair contains: monthNumber, savings in kr for that month
 */
fun calculateMonthlySavings(
    monthlyProduction: List<Pair<Int, Double>>,
    monthlyPrices: Map<String, Double>,
    consumptionRate: Double,
    sellPrice: Double
): List<Pair<Int, Double>> {
    return monthlyProduction.map { (monthNumber, kWhProduced) ->
        val monthName = numberToMonthName(monthNumber)
        val price = monthlyPrices[monthName] ?: 1.0

        val usedSavings = kWhProduced * consumptionRate * price
        val soldEarnings = kWhProduced * (1 - consumptionRate) * sellPrice
        val totalSavings = usedSavings + soldEarnings

        monthNumber to totalSavings
    }
}

/**
 * Converts a numerical month value (1–12) to its corresponding abbreviated name
 *
 * @param month The month number (1 = January, 12 = December)
 * @return The corresponding month
 */
private fun numberToMonthName(month: Int): String {
    return listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )[month - 1]
}