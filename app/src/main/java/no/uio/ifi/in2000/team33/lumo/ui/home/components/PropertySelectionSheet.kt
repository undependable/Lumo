package no.uio.ifi.in2000.team33.lumo.ui.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team33.lumo.ui.mappoint.MapPointUIState
import no.uio.ifi.in2000.team33.lumo.ui.mappoint.MapPointViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertySelectionSheet(
    mapPointUIState: MapPointUIState,
    sheetState: SheetState,
    onShowSheetChange: (Boolean) -> Unit,
    mapPointViewModel: MapPointViewModel
) {
    ModalBottomSheet(
        onDismissRequest = { onShowSheetChange(false) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Start)
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Velg primærbolig",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Her kan du velge hvilken bolig som skal vises",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            itemsIndexed(mapPointUIState.mapPoints) { index, mapPoint ->
                PropertyCard(
                    mapPoint = mapPoint,
                    isPrimary = index == 0,
                    onClick = {
                        mapPointViewModel.selectPoint(mapPoint)
                        onShowSheetChange(false)
                    }
                )
            }
        }
    }
}