package no.uio.ifi.in2000.team33.lumo.ui.utility.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Boolean to
var isDark by mutableStateOf(false)

// Dark mode colour schemes
private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    background = md_theme_dark_background,
    surface = md_theme_dark_surface,
    secondary = md_theme_dark_secondary,
    outline = md_theme_dark_outline,
    surfaceVariant = md_theme_dark_surfaceVariant,
    primaryContainer = md_theme_dark_bar,
    tertiaryContainer = md_theme_dark_chart,
    scrim = md_theme_dark_destination_card
)


// Light mode colour schemes
private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    background = md_theme_light_background,
    surface = md_theme_light_surface,
    secondary = md_theme_light_secondary,
    outline = md_theme_light_outline,
    surfaceVariant = md_theme_light_surfaceVariant,
    primaryContainer = md_theme_light_bar,
    tertiaryContainer = md_theme_light_chart,
    scrim = md_theme_light_destination_card
)

// Function to switch to turn on dark mode or light mode depending on the boolean value
fun turnOn() {
    isDark = !isDark
}

// Composable to actually change the theme on the app
@Composable
fun SolcelleTheme(
    darkTheme: Boolean = isDark,
    content: @Composable () -> Unit
) {

    // Fallback to manually defined color schemes
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}