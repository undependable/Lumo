package no.uio.ifi.in2000.team33.lumo.data.frost.model.observation

data class ObservationResponse(
    val apiVersion: String,
    val createdAt: String,
    val currentItemCount: Int,
    val currentLink: String,
    val `data`: List<Data>,
    val itemsPerPage: Int,
    val license: String,
    val offset: Int,
    val queryTime: Double,
    val totalItemCount: Int
)