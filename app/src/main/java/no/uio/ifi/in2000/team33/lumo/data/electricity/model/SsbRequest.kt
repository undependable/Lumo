package no.uio.ifi.in2000.team33.lumo.data.electricity.model

data class SsbRequest(
    val query: List<SsbQuery>,
    val response: SsbResponse
)

data class SsbQuery(
    val code: String,
    val selection: SsbSelection
)

data class SsbSelection(
    val filter: String,
    val values: List<String>
)

data class SsbResponse(
    val format: String
)