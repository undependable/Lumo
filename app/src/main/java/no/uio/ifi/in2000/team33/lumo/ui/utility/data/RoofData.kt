package no.uio.ifi.in2000.team33.lumo.ui.home.ViewModel

/**
 * Represents information about a roof surface (takflate) used in solar panel calculations.
 *
 * @property navn The name or label of the roof surface.
 * @property area The area of the roof surface, typically in square meters (as a string).
 * @property vinkel The tilt angle of the roof surface, in degrees (as a string).
 * @property retning The orientation/direction of the roof surface (e.g., "Sør", "Nord").
 */
data class RoofData(
    val navn: String,
    val area: String,
    val vinkel: String,
    val retning: String
)
