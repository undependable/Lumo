package no.uio.ifi.in2000.team33.lumo.ui.utility.functions

import java.util.Locale

/**
 * Formats an address name by adding a newline where appropriate to ensure it fits well in UI elements.
 * This function splits the address name by spaces and capitalizes each word. If the address name is
 * longer than a certain length, it breaks the text into multiple lines to improve readability.
 *
 * @param addressName The address name to be formatted.
 * @return A formatted address string, where long addresses are split into multiple lines.
 */
fun formatAddressName(addressName: String): String {
    var newName = ""
    if (addressName.split("").size >= 20) {  // If address name is long enough to need formatting
        var count = 0
        val formattedName = addressName.split(" ")  // Split address into words
        println(formattedName)

        // Iterate through each word and format
        formattedName.forEach { word ->
            if (count == 1) newName += "\n"  // Add newline after the first word
            if (count > 1) newName += " "  // Add space after the first word

            newName += word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }  // Capitalize the first letter of each word
            count++
        }
    }
    // Return the original address name if no formatting was applied
    return if (newName == "") addressName else newName
}
