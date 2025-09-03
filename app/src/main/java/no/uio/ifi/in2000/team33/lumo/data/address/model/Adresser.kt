package no.uio.ifi.in2000.team33.lumo.data.address.model


data class Adresser(
    val adressekode: Int,
    val adressenavn: String,
    val adressetekst: String,
    val adressetekstutenadressetilleggsnavn: String,
    val adressetilleggsnavn: Any,
    val bokstav: String,
    val bruksenhetsnummer: List<Any>,
    val bruksnummer: Int,
    val festenummer: Int,
    val gardsnummer: Int,
    val kommunenavn: String,
    val kommunenummer: String,
    val nummer: Int,
    val objtype: String,
    val oppdateringsdato: String,
    val postnummer: String,
    val poststed: String,
    val representasjonspunkt: Representasjonspunkt,
    val stedfestingverifisert: Boolean,
    val undernummer: Any
)