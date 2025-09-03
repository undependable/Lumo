package no.uio.ifi.in2000.team33.lumo.ui.utility.data

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents a single page in the onboarding flow.
 *
 * @property title The main title text shown on the page.
 * @property description Additional descriptive text for the page.
 * @property image A vector graphic displayed as the main image (typically an illustration).
 * @property icon An optional resource ID for a smaller icon (e.g., used for accent or decoration).
 */
data class OnboardingPage(
    val title: String,
    val description: String,
    val image: ImageVector,
    val icon: Int?
)
