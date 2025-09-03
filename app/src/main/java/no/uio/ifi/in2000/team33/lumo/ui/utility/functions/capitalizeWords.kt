package no.uio.ifi.in2000.team33.lumo.ui.utility.functions

fun capitalizeWords(input: String): String {
    return input.split(" ").joinToString(" ") { word ->
        if (word.isNotEmpty()) {
            word.lowercase().replaceFirstChar { it.titlecase() }
        } else {
            word
        }
    }
}