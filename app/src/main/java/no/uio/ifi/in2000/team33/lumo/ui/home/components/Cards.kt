package no.uio.ifi.in2000.team33.lumo.ui.home.components

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.SolarPower
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import no.uio.ifi.in2000.team33.lumo.ui.utility.components.SimpleRotatingLoader
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DividerProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorPosition
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.team33.lumo.data.address.AddressRepository
import no.uio.ifi.in2000.team33.lumo.data.address.model.Adresser
import no.uio.ifi.in2000.team33.lumo.ui.mappoint.MapPointUIState
import no.uio.ifi.in2000.team33.lumo.ui.utility.components.DestinationCard
import no.uio.ifi.in2000.team33.lumo.ui.utility.components.LargeCard
import no.uio.ifi.in2000.team33.lumo.ui.utility.functions.formatDoubleToNoDecimal
import no.uio.ifi.in2000.team33.lumo.ui.utility.functions.formatNumberWithSeparator
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.MapPoint
import no.uio.ifi.in2000.team33.lumo.ui.utility.theme.isDark

@Composable
fun DetailsCard(
    navController: NavController,
    mapPoint: MapPoint,
    isLoading: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                // Price card
                Box(modifier = Modifier.weight(1f)) {
                    if (isLoading || !mapPoint.hasPrice) {
                        LoadingCard()
                    } else {
                        LargeCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            title = mapPoint.priceThisHour,
                            description = "Dagens timespris",
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = "Timepris"
                        )
                    }
                }

                // Annual estimate card
                Box(modifier = Modifier.weight(1f)) {
                    if (isLoading || !mapPoint.hasAnnualEstimate) {
                        LoadingCard()
                    } else {
                        LargeCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            title = "${formatNumberWithSeparator(formatDoubleToNoDecimal(mapPoint.annualEstimate).toDouble())} kWh",
                            description = "Årlig estimat",
                            imageVector = Icons.Outlined.SolarPower,
                            contentDescription = "Solenergi"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                DestinationCard(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.scrim),
                    title = "Takflater",
                    destination = "roofscreen",
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun PropertyCard(
    mapPoint: MapPoint,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    val addressRepo = AddressRepository()
    var addressInfo by remember { mutableStateOf<Adresser?>(null) }

    LaunchedEffect(mapPoint.name) {
        addressInfo = withContext(Dispatchers.IO) {
            addressRepo.getAddressInformation(mapPoint.name.replace('.', ' '))
        }
    }

    val imageUrl =
        "https://api.mapbox.com/styles/v1/mapbox/${if (isDark) "dark-v11" else "streets-v12"}/static/pin-l-home+f74e4e(${mapPoint.lon},${mapPoint.lat})/${mapPoint.lon},${mapPoint.lat},18.2,25/700x400?access_token="

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(5.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Column {
            Box {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Map preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(
                            Color.White,
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = if (mapPoint.isHouse) Icons.Filled.House else Icons.Filled.Apartment,
                        contentDescription = "BOLIG_TYPE",
                        tint = Color.Black
                    )
                }
            }

            Column(horizontalAlignment = Alignment.Start) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "${if (isPrimary && mapPoint.isFavorite) "👑 " else ""}${mapPoint.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ContentCards(
    currentPoint: MapPoint,
    mapPointUIState: MapPointUIState,
    isLoading: Boolean,
    navController: NavController
) {
    if (isLoading && !mapPointUIState.hasMonthlyData) {
        // Show loading for estimates
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            SimpleRotatingLoader(
                size = 48.dp,
                outerCircleColor = MaterialTheme.colorScheme.primary,
                middleCircleColor = Color(0xFFFCC00D)
            )
        }
    } else {
        // Show charts and details
        Spacer(modifier = Modifier.height(20.dp))
        SunlightCard(
            plottingData = mapPointUIState.chartDataString,
            navController = navController
        )

        DetailsCard(
            navController = navController,
            mapPoint = currentPoint,
            isLoading = isLoading
        )
    }
}

@Composable
fun DetailPill(currentPoint: MapPoint, isLoadingTemperature: Boolean = false) {
    Surface(
        color = MaterialTheme.colorScheme.scrim,
        shape = RoundedCornerShape(50),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.scrim)
                .padding(vertical = 9.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoadingTemperature || !currentPoint.hasTemperature) {
                SimpleRotatingLoader(
                    size = 24.dp,
                    outerCircleColor = MaterialTheme.colorScheme.primary,
                    middleCircleColor = Color(0xFFFCC00D)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = "Temperature",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "${currentPoint.temperature}°C",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


@Composable
fun SunlightCard(plottingData: String, navController: NavController) {
    val months =
        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    // Use remember with key to ensure chart redraws when data changes
    val monthlyData: List<Double> = remember(plottingData) {
        if (plottingData.isEmpty()) {
            List(12) { 0.0 }
        } else {
            val data = mutableListOf<Double>()
            plottingData.split(",").forEach { month ->
                try {
                    data.add(month.toDouble())
                } catch (e: NumberFormatException) {
                    data.add(0.0)
                }
            }
            data
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(230.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick = { navController.navigate("statisticsscreen") }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Statistikk",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    ),
                    textAlign = TextAlign.Start
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Gå videre",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "Estimert månedlig strømproduksjon",
                style = MaterialTheme.typography.titleSmall.copy(color = Color.Gray),
                textAlign = TextAlign.Start
            )

            val chartColor = MaterialTheme.colorScheme.tertiaryContainer

            Spacer(modifier = Modifier.height(20.dp))

            // Add key to LineChart to force redraw when data changes
            key(plottingData) {
                LineChart(
                    labelHelperProperties = LabelHelperProperties(enabled = false),
                    modifier = Modifier.fillMaxSize(),
                    data = listOf(
                        Line(
                            label = "",
                            values = monthlyData,
                            color = SolidColor(chartColor),
                            firstGradientFillColor = chartColor.copy(alpha = 0.5f),
                            secondGradientFillColor = Color.Transparent,
                            strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                            gradientAnimationDelay = 1000,
                            drawStyle = DrawStyle.Stroke(width = 2.dp),
                        )
                    ),
                    indicatorProperties = HorizontalIndicatorProperties(
                        enabled = true,
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 12.sp
                        ),
                        contentBuilder = { value ->
                            if (value >= 1000) "${(value / 1000).toInt()}K"
                            else "${value.toInt()}"
                        },
                        position = IndicatorPosition.Horizontal.Start
                    ),
                    labelProperties = LabelProperties(
                        enabled = true,
                        labels = months,
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 12.sp
                        ),
                        padding = 10.dp
                    ),
                    gridProperties = GridProperties(enabled = false),
                    dividerProperties = DividerProperties(enabled = true),
                    animationMode = AnimationMode.Together(delayBuilder = { it * 500L })
                )
            }
        }
    }
}
