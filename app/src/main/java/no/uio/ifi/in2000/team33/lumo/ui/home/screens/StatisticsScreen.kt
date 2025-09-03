package no.uio.ifi.in2000.team33.lumo.ui.home.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import no.uio.ifi.in2000.team33.lumo.ui.utility.components.SimpleRotatingLoader
import no.uio.ifi.in2000.team33.lumo.ui.home.components.Savings
import no.uio.ifi.in2000.team33.lumo.ui.home.components.PowerProduction
import no.uio.ifi.in2000.team33.lumo.ui.mappoint.MapPointUIState
import no.uio.ifi.in2000.team33.lumo.ui.mappoint.MapPointViewModel
import no.uio.ifi.in2000.team33.lumo.ui.network.NetworkViewModel
import no.uio.ifi.in2000.team33.lumo.ui.profile.ProfileViewModel
import no.uio.ifi.in2000.team33.lumo.ui.utility.components.ErrorPopUp
import no.uio.ifi.in2000.team33.lumo.ui.utility.functions.formatNumberWithSeparator
import no.uio.ifi.in2000.team33.lumo.ui.utility.components.months
import kotlin.math.roundToInt

/**
 * Statistics screen that shows solar panel savings and production estimates.
 * Uses StateFlow for all state management and reactive UI updates.
 */
@Composable
fun StatisticsScreen(
    profileViewModel: ProfileViewModel,
    navController: NavController,
    networkViewModel: NetworkViewModel,
    mapPointViewModel: MapPointViewModel
) {
    // Collect states from ViewModels
    val mapPointUIState by mapPointViewModel.mapPointUIState.collectAsState()
    val isOnline by networkViewModel.isOnline.collectAsState()
    val isLoading by mapPointViewModel.isLoading.collectAsState()

    val context = LocalContext.current

    // Request monthly data when screen loads
    LaunchedEffect(Unit) {
        Log.d("StatisticsScreen", "Launched - requesting monthly data")
        mapPointViewModel.ensureMonthlyDataLoaded()
        networkViewModel.checkConnectivity(context)
    }

    // Calculate monthly savings when data is available
    LaunchedEffect(mapPointUIState.monthlyProduction) {
        if (mapPointUIState.monthlyProduction != null) {
            mapPointViewModel.calculateMonthlySavingsFromJson(context)
        }
    }

    // Main container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        // Background curve
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.BottomCenter)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(topStart = 170.dp, topEnd = 0.dp)
                )
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            StatisticsHeader(navController = navController)

            // Annual savings section
            AnnualSavingsSection(
                mapPointViewModel = mapPointViewModel,
                context = context
            )

            // Monthly production section
            MonthlyProductionSection(
                mapPointUIState = mapPointUIState,
                isLoading = isLoading
            )

            // Charts sections
            Spacer(modifier = Modifier.height(30.dp))
            Savings(mapPointUIState.monthlySavings.orEmpty())

            Spacer(modifier = Modifier.height(20.dp))
            PowerProduction(profileViewModel, mapPointViewModel)

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Network error popup
        if (!isOnline) {
            ErrorPopUp(networkViewModel)
        }
    }
}

@Composable
private fun StatisticsHeader(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp, bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.outline
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Statistikk",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun AnnualSavingsSection(
    mapPointViewModel: MapPointViewModel,
    context: android.content.Context
) {
    // Calculate savings dynamically from monthly data
    val estimatedSavings = mapPointViewModel.calculateAnnualSavingsFromMonths(context) ?: 0.0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .height(104.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Du kan spare",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF79747E)
                    )

                    Text(
                        text = "kr ${formatNumberWithSeparator(estimatedSavings)}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFF358110)
                    )

                    Text(
                        text = "per år",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF79747E)
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.Savings,
                    contentDescription = "Savings",
                    tint = Color(0xFF79747E),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun MonthlyProductionSection(
    mapPointUIState: MapPointUIState,
    isLoading: Boolean
) {
    Spacer(modifier = Modifier.height(15.dp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Forventet strømproduksjon",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "Forventet strømproduksjon de 12 neste månedene.",
                style = MaterialTheme.typography.titleSmall.copy(color = Color.Gray),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    SimpleRotatingLoader(
                        size = 48.dp,
                        outerCircleColor = MaterialTheme.colorScheme.primary,
                        middleCircleColor = Color(0xFFFCC00D)
                    )
                }
            } else if (!mapPointUIState.monthlyProduction.isNullOrEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    months.forEachIndexed { index, month ->
                        val monthNumber = index + 1
                        val productionValue = mapPointUIState.monthlyProduction
                            .find { it.first == monthNumber }
                            ?.second
                            ?.roundToInt()
                            ?.toString() ?: "0"

                        MonthProductionItem(month, productionValue)
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ingen produksjonsdata tilgjengelig",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun MonthProductionItem(month: String, kWh: String) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(140.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.scrim),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = month,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF322F35)
            )

            Text(
                text = "${formatNumberWithSeparator(kWh.toDouble())} kWh",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4F378A)
            )
        }
    }
}