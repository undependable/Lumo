package no.uio.ifi.in2000.team33.lumo.ui.home.components


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import no.uio.ifi.in2000.team33.lumo.ui.mappoint.MapPointViewModel
import no.uio.ifi.in2000.team33.lumo.ui.profile.ProfileViewModel
import no.uio.ifi.in2000.team33.lumo.ui.search.viewmodel.SearchScreenViewModel
import no.uio.ifi.in2000.team33.lumo.ui.utility.BottomNavigationBar
import no.uio.ifi.in2000.team33.lumo.ui.utility.Navigation

@Composable
fun Display(
    searchScreenViewModel: SearchScreenViewModel,
    profileViewModel: ProfileViewModel,
    mapPointViewModel: MapPointViewModel,
    fusedLocation: FusedLocationProviderClient
) {
    val navController = rememberNavController()

    // Collect state from ViewModels
    val userInfoUiState by profileViewModel.userInfo.collectAsState()

    // Show loading screen while user data is being fetched
    if (userInfoUiState.isLoading) {
        LoadingScreen()
        return
    }

    // Determine if onboarding is completed
    val isOnboardingDone = userInfoUiState.user?.onboardingCompleted == true

    Scaffold(
        bottomBar = {
            if (isOnboardingDone) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Navigation(
                navController,
                searchScreenViewModel,
                mapPointViewModel,
                fusedLocation,
                profileViewModel
            )
        }
    }
}