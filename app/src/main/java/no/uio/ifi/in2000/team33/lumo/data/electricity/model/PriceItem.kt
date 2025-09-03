package no.uio.ifi.in2000.team33.lumo.data.electricity.model

/**
 * data class representing an electricity price item from the hvakosterstrommen.no API
 *
 * @property EUR_per_kWh The price in EUR per kWh
 * @property EXR The exchange rate from EUR to NOK
 * @property NOK_per_kWh The price in NOK per kWh
 * @property time_end The ISO-8601 formatted end time of the price item (end of the hour)
 * @property time_start The ISO-8601 formatted start time of the price item (start of the hour)
 */

data class PriceItem(
    val EUR_per_kWh: Double,
    val EXR: Double, //exchangerate
    val NOK_per_kWh: Double,
    val time_end: String,
    val time_start: String
)