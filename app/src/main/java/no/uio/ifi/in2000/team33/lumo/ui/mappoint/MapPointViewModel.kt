package no.uio.ifi.in2000.team33.lumo.ui.mappoint

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.team33.lumo.data.address.model.Adresser
import no.uio.ifi.in2000.team33.lumo.data.database.MapPointRepository
import no.uio.ifi.in2000.team33.lumo.data.electricity.AddressToRegion.getRegionForAddress
import no.uio.ifi.in2000.team33.lumo.data.electricity.ElectricityPriceDataSource
import no.uio.ifi.in2000.team33.lumo.data.electricity.ElectricityPriceRepository
import no.uio.ifi.in2000.team33.lumo.data.electricity.calculateMonthlySavings
import no.uio.ifi.in2000.team33.lumo.data.frost.FrostRepository
import no.uio.ifi.in2000.team33.lumo.data.pvgis.PvgisRepository
import no.uio.ifi.in2000.team33.lumo.ui.home.ViewModel.RoofData
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.LoadingState
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.MapPoint
import no.uio.ifi.in2000.team33.lumo.ui.utility.functions.directionToAspect
import javax.inject.Inject

@HiltViewModel
class MapPointViewModel @Inject constructor(
    private val mapPointRepository: MapPointRepository,
    private val frostRepository: FrostRepository,
    private val pvgisRepository: PvgisRepository,
    private val electricityPriceRepository: ElectricityPriceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapPointUIState())
    val mapPointUIState: StateFlow<MapPointUIState> = _uiState.asStateFlow()

    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    // Selected takflate state for annual estimates
    private val _selectedTakflateForEstimate = MutableStateFlow(0)
    val selectedTakflateForEstimate: StateFlow<Int> = _selectedTakflateForEstimate.asStateFlow()

    // Expose computed properties from UI state
    val isLoading: StateFlow<Boolean> = combine(_loadingState, _uiState) { loadingState, _ ->
        loadingState != LoadingState.Idle
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val currentMapPoint: StateFlow<MapPoint?> = _uiState
        .map { it.currentMapPoint }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val favoriteMapPoints: StateFlow<List<MapPoint>> = _uiState
        .map { it.mapPoints.filter { point -> point.isFavorite } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        loadPoints()
    }

    fun loadPoints() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.LoadingMapPoints
            try {
                val mapPoints = mapPointRepository.getMapPointsWithTakflateData().first()
                val favoritePoints = mapPoints.filter { it.isFavorite }

                // Determine current point - prefer the first one with takflater
                val currentPoint = favoritePoints.firstOrNull { it.registeredRoofs.isNotEmpty() }
                    ?: favoritePoints.firstOrNull()

                _uiState.value = _uiState.value.copy(
                    mapPoints = favoritePoints.toMutableList(),
                    currentMapPoint = currentPoint
                )

                // Load additional data for current point if available
                currentPoint?.let { point ->
                    if (point.registeredRoofs.isNotEmpty()) {
                        loadSupplementaryData(point)
                    }
                }
            } catch (e: Exception) {
                Log.e("MapPointViewModel", "Error loading points", e)
            } finally {
                _loadingState.value = LoadingState.Idle
            }
        }
    }

    fun selectPoint(newPoint: MapPoint) {
        val currentState = _uiState.value
        val updatedPoints = currentState.mapPoints.toMutableList()

        // Move selected point to first position
        val existingIndex = updatedPoints.indexOfFirst { it.name == newPoint.name }
        if (existingIndex != -1 && existingIndex != 0) {
            updatedPoints.removeAt(existingIndex)
            updatedPoints.add(0, newPoint)
        }

        _uiState.value = currentState.copy(
            mapPoints = updatedPoints,
            currentMapPoint = newPoint,
            selectedTakflateIndex = 0 // Reset to first takflate
        )

        // Always load estimates when switching points, even if data exists
        // This ensures the graph updates based on the first takflate of the new address
        if (newPoint.registeredRoofs.isNotEmpty()) {
            viewModelScope.launch {
                selectTakflateForAnnualEstimate(0) // This will reload estimates for first takflate
            }
        } else {
            viewModelScope.launch {
                loadSupplementaryData(newPoint)
            }
        }
    }

    fun setCurrentPoint(point: MapPoint) {
        _uiState.value = _uiState.value.copy(currentMapPoint = point)
    }

    fun loadCurrentTemperature(mapPoint: MapPoint) {
        if (mapPoint.temperature != 10000.0) return // Already loaded

        _loadingState.value = LoadingState.LoadingTemperature
        viewModelScope.launch {
            try {
                val currentTemp = withContext(Dispatchers.IO) {
                    frostRepository.getCurrentTemperature(mapPoint.closestStationID)
                }

                updateMapPoint(mapPoint.name) { it.copy(temperature = currentTemp) }
            } catch (e: Exception) {
                Log.e("MapPointViewModel", "Error loading temperature", e)
            } finally {
                _loadingState.value = LoadingState.Idle
            }
        }
    }

    fun beregnProduksjonMedVinkel(mapPoint: MapPoint, vinkel: Int, area: Double, retning: Int) {
        _loadingState.value = LoadingState.SavingTakflate
        viewModelScope.launch {
            try {
                val roofData = RoofData(
                    navn = "Takflate ${mapPoint.registeredRoofs.size + 1}",
                    area = area.toString(),
                    vinkel = vinkel.toString(),
                    retning = when (retning) {
                        0 -> "soer"
                        90 -> "vest"
                        -90 -> "ost"
                        180 -> "nord"
                        else -> "soer"
                    }
                )

                // Calculate production for this takflate
                val estimates = coroutineScope {
                    val annualDeferred = async {
                        pvgisRepository.calculateAdjustedAnnualProduction(
                            lat = mapPoint.lat,
                            lon = mapPoint.lon,
                            area = area,
                            efficiency = 0.20,
                            angle = vinkel,
                            aspect = retning
                        )
                    }

                    val monthlyDeferred = async {
                        pvgisRepository.calculateAdjustedMonthlyProduction(
                            lat = mapPoint.lat,
                            lon = mapPoint.lon,
                            area = area,
                            efficiency = 0.20,
                            angle = vinkel,
                            aspect = retning
                        )
                    }

                    Pair(annualDeferred.await(), monthlyDeferred.await())
                }

                val (produksjon, monthlyData) = estimates

                // Create updated point with new takflate
                val updatedTakflater = mapPoint.registeredRoofs.toMutableList().apply {
                    add(roofData)
                }

                val updatedPoint = mapPoint.copy(
                    registeredRoofs = updatedTakflater,
                    annualEstimate = produksjon ?: 0.0,
                    isFavorite = true  // Ensure it's marked as favorite
                )

                // Save to database FIRST
                withContext(Dispatchers.IO) {
                    mapPointRepository.saveMapPointWithTakflate(updatedPoint)
                }

                // Update UI state - ensure point is at the front of favorites
                val currentState = _uiState.value
                val updatedMapPoints = currentState.mapPoints.toMutableList()

                // Remove any existing instance of this point
                updatedMapPoints.removeAll {
                    it.name == updatedPoint.name && it.lat == updatedPoint.lat && it.lon == updatedPoint.lon
                }

                // Add the updated point at the front
                updatedMapPoints.add(0, updatedPoint)

                // Update state with proper current point selection
                _uiState.value = currentState.copy(
                    mapPoints = updatedMapPoints,
                    currentMapPoint = updatedPoint,  // This is crucial
                    selectedTakflateIndex = updatedTakflater.size - 1,
                    monthlyProduction = monthlyData,
                    chartDataString = monthlyData?.joinToString(",") { it.second.toString() } ?: ""
                )

                // Load any additional required data
                loadSupplementaryData(updatedPoint)

            } catch (e: Exception) {
                Log.e("MapPointViewModel", "Error saving takflate", e)
            } finally {
                _loadingState.value = LoadingState.Idle
            }
        }
    }

    fun selectTakflateForAnnualEstimate(takflateIndex: Int) {
        val currentPoint = _uiState.value.currentMapPoint ?: return
        if (takflateIndex !in 0 until currentPoint.registeredRoofs.size) return

        _uiState.value = _uiState.value.copy(selectedTakflateIndex = takflateIndex)

        _loadingState.value = LoadingState.LoadingEstimates
        viewModelScope.launch {
            try {
                val selectedTakflate = currentPoint.registeredRoofs[takflateIndex]

                // Calculate production for selected takflate
                val estimates = coroutineScope {
                    val annualDeferred = async {
                        pvgisRepository.calculateAdjustedAnnualProduction(
                            lat = currentPoint.lat,
                            lon = currentPoint.lon,
                            area = selectedTakflate.area.toDoubleOrNull() ?: 0.0,
                            efficiency = 0.20,
                            angle = selectedTakflate.vinkel.toIntOrNull() ?: 0,
                            aspect = directionToAspect(selectedTakflate.retning)
                        )
                    }

                    val monthlyDeferred = async {
                        pvgisRepository.calculateAdjustedMonthlyProduction(
                            lat = currentPoint.lat,
                            lon = currentPoint.lon,
                            area = selectedTakflate.area.toDoubleOrNull() ?: 0.0,
                            efficiency = 0.20,
                            angle = selectedTakflate.vinkel.toIntOrNull() ?: 0,
                            aspect = directionToAspect(selectedTakflate.retning)
                        )
                    }

                    Pair(annualDeferred.await(), monthlyDeferred.await())
                }

                val (annualEstimate, monthlyData) = estimates

                // Update the point with new estimates
                val updatedPoint = currentPoint.copy(annualEstimate = annualEstimate ?: 0.0)
                updateMapPointInList(updatedPoint)

                _uiState.value = _uiState.value.copy(
                    currentMapPoint = updatedPoint,
                    monthlyProduction = monthlyData,
                    chartDataString = monthlyData?.joinToString(",") { it.second.toString() } ?: ""
                )

                // Save to database
                withContext(Dispatchers.IO) {
                    mapPointRepository.saveMapPointWithTakflate(updatedPoint)
                }

            } catch (e: Exception) {
                Log.e("MapPointViewModel", "Error updating takflate estimate", e)
            } finally {
                _loadingState.value = LoadingState.Idle
            }
        }
    }

    suspend fun addMapPoint(address: Adresser): MapPoint {
        // Check if point already exists
        val existingPoint = _uiState.value.mapPoints.find { it.name == address.adressetekst }
        if (existingPoint != null) return existingPoint

        _loadingState.value = LoadingState.LoadingMapPoints
        return try {
            coroutineScope {
                val stationIDDeferred = async(Dispatchers.IO) {
                    frostRepository.getStationId(address.representasjonspunkt)
                }

                val stationID = stationIDDeferred.await()
                val region = getRegionForAddress(address.postnummer)

                val mapPoint = MapPoint(
                    name = address.adressetekst,
                    lat = address.representasjonspunkt.lat,
                    lon = address.representasjonspunkt.lon,
                    closestStationID = stationID,
                    areaCode = address.postnummer,
                    areaPlace = address.poststed,
                    isHouse = address.bruksenhetsnummer.isEmpty(),
                    region = region ?: ""
                )

                // Add to UI state
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    mapPoints = (currentState.mapPoints + mapPoint).toMutableList()
                )

                // Load additional data
                loadElectricityPrice(mapPoint)

                mapPoint
            }
        } catch (e: Exception) {
            Log.e("MapPointViewModel", "Error adding map point", e)
            throw e
        } finally {
            _loadingState.value = LoadingState.Idle
        }
    }

    fun toggleFavorite(mapPoint: MapPoint) {
        _loadingState.value = LoadingState.LoadingMapPoints
        viewModelScope.launch {
            try {
                val updatedPoint = mapPoint.copy(isFavorite = !mapPoint.isFavorite)

                if (!updatedPoint.isFavorite) {
                    // Remove from database
                    withContext(Dispatchers.IO) {
                        mapPointRepository.deleteMapPoint(updatedPoint)
                    }
                    // Remove from UI state immediately
                    removeMapPointFromList(mapPoint)

                    // Update current point if it was the deleted one
                    val currentState = _uiState.value
                    if (currentState.currentMapPoint?.name == updatedPoint.name) {
                        val remainingPoints = currentState.mapPoints.filter {
                            it.name != updatedPoint.name && it.isFavorite
                        }
                        _uiState.value = currentState.copy(
                            currentMapPoint = remainingPoints.firstOrNull()
                        )
                    }
                } else {
                    // Update in database and UI
                    withContext(Dispatchers.IO) {
                        mapPointRepository.saveMapPointWithTakflate(updatedPoint)
                    }
                    updateMapPointInList(updatedPoint)
                }

            } catch (e: Exception) {
                Log.e("MapPointViewModel", "Error toggling favorite", e)
            } finally {
                _loadingState.value = LoadingState.Idle
            }
        }
    }

    fun ensureMonthlyDataLoaded() {
        val currentPoint = _uiState.value.currentMapPoint ?: return
        if (_uiState.value.monthlyProduction.isNullOrEmpty()) {
            viewModelScope.launch {
                loadMonthlyProductionForPoint(currentPoint)
            }
        }
    }

    fun calculateMonthlySavingsFromJson(context: Context) {
        val currentState = _uiState.value
        val point = currentState.currentMapPoint ?: return
        val monthlyProduction = currentState.monthlyProduction ?: return

        viewModelScope.launch {
            try {
                val prices =
                    ElectricityPriceDataSource().getMonthlyPriceMapForRegion(context, point.region)
                val monthlySavings = calculateMonthlySavings(
                    monthlyProduction,
                    prices,
                    consumptionRate = 0.6,
                    sellPrice = ElectricityPriceRepository.DEFAULT_SELLING_PRICE
                )

                _uiState.value = currentState.copy(monthlySavings = monthlySavings)
            } catch (e: Exception) {
                Log.e("MapPointViewModel", "Error calculating savings", e)
            }
        }
    }

    fun calculateAnnualSavingsFromMonths(context: Context, consumption: Double = 5000.0): Double? {
        val currentState = _uiState.value
        val point = currentState.currentMapPoint ?: return null
        val monthlyProduction = currentState.monthlyProduction ?: return null

        val electricityPrices =
            ElectricityPriceDataSource().getMonthlyPriceMapForRegion(context, point.region)
        val annualProduction = point.annualEstimate
        if (annualProduction <= 0.0) return null

        val consumptionRate = if (annualProduction > 0) minOf(
            consumption,
            annualProduction
        ) / annualProduction else 0.0

        val monthlySavings = calculateMonthlySavings(
            monthlyProduction = monthlyProduction,
            monthlyPrices = electricityPrices,
            consumptionRate = consumptionRate,
            sellPrice = ElectricityPriceRepository.DEFAULT_SELLING_PRICE
        )

        return monthlySavings.sumOf { it.second }
    }

    // Private helper methods
    private suspend fun loadSupplementaryData(point: MapPoint) {
        _loadingState.value = LoadingState.LoadingEstimates
        try {
            coroutineScope {
                val jobs = mutableListOf<suspend () -> Unit>()

                // Load temperature if needed
                if (point.temperature == 10000.0) {
                    jobs.add {
                        val temp = frostRepository.getCurrentTemperature(point.closestStationID)
                        updateMapPoint(point.name) { it.copy(temperature = temp) }
                    }
                }

                // Load price if needed
                if (point.priceThisHour.isEmpty()) {
                    jobs.add {
                        val price = electricityPriceRepository.fetchHourPrice(point.region)
                        updateMapPoint(point.name) { it.copy(priceThisHour = price) }
                    }
                }

                // Load estimates if needed
                if (point.annualEstimate == 0.0 && point.registeredRoofs.isNotEmpty()) {
                    jobs.add {
                        val estimates = loadEstimatesForPoint(point)
                        updateMapPoint(point.name) {
                            it.copy(annualEstimate = estimates.first)
                        }
                        _uiState.value = _uiState.value.copy(
                            monthlyProduction = estimates.second,
                            chartDataString = estimates.second?.joinToString(",") { it.second.toString() }
                                ?: ""
                        )
                    }
                }

                // Execute all jobs concurrently
                jobs.map { async(Dispatchers.IO) { it() } }.awaitAll()
            }
        } catch (e: Exception) {
            Log.e("MapPointViewModel", "Error loading supplementary data", e)
        } finally {
            _loadingState.value = LoadingState.Idle
        }
    }

    private suspend fun loadEstimatesForPoint(point: MapPoint): Pair<Double, List<Pair<Int, Double>>?> {
        val selectedIndex =
            _uiState.value.selectedTakflateIndex.coerceIn(0, point.registeredRoofs.size - 1)
        val takflate = point.registeredRoofs.getOrNull(selectedIndex) ?: return Pair(0.0, null)

        val area = takflate.area.toDoubleOrNull() ?: 0.0
        val angle = takflate.vinkel.toIntOrNull() ?: 0
        val aspect = directionToAspect(takflate.retning)

        return coroutineScope {
            val annualDeferred = async {
                pvgisRepository.calculateAdjustedAnnualProduction(
                    point.lat, point.lon, area, 0.20, angle, aspect
                ) ?: 0.0
            }

            val monthlyDeferred = async {
                pvgisRepository.calculateAdjustedMonthlyProduction(
                    point.lat, point.lon, area, 0.20, angle, aspect
                )
            }

            Pair(annualDeferred.await(), monthlyDeferred.await())
        }
    }

    private suspend fun loadMonthlyProductionForPoint(point: MapPoint) {
        if (point.registeredRoofs.isEmpty()) return

        _loadingState.value = LoadingState.LoadingMonthlyData
        try {
            val selectedIndex =
                _uiState.value.selectedTakflateIndex.coerceIn(0, point.registeredRoofs.size - 1)
            val takflate = point.registeredRoofs[selectedIndex]

            val monthlyData = withContext(Dispatchers.IO) {
                pvgisRepository.calculateAdjustedMonthlyProduction(
                    lat = point.lat,
                    lon = point.lon,
                    area = takflate.area.toDoubleOrNull() ?: 0.0,
                    efficiency = 0.20,
                    angle = takflate.vinkel.toIntOrNull() ?: 0,
                    aspect = directionToAspect(takflate.retning)
                )
            }

            _uiState.value = _uiState.value.copy(
                monthlyProduction = monthlyData,
                chartDataString = monthlyData?.joinToString(",") { it.second.toString() } ?: ""
            )
        } catch (e: Exception) {
            Log.e("MapPointViewModel", "Error loading monthly production", e)
        } finally {
            _loadingState.value = LoadingState.Idle
        }
    }

    private suspend fun loadElectricityPrice(mapPoint: MapPoint) {
        try {
            val price = withContext(Dispatchers.IO) {
                electricityPriceRepository.fetchHourPrice(mapPoint.region)
            }
            updateMapPoint(mapPoint.name) { it.copy(priceThisHour = price) }
        } catch (e: Exception) {
            Log.e("MapPointViewModel", "Error loading electricity price", e)
        }
    }

    private fun updateMapPoint(pointName: String, update: (MapPoint) -> MapPoint) {
        val currentState = _uiState.value
        val updatedMapPoints = currentState.mapPoints.map { point ->
            if (point.name == pointName) update(point) else point
        }.toMutableList()

        val updatedCurrentPoint = if (currentState.currentMapPoint?.name == pointName) {
            currentState.currentMapPoint.let(update)
        } else {
            currentState.currentMapPoint
        }

        _uiState.value = currentState.copy(
            mapPoints = updatedMapPoints,
            currentMapPoint = updatedCurrentPoint
        )
    }

    private fun updateMapPointInList(updatedPoint: MapPoint) {
        val currentState = _uiState.value
        val updatedMapPoints = currentState.mapPoints.map { point ->
            if (point.name == updatedPoint.name &&
                point.lat == updatedPoint.lat &&
                point.lon == updatedPoint.lon
            ) {
                updatedPoint
            } else {
                point
            }
        }.toMutableList()

        // Add point if it's not in the list and is favorited
        if (updatedPoint.isFavorite && updatedMapPoints.none {
                it.name == updatedPoint.name && it.lat == updatedPoint.lat && it.lon == updatedPoint.lon
            }) {
            updatedMapPoints.add(updatedPoint)
        }

        _uiState.value = currentState.copy(mapPoints = updatedMapPoints)
    }

    private fun removeMapPointFromList(pointToRemove: MapPoint) {
        val currentState = _uiState.value
        val updatedMapPoints = currentState.mapPoints.filter { point ->
            !(point.name == pointToRemove.name && point.lat == pointToRemove.lat && point.lon == pointToRemove.lon)
        }.toMutableList()

        val updatedCurrentPoint = if (currentState.currentMapPoint?.name == pointToRemove.name) {
            updatedMapPoints.firstOrNull()
        } else {
            currentState.currentMapPoint
        }

        _uiState.value = currentState.copy(
            mapPoints = updatedMapPoints,
            currentMapPoint = updatedCurrentPoint
        )
    }

    private fun MapPoint.needsDataRefresh(): Boolean {
        return temperature == 10000.0 || priceThisHour.isEmpty() ||
                (registeredRoofs.isNotEmpty() && annualEstimate == 0.0)
    }

    fun removeUnsavedMapPoints() {
        val currentState = _uiState.value
        val pointsToKeep = currentState.mapPoints.filter {
            it.isFavorite || it.registeredRoofs.isNotEmpty()
        }.toMutableList()
        _uiState.value = currentState.copy(mapPoints = pointsToKeep)
    }

    fun ensurePointInitialized(point: MapPoint) {
        val exists = _uiState.value.mapPoints.any {
            it.name == point.name && it.lat == point.lat && it.lon == point.lon
        }
        if (!exists) {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                mapPoints = (currentState.mapPoints + point).toMutableList()
            )
        }
    }

    fun loadEstimates(point: MapPoint) {
        if (point.registeredRoofs.isNotEmpty()) {
            viewModelScope.launch {
                loadSupplementaryData(point)
            }
        }
    }
}