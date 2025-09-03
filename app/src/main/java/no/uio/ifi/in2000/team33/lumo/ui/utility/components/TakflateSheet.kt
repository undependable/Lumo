package no.uio.ifi.in2000.team33.lumo.ui.utility.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team33.lumo.ui.mappoint.MapPointViewModel
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.MapPoint
import no.uio.ifi.in2000.team33.lumo.ui.utility.functions.directionToAspect


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakflateSheet(
    point: MapPoint,
    onDismiss: () -> Unit,
    onAngleChosen: (Int, Double, Int) -> Unit,
    navController: NavController,
    mapPointViewModel: MapPointViewModel,
) {
    // Local form state - only exists while sheet is open
    var area by remember { mutableStateOf("") }
    var angle by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf("") }
    var areaError by remember { mutableStateOf(false) }
    var angleError by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showSavedEffect by remember { mutableStateOf(false) }

    // Collect ViewModel state for loading indicators if needed
    val isLoading by mapPointViewModel.isLoading.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Computed properties
    val isFormValid = area.toDoubleOrNull() != null &&
            angle.toIntOrNull() != null &&
            direction.isNotBlank()

    Box {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxHeight(0.72f),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = "Legg til informasjon om takflate",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Her kan du legge inn informasjon om én eller flere takflater.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Address field (read-only)
                TextField(
                    value = point.name,
                    onValueChange = { },
                    label = { Text("Adresse") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false
                )

                // Areal input field
                OutlinedTextField(
                    value = area,
                    onValueChange = { newValue ->
                        // Validation logic
                        val filtered = newValue.filter { it.isDigit() || it == '.' }
                        val parts = filtered.split('.')
                        val validText = if (parts.size > 2) {
                            parts[0] + "." + parts.subList(1, parts.size).joinToString("")
                        } else filtered

                        val numericValue = validText.toDoubleOrNull()
                        val limitedText = if (numericValue != null && numericValue > 10000) {
                            "10000"
                        } else validText

                        area = limitedText
                        areaError = false
                    },
                    label = { Text("*Areal (i m²)") },
                    singleLine = true,
                    colors = if (areaError) textFieldErrorColors() else textFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        if (areaError) Text("Kun tall er tillatt", color = Color.Red)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = areaError,
                    enabled = !isLoading
                )

                // Vinkel input field
                OutlinedTextField(
                    value = angle,
                    onValueChange = { newValue ->
                        // Validation logic
                        val filtered = newValue.filter { it.isDigit() }
                        val validText =
                            filtered.toIntOrNull()?.coerceAtMost(89)?.toString() ?: filtered
                        angle = validText
                        angleError = false
                    },
                    label = { Text("*Vinkel (° tilt på taket)") },
                    singleLine = true,
                    colors = if (angleError) textFieldErrorColors() else textFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        if (angleError) Text("Kun tall under 90 er tillatt", color = Color.Red)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = angleError,
                    enabled = !isLoading
                )

                // Direction selection buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val directions = listOf("nord", "sør", "vest", "øst")

                    directions.forEach { chosenDirection ->
                        val isChosen = direction.lowercase() == chosenDirection
                        Button(
                            onClick = { direction = chosenDirection },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isChosen) MaterialTheme.colorScheme.surfaceVariant else Color.White
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            enabled = !isLoading
                        ) {
                            Text(
                                chosenDirection.replaceFirstChar { it.uppercaseChar() },
                                color = Color(0xFF2E1A47)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(17.dp))

                // Save button
                Button(
                    onClick = {
                        if (isFormValid) {
                            showConfirmationDialog = true
                        } else {
                            showError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isFormValid && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showSavedEffect) Color(0xFFD0BCFF)
                        else if (isFormValid) Color(0xFFD0BCFF)
                        else Color.LightGray
                    )
                ) {
                    if (isLoading) {
                        Text("Lagrer...", fontWeight = FontWeight.Bold)
                    } else {
                        val textHue = if (isFormValid) 1f else 0.2f
                        Text(
                            "Lagre takflate",
                            color = MaterialTheme.colorScheme.outline.copy(textHue),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Error dialog
        if (showError) {
            AlertDialog(
                onDismissRequest = { showError = false },
                title = { Text("Mangler informasjon") },
                text = { Text("Du må fylle ut alle feltene for å lagre takflaten") },
                confirmButton = {
                    Button(onClick = { showError = false }) {
                        Text("OK")
                    }
                }
            )
        }

        // Confirmation dialog
        if (showConfirmationDialog) {
            val angleInt = angle.toIntOrNull()
            val arealDouble = area.toDoubleOrNull()
            val arealFormatted = String.format("%.1f", arealDouble)

            AlertDialog(
                containerColor = Color.White,
                onDismissRequest = { showConfirmationDialog = false },
                title = { Text("Bekreft takflate") },
                text = {
                    Text(
                        "Er du sikker på at du vil legge til takflate med:\n\n" +
                                "Areal: $arealFormatted m²\n" +
                                "Vinkel: $angleInt°\n" +
                                "Retning: ${direction.replaceFirstChar { it.uppercaseChar() }}"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val aspect = directionToAspect(direction)
                            if (angleInt != null && arealDouble != null) {
                                onAngleChosen(angleInt, arealDouble, aspect)

                                scope.launch {
                                    snackbarHostState.showSnackbar("Takflate registrert for ${point.name}!")
                                }

                                showConfirmationDialog = false
                                onDismiss()
                                // Navigate to homescreen to see the updated data
                                navController.navigate("homescreen") {
                                    popUpTo("homescreen") { inclusive = true }
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Text("Ja, legg til")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showConfirmationDialog = false },
                        enabled = !isLoading
                    ) {
                        Text("Nei, avbryt")
                    }
                }
            )
        }

        // Show saved effect
        if (showSavedEffect) {
            LaunchedEffect(showSavedEffect) {
                kotlinx.coroutines.delay(2000)
                showSavedEffect = false
            }
        }
    }
}

@Composable
fun textFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedLabelColor = MaterialTheme.colorScheme.outline,
        unfocusedLabelColor = Color.Gray,
        focusedBorderColor = MaterialTheme.colorScheme.outline,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    )
}

@Composable
fun textFieldErrorColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color(0xFF2E1A47),
        unfocusedTextColor = Color(0xFF2E1A47),
        focusedLabelColor = Color.Red,
        unfocusedLabelColor = Color.Red,
        focusedBorderColor = Color.Red,
        unfocusedBorderColor = Color.Red
    )
}