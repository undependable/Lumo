package no.uio.ifi.in2000.team33.lumo.ui.utility.functions

import java.text.NumberFormat
import java.util.Locale

/**
 * Formats a given [Double] number into a string with comma separators for thousands,
 * using the American number formatting style (e.g., 1,200,000).
 *
 * @param number The [Double] number to be formatted.
 * @return A string representing the formatted number with comma separators.
 */
fun formatNumberWithSeparator(number: Double): String {
    // Using US locale to format the number with commas as thousands separators
    return NumberFormat.getInstance(Locale.US).format(number)
}
