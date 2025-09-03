package no.uio.ifi.in2000.team33.lumo.ui.home.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DividerProperties
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorPosition
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.PopupProperties
import no.uio.ifi.in2000.team33.lumo.ui.mappoint.MapPointViewModel
import no.uio.ifi.in2000.team33.lumo.ui.profile.ProfileViewModel


/**
 * Graph component for displaying estimated savings
 * Shows a graph with three different scenarios (worst case, median, best case)
 */
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun Savings(
    chartData: List<Pair<Int, Double>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Graph section title
                Text(
                    text = "Besparelser",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "Forventede besparelser for ett år.",
                    style = MaterialTheme.typography.titleSmall.copy(color = Color.Gray),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Graph visualization area - empty box with border
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {

                    println(chartData)
                    val monthlyData = remember(chartData) {
                        chartData.map { it.second }
                    }


                    val months = listOf(
                        "Jan",
                        "Feb",
                        "Mar",
                        "Apr",
                        "May",
                        "Jun",
                        "Jul",
                        "Aug",
                        "Sep",
                        "Okt",
                        "Nov",
                        "Dec"
                    )

                    if (monthlyData.all { it == 0.0 }) {
                        // Show empty state when no data
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ingen besparelsesdata tilgjengelig",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    } else {
                        LineChart(
                            curvedEdges = false,
                            labelHelperProperties = LabelHelperProperties(enabled = false), // Disables all line labels
                            modifier = Modifier.fillMaxSize(),
                            data = remember {
                                listOf(
                                    Line(
                                        label = "Monthly Savings",
                                        values = monthlyData,
                                        color = SolidColor(Color(0xFF6C9E74)),
                                        firstGradientFillColor = Color(0xFF6C9E74).copy(alpha = 0.5f),
                                        secondGradientFillColor = Color.Transparent,
                                        strokeAnimationSpec = tween(
                                            2000,
                                            easing = EaseInOutCubic
                                        ),
                                        gradientAnimationDelay = 1000,
                                        drawStyle = DrawStyle.Stroke(width = 2.dp),
                                        curvedEdges = false,
                                        dotProperties = DotProperties(
                                            enabled = true,
                                            color = SolidColor(Color.White),
                                            strokeColor = SolidColor(Color(0xFF4CAF50)),
                                        )
                                    )
                                )
                            },
                            // Y-axis configuration (data values)
                            indicatorProperties = HorizontalIndicatorProperties(
                                enabled = true,
                                textStyle = TextStyle(color = MaterialTheme.colorScheme.outline),
                                contentBuilder = { value ->
                                    if (value >= 1000) "${(value / 1000).toInt()}K"
                                    else "${value.toInt()}" // Format as whole numbers
                                },
                                position = IndicatorPosition.Horizontal.Start
                            ),
                            // X-axis configuration (months)
                            labelProperties = LabelProperties(
                                enabled = true,
                                labels = months,
                                textStyle = TextStyle(color = MaterialTheme.colorScheme.outline),
                                padding = 10.dp // Space between chart and labels
                            ),
                            // Chart styling
                            gridProperties = GridProperties(enabled = false), // Disable grid
                            dividerProperties = DividerProperties(enabled = true), // Disable dividers
                            animationMode = AnimationMode.Together(delayBuilder = { it * 500L })
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PowerProduction(profile: ProfileViewModel, mapPointViewModel: MapPointViewModel) {
    val consumptionColor = MaterialTheme.colorScheme.tertiaryContainer
    val productionColor = Color(0xFFFF9800)
    val uiState by mapPointViewModel.mapPointUIState.collectAsState()
    val chartData = uiState.chartDataString

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Text(
                    text = "Strømproduksjon",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "Forventet strømproduksjon og -forbruk de neste 12 månedene",
                    style = MaterialTheme.typography.titleSmall.copy(color = Color.Gray),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Parse data
                val productionData = profile.getMonthlyConsumptionValues()
                val consumptionData = mutableListOf<Double>()
                chartData.split(",").forEach { month ->
                    try {
                        consumptionData.add(month.toDouble())
                    } catch (e: NumberFormatException) {
                        consumptionData.add(0.0)
                    }
                }

                val months = listOf(
                    "Jan",
                    "Feb",
                    "Mar",
                    "Apr",
                    "May",
                    "Jun",
                    "Jul",
                    "Aug",
                    "Sep",
                    "Oct",
                    "Nov",
                    "Dec"
                )
                val maxValue = maxOf(
                    productionData.maxOrNull() ?: 0.0,
                    consumptionData.maxOrNull() ?: 0.0
                ) * 1.2 // Add 20% padding

                LineChart(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp, bottom = 8.dp),
                    data = remember {
                        listOf(
                            Line(
                                label = "Forbruk",
                                values = productionData,
                                color = SolidColor(productionColor),
                                strokeAnimationSpec = tween(2000),
                                drawStyle = DrawStyle.Stroke(width = 3.dp),
                                dotProperties = DotProperties(
                                    enabled = true,
                                    color = SolidColor(productionColor),
                                    radius = 5.dp
                                )
                            ),
                            Line(
                                label = "Produksjon",
                                values = consumptionData,
                                color = SolidColor(consumptionColor),
                                strokeAnimationSpec = tween(2000),
                                drawStyle = DrawStyle.Stroke(width = 3.dp),
                                dotProperties = DotProperties(
                                    enabled = true,
                                    color = SolidColor(consumptionColor),
                                    radius = 5.dp
                                )
                            )
                        )
                    },
                    labelProperties = LabelProperties(
                        enabled = true,
                        labels = months,
                        textStyle = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        padding = 4.dp
                    ),
                    indicatorProperties = HorizontalIndicatorProperties(
                        enabled = true,
                        textStyle = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        contentBuilder = { value -> "${value.toInt()} kWh" },
                        position = IndicatorPosition.Horizontal.Start
                    ),
                    labelHelperProperties = LabelHelperProperties(
                        enabled = true,
                        textStyle = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ),
                    maxValue = maxValue,
                    gridProperties = GridProperties(
                        enabled = true,
                        xAxisProperties = GridProperties.AxisProperties(
                            thickness = 1.dp
                        ),
                        yAxisProperties = GridProperties.AxisProperties(
                            thickness = 1.dp
                        )
                    ),
                    popupProperties = PopupProperties(
                        enabled = true,
                        textStyle = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onPrimary
                        ),
                        containerColor = MaterialTheme.colorScheme.primary,
                        cornerRadius = 8.dp
                    ),
                    animationMode = AnimationMode.Together { it * 300L }
                )
            }
        }
    }
}