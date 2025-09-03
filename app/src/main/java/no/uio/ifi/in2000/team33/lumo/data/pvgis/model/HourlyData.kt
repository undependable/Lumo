package no.uio.ifi.in2000.team33.lumo.data.pvgis.model

import com.google.gson.annotations.JsonAdapter

// This data class represents a single hourly data point from PVGIS
// It uses a custom deserializer to correctly handle field names like "G(i)"
@JsonAdapter(HourlyDataDeserializer::class)
data class HourlyData(
    val time: String,   // Timestamp for the data point (e.g., "2018-01-01T00:00")
    val Gi: Double?,    // Global irradiance on the tilted plane (W/m²), corresponds to "G(i)"
    val T2m: Double?,   // Air temperature at 2 meters (°C)
    val P: Double?      // Power output (W) at that hour, if available
)
