package no.uio.ifi.in2000.team33.lumo.ui.home.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SolarPower
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun WelcomeHomeScreen(navController: NavController, name: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        // Animated wave background
        val waveHeight = 50f
        val waveLength = 400f
        val waveColor = Color.White

        val infiniteTransition = rememberInfiniteTransition(label = "waveTransition")
        val offsetX by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = waveLength,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 8000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "offsetX"
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .align(Alignment.BottomCenter)
        ) {
            val width = size.width
            val height = size.height

            val path = Path().apply {
                moveTo(-waveLength + offsetX, height / 2)
                var x = -waveLength + offsetX
                while (x < width + waveLength) {
                    quadraticTo(
                        x + waveLength / 4, height / 2 - waveHeight,
                        x + waveLength / 2, height / 2
                    )
                    quadraticTo(
                        x + 3 * waveLength / 4, height / 2 + waveHeight,
                        x + waveLength, height / 2
                    )
                    x += waveLength
                }
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            drawPath(path = path, color = waveColor)
        }

        // Bottom button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 45.dp)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { navController.navigate("searchscreen") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF)),
                shape = RoundedCornerShape(50)
            ) {
                Icon(
                    imageVector = Icons.Outlined.SolarPower,
                    contentDescription = "sol ikon",
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Sjekk ditt tak", color = MaterialTheme.colorScheme.outline)
            }
        }

        // Welcome message
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 230.dp)
                .padding(horizontal = 25.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Hei, $name!",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // Center content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 25.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Klar til å legge til en bolig?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "Med solceller kan du produsere din egen strøm og få full oversikt over estimert solproduksjon her.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
