package no.uio.ifi.in2000.team33.lumo.ui.utility.functions

fun directionToAspect(direction: String): Int {
    return when (direction.lowercase()) {
        "soer", "sør" -> 0
        "vest" -> 90
        "ost", "øst" -> -90
        "nord" -> 180
        else -> 0
    }
}