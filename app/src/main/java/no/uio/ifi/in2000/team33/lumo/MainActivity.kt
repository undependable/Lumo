package no.uio.ifi.in2000.team33.lumo

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.common.MapboxOptions
import dagger.hilt.android.AndroidEntryPoint
import no.uio.ifi.in2000.team33.lumo.ui.home.components.Display
import no.uio.ifi.in2000.team33.lumo.ui.mappoint.MapPointViewModel
import no.uio.ifi.in2000.team33.lumo.ui.profile.ProfileViewModel
import no.uio.ifi.in2000.team33.lumo.ui.search.viewmodel.SearchScreenViewModel
import no.uio.ifi.in2000.team33.lumo.ui.utility.theme.SolcelleTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val searchScreenViewModel: SearchScreenViewModel by viewModels()
    private val mapPointViewModel: MapPointViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Permission request launcher for startup - optional for better UX
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        Log.d("MainActivity", "Location permission granted: $hasLocationPermission")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set fullscreen
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN)

        // Initialize Mapbox 
        MapboxOptions.accessToken = ...

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        requestLocationPermissions()

        // Setting the content of the app
        setContent {
            SolcelleTheme {
                Display(
                    searchScreenViewModel = searchScreenViewModel,
                    profileViewModel = profileViewModel,
                    mapPointViewModel = mapPointViewModel,
                    fusedLocation = fusedLocationClient
                )
            }
        }
    }

    private fun requestLocationPermissions() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}