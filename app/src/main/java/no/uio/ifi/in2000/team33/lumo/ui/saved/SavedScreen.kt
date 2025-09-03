package no.uio.ifi.in2000.team33.lumo.ui.saved

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.House
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team33.lumo.ui.mappoint.MapPointViewModel
import no.uio.ifi.in2000.team33.lumo.ui.network.NetworkViewModel
import no.uio.ifi.in2000.team33.lumo.ui.search.viewmodel.SearchScreenViewModel
import no.uio.ifi.in2000.team33.lumo.ui.utility.components.ConfirmationDialog
import no.uio.ifi.in2000.team33.lumo.ui.utility.components.ErrorPopUp
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.MapPoint
import no.uio.ifi.in2000.team33.lumo.ui.utility.theme.isDark

@Composable
fun SavedScreen(
    navController: NavController,
    networkViewModel: NetworkViewModel,
    searchScreenViewModel: SearchScreenViewModel,
    mapPointViewModel: MapPointViewModel
) {
    // Collect state from ViewModels
    val favoritesState by mapPointViewModel.favoriteMapPoints.collectAsState()
    val isOnline by networkViewModel.isOnline.collectAsState()

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var pointToDelete by remember { mutableStateOf<MapPoint?>(null) }

    val context = LocalContext.current

    // Check network connectivity when screen loads
    LaunchedEffect(Unit) {
        networkViewModel.checkConnectivity(context)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(if (isDark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 29.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Spacer(modifier = Modifier.height(70.dp))

            Text(
                text = "Lagret",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline
            )

            if (favoritesState.isNotEmpty()) {
                Text(
                    text = "Oversikt over dine lagrede addresser.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            if (favoritesState.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ingen lagrede steder",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Søk etter adresser for å lagre steder på kartet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                // List of saved points
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    favoritesState.forEachIndexed { index, mapPoint ->
                        SavedPointCard(
                            mapPoint = mapPoint,
                            isPrimary = index == 0,
                            isOnline = isOnline,
                            onClick = {
                                searchScreenViewModel.clearSearchBarText()
                                // Set the selected point
                                searchScreenViewModel.updateSelectedMapPoint(mapPoint)
                                navController.navigate("searchscreen") {
                                    popUpTo("homescreen") {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onRemove = {
                                pointToDelete = mapPoint
                                showConfirmationDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Network error popup
        if (!isOnline) {
            ErrorPopUp(networkViewModel)
        }

        // Confirmation dialog for removing points
        if (showConfirmationDialog && pointToDelete != null) {
            ConfirmationDialog(
                title = "Fjern adresse",
                message = "Er du sikker på at du vil fjerne '${pointToDelete?.name}' fra dine lagrede steder?",
                confirmButtonText = "Ja",
                cancelButtonText = "Nei",
                onConfirm = {
                    pointToDelete?.let { point ->
                        mapPointViewModel.toggleFavorite(point)
                        scope.launch {
                            snackbarHostState.showSnackbar("Adresse fjernet")
                        }
                    }
                    showConfirmationDialog = false
                    pointToDelete = null
                },
                onDismiss = {
                    showConfirmationDialog = false
                    pointToDelete = null
                }
            )
        }
    }
}

@Composable
private fun SavedPointCard(
    mapPoint: MapPoint,
    isPrimary: Boolean,
    isOnline: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val imageUrl =
        "https://api.mapbox.com/styles/v1/mapbox/${if (isDark) "dark-v11" else "streets-v12"}/static/pin-l-home+f74e4e(${mapPoint.lon},${mapPoint.lat})/${mapPoint.lon},${mapPoint.lat},18.2,25/700x400?access_token="

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(5.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Column {
            Box {
                if (isOnline) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Map preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp
                                )
                            ),
                        contentScale = ContentScale.Crop
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = if (!mapPoint.isHouse) Icons.Filled.Apartment else Icons.Filled.House,
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
                        text = "${if (isPrimary) "👑 " else ""} ${mapPoint.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onRemove) {
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = "Fjern",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${mapPoint.areaCode} \t|\t ${mapPoint.areaPlace}, Norge",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}