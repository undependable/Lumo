package no.uio.ifi.in2000.team33.lumo.data.address.model

import kotlinx.serialization.Serializable

@Serializable
data class Representasjonspunkt(
    var epsg: String,
    val lat: Double,
    val lon: Double
)