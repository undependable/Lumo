package no.uio.ifi.in2000.team33.lumo.data.frost.model.station

data class Data(
    val country: String,
    val countryCode: String,
    val county: String,
    val countyId: Int,
    val distance: Double,
    val externalIds: List<String>,
    val geometry: Geometry,
    val icaoCodes: List<String>,
    val id: String,
    val masl: Int,
    val municipality: String,
    val municipalityId: Int,
    val name: String,
    val ontologyId: Int,
    val shortName: String,
    val stationHolders: List<String>,
    val validFrom: String,
    val wigosId: String,
    val wmoId: Int
)