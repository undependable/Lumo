package no.uio.ifi.in2000.team33.lumo.data.frost.model.observation

data class Data(
    val observations: List<Observation>,
    val referenceTime: String,
    val sourceId: String
)