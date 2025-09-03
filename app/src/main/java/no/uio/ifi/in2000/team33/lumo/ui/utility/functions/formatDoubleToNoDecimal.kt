package no.uio.ifi.in2000.team33.lumo.ui.utility.functions

import android.util.Log
import java.text.DecimalFormat


/**
 * Formats a given [Double] value to a string without any decimal points.
 * If the value is null, a log message is printed to indicate that no value was found for conversion.
 *
 * @param value The [Double] value to be formatted. If the value is null, no conversion occurs.
 * @return A string representing the [Double] value rounded to no decimal places.
 * If the value is null, an empty string will be returned.
 */
fun formatDoubleToNoDecimal(value: Double?): String {
    // Check if the value is null and log a message if so
    if (value == null) {
        Log.i("ANNUAL ESTIMATE", "Fant ikke verdi å konvertere i homescreen")
        return ""  // Return empty string when value is null
    }

    // Create a DecimalFormat to remove decimals
    val decimalFormat = DecimalFormat("#")

    // Return the formatted value
    return decimalFormat.format(value)
}
