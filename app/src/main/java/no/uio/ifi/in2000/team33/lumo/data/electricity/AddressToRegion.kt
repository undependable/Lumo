package no.uio.ifi.in2000.team33.lumo.data.electricity

/**
 * Utility object for mapping Norwegian postal codes (postnummer) to electricity price zones.
 *
 * Price zones are divided as follows:
 * - NO1: Eastern Norway (postal codes 00–39)
 * - NO2: Southern Norway (postal codes 44–49)
 * - NO3: Central Norway (postal codes 70–79)
 * - NO4: Northern Norway (postal codes 80–99)
 * - NO5: Western Norway (postal codes 40–43 and 50–69)
 */
object AddressToRegion {

    /**
     * Returns the electricity price zone (e.g., NO1, NO2...) based on the provided postal code.
     *
     * @param postnummer A Norwegian postal code as a string (4 digits).
     * @return The corresponding electricity region zone code, or null if not matched.
     */
    fun getRegionForAddress(postnummer: String): String? {
        val prefix = postnummer.substring(0, 2).toInt()

        return when {
            prefix in 0..39 -> "NO1"  // East
            prefix in 44..49 -> "NO2" // South
            prefix in 70..79 -> "NO3" // Central
            prefix in 80..99 -> "NO4" // North
            prefix in 40..43 || prefix in 50..69 -> "NO5" // West
            else -> null
        }
    }
}
