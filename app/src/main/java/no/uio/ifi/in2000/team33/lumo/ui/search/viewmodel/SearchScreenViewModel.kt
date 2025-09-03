package no.uio.ifi.in2000.team33.lumo.ui.search.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.team33.lumo.data.address.AddressRepository
import no.uio.ifi.in2000.team33.lumo.data.address.model.Adresser
import no.uio.ifi.in2000.team33.lumo.ui.search.MAP_STYLE_2D
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.ErrorType
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.MapPoint
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.SearchOperation
import javax.inject.Inject

@HiltViewModel
class SearchScreenViewModel @Inject constructor(
    private val addressRepository: AddressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchScreenUIState())
    val mapScreenUiState: StateFlow<SearchScreenUIState> = _uiState.asStateFlow()

    private val _mapCameraState = MutableStateFlow(DEFAULT_CAMERA_OPTIONS)
    val mapCameraState: StateFlow<CameraOptions> = _mapCameraState.asStateFlow()

    private val _searchOperation = MutableStateFlow<SearchOperation>(SearchOperation.Idle)
    val searchOperation: StateFlow<SearchOperation> = _searchOperation.asStateFlow()

    // Derived state flows for UI
    val isLoading: StateFlow<Boolean> = _searchOperation.map {
        it != SearchOperation.Idle
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val isSearchLoading: StateFlow<Boolean> = _searchOperation.map {
        it is SearchOperation.SearchingAddresses || it is SearchOperation.SearchingCoordinates
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val isLocationLoading: StateFlow<Boolean> = _searchOperation.map {
        it is SearchOperation.LoadingLocation
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    companion object {
        private const val DEFAULT_LAT = 59.94275
        private const val DEFAULT_LNG = 10.72
        private const val DEFAULT_ZOOM = 13.5
        private const val DEFAULT_BEARING = 25.0

        private val DEFAULT_CAMERA_OPTIONS = CameraOptions.Builder()
            .center(Point.fromLngLat(DEFAULT_LNG, DEFAULT_LAT))
            .zoom(DEFAULT_ZOOM)
            .bearing(DEFAULT_BEARING)
            .build()
    }

    // Text input methods
    fun updateSearchBarText(text: String) {
        _uiState.value = _uiState.value.copy(searchBarText = text)
    }

    fun clearSearchBarText() {
        _uiState.value = _uiState.value.copy(searchBarText = "")
    }

    // Map point selection
    fun updateSelectedMapPoint(point: MapPoint?) {
        _uiState.value = _uiState.value.copy(
            selectedMapPoint = point,
            showAddressBottomSheet = point != null
        )
    }

    // Modal visibility
    fun showInfoModal(show: Boolean) {
        _uiState.value = _uiState.value.copy(showInfoModal = show)
    }

    fun showTakflateSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showTakflateBottomSheet = show)
    }

    fun dismissAddressModal() {
        _uiState.value = _uiState.value.copy(
            showAddressBottomSheet = false,
            showTakflateBottomSheet = false
        )
    }

    // Permission dialog
    fun showLocationPermissionDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showLocationPermissionDialog = show)
    }

    // Camera state
    fun updateMapCameraState(cameraOptions: CameraOptions) {
        _mapCameraState.value = cameraOptions
    }

    // Permission checking
    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    // Location handling
    fun handleLocationButtonClick(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient,
        onLocationFound: (Double, Double) -> Unit
    ) {
        if (hasLocationPermission(context)) {
            getCurrentLocation(context, fusedLocationClient, onLocationFound)
        } else {
            showLocationPermissionDialog(true)
        }
    }

    fun handleLocationPermissionResult(
        permissions: Map<String, Boolean>,
        context: Context,
        fusedLocationClient: FusedLocationProviderClient,
        onLocationFound: (Double, Double) -> Unit
    ) {
        val hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (hasLocationPermission) {
            getCurrentLocation(context, fusedLocationClient, onLocationFound)
        }
    }

    // Address search methods
    suspend fun getAddressFromCoordinates(lat: Double, lon: Double): Adresser? {
        _searchOperation.value = SearchOperation.SearchingCoordinates
        return try {
            val address = withContext(Dispatchers.IO) {
                addressRepository.getAddressInformationFromPoint(lat, lon)
            }

            if (address == null) {
                setError(ErrorType.INVALID_ADDRESS)
                null
            } else {
                _uiState.value = _uiState.value.copy(
                    showError = false,
                    error = null
                )
                address
            }
        } catch (e: Exception) {
            Log.e("SearchScreenViewModel", "Error getting address from coordinates", e)
            setError(ErrorType.INVALID_ADDRESS)
            null
        } finally {
            _searchOperation.value = SearchOperation.Idle
        }
    }

    suspend fun getAddressuggestions(address: String): List<Adresser> {
        _searchOperation.value = SearchOperation.SearchingAddresses
        return try {
            val addressSuggestions = withContext(Dispatchers.IO) {
                addressRepository.getAllAddresses(address)
            }

            when {
                addressSuggestions.isNullOrEmpty() -> {
                    setError(ErrorType.INVALID_ADDRESS)
                    _uiState.value = _uiState.value.copy(
                        suggestedAddresses = emptyList(),
                        showSuggestions = false
                    )
                    emptyList()
                }

                addressSuggestions.size == 1 -> {
                    _uiState.value = _uiState.value.copy(
                        suggestedAddresses = addressSuggestions,
                        showSuggestions = false,
                        showError = false,
                        error = null
                    )
                    addressSuggestions
                }

                else -> {
                    _uiState.value = _uiState.value.copy(
                        suggestedAddresses = addressSuggestions,
                        showSuggestions = true,
                        showError = false,
                        error = null
                    )
                    addressSuggestions
                }
            }
        } catch (e: Exception) {
            Log.e("SearchScreenViewModel", "Error getting address suggestions", e)
            setError(ErrorType.INVALID_ADDRESS)
            _uiState.value = _uiState.value.copy(
                suggestedAddresses = emptyList(),
                showSuggestions = false
            )
            emptyList()
        } finally {
            _searchOperation.value = SearchOperation.Idle
        }
    }

    // Error handling
    fun clearAddressError() {
        _uiState.value = _uiState.value.copy(
            suggestedAddresses = emptyList(),
            showSuggestions = false,
            showError = false,
            error = null
        )
    }

    fun clearAddressSuggestions() {
        _uiState.value = _uiState.value.copy(
            suggestedAddresses = emptyList(),
            showSuggestions = false
        )
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient,
        onLocationFound: (Double, Double) -> Unit
    ) {
        if (!hasLocationPermission(context)) {
            setError(ErrorType.NO_LOCATION_PERMISSION)
            return
        }

        _searchOperation.value = SearchOperation.LoadingLocation

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnCompleteListener { task ->
            _searchOperation.value = SearchOperation.Idle

            if (task.isSuccessful && task.result != null) {
                onLocationFound(task.result.latitude, task.result.longitude)
            } else {
                setError(ErrorType.COULD_NOT_GET_LOCATION)
            }
        }
    }

    private fun setError(errorType: ErrorType) {
        _uiState.value = _uiState.value.copy(
            showError = true,
            error = errorType
        )
    }

    val mapStyle2D: StateFlow<Boolean> = _uiState.map { it.mapStyle2D }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun toggleMapStyle() {
        _uiState.value = _uiState.value.copy(mapStyle2D = !_uiState.value.mapStyle2D)
        // Update the global MAP_STYLE_2D if needed
        MAP_STYLE_2D = _uiState.value.mapStyle2D
    }
}