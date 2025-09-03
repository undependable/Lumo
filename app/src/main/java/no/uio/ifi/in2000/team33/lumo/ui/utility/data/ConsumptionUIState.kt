package no.uio.ifi.in2000.team33.lumo.ui.utility.data

data class ConsumptionUIState(
    val selectedView: Int = 0, // 0 for Yearly, 1 for Monthly
    val yearlyInputText: String = "",
    val monthlyInputTexts: Map<String, String> = mapOf(
        "january" to "",
        "february" to "",
        "march" to "",
        "april" to "",
        "may" to "",
        "june" to "",
        "july" to "",
        "august" to "",
        "september" to "",
        "october" to "",
        "november" to "",
        "december" to ""
    ),
    val showError: Boolean = false,
    val showMinimumError: Boolean = false
)