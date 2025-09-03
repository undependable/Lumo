package no.uio.ifi.in2000.team33.lumo.data.address.model


data class Metadata(
    val asciiKompatibel: Boolean,
    val side: Int,
    val sokeStreng: String,
    val totaltAntallTreff: Int,
    val treffPerSide: Int,
    val viserFra: Int,
    val viserTil: Int
)