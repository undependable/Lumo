package no.uio.ifi.in2000.team33.lumo.ui.utility

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.bottombar.AnimatedBottomBar
import com.example.bottombar.components.BottomBarItem
import com.example.bottombar.model.IndicatorDirection
import com.example.bottombar.model.IndicatorStyle
import com.example.bottombar.model.VisibleItem
import com.google.android.gms.location.FusedLocationProviderClient
import no.uio.ifi.in2000.team33.lumo.ui.home.screens.HomeScreen
import no.uio.ifi.in2000.team33.lumo.ui.home.screens.StatisticsScreen
import no.uio.ifi.in2000.team33.lumo.ui.mappoint.MapPointViewModel
import no.uio.ifi.in2000.team33.lumo.ui.network.NetworkViewModel
import no.uio.ifi.in2000.team33.lumo.ui.onboarding.OnboardingScreen
import no.uio.ifi.in2000.team33.lumo.ui.onboarding.PowerProductionOnboarding
import no.uio.ifi.in2000.team33.lumo.ui.onboarding.ProfileCreationScreen
import no.uio.ifi.in2000.team33.lumo.ui.profile.ProfileViewModel
import no.uio.ifi.in2000.team33.lumo.ui.profile.screens.PowerConsumptionScreen
import no.uio.ifi.in2000.team33.lumo.ui.profile.screens.ProfileScreen
import no.uio.ifi.in2000.team33.lumo.ui.profile.screens.SettingsScreen
import no.uio.ifi.in2000.team33.lumo.ui.profile.screens.UserInfoScreen
import no.uio.ifi.in2000.team33.lumo.ui.roof.RegisteredRoofScreen
import no.uio.ifi.in2000.team33.lumo.ui.saved.SavedScreen
import no.uio.ifi.in2000.team33.lumo.ui.search.MapScreen
import no.uio.ifi.in2000.team33.lumo.ui.search.viewmodel.SearchScreenViewModel


// Main navigation graph with onboarding and main app flows
// Start destination depends on user auth state -> if they have a profile or not.
@Composable
fun Navigation(
    navController: NavHostController,
    searchScreenViewModel: SearchScreenViewModel,
    mapPointViewModel: MapPointViewModel,
    fusedLocationProviderClient: FusedLocationProviderClient,
    profileViewModel: ProfileViewModel
) {
    // Create a single shared instance of HomeScreenViewModel
    val networkViewModel: NetworkViewModel = hiltViewModel()

    // Check if a user exists and if onboarding is completed
    val userInfoUiState by profileViewModel.userInfo.collectAsState()
    val userInfo = userInfoUiState.user

    // Determine start destination based on user existence and onboarding status
    val startDestination = when {
        userInfo == null -> "onboarding"  // No user exists
        userInfo.onboardingCompleted -> "homescreen"  // User exists and completed onboarding
        else -> "estimateproduction"  // User exists but hasn't completed onboarding
    }

    // Onboarding screens: Profile creation and power estimation
    // Clears back stack after completion
    NavHost(navController, startDestination = startDestination) {
        composable("onboarding") {
            OnboardingScreen(onFinish = {
                navController.navigate("profilecreation") {
                    popUpTo("onboarding") { inclusive = true }
                }
            })
        }

        composable("profilecreation") {
            ProfileCreationScreen(
                profileViewModel = profileViewModel,
                onComplete = {
                    navController.navigate("estimateproduction")
                }
            )
        }

        composable("estimateproduction") {
            PowerProductionOnboarding(
                profileViewModel = profileViewModel,
                onComplete = {
                    // Mark onboarding as completed when user finishes power consumption setup
                    userInfo?.let {
                        profileViewModel.updateUserInfo(it.copy(onboardingCompleted = true))
                    }
                    navController.navigate("homescreen") {
                        popUpTo(0) // Clear entire back stack
                    }
                }
            )
        }

        // Main app screens with ViewModel integration
        // Shared ViewModels for data consistency across screens
        composable("homescreen") {
            HomeScreen(
                navController = navController,
                networkViewModel = networkViewModel,
                mapPointViewModel = mapPointViewModel,
                profileViewModel = profileViewModel
            )
        }

        composable("searchscreen") {
            MapScreen(
                navController = navController,
                searchScreenViewModel = searchScreenViewModel,
                networkViewModel = networkViewModel,
                mapPointViewModel = mapPointViewModel,
                fusedLocationProviderClient = fusedLocationProviderClient
            )
        }
        composable("savedscreen") {
            SavedScreen(
                navController = navController,
                networkViewModel = networkViewModel,
                searchScreenViewModel = searchScreenViewModel,
                mapPointViewModel = mapPointViewModel
            )
        }
        composable("profilescreen") {
            ProfileScreen(
                navController = navController,
                profileViewModel = profileViewModel
            )
        }
        composable("userinfoscreen") {
            UserInfoScreen(
                navController = navController,
                profileViewModel = profileViewModel
            )
        }
        composable("statisticsscreen") {
            StatisticsScreen(
                profileViewModel = profileViewModel,
                navController = navController,
                networkViewModel = networkViewModel,
                mapPointViewModel = mapPointViewModel
            )
        }
        composable("powerconsumptionscreen") {
            PowerConsumptionScreen(
                navController = navController,
                profileViewModel = profileViewModel
            )
        }

        composable("settingsscreen") {
            SettingsScreen(navController = navController)
        }

        composable("roofscreen") {
            RegisteredRoofScreen(
                navController = navController,
                networkViewModel = networkViewModel,
                mapPointViewModel = mapPointViewModel
            )
        }
    }
}

// Bottom navigation bar with 4 main tabs
// Handles route mapping for nested navigation
// Visual indicators for selected tab
@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val effectiveRoute = MainNavigation.subRouteMapping[currentRoute] ?: currentRoute
    val selectedIndex = remember(effectiveRoute) {
        MainNavigation.items.indexOfFirst { it.route == effectiveRoute }.takeIf { it >= 0 } ?: 0
    }

    // Animation config for smooth tab transitions
    // Handles tab selection and navigation
    AnimatedBottomBar(
        animationSpec = spring(
            dampingRatio = 2f,
            stiffness = Spring.StiffnessLow
        ),
        selectedItem = selectedIndex,
        itemSize = MainNavigation.items.size,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        indicatorStyle = IndicatorStyle.LINE,
        indicatorColor = MaterialTheme.colorScheme.tertiaryContainer,
        indicatorHeight = 5.dp,
        indicatorDirection = IndicatorDirection.BOTTOM
    ) {
        MainNavigation.items.forEachIndexed { index, item ->
            // State tracking for current route
            // Smart back stack management
            // Prevents duplicate screens
            BottomBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination to avoid building up a large stack
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when navigating back to a previously selected item
                            restoreState = true
                        }
                    }
                },
                imageVector = item.icon,
                visibleItem = VisibleItem.BOTH,
                label = item.title,
                containerColor = Color.Transparent,
            )
        }
    }
}


// Sealed class defines navigation structure
// Maps sub-routes to main tabs for proper highlighting
// Icons and labels for each navigation item
sealed class MainNavigation(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Hjem : MainNavigation("homescreen", "Hjem", Icons.Outlined.Home)
    object Soek : MainNavigation("searchscreen", "Søk", Icons.Outlined.Search)
    object Lagret : MainNavigation("savedscreen", "Lagret", Icons.Outlined.BookmarkBorder)
    object Profil : MainNavigation("profilescreen", "Profil", Icons.Outlined.AccountCircle)

    companion object {
        val items = listOf(Hjem, Soek, Lagret, Profil)
        val subRouteMapping = mapOf(
            "statisticsscreen" to "homescreen",
            "roofscreen" to "homescreen",
            "userinfoscreen" to "profilescreen",
            "powerconsumptionscreen" to "profilescreen",
            "settingsscreen" to "profilescreen"
        )
    }
}