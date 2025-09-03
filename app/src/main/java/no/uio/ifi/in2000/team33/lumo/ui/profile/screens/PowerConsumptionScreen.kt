package no.uio.ifi.in2000.team33.lumo.ui.profile.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team33.lumo.ui.profile.ProfileViewModel
import no.uio.ifi.in2000.team33.lumo.ui.utility.theme.isDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PowerConsumptionScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel
) {
    val powerConsumptionState by profileViewModel.powerConsumption.collectAsState()
    val consumption = powerConsumptionState.consumption

    // Theme colors
    val backgroundColor = MaterialTheme.colorScheme.primary
    val purpleTextColor = if (isDark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val cardColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFF5EFF7)
    val inputFieldColor =
        if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFFFFFFF)
    val inputFieldBorderColor = if (isDark) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline
    val secondaryContainerColor =
        if (isDark) MaterialTheme.colorScheme.secondaryContainer else Color(0xFFE8DEF8)
    val onSecondaryContainerColor =
        if (isDark) MaterialTheme.colorScheme.onSecondaryContainer else Color(0xFF4A4459)
    val annualBoxColor = MaterialTheme.colorScheme.scrim
    val annualTextColor =
        if (isDark) MaterialTheme.colorScheme.onTertiaryContainer else Color(0xFF625B71)
    val lightPurpleColor =
        if (isDark) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF5EFF7)
    val darkPurpleColor = if (isDark) MaterialTheme.colorScheme.primary else Color(0xFF4F378A)

    var selectedView by remember { mutableStateOf(0) } // 0 for Yearly, 1 for Monthly
    var showError by remember { mutableStateOf(false) } // Control alert dialog
    var showMinimumError by remember { mutableStateOf(false) }


    // Temporary yearly input state
    var yearlyInputText by remember { mutableStateOf(consumption.yearlyConsumption.toString()) }
    var isYearlyInputValid by remember { mutableStateOf(true) }

    // Temporary monthly input states
    var janInputText by remember { mutableStateOf(consumption.januaryConsumption.toString()) }
    var febInputText by remember { mutableStateOf(consumption.februaryConsumption.toString()) }
    var marInputText by remember { mutableStateOf(consumption.marchConsumption.toString()) }
    var aprInputText by remember { mutableStateOf(consumption.aprilConsumption.toString()) }
    var mayInputText by remember { mutableStateOf(consumption.mayConsumption.toString()) }
    var junInputText by remember { mutableStateOf(consumption.juneConsumption.toString()) }
    var julInputText by remember { mutableStateOf(consumption.julyConsumption.toString()) }
    var augInputText by remember { mutableStateOf(consumption.augustConsumption.toString()) }
    var sepInputText by remember { mutableStateOf(consumption.septemberConsumption.toString()) }
    var octInputText by remember { mutableStateOf(consumption.octoberConsumption.toString()) }
    var novInputText by remember { mutableStateOf(consumption.novemberConsumption.toString()) }
    var decInputText by remember { mutableStateOf(consumption.decemberConsumption.toString()) }

    // Check if all required fields are filled
    val areAllFieldsFilled = remember(
        selectedView, yearlyInputText, janInputText, febInputText, marInputText,
        aprInputText, mayInputText, junInputText, julInputText, augInputText,
        sepInputText, octInputText, novInputText, decInputText
    ) {
        if (selectedView == 0) {
            yearlyInputText.isNotEmpty()
        } else {
            janInputText.isNotEmpty() && febInputText.isNotEmpty() &&
                    marInputText.isNotEmpty() && aprInputText.isNotEmpty() &&
                    mayInputText.isNotEmpty() && junInputText.isNotEmpty() &&
                    julInputText.isNotEmpty() && augInputText.isNotEmpty() &&
                    sepInputText.isNotEmpty() && octInputText.isNotEmpty() &&
                    novInputText.isNotEmpty() && decInputText.isNotEmpty()
        }
    }

    // Dynamic calculated total
    val calculatedTotal = remember(
        janInputText, febInputText, marInputText, aprInputText, mayInputText,
        junInputText, julInputText, augInputText, sepInputText, octInputText,
        novInputText, decInputText, yearlyInputText, selectedView
    ) {
        if (selectedView == 0) {
            yearlyInputText.toIntOrNull() ?: 0
        } else {
            val jan = janInputText.toIntOrNull() ?: 0
            val feb = febInputText.toIntOrNull() ?: 0
            val mar = marInputText.toIntOrNull() ?: 0
            val apr = aprInputText.toIntOrNull() ?: 0
            val may = mayInputText.toIntOrNull() ?: 0
            val jun = junInputText.toIntOrNull() ?: 0
            val jul = julInputText.toIntOrNull() ?: 0
            val aug = augInputText.toIntOrNull() ?: 0
            val sep = sepInputText.toIntOrNull() ?: 0
            val oct = octInputText.toIntOrNull() ?: 0
            val nov = novInputText.toIntOrNull() ?: 0
            val dec = decInputText.toIntOrNull() ?: 0
            jan + feb + mar + apr + may + jun + jul + aug + sep + oct + nov + dec
        }
    }

    // Update local state when ViewModel data changes
    LaunchedEffect(consumption) {
        yearlyInputText = consumption.yearlyConsumption.toString().takeIf { it != "0" } ?: ""
        janInputText = consumption.januaryConsumption.toString().takeIf { it != "0" } ?: ""
        febInputText = consumption.februaryConsumption.toString().takeIf { it != "0" } ?: ""
        marInputText = consumption.marchConsumption.toString().takeIf { it != "0" } ?: ""
        aprInputText = consumption.aprilConsumption.toString().takeIf { it != "0" } ?: ""
        mayInputText = consumption.mayConsumption.toString().takeIf { it != "0" } ?: ""
        junInputText = consumption.juneConsumption.toString().takeIf { it != "0" } ?: ""
        julInputText = consumption.julyConsumption.toString().takeIf { it != "0" } ?: ""
        augInputText = consumption.augustConsumption.toString().takeIf { it != "0" } ?: ""
        sepInputText = consumption.septemberConsumption.toString().takeIf { it != "0" } ?: ""
        octInputText = consumption.octoberConsumption.toString().takeIf { it != "0" } ?: ""
        novInputText = consumption.novemberConsumption.toString().takeIf { it != "0" } ?: ""
        decInputText = consumption.decemberConsumption.toString().takeIf { it != "0" } ?: ""
    }

    val scope = rememberCoroutineScope()

    // Modify your saveYearlyConsumption function
    fun saveYearlyConsumption() {
        val yearlyValue = yearlyInputText.toIntOrNull() ?: 0

        if (yearlyValue < 20) {
            showMinimumError = true
            return
        }

        profileViewModel.updateYearlyConsumption(yearlyValue)
    }

    // Modify your saveMonthlyConsumption function
    fun saveMonthlyConsumption() {
        val jan = janInputText.toIntOrNull() ?: 0
        val feb = febInputText.toIntOrNull() ?: 0
        val mar = marInputText.toIntOrNull() ?: 0
        val apr = aprInputText.toIntOrNull() ?: 0
        val may = mayInputText.toIntOrNull() ?: 0
        val jun = junInputText.toIntOrNull() ?: 0
        val jul = julInputText.toIntOrNull() ?: 0
        val aug = augInputText.toIntOrNull() ?: 0
        val sep = sepInputText.toIntOrNull() ?: 0
        val oct = octInputText.toIntOrNull() ?: 0
        val nov = novInputText.toIntOrNull() ?: 0
        val dec = decInputText.toIntOrNull() ?: 0

        val total = jan + feb + mar + apr + may + jun + jul + aug + sep + oct + nov + dec

        if (total < 20) {
            showMinimumError = true
            return
        }

        profileViewModel.updateMonthlyConsumption(
            january = jan, february = feb, march = mar,
            april = apr, may = may, june = jun,
            july = jul, august = aug, september = sep,
            october = oct, november = nov, december = dec
        )
    }

    fun resetFields() {
        // Clear all input fields
        yearlyInputText = ""
        janInputText = ""
        febInputText = ""
        marInputText = ""
        aprInputText = ""
        mayInputText = ""
        junInputText = ""
        julInputText = ""
        augInputText = ""
        sepInputText = ""
        octInputText = ""
        novInputText = ""
        decInputText = ""
    }
    if (showMinimumError) {
        AlertDialog(
            onDismissRequest = { showMinimumError = false },
            title = { Text("For lavt strømforbruk") },
            text = {
                Text("Årlig forbruk er nødt til å være over 20 kWh for å kunne gjennomføre beregninger for besparelse.")
            },
            confirmButton = {
                Button(
                    onClick = { showMinimumError = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = darkPurpleColor
                    )
                ) {
                    Text("OK")
                }
            }
        )
    }
    // Alert dialog for missing fields
    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("Mangler informasjon") },
            text = {
                Text(
                    if (selectedView == 0)
                        "Du må fylle ut årlig strømforbruk."
                    else
                        "Du må fylle ut alle feltene for månedlig strømforbruk."
                )
            },
            confirmButton = {
                Button(
                    onClick = { showError = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = darkPurpleColor
                    )
                ) {
                    Text(
                        text ="OK",
                        color = Color.White
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 48.dp), // Compensate for back button to center text
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Mitt strømforbruk",
                            style = MaterialTheme.typography.headlineMedium,
                            color = purpleTextColor,
                            modifier = Modifier.padding(top = 8.dp) // Move the title a bit down
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Tilbake",
                            tint = Color(0xFF1D1B20)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Segmented button for view selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 94.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                // Button for yearly consumption
                Button(
                    onClick = { selectedView = 0 },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .width(160.dp),
                    shape = RoundedCornerShape(topStart = 100.dp, bottomStart = 100.dp),
                    border = BorderStroke(1.dp, Color(0xFF79747E)),
                    colors = if (selectedView == 0) ButtonDefaults.buttonColors(
                        containerColor = secondaryContainerColor,
                        contentColor = onSecondaryContainerColor
                    ) else ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF1D1B20)
                    )
                ) {
                    if (selectedView == 0) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text("Årlig")
                }

                // Button for monthly consumption
                Button(
                    onClick = { selectedView = 1 },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .width(160.dp),
                    shape = RoundedCornerShape(topEnd = 100.dp, bottomEnd = 100.dp),
                    border = BorderStroke(1.dp, Color(0xFF79747E)),
                    colors = if (selectedView == 1) ButtonDefaults.buttonColors(
                        containerColor = secondaryContainerColor,
                        contentColor = onSecondaryContainerColor
                    ) else ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF1D1B20)
                    )
                ){
                    if (selectedView == 1) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text("Månedlig")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main content area - Fixed size for both views
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
                    .height(380.dp) // Fixed height for both views
                    .background(cardColor, RoundedCornerShape(14.dp))
            ) {
                when (selectedView) {
                    0 -> {
                        // Yearly consumption view
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Skriv inn ditt årlige strømforbruk.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Custom yearly input - made smaller with proper sizing
                            CustomTextField(
                                value = yearlyInputText,
                                onValueChange = {
                                    val newText = it.filter { char -> char.isDigit() }
                                    yearlyInputText = newText
                                    isYearlyInputValid = newText.toIntOrNull() != null || newText.isEmpty()
                                },
                                modifier = Modifier
                                    .width(160.dp) // Reduced from 210dp to 160dp
                                    .height(40.dp) // Fixed height
                                    .padding(vertical = 0.dp), // Reduced padding
                                bgColor = inputFieldColor,
                                borderColor = inputFieldBorderColor,
                                suffixText = "kWh",
                                isError = !isYearlyInputValid
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Forbruket er fordelt gjennom året etter en typisk norsk strømprofil.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .width(206.dp)
                            )
                        }
                    }
                    1 -> {
                        // Monthly consumption view with internal scrolling
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 8.dp, horizontal = 22.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(vertical = 8.dp)
                            ) {
                                // Monthly consumption inputs with reduced spacing
                                MonthInputRow("Januar", janInputText) { janInputText = it }
                                Spacer(modifier = Modifier.height(8.dp))

                                MonthInputRow("Februar", febInputText) { febInputText = it }
                                Spacer(modifier = Modifier.height(8.dp))

                                MonthInputRow("Mars", marInputText) { marInputText = it }
                                Spacer(modifier = Modifier.height(8.dp))

                                MonthInputRow("April", aprInputText) { aprInputText = it }
                                Spacer(modifier = Modifier.height(8.dp))

                                MonthInputRow("Mai", mayInputText) { mayInputText = it }
                                Spacer(modifier = Modifier.height(8.dp))

                                MonthInputRow("Juni", junInputText) { junInputText = it }
                                Spacer(modifier = Modifier.height(8.dp))

                                MonthInputRow("Juli", julInputText) { julInputText = it }
                                Spacer(modifier = Modifier.height(8.dp))

                                MonthInputRow("August", augInputText) { augInputText = it }
                                Spacer(modifier = Modifier.height(8.dp))

                                MonthInputRow("September", sepInputText) { sepInputText = it }
                                Spacer(modifier = Modifier.height(8.dp))

                                MonthInputRow("Oktober", octInputText) { octInputText = it }
                                Spacer(modifier = Modifier.height(8.dp))

                                MonthInputRow("November", novInputText) { novInputText = it }
                                Spacer(modifier = Modifier.height(8.dp))

                                MonthInputRow("Desember", decInputText) { decInputText = it }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Annual Consumption Display Box - Now with dynamic updates from calculated total
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 30.dp, end = 30.dp)
                    .height(77.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(annualBoxColor)
            ) {
                Column(modifier = Modifier.padding(start = 29.dp)) {
                    Text(
                        text = "Årlig forbruk",
                        style = MaterialTheme.typography.titleSmall,
                        color = annualTextColor,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        // Use the calculated total instead of the database value for instant feedback
                        text = "$calculatedTotal kWh",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                        color = Color.Black,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Reset button
            OutlinedButton(
                onClick = { resetFields() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 26.dp),
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(1.dp, Color(0xFF4F378A)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF4F378A)
                )
            ) {
                Text(
                    text = "Nullstill",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button - with conditional colors based on field status
            Button(
                onClick = {
                    if (areAllFieldsFilled) {
                        scope.launch {
                            if (selectedView == 0) {
                                saveYearlyConsumption()
                            } else {
                                saveMonthlyConsumption()
                            }
                        }
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 26.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (areAllFieldsFilled) darkPurpleColor else lightPurpleColor,
                    contentColor = if (areAllFieldsFilled) lightPurpleColor else Color(0xFF4F378A)
                ),
                border = BorderStroke(2.dp, Color(0xFF4F378A))
            ) {
                Text(
                    text = "Lagre endringer",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MonthInputRow(
    month: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    val inputFieldColor =
        if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFFFFFFF)
    val inputFieldBorderColor = if (isDark) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // Ensure month is on left, field on right
    ) {
        // Month label aligned to the left with ellipsis to prevent text wrapping
        Text(
            text = month,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis, // Prevent line breaking
            modifier = Modifier
                .padding(start = 8.dp)
                .width(85.dp) // Slightly increased to accommodate longer names
        )

        // Custom text field for better control, right-aligned
        CustomTextField(
            value = value,
            onValueChange = {
                val newText = it.filter { char -> char.isDigit() }
                onValueChange(newText)
            },
            modifier = Modifier
                .width(120.dp)
                .height(40.dp),
            bgColor = inputFieldColor,
            borderColor = inputFieldBorderColor,
            suffixText = "kWh"
        )
    }
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    bgColor: Color = Color.White,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    suffixText: String = "",
    isError: Boolean = false
) {
    val borderColorToUse = if (isError) Color.Red else borderColor

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = borderColorToUse,
                shape = RoundedCornerShape(10.dp)
            )
            .background(bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    textAlign = TextAlign.Right
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        innerTextField()
                    }
                }
            )

            if (suffixText.isNotEmpty()) {
                Text(
                    text = suffixText,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}