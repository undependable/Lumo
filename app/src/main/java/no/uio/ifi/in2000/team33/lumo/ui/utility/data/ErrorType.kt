package no.uio.ifi.in2000.team33.lumo.ui.utility.data

/**
 * Represents different types of errors that can occur in the application.
 *
 * @property type The title of the error, shown in the UI.
 * @property message The detailed error message shown to the user.
 */
enum class ErrorType(
    val type: String,
    val message: String
) {
    /**
     * Shown when the user inputs an invalid address.
     */
    INVALID_ADDRESS(
        type = "Adressefeil",
        message = "Ugyldig adresse"
    ),

    /**
     * Displayed when the app lacks permission to access location.
     */
    NO_LOCATION_PERMISSION(
        type = "Kan ikke hente din lokasjon",
        message = "Du må gi tilgang til lokasjon"
    ),

    /**
     * Error shown when retrieving the user's location fails due to an internal issue.
     */
    COULD_NOT_GET_LOCATION(
        type = "Kan ikke hente din lokasjon",
        message = "Det har skjedd en feil"
    )
}
