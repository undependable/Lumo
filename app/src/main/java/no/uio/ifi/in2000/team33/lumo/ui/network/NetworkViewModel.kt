package no.uio.ifi.in2000.team33.lumo.ui.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class NetworkViewModel @Inject constructor() : ViewModel() {

    private val _network = MutableStateFlow(true)
    val network: StateFlow<Boolean> = _network.asStateFlow()

    // Derived states for easier UI consumption
    val isOnline: StateFlow<Boolean> = network.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun checkConnectivity(context: Context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return

        val network = connectivityManager.activeNetwork
        if (network == null) {
            _network.value = false
            return
        }

        val capabilities = connectivityManager.getNetworkCapabilities(network)
        _network.value =
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false

        Log.d("NetworkViewModel", "Network connectivity: ${_network.value}")
    }
}