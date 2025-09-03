package no.uio.ifi.in2000.team33.lumo.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import no.uio.ifi.in2000.team33.lumo.ui.mappoint.MapPointUIState
import no.uio.ifi.in2000.team33.lumo.ui.mappoint.MapPointViewModel
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.MapPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHomeContent(
    currentPoint: MapPoint,
    mapPointUIState: MapPointUIState,
    isLoading: Boolean,
    showSheet: Boolean,
    onShowSheetChange: (Boolean) -> Unit,
    sheetState: SheetState,
    mapPointViewModel: MapPointViewModel,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .verticalScroll(rememberScrollState())
    ) {
        // Map background section
        MapBackgroundSection(
            currentPoint = currentPoint,
            mapPointUIState = mapPointUIState,
            isLoading = isLoading,
            showSheet = showSheet,
            onShowSheetChange = onShowSheetChange,
            sheetState = sheetState,
            mapPointViewModel = mapPointViewModel
        )

        // Load estimates when takflater change
        LaunchedEffect(currentPoint.name, currentPoint.registeredRoofs.size) {
            currentPoint.let { point ->
                if (point.registeredRoofs.isNotEmpty()) {
                    mapPointViewModel.loadEstimates(point)
                }
            }
        }

        LaunchedEffect(
            mapPointUIState.selectedTakflateIndex,
            currentPoint.registeredRoofs.size
        ) {
            currentPoint.let { point ->
                if (point.registeredRoofs.isNotEmpty()) {
                    val validIndex = mapPointUIState.selectedTakflateIndex.coerceIn(
                        0,
                        point.registeredRoofs.size - 1
                    )
                    if (validIndex != mapPointUIState.selectedTakflateIndex) {
                        mapPointViewModel.selectTakflateForAnnualEstimate(validIndex)
                    }
                }
            }
        }

        // Main content cards
        ContentCards(
            currentPoint = currentPoint,
            mapPointUIState = mapPointUIState,
            isLoading = isLoading,
            navController = navController
        )
    }
}

