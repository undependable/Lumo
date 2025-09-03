package no.uio.ifi.in2000.team33.lumo.ui.search

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.outlined.EnergySavingsLeaf
import androidx.compose.material.icons.outlined.Roofing
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.IconImage
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotationState
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import no.uio.ifi.in2000.team33.lumo.ui.utility.components.SimpleRotatingLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team33.lumo.R
import no.uio.ifi.in2000.team33.lumo.data.address.model.Adresser
import no.uio.ifi.in2000.team33.lumo.ui.mappoint.MapPointViewModel
import no.uio.ifi.in2000.team33.lumo.ui.network.NetworkViewModel
import no.uio.ifi.in2000.team33.lumo.ui.search.viewmodel.SearchScreenUIState
import no.uio.ifi.in2000.team33.lumo.ui.search.viewmodel.SearchScreenViewModel
import no.uio.ifi.in2000.team33.lumo.ui.utility.components.AddressSuggestions
import no.uio.ifi.in2000.team33.lumo.ui.utility.components.ErrorPopUp
import no.uio.ifi.in2000.team33.lumo.ui.utility.components.GeneralPopup
import no.uio.ifi.in2000.team33.lumo.ui.utility.components.SearchBar
import no.uio.ifi.in2000.team33.lumo.ui.utility.components.TakflateSheet
import no.uio.ifi.in2000.team33.lumo.ui.utility.components.smallCard
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.ErrorType
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.MapPoint
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.OnboardingPage
import no.uio.ifi.in2000.team33.lumo.ui.utility.functions.formatAddressName
import no.uio.ifi.in2000.team33.lumo.ui.utility.theme.isDark

// Static values
var SPACER_FOR_RESIZE: Dp = 5.dp

// Map styles
val MAP_STYLE_DARK by mutableStateOf("mapbox://styles/caaaaasi/cm8hmj6gt011y01sehqs4gotm")
val MAP_STYLE_LIGHT by mutableStateOf("mapbox://styles/caaaaasi/cm8hi42za013o01sbhtlm7qei")
val MAP_STYLE_2D_LIGHT by mutableStateOf("mapbox://styles/caaaaasi/cm9wt747w008u01sih1437xjo")
val MAP_STYLE_2D_DARK by mutableStateOf("mapbox://styles/caaaaasi/cm9wtfqt101hl01quey196nse")
var MAP_STYLE_2D by mutableStateOf(false)

// Resource IDs for markers
val normalMarkerResourceId = R.drawable.red_marker
val favoriteMarkerResourceId = R.drawable.favourite_house

fun flyToLocation(
    mapViewportState: MapViewportState,
    point: Point,
    zoom: Double = 17.5,
    duration: Long = 1000L
) {
    Log.i("FLYTOLOCATION", "FLYING TO LOCATION")
    mapViewportState.flyTo(
        cameraOptions = CameraOptions.Builder()
            .center(point)
            .zoom(zoom)
            .build(),
        animationOptions = MapAnimationOptions.Builder()
            .duration(duration)
            .build()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    searchScreenViewModel: SearchScreenViewModel,
    networkViewModel: NetworkViewModel,
    mapPointViewModel: MapPointViewModel,
    fusedLocationProviderClient: FusedLocationProviderClient
) {
    // Collect states from ViewModels
    val searchUIState by searchScreenViewModel.mapScreenUiState.collectAsState()
    val mapPointUIState by mapPointViewModel.mapPointUIState.collectAsState()
    val isSearchLoading by searchScreenViewModel.isSearchLoading.collectAsState()
    val isLocationLoading by searchScreenViewModel.isLocationLoading.collectAsState()
    val isMapPointLoading by mapPointViewModel.isLoading.collectAsState()
    val isOnline by networkViewModel.isOnline.collectAsState()

    val context = LocalContext.current

    // Check network connectivity when screen loads
    LaunchedEffect(Unit) {
        networkViewModel.checkConnectivity(context)
    }

    // Initialize map markers
    val normalMarker = rememberIconImage(
        key = normalMarkerResourceId,
        painter = painterResource(normalMarkerResourceId)
    )
    val favoriteMarker = rememberIconImage(
        key = favoriteMarkerResourceId,
        painter = painterResource(favoriteMarkerResourceId)
    )

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val savedCameraState by searchScreenViewModel.mapCameraState.collectAsState()

    // Initialize map viewport with saved state
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(savedCameraState.center)
            zoom(savedCameraState.zoom ?: 13.5)
            bearing(savedCameraState.bearing ?: 25.0)
            pitch(savedCameraState.pitch ?: 0.0)
        }
    }

    LaunchedEffect(key1 = Unit) {
        // Check if we have a selected point from navigation
        searchUIState.selectedMapPoint?.let { point ->
            // Wait a short moment for the map to initialize
            kotlinx.coroutines.delay(300)

            // Zoom to the selected point
            val selectedPoint = Point.fromLngLat(point.lon, point.lat)
            flyToLocation(
                mapViewportState = mapViewportState,
                point = selectedPoint,
                zoom = 17.5 // You can adjust this zoom level
            )
            mapPointViewModel.loadCurrentTemperature(point)
            searchScreenViewModel.updateSearchBarText(point.name)
        }
    }

    // Update camera state when viewport changes
    LaunchedEffect(Unit) {
        snapshotFlow { mapViewportState.cameraState }.collect { cameraState ->
            val newCamera = CameraOptions.Builder()
                .center(cameraState?.center)
                .zoom(cameraState?.zoom)
                .bearing(cameraState?.bearing)
                .pitch(cameraState?.pitch)
                .build()
            searchScreenViewModel.updateMapCameraState(newCamera)
        }
    }


    // Handle bottom sheet visibility
    LaunchedEffect(searchUIState.showAddressBottomSheet) {
        if (searchUIState.showAddressBottomSheet && searchUIState.selectedMapPoint != null) {
            bottomSheetState.show()
        } else {
            bottomSheetState.hide()
        }
    }

    // Event handlers
    val onAddressChange: (String) -> Unit = { text ->
        searchScreenViewModel.updateSearchBarText(text)
    }

    val onAddressEntered: (String) -> Unit = { address ->
        if (isOnline) {
            CoroutineScope(Dispatchers.IO).launch {
                val suggestions = searchScreenViewModel.getAddressuggestions(address)
                if (suggestions.size == 1) {
                    val mapPoint = mapPointViewModel.addMapPoint(suggestions[0])
                    searchScreenViewModel.updateSelectedMapPoint(mapPoint)
                    mapPointViewModel.loadCurrentTemperature(mapPoint)
                    val point = Point.fromLngLat(mapPoint.lon, mapPoint.lat)
                    flyToLocation(mapViewportState = mapViewportState, point = point)
                }
            }
        }
    }

    val onAddressChosen: (Adresser) -> Unit = { address ->
        searchScreenViewModel.updateSearchBarText(address.adressetekst)
        if (isOnline) {
            CoroutineScope(Dispatchers.IO).launch {
                searchScreenViewModel.clearAddressSuggestions()
                val mapPoint = mapPointViewModel.addMapPoint(address)
                searchScreenViewModel.updateSelectedMapPoint(mapPoint)
                mapPointViewModel.loadCurrentTemperature(mapPoint)
                val point = Point.fromLngLat(mapPoint.lon, mapPoint.lat)
                flyToLocation(mapViewportState = mapViewportState, point = point)
            }
        }
    }

    val onMapClick: (Double, Double) -> Unit = { lat, lon ->
        if (isOnline) {
            CoroutineScope(Dispatchers.IO).launch {
                val address = searchScreenViewModel.getAddressFromCoordinates(lat, lon)
                if (address != null) {
                    val mapPoint = mapPointViewModel.addMapPoint(address)
                    searchScreenViewModel.updateSelectedMapPoint(mapPoint)
                    mapPointViewModel.loadCurrentTemperature(mapPoint)
                    val point = Point.fromLngLat(mapPoint.lon, mapPoint.lat)
                    flyToLocation(mapViewportState = mapViewportState, point = point)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Map content
        MapContent(
            mapPoints = mapPointUIState.mapPoints,
            normalMarker = normalMarker,
            favoriteMarker = favoriteMarker,
            setSelectedPoint = { point ->
                searchScreenViewModel.updateSelectedMapPoint(point)
            },
            mapViewportState = mapViewportState,
            onGetCoordsFromClick = onMapClick
        )

        // Search bar content
        SearchBarContent(
            searchScreenViewModel = searchScreenViewModel,
            onAddressEntered = onAddressEntered,
            onAddressChosen = onAddressChosen,
            onAddressChange = onAddressChange,
            searchUIState = searchUIState,
            mapViewportState = mapViewportState,
            showInfo = { searchScreenViewModel.showInfoModal(it) },
            fusedLocationClient = fusedLocationProviderClient
        )

        // Loading animation
        val shouldShowLoading = isSearchLoading || isLocationLoading || isMapPointLoading
        AnimatedVisibility(
            visible = shouldShowLoading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                SimpleRotatingLoader(
                    size = 80.dp,
                    outerCircleColor = MaterialTheme.colorScheme.primary,
                    middleCircleColor = Color(0xFFFCC00D),
                    innerCircleColor = Color(0xFFFC9C0D)
                )
            }
        }

        // Address modal
        if (searchUIState.showAddressBottomSheet && searchUIState.selectedMapPoint != null) {
            AddresseModal(
                selectedPoint = searchUIState.selectedMapPoint!!,
                bottomSheetState = bottomSheetState,
                onDismiss = {
                    searchScreenViewModel.updateSelectedMapPoint(null)
                    mapPointViewModel.removeUnsavedMapPoints()
                    searchScreenViewModel.dismissAddressModal()
                },
                showTakflateSheet = searchUIState.showTakflateBottomSheet,
                setShowTakflateSheet = { searchScreenViewModel.showTakflateSheet(it) },
                mapPointViewModel = mapPointViewModel,
                navController = navController
            )
        }

        // Error handling
        if (searchUIState.hasError && searchUIState.error == ErrorType.INVALID_ADDRESS) {
            GeneralPopup(
                errorType = searchUIState.error!!,
                onRetryClick = { searchScreenViewModel.clearAddressError() },
                onDismiss = { searchScreenViewModel.clearAddressError() }
            )
        }

        // Network error popup
        if (!isOnline) {
            ErrorPopUp(networkViewModel)
        }

        // Info modal
        if (searchUIState.showInfoModal) {
            InfoModal(
                bottomSheetState = bottomSheetState,
                onDismiss = { searchScreenViewModel.showInfoModal(false) }
            )
        }
    }
}

@Composable
fun MapContent(
    onGetCoordsFromClick: (Double, Double) -> Unit,
    mapPoints: List<MapPoint>,
    normalMarker: IconImage,
    favoriteMarker: IconImage,
    setSelectedPoint: (MapPoint?) -> Unit,
    mapViewportState: MapViewportState
) {
    MapboxMap(
        modifier = Modifier.fillMaxSize(),
        style = {
            MapStyle(
                style = if (!MAP_STYLE_2D) {
                    if (isDark) MAP_STYLE_DARK else MAP_STYLE_LIGHT
                } else {
                    if (isDark) MAP_STYLE_2D_DARK else MAP_STYLE_2D_LIGHT
                }
            )
        },
        mapViewportState = mapViewportState,
        scaleBar = { },
        logo = { },
        compass = { },
        attribution = { },
        onMapClickListener = { geopoint ->
            onGetCoordsFromClick(geopoint.latitude(), geopoint.longitude())
            setSelectedPoint(null)
            false
        }
    ) {
        // Display ALL map points, not just favorites in search screen
        mapPoints.forEach { point ->
            {
                setSelectedPoint(point)
                true
            }
            PointAnnotation(point = Point.fromLngLat(point.lon, point.lat),
                init = fun PointAnnotationState.() {
                    iconImage = if (point.isFavorite) favoriteMarker else normalMarker
                })
        }
    }
}

@Composable
fun SearchBarContent(
    searchScreenViewModel: SearchScreenViewModel,
    onAddressEntered: (String) -> Unit,
    onAddressChosen: (Adresser) -> Unit,
    onAddressChange: (String) -> Unit,
    searchUIState: SearchScreenUIState,
    mapViewportState: MapViewportState,
    showInfo: (Boolean) -> Unit,
    fusedLocationClient: FusedLocationProviderClient
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                if (isDark) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.83f)
                else Color(0xFFe8ecec).copy(alpha = 0.83f)
            )
    ) {
        // Search bar
        SearchBar(
            searchScreenViewModel = searchScreenViewModel,
            showInfo = showInfo,
            modifier = Modifier.padding(top = 50.dp),
            onAddressEntered = onAddressEntered,
            onAddressChange = onAddressChange,
            addressText = searchUIState.searchBarText,
            fusedLocationClient = fusedLocationClient,
            onLocationFound = { latitude, longitude ->
                val point = Point.fromLngLat(longitude, latitude)
                flyToLocation(mapViewportState = mapViewportState, point = point)
            }
        )

        // Address suggestions
        AnimatedVisibility(
            visible = searchUIState.showSuggestions,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .width(350.dp)
                .align(Alignment.TopCenter)
                .offset(y = 100.dp)
                .zIndex(1f)
        ) {
            if (searchUIState.hasAddressSuggestions) {
                AddressSuggestions(
                    addresses = searchUIState.suggestedAddresses,
                    onAddressSelected = onAddressChosen
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoModal(
    bottomSheetState: SheetState,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        sheetState = bottomSheetState,
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxHeight(0.7f),
        onDismissRequest = onDismiss,
    ) {
        val pages = listOf(
            OnboardingPage(
                title = "Finn din bolig",
                description = "Skriv inn adressen i søkefeltet og klikk deretter på tomten som kommer opp",
                image = Icons.Outlined.Search,
                icon = R.drawable.guide1
            ),
            OnboardingPage(
                title = "Legg til takflate",
                description = "Legg deretter til takflaten ved å skrive inn areal, vinkel og retning.",
                image = Icons.Outlined.Roofing,
                icon = R.drawable.guide2
            ),
            OnboardingPage(
                title = "Få oversikt",
                description = "Din bolig er nå registrert og du kan undersøke estimerte besparelser og strømproduksjon.",
                image = Icons.Outlined.EnergySavingsLeaf,
                icon = R.drawable.guide3
            ),
        )

        var currentPage by remember { mutableStateOf(0) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(3.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.scrim),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Page content based on current page
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = Color(0xFFFCC00D),
                                shape = CircleShape
                            )
                            .border(1.dp, Color(0xFFDEA600), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = pages[currentPage].image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(40.dp)
                        )
                        when(currentPage){
                            0 -> Image(painter = painterResource(id = R.drawable.guide1), contentDescription = "Guidebilde")
                            1 -> Image(painter = painterResource(id = R.drawable.guide2), contentDescription = "Guidebilde")
                            2 -> Image(painter = painterResource(id = R.drawable.guide3), contentDescription = "Guidebilde")

                        }
                    }

                    Text(
                        text = pages[currentPage].title,
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = pages[currentPage].description,
                        fontSize = 12.sp,
                        letterSpacing = 0.4.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.padding(bottom = 30.dp)
            ) {
                pages.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == currentPage) 12.dp else 8.dp)
                            .background(
                                color = if (index == currentPage) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline
                            )
                    )
                }
            }

            Column {
                Button(
                    onClick = {
                        if (currentPage < pages.lastIndex) currentPage++
                        else onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(59.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (currentPage == pages.lastIndex) "Skjønner" else "Neste",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.outline,
                        letterSpacing = 0.15.sp
                    )
                }

                if (currentPage > 0) {
                    OutlinedButton(
                        onClick = { currentPage-- },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 13.dp)
                            .height(59.dp),
                        shape = RoundedCornerShape(50.dp),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
                    ) {
                        Text(
                            text = "Tilbake",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.outline,
                            letterSpacing = 0.15.sp
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(59.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddresseModal(
    selectedPoint: MapPoint,
    bottomSheetState: SheetState,
    onDismiss: () -> Unit,
    showTakflateSheet: Boolean,
    setShowTakflateSheet: (Boolean) -> Unit,
    mapPointViewModel: MapPointViewModel,
    navController: NavController
) {
    ModalBottomSheet(
        sheetState = bottomSheetState,
        containerColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxHeight(0.35f),
        onDismissRequest = onDismiss,
    ) {
        AddresseModalDetails(
            point = selectedPoint,
            showTakflateSheet = showTakflateSheet,
            setShowTakflateSheet = setShowTakflateSheet,
            mapPointViewModel = mapPointViewModel,
            navController = navController,
            onDismissModal = onDismiss
        )
    }
}

@Composable
fun AddresseModalDetails(
    point: MapPoint,
    showTakflateSheet: Boolean,
    setShowTakflateSheet: (Boolean) -> Unit,
    mapPointViewModel: MapPointViewModel,
    navController: NavController,
    onDismissModal: () -> Unit
) {
    val mapPointUIState by mapPointViewModel.mapPointUIState.collectAsState()

    val currentPoint = mapPointUIState.mapPoints.find {
        it.name == point.name && it.lat == point.lat && it.lon == point.lon
    } ?: point

    LaunchedEffect(point) {
        mapPointViewModel.ensurePointInitialized(point)
        if (point.temperature == 10000.0) {
            mapPointViewModel.loadCurrentTemperature(point)
        }
    }

    // Ensure point is initialized in ViewModel
    LaunchedEffect(point) {
        mapPointViewModel.ensurePointInitialized(point)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SPACER_FOR_RESIZE = if (formatAddressName(point.name) != point.name) 15.dp
                else 30.dp

                Text(
                    modifier = Modifier.weight(1f),
                    text = formatAddressName(point.name),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Temperature card with loading support
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(vertical = 9.dp, horizontal = 12.dp)
                ) {
                    if (currentPoint.temperature == 10000.0) {
                        Box(
                            modifier = Modifier.size(height = 24.dp, width = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            SimpleRotatingLoader(
                                size = 20.dp,
                                outerCircleColor = MaterialTheme.colorScheme.primary,
                                middleCircleColor = Color(0xFFFCC00D)
                            )
                        }
                    } else {
                        smallCard(
                            modifier = Modifier,
                            title = "${currentPoint.temperature}°C",
                            destination = "Temperature pill",
                            vector = Icons.Default.Thermostat
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(SPACER_FOR_RESIZE))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(5.dp, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surface
                    else Color(0xFFF5EFF7)
                ),
                onClick = {
                    mapPointViewModel.setCurrentPoint(point)
                    setShowTakflateSheet(true)
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Legg til takflate",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // Takflate sheet
        if (showTakflateSheet) {
            TakflateSheet(
                point = point,
                onDismiss = {
                    setShowTakflateSheet(false)
                    onDismissModal()
                },
                onAngleChosen = { vinkel, areal, aspect ->
                    mapPointViewModel.beregnProduksjonMedVinkel(
                        point,
                        vinkel,
                        areal,
                        aspect
                    )
                },
                navController = navController,
                mapPointViewModel = mapPointViewModel
            )
        }
    }
}