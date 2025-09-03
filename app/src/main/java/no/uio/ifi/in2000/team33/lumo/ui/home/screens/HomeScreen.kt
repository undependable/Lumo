package no.uio.ifi.in2000.team33.lumo.ui.home.screens

import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import no.uio.ifi.in2000.team33.lumo.ui.home.components.ErrorAlert
import no.uio.ifi.in2000.team33.lumo.ui.home.components.LoadingScreen
import no.uio.ifi.in2000.team33.lumo.ui.home.components.MainHomeContent
import no.uio.ifi.in2000.team33.lumo.ui.home.components.WelcomeHomeScreen
import no.uio.ifi.in2000.team33.lumo.ui.mappoint.MapPointViewModel
import no.uio.ifi.in2000.team33.lumo.ui.network.NetworkViewModel
import no.uio.ifi.in2000.team33.lumo.ui.profile.ProfileViewModel
import no.uio.ifi.in2000.team33.lumo.ui.utility.components.ErrorPopUp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    mapPointViewModel: MapPointViewModel,
    networkViewModel: NetworkViewModel,
    profileViewModel: ProfileViewModel
) {
    // Collect states from ViewModels
    val mapPointUIState by mapPointViewModel.mapPointUIState.collectAsState()
    val currentPoint = mapPointUIState.currentMapPoint
    val favoritePoints by mapPointViewModel.favoriteMapPoints.collectAsState()
    val isLoading by mapPointViewModel.isLoading.collectAsState()
    val isOnline by networkViewModel.isOnline.collectAsState()
    val userInfoUiState by profileViewModel.userInfo.collectAsState()

    val context = LocalContext.current
    val userName = userInfoUiState.user?.firstName ?: "sjef"

    // Get current route to detect navigation returns
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Always reload points when coming to homescreen
    LaunchedEffect(currentRoute) {
        if (currentRoute == "homescreen" && mapPointUIState.mapPoints.filter { it.isFavorite }.isEmpty()) {
            mapPointViewModel.loadPoints()
        }
    }

    // Check network connectivity
    LaunchedEffect(Unit) {
        networkViewModel.checkConnectivity(context)
    }
    LaunchedEffect(Unit) {
        if (favoritePoints.isEmpty()) {
            mapPointViewModel.loadPoints()
        }
    }

    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Content based on current state
    when {
        // Show loading during initial app load (no points at all)
        isLoading && favoritePoints.isEmpty() -> {
            LoadingScreen()
            Log.d("WHS", "when 1 - initial loading")
        }

        // Show welcome screen if no favorite points exist after loading
        favoritePoints.isEmpty() -> {
            WelcomeHomeScreen(navController, userName)
            Log.d("WHS", "when 2 - no favorites")
        }

        // Show error if current point has no takflater (but is favorited)
        currentPoint != null && currentPoint.isFavorite && currentPoint.registeredRoofs.isEmpty() -> {
            ErrorAlert(currentPoint, navController, "IKKE_TAKFLATE")
            Log.d("WHS", "when 3 - no takflater")
        }

        // Show main content if we have a valid favorited current point with takflater
        currentPoint != null && currentPoint.isFavorite && currentPoint.registeredRoofs.isNotEmpty() -> {
            MainHomeContent(
                currentPoint = currentPoint,
                mapPointUIState = mapPointUIState,
                isLoading = isLoading,
                showSheet = showSheet,
                onShowSheetChange = { showSheet = it },
                sheetState = sheetState,
                mapPointViewModel = mapPointViewModel,
                navController = navController
            )
            Log.d("WHS", "when 4 - main content")
        }

        // Fallback to welcome screen
        else -> {
            LoadingScreen()
            Log.d("WHS", "when 6 - fallback")
        }
    }
    // Show network error if no connectivity
    if (!isOnline) {
        ErrorPopUp(networkViewModel)
    }
}