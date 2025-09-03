package no.uio.ifi.in2000.team33.lumo.data.pvgis.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Custom deserializer for parsing hourly data objects from the PVGIS API response.
 *
 * PVGIS returns hourly solar data with field names that contain special characters,
 * such as "G(i)" for solar irradiation, which makes default Gson parsing fail.
 * This class manually extracts these fields and handles optional null values.
 */
class HourlyDataDeserializer : JsonDeserializer<HourlyData> {

    /**
     * Parses a single hourly data entry from the PVGIS "hourly" JSON array.
     *
     * @param json The JsonElement representing one object in the "hourly" array.
     * @param typeOfT The type we're deserializing into (HourlyData).
     * @param context Gson context (unused here).
     * @return A fully constructed HourlyData instance with optional fields safely parsed.
     */
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): HourlyData {
        val obj = json.asJsonObject

        // Extract time as a string (ISO 8601 format), fallback to empty string if missing
        val time = obj["time"]?.asString ?: ""

        // Extract "G(i)" (global tilted irradiance), nullable
        val Gi = if (obj.has("G(i)") && !obj["G(i)"].isJsonNull) obj["G(i)"].asDouble else null

        // Extract temperature at 2 meters above ground (T2m), nullable
        val T2m = if (obj.has("T2m") && !obj["T2m"].isJsonNull) obj["T2m"].asDouble else null

        // Extract power output P (if present), nullable
        val P = if (obj.has("P") && !obj["P"].isJsonNull) obj["P"].asDouble else null

        // Construct and return the HourlyData data class instance
        return HourlyData(
            time = time,
            Gi = Gi,
            T2m = T2m,
            P = P
        )
    }
}
