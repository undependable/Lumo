package no.uio.ifi.in2000.team33.lumo.ui.home.components

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import no.uio.ifi.in2000.team33.lumo.ui.mappoint.MapPointUIState
import no.uio.ifi.in2000.team33.lumo.ui.mappoint.MapPointViewModel
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.MapPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapBackgroundSection(
    currentPoint: MapPoint,
    mapPointUIState: MapPointUIState,
    isLoading: Boolean,
    showSheet: Boolean,
    onShowSheetChange: (Boolean) -> Unit,
    sheetState: SheetState,
    mapPointViewModel: MapPointViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .shadow(8.dp)
    ) {
        // Background image
        AsyncImage(
            model = "https://api.mapbox.com/styles/v1/mapbox/streets-v12/static/pin-l-home+f74e4e(${currentPoint.lon},${currentPoint.lat})/${currentPoint.lon},${currentPoint.lat},18.2,25/700x400?access_token=",
            contentDescription = "Map background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.4f),
                        0.3f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.1f)
                    )
                )
        )

        // Content overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(70.dp))

            AddressTitleDisplay(
                showSheet = onShowSheetChange,
                mapPoint = currentPoint,
            )

            // Property selection sheet
            if (showSheet) {
                PropertySelectionSheet(
                    mapPointUIState = mapPointUIState,
                    sheetState = sheetState,
                    onShowSheetChange = onShowSheetChange,
                    mapPointViewModel = mapPointViewModel
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailPill(
                    currentPoint = currentPoint,
                    isLoadingTemperature = isLoading
                )
            }
        }

        // Bottom gradient
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(20.dp)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        1f to MaterialTheme.colorScheme.surfaceVariant
                    )
                )
        )
    }
}