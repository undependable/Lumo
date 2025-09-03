package no.uio.ifi.in2000.team33.lumo.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team33.lumo.data.database.PowerConsumptionEntity
import no.uio.ifi.in2000.team33.lumo.data.database.UserRepository
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.ConsumptionUIState
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.PowerConsumptionUiState
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.UserInfo
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.UserInfoUiState
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val userRepository: UserRepository) :
    ViewModel() {

    // User Info state
    private val _userInfo = MutableStateFlow(UserInfoUiState(isLoading = true))
    val userInfo: StateFlow<UserInfoUiState> = _userInfo.asStateFlow()

    // Power Consumption state
    private val _powerConsumption = MutableStateFlow(PowerConsumptionUiState())
    val powerConsumption: StateFlow<PowerConsumptionUiState> = _powerConsumption.asStateFlow()

    // Consumption UI state
    private val _consumptionUIState = MutableStateFlow(ConsumptionUIState())
    val consumptionUIState: StateFlow<ConsumptionUIState> = _consumptionUIState.asStateFlow()

    // Derived states
    val hasValidUser: StateFlow<Boolean> = _userInfo.map {
        it.user?.firstName?.isNotEmpty() == true && it.user.lastName.isNotEmpty()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        loadUserInfo()
        loadPowerConsumption()

        // Initialize consumption UI state with loaded data
        combine(_powerConsumption, _consumptionUIState) { consumption, uiState ->
            if (consumption.consumption.yearlyConsumption > 0 && uiState.yearlyInputText.isEmpty()) {
                updateConsumptionUIFromEntity(consumption.consumption)
            }
        }.launchIn(viewModelScope)
    }

    // User Info methods
    private fun loadUserInfo() {
        _userInfo.update { it.copy(isLoading = true) }
        userRepository.getUserInfo()
            .flowOn(Dispatchers.IO)
            .onEach { userInfo ->
                _userInfo.update {
                    it.copy(
                        user = userInfo,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateUserInfo(newUserInfo: UserInfo) {
        viewModelScope.launch {
            userRepository.updateUserInfo(newUserInfo)
        }
    }

    // Power Consumption methods
    private fun loadPowerConsumption() {
        viewModelScope.launch {
            userRepository.getPowerConsumption().collect { consumption ->
                _powerConsumption.update {
                    it.copy(
                        consumption = consumption ?: PowerConsumptionEntity(),
                        isLoading = false
                    )
                }
            }
        }
    }

//    // Consumption UI State methods
//    fun updateSelectedView(view: Int) {
//        _consumptionUIState.update { it.copy(selectedView = view) }
//    }

//    fun updateYearlyInput(text: String) {
//        val filtered = text.filter { it.isDigit() }
//        _consumptionUIState.update { it.copy(yearlyInputText = filtered) }
//    }

//    fun updateMonthlyInput(month: String, text: String) {
//        val filtered = text.filter { it.isDigit() }
//        _consumptionUIState.update {
//            it.copy(monthlyInputTexts = it.monthlyInputTexts.toMutableMap().apply {
//                this[month] = filtered
//            })
//        }
//    }

//    fun showError(show: Boolean) {
//        _consumptionUIState.update { it.copy(showError = show) }
//    }

    fun showMinimumError(show: Boolean) {
        _consumptionUIState.update { it.copy(showMinimumError = show) }
    }

    private fun updateConsumptionUIFromEntity(consumption: PowerConsumptionEntity) {
        _consumptionUIState.update {
            it.copy(
                yearlyInputText = if (consumption.yearlyConsumption > 0)
                    consumption.yearlyConsumption.toString() else "",
                monthlyInputTexts = mapOf(
                    "january" to if (consumption.januaryConsumption > 0) consumption.januaryConsumption.toString() else "",
                    "february" to if (consumption.februaryConsumption > 0) consumption.februaryConsumption.toString() else "",
                    "march" to if (consumption.marchConsumption > 0) consumption.marchConsumption.toString() else "",
                    "april" to if (consumption.aprilConsumption > 0) consumption.aprilConsumption.toString() else "",
                    "may" to if (consumption.mayConsumption > 0) consumption.mayConsumption.toString() else "",
                    "june" to if (consumption.juneConsumption > 0) consumption.juneConsumption.toString() else "",
                    "july" to if (consumption.julyConsumption > 0) consumption.julyConsumption.toString() else "",
                    "august" to if (consumption.augustConsumption > 0) consumption.augustConsumption.toString() else "",
                    "september" to if (consumption.septemberConsumption > 0) consumption.septemberConsumption.toString() else "",
                    "october" to if (consumption.octoberConsumption > 0) consumption.octoberConsumption.toString() else "",
                    "november" to if (consumption.novemberConsumption > 0) consumption.novemberConsumption.toString() else "",
                    "december" to if (consumption.decemberConsumption > 0) consumption.decemberConsumption.toString() else ""
                )
            )
        }
    }

//    // Save methods
//    fun saveYearlyConsumption() {
//        val uiState = _consumptionUIState.value
//        val yearlyValue = uiState.yearlyInputText.toIntOrNull() ?: 0
//
//        if (yearlyValue < 20) {
//            showMinimumError(true)
//            return
//        }
//
//        updateYearlyConsumption(yearlyValue)
//    }

//    fun saveMonthlyConsumption() {
//        val uiState = _consumptionUIState.value
//        val monthlyValues = uiState.monthlyInputTexts
//
//        val jan = monthlyValues["january"]?.toIntOrNull() ?: 0
//        val feb = monthlyValues["february"]?.toIntOrNull() ?: 0
//        val mar = monthlyValues["march"]?.toIntOrNull() ?: 0
//        val apr = monthlyValues["april"]?.toIntOrNull() ?: 0
//        val may = monthlyValues["may"]?.toIntOrNull() ?: 0
//        val jun = monthlyValues["june"]?.toIntOrNull() ?: 0
//        val jul = monthlyValues["july"]?.toIntOrNull() ?: 0
//        val aug = monthlyValues["august"]?.toIntOrNull() ?: 0
//        val sep = monthlyValues["september"]?.toIntOrNull() ?: 0
//        val oct = monthlyValues["october"]?.toIntOrNull() ?: 0
//        val nov = monthlyValues["november"]?.toIntOrNull() ?: 0
//        val dec = monthlyValues["december"]?.toIntOrNull() ?: 0
//
//        val total = jan + feb + mar + apr + may + jun + jul + aug + sep + oct + nov + dec
//
//        if (total < 20) {
//            showMinimumError(true)
//            return
//        }
//
//        updateMonthlyConsumption(
//            january = jan, february = feb, march = mar,
//            april = apr, may = may, june = jun,
//            july = jul, august = aug, september = sep,
//            october = oct, november = nov, december = dec
//        )
//    }

    fun updateMonthlyConsumption(
        january: Int = _powerConsumption.value.consumption.januaryConsumption,
        february: Int = _powerConsumption.value.consumption.februaryConsumption,
        march: Int = _powerConsumption.value.consumption.marchConsumption,
        april: Int = _powerConsumption.value.consumption.aprilConsumption,
        may: Int = _powerConsumption.value.consumption.mayConsumption,
        june: Int = _powerConsumption.value.consumption.juneConsumption,
        july: Int = _powerConsumption.value.consumption.julyConsumption,
        august: Int = _powerConsumption.value.consumption.augustConsumption,
        september: Int = _powerConsumption.value.consumption.septemberConsumption,
        october: Int = _powerConsumption.value.consumption.octoberConsumption,
        november: Int = _powerConsumption.value.consumption.novemberConsumption,
        december: Int = _powerConsumption.value.consumption.decemberConsumption
    ) {
        val totalYearly = january + february + march + april + may + june +
                july + august + september + october + november + december

        val updatedConsumption = _powerConsumption.value.consumption.copy(
            januaryConsumption = january,
            februaryConsumption = february,
            marchConsumption = march,
            aprilConsumption = april,
            mayConsumption = may,
            juneConsumption = june,
            julyConsumption = july,
            augustConsumption = august,
            septemberConsumption = september,
            octoberConsumption = october,
            novemberConsumption = november,
            decemberConsumption = december,
            yearlyConsumption = totalYearly
        )

        _powerConsumption.update { it.copy(consumption = updatedConsumption) }
        savePowerConsumption(updatedConsumption)
    }

    fun updateYearlyConsumption(yearlyValue: Int) {
        // Norwegian power consumption profile (higher in winter, lower in summer)
        val januaryPercent = 0.14f
        val februaryPercent = 0.13f
        val marchPercent = 0.12f
        val aprilPercent = 0.10f
        val mayPercent = 0.08f
        val junePercent = 0.07f
        val julyPercent = 0.06f
        val augustPercent = 0.07f
        val septemberPercent = 0.08f
        val octoberPercent = 0.10f
        val novemberPercent = 0.12f
        val decemberPercent = 0.13f

        val jan = (yearlyValue * januaryPercent).toInt()
        val feb = (yearlyValue * februaryPercent).toInt()
        val mar = (yearlyValue * marchPercent).toInt()
        val apr = (yearlyValue * aprilPercent).toInt()
        val may = (yearlyValue * mayPercent).toInt()
        val jun = (yearlyValue * junePercent).toInt()
        val jul = (yearlyValue * julyPercent).toInt()
        val aug = (yearlyValue * augustPercent).toInt()
        val sep = (yearlyValue * septemberPercent).toInt()
        val oct = (yearlyValue * octoberPercent).toInt()
        val nov = (yearlyValue * novemberPercent).toInt()
        val dec = (yearlyValue * decemberPercent).toInt()

        val updatedConsumption = _powerConsumption.value.consumption.copy(
            januaryConsumption = jan,
            februaryConsumption = feb,
            marchConsumption = mar,
            aprilConsumption = apr,
            mayConsumption = may,
            juneConsumption = jun,
            julyConsumption = jul,
            augustConsumption = aug,
            septemberConsumption = sep,
            octoberConsumption = oct,
            novemberConsumption = nov,
            decemberConsumption = dec,
            yearlyConsumption = yearlyValue
        )

        _powerConsumption.update { it.copy(consumption = updatedConsumption) }
        savePowerConsumption(updatedConsumption)
    }

    private fun savePowerConsumption(consumption: PowerConsumptionEntity) {
        viewModelScope.launch {
            userRepository.updatePowerConsumption(consumption)
        }
    }

    fun getMonthlyConsumptionValues(): List<Double> {
        val consumption = _powerConsumption.value.consumption

        return listOf(
            consumption.januaryConsumption.toDouble(),
            consumption.februaryConsumption.toDouble(),
            consumption.marchConsumption.toDouble(),
            consumption.aprilConsumption.toDouble(),
            consumption.mayConsumption.toDouble(),
            consumption.juneConsumption.toDouble(),
            consumption.julyConsumption.toDouble(),
            consumption.augustConsumption.toDouble(),
            consumption.septemberConsumption.toDouble(),
            consumption.octoberConsumption.toDouble(),
            consumption.novemberConsumption.toDouble(),
            consumption.decemberConsumption.toDouble()
        )
    }
}