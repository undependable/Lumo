package no.uio.ifi.in2000.team33.lumo.ui.utility.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.ErrorType

@Composable
fun GeneralPopup(
    errorType: ErrorType, // Holds the error title and message
    buttonText: String = "Prøv igjen", // Button label, customizable
    onRetryClick: () -> Unit = {}, // Retry logic (default: no-op)
    onDismiss: () -> Unit = {} // Called when user taps outside popup
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            // Dimmed background to block interaction with underlying content
            .background(Color.Black.copy(alpha = 0.3f))
            // Tapping outside the popup triggers dismiss
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    ) {
        // Centered popup card
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 300.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Error title
                Text(
                    text = errorType.type,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Error message body
                Text(
                    text = errorType.message,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Retry button
                Button(
                    onClick = onRetryClick,
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text(buttonText)
                }
            }
        }
    }
}
