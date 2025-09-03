package no.uio.ifi.in2000.team33.lumo.data.electricity

import no.uio.ifi.in2000.team33.lumo.data.electricity.model.PriceItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import kotlin.math.roundToInt

/**
 * Repository class for retrieving electricity price data from various sources.
 *
 * Provides access to:
 * - Today's hourly prices
 * - Specific date prices
 * - The current hour's price (formatted)
 *
 * Uses a data source (`ElectricityPriceDataSource`) for actual HTTP/API fetching.
 */
class ElectricityPriceRepository {
    companion object {
        const val DEFAULT_SELLING_PRICE = 0.60
    }

    private val electricityDataSource: ElectricityPriceDataSource = ElectricityPriceDataSource()


    /**
     * Fetches hourly electricity prices for today from hvakosterstrommen.no.
     *
     * @param region The electricity region (e.g., NO1, NO2)
     * @return List of price items for today, one for each hour
     */
    suspend fun fetchPricesToday(region: String): List<PriceItem> {
        return electricityDataSource.fetchPricesToday(region)
    }

    /**
     * Fetches hourly electricity prices for a specific date.
     *
     * @param region The electricity region (e.g., NO1, NO2)
     * @param date The date to fetch prices for
     * @return List of price items for the specified date
     */
    suspend fun fetchPrices(region: String, date: LocalDate): List<PriceItem> {
        return electricityDataSource.fetchPrices(region, date)
    }

    /**
     * Fetches the current hour's electricity price and formats it as a string.
     *
     * - If the price is less than 1.0 kr, it will be returned in øre (e.g., "89.8 øre").
     * - If the price is 1.0 kr or more, it will be formatted as kr (e.g., "1.02 kr").
     *
     * @param region The electricity region (e.g., NO1)
     * @return A formatted string with the current hour's price, or an error message if unavailable
     */
    suspend fun fetchHourPrice(region: String): String {
        val prices = fetchPricesToday(region)
        if (prices.isEmpty()) {
            return "No prices found"
        }

        val now = LocalDateTime.now()
        val thisHour = now.hour
        var hourPrice: Double? = null

        prices.forEach { priceItem ->
            val startTime = OffsetDateTime.parse(priceItem.time_start)
            val priceItemHour = startTime.hour
            if (priceItemHour == thisHour) {
                hourPrice = priceItem.NOK_per_kWh
                return@forEach
            }
        }
        return hourPrice?.let { price ->
            if (price < 1.0) {
                val priceOre = (price * 10000).roundToInt() / 100.0
                "$priceOre øre"
            } else {
                val roundedPrice = (price * 100).roundToInt() / 1000.0
                "$roundedPrice kr"
            }
        } ?: "Couldn't fetch current hour price"
    }

    /**
     * Fetches the average electricity price for 2024 from SSB
     *
     * @return A formatted string representing the average price in kr/kWh for 2024
     */
    suspend fun fetchAverageSSBPriceFor2024(): String {
        val prices = electricityDataSource.fetchQuarterlyPricesSSB()
        if (prices.isEmpty()) return "Ingen priser fra SSB tilgjengelig"

        // Convert values from øre to kr, calculate the average, and round to two decimal places
        val avg = (prices.map { it.second }.average() / 100).let {
            (it * 100).roundToInt() / 100.0
        }
        return "$avg kr/kWh (SSB snitt 2024)"
    }
}