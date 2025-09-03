package no.uio.ifi.in2000.team33.lumo.ui.onboarding

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team33.lumo.ui.profile.ProfileViewModel

@Composable
fun PowerProductionOnboarding(
    profileViewModel: ProfileViewModel,
    onComplete: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.surface
    val accentColor = MaterialTheme.colorScheme.tertiaryContainer
    val textColor = MaterialTheme.colorScheme.outline
    val descriptionColor = MaterialTheme.colorScheme.onSurface.copy(0.8f)
    val cardColor = MaterialTheme.colorScheme.surfaceVariant

    // Monthly production state with month names
    data class MonthData(val name: String, var value: String)

    val monthsList = remember {
        listOf(
            MonthData("Januar", ""),
            MonthData("Februar", ""),
            MonthData("Mars", ""),
            MonthData("April", ""),
            MonthData("Mai", ""),
            MonthData("Juni", ""),
            MonthData("Juli", ""),
            MonthData("August", ""),
            MonthData("September", ""),
            MonthData("Oktober", ""),
            MonthData("November", ""),
            MonthData("Desember", "")
        )
    }

    // Convert to mutable state list
    val monthsState = remember { mutableStateListOf(*monthsList.toTypedArray()) }

    // Check if all fields are filled - this will now properly recompose when monthsState changes
    val areAllFieldsFilled by remember(monthsState) {
        derivedStateOf { monthsState.all { it.value.isNotEmpty() } }
    }

    // Calculate total production - also using derivedStateOf for better performance
    val totalProduction by remember(monthsState) {
        derivedStateOf { monthsState.sumOf { it.value.toIntOrNull() ?: 0 } }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        WavyHeader()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 160.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Strømforbruk",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )

            Text(
                text = "Angi ditt forventede strømforbruk per måned (i kWh)",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = descriptionColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            val lazyRowState = rememberLazyListState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column {
                    // Month cards in horizontal scrollable layout
                    LazyRow(
                        state = lazyRowState,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        items(monthsState) { monthData ->
                            MonthCard(
                                month = monthData.name,
                                value = monthData.value,
                                onValueChange = { newValue ->
                                    val index = monthsState.indexOf(monthData)
                                    if (index != -1) {
                                        monthsState[index] = monthData.copy(value = newValue)
                                    }
                                }
                            )
                        }
                    }
                    HorizontalScrollIndicator(lazyRowState)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Total production display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardColor)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Total årlig produksjon",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor
                    )
                    Text(
                        text = "$totalProduction kWh",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val transition =
                updateTransition(targetState = areAllFieldsFilled, label = "formTransition")
            val backgroundColor by transition.animateColor(
                label = "buttonColor"
            ) { completed -> if (completed) accentColor else Color.Gray.copy(alpha = 0.3f) }

            val scale by transition.animateFloat(
                label = "scaleAnim",
                transitionSpec = { tween(durationMillis = 300) }
            ) { completed -> if (completed) 1.05f else 1.0f }

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
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            enabled = areAllFieldsFilled,
                            onClick = {
                                if (areAllFieldsFilled) {

                                    profileViewModel.updateMonthlyConsumption(
                                        january = monthsState[0].value.toIntOrNull() ?: 0,
                                        february = monthsState[1].value.toIntOrNull() ?: 0,
                                        march = monthsState[2].value.toIntOrNull() ?: 0,
                                        april = monthsState[3].value.toIntOrNull() ?: 0,
                                        may = monthsState[4].value.toIntOrNull() ?: 0,
                                        june = monthsState[5].value.toIntOrNull() ?: 0,
                                        july = monthsState[6].value.toIntOrNull() ?: 0,
                                        august = monthsState[7].value.toIntOrNull() ?: 0,
                                        september = monthsState[8].value.toIntOrNull() ?: 0,
                                        october = monthsState[9].value.toIntOrNull() ?: 0,
                                        november = monthsState[10].value.toIntOrNull() ?: 0,
                                        december = monthsState[11].value.toIntOrNull() ?: 0
                                    )

                                    onComplete()
                                }
                            }
                        )
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (areAllFieldsFilled) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = "Fullfør",
                        modifier = Modifier.size(24.dp),
                        tint = if (areAllFieldsFilled) MaterialTheme.colorScheme.onPrimary else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
        }
    }
}

@Composable
private fun MonthCard(
    month: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = MaterialTheme.colorScheme.surfaceVariant
    val inputFieldColor = MaterialTheme.colorScheme.surface
    val inputFieldBorderColor = MaterialTheme.colorScheme.outline

    Card(
        modifier = modifier
            .width(180.dp)
            .height(150.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Month name
            Text(
                text = month,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input field
            CustomTextField(
                value = value,
                onValueChange = {
                    val newText = it.filter { char -> char.isDigit() }
                    onValueChange(newText)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                bgColor = inputFieldColor,
                borderColor = inputFieldBorderColor,
                suffixText = "kWh"
            )
        }
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
                    color = MaterialTheme.colorScheme.onSurface,
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
                        if (value.isEmpty()) {
                            Text(
                                text = "0",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Right
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (suffixText.isNotEmpty()) {
                Text(
                    text = suffixText,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun HorizontalScrollIndicator(
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.outline,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    indicatorWidth: Dp = 8.dp,
    indicatorHeight: Dp = 4.dp,
    spacing: Dp = 4.dp
) {
    val itemCount = 12
    val visibleItemsInfo = remember(scrollState) {
        derivedStateOf {
            scrollState.layoutInfo.visibleItemsInfo
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(itemCount) { index ->
            val isActive = remember {
                derivedStateOf {
                    visibleItemsInfo.value.any { it.index == index }
                }
            }

            Box(
                modifier = Modifier
                    .width(indicatorWidth)
                    .height(indicatorHeight)
                    .background(
                        color = if (isActive.value) activeColor else inactiveColor,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = spacing / 2)
            )
        }
    }
}