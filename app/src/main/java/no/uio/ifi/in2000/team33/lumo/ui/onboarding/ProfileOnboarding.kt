package no.uio.ifi.in2000.team33.lumo.ui.onboarding

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team33.lumo.ui.profile.ProfileViewModel
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.UserInfo
import no.uio.ifi.in2000.team33.lumo.ui.utility.functions.capitalizeWords


@Composable
fun ProfileCreationScreen(
    profileViewModel: ProfileViewModel,
    onComplete: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.surface
    val accentColor = MaterialTheme.colorScheme.tertiaryContainer
    val textColor = MaterialTheme.colorScheme.outline
    val placeholderColor = textColor.copy(alpha = 0.6f)

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val formCompleted = firstName.isNotBlank() && lastName.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        WavyHeader()

        // Scrollable top content
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .height(300.dp)
        ) {
            Spacer(modifier = Modifier.height(160.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "",
                    modifier = Modifier.size(50.dp),
                    tint = MaterialTheme.colorScheme.outline
                )

                Text(
                    text = "La oss bli kjent med deg",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
            }
        }

        // Fixed form content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 350.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Form fields with consistent spacing
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                InputField(
                    value = firstName,
                    onValueChange = {
                        firstName = capitalizeWords(it)
                    },
                    placeholder = "*Fornavn",
                    keyboardType = KeyboardType.Text,
                    keyboardCapitalization = KeyboardCapitalization.Words,
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    textColor = textColor,
                    placeholderColor = placeholderColor
                )

                InputField(
                    value = lastName,
                    onValueChange = {
                        lastName = capitalizeWords(it)
                    },
                    placeholder = "*Etternavn",
                    keyboardType = KeyboardType.Text,
                    keyboardCapitalization = KeyboardCapitalization.Words,
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    textColor = textColor,
                    placeholderColor = placeholderColor
                )
            }

            // Submit button section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val transition =
                    updateTransition(targetState = formCompleted, label = "formTransition")
                val backgroundColor by transition.animateColor(
                    label = "buttonColor"
                ) { completed -> if (completed) accentColor else Color.Gray.copy(alpha = 0.3f) }

                val scale by transition.animateFloat(
                    label = "scaleAnim",
                    transitionSpec = { tween(durationMillis = 300) }
                ) { completed -> if (completed) 1.1f else 1.0f }

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .size(64.dp)
                        .background(
                            color = backgroundColor,
                            shape = RoundedCornerShape(32.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            if (formCompleted) {
                                profileViewModel.updateUserInfo(
                                    UserInfo(
                                        firstName = firstName,
                                        lastName = lastName
                                    )
                                )
                                onComplete()
                            }
                        },
                        enabled = formCompleted,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (formCompleted) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = "Fullfør",
                            tint = if (formCompleted) MaterialTheme.colorScheme.onPrimary else Color.Gray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    keyboardCapitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    onNext: () -> Unit,
    textColor: Color,
    placeholderColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = placeholderColor) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                capitalization = keyboardCapitalization
            ),
            keyboardActions = KeyboardActions(onNext = { onNext() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = textColor,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun WavyHeader(modifier: Modifier = Modifier) {
    val waveHeight = 50f
    val waveLength = 400f
    val waveColor = MaterialTheme.colorScheme.primary

    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = waveLength,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(2000.dp)
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
        drawPath(path, color = waveColor)
    }
}