package no.uio.ifi.in2000.team33.lumo.data.electricity

import android.content.Context
import com.google.gson.JsonParser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.gson.gson
import no.uio.ifi.in2000.team33.lumo.data.electricity.model.SsbQuery
import no.uio.ifi.in2000.team33.lumo.data.electricity.model.SsbRequest
import no.uio.ifi.in2000.team33.lumo.data.electricity.model.SsbResponse
import no.uio.ifi.in2000.team33.lumo.data.electricity.model.SsbSelection
import no.uio.ifi.in2000.team33.lumo.data.electricity.model.PriceItem
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Data source class responsible for fetching electricity price data from external APIs.
 *
 * Specifically connects to:
 * - hvakosterstrommen.no for daily and historical hourly prices.
 *
 * Uses Ktor HTTP client with Gson for JSON deserialization.
 */
class ElectricityPriceDataSource {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            gson()
        }
    }

    // NO1 = Oslo / Øst-Norge
    // NO2 = Kristiansand / Sør-Norge
    // NO3 = Trondheim / Midt-Norge
    // NO4 = Tromsø / Nord-Norge
    // NO5 = Bergen / Vest-Norge
    // https://www.hvakosterstrommen.no/api/v1/prices/2025/03-19_NO1.json

    private val lastFetchDate = mutableMapOf<String, LocalDate>()
    // Cache for the actual price data by region
    private val todaysRegionalPrice = mutableMapOf<String, List<PriceItem>>()

    /**
     * Fetches electricity prices for the current day for a given region.
     *
     * @param region The electricity price zone (e.g., "NO1", "NO2")
     * @return A list of hourly electricity price items for today.
     */
    suspend fun fetchPricesToday(region: String): List<PriceItem> {
        val today = LocalDate.now()

        // Check if we already have data for today for this region
        if (lastFetchDate[region] == today && todaysRegionalPrice.containsKey(region)) {
            println("Returning cached prices for $region")
            return todaysRegionalPrice[region]!!
        }
        return try {
            println("try to fetch")
            val formatter = DateTimeFormatter.ofPattern("MM-dd")
            val formattedDate = today.format(formatter)
            val url =
                "https://www.hvakosterstrommen.no/api/v1/prices/${today.year}/${formattedDate}_$region.json"
            println("url: $url")
            val response: HttpResponse = client.get(url)
            if (response.status == HttpStatusCode.OK) {
                val priceItems = response.body<List<PriceItem>>()
                todaysRegionalPrice[region] = priceItems
                lastFetchDate[region] = today
                priceItems
            } else {
                println("Error: ${response.status}")
                emptyList()
            }
        } catch (e: Exception) {
            println("feil ${e.message}")
            emptyList()
        }
    }

    /**
     * Fetches electricity prices for a specific date for a given region.
     *
     * @param region The electricity price zone (e.g., "NO1", "NO2")
     * @param date The date to fetch prices for (format: LocalDate)
     * @return A list of hourly electricity price items for the specified date.
     */
    suspend fun fetchPrices(region: String, date: LocalDate): List<PriceItem> {
        return try {
            println("try to fetch")
            val formatter = DateTimeFormatter.ofPattern("MM-dd")
            val formattedDate = date.format(formatter)
            val url =
                "https://www.hvakosterstrommen.no/api/v1/prices/${date.year}/${formattedDate}_$region.json"
            println("url: $url")
            val response: HttpResponse = client.get(url)
            if (response.status == HttpStatusCode.OK) {
                response.body<List<PriceItem>>()
            } else {
                println("Error: ${response.status}")
                emptyList()
            }
        } catch (e: Exception) {
            println("feil ${e.message}")
            emptyList()
        }
    }

    /**
     * Sends a POST request to SSB to fetch quarterly electricity prices for 2024
     *
     * This function sends a structured JSON-stat2 request to the SSB API, specifically requesting
     * the variable "KraftprisUA" for the four quarters of 2024. It then parses the response and returns
     * a list of (quarterLabel, priceInØre)
     *
     * @return A list of pairs where each pair contains a quarter label and its price value
     */
    suspend fun fetchQuarterlyPricesSSB(): List<Pair<String, Double>> {
        val url = "https://data.ssb.no/api/v0/no/table/09387/"

        val payload = SsbRequest(
            query = listOf(
                SsbQuery(
                    code = "ContentsCode",
                    selection = SsbSelection(
                        filter = "item",
                        values = listOf("KraftprisUA")
                    )
                ),
                SsbQuery(
                    code = "Tid",
                    selection = SsbSelection(
                        filter = "item",
                        values = listOf("2024K1", "2024K2", "2024K3", "2024K4")
                    )
                )
            ),
            response = SsbResponse(format = "json-stat2")
        )

        return try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }

            val json = JsonParser.parseString(response.bodyAsText()).asJsonObject

            val values = json["value"].asJsonArray.map { it.asDouble }
            val labels = json["dimension"].asJsonObject["Tid"]
                .asJsonObject["category"]
                .asJsonObject["label"]
                .asJsonObject.entrySet()
                .map { it.value.asString }

            labels.zip(values)
        } catch (e: Exception) {
            println("Feil ved henting av SSB-data: ${e.message}")
            emptyList()
        }
    }

    /**
     * Loads monthly electricity prices for a specific region from a local JSON file in assets
     *
     * The function reads the `hvakosterstrommenData.json` file from the assets folder, which contains
     * pre-fetched monthly price data categorized by region
     * It parses the JSON to extract the price data for the requested region and returns it as a map
     *
     * @param context The application context used to access the assets
     * @param region The electricity region code
     * @return A map of (monthName -> priceInKr) for the given region
     */
    fun getMonthlyPriceMapForRegion(context: Context, region: String): Map<String, Double> {
        val jsonString = context.assets.open("hvakosterstrommenData.json")
            .bufferedReader().use { it.readText() }

        val json = JSONObject(jsonString)
        val zones = json.getJSONArray("zones")
        for (i in 0 until zones.length()) {
            val zoneObj = zones.getJSONObject(i)
            if (zoneObj.getString("zone") == region) {
                val data = zoneObj.getJSONObject("data")
                return data.keys().asSequence().associateWith { data.getDouble(it) }
            }
        }
        return emptyMap()
    }
}