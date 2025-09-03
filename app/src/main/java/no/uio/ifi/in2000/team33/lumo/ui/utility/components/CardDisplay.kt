package no.uio.ifi.in2000.team33.lumo.ui.utility.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CardColors
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * A large card with a title, description, icon, and optional loading spinner.
 */
@Composable
fun LargeCard(
    modifier: Modifier,
    colors: CardColors,
    title: String,
    description: String,
    imageVector: ImageVector,
    contentDescription: String,
    isLoading: Boolean = false
) {
    // Card container with custom shadow and corner shape
    ElevatedCard(
        modifier = modifier.shadow(elevation = 0.dp, shape = RoundedCornerShape(12.dp)),
        colors = colors
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                // Spinner shown instead of title while loading
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .align(Alignment.Start),
                    contentAlignment = Alignment.CenterStart
                ) {
                    SimpleRotatingLoader(
                        size = 48.dp,
                        outerCircleColor = MaterialTheme.colorScheme.primary,
                        middleCircleColor = Color(0xFFFCC00D),
                    )
                }
            } else {
                // Title text
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            // Description text
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Icon displayed on the right
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.End)
            )
        }
    }
}

/**
 * A clickable card that navigates to a specified destination when tapped.
 */
@Composable
fun DestinationCard(
    colors: CardColors,
    title: String,
    destination: String,
    navController: NavController
) {
    // Card with click behavior and arrow icon
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 0.dp, shape = RoundedCornerShape(12.dp)),
        colors = colors,
        onClick = { navController.navigate(destination) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title text
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.weight(1f)
            )

            // Navigation arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = destination,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/**
 * A minimal horizontal card with icon and text.
 */
@Composable
fun smallCard(
    modifier: Modifier,
    title: String,
    destination: String,
    vector: ImageVector
) {
    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            // Icon
            Icon(
                imageVector = vector,
                contentDescription = destination,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.size(10.dp))

            // Title text
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
