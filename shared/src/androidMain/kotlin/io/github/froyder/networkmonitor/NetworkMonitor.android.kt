package io.github.froyder.networkmonitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

actual class NetworkMonitor actual constructor() : INetworkMonitor {
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Unknown)
    actual override val connectionState: Flow<ConnectionState> = _connectionState

    init {
        val context = NetworkMonitorInitializer.appContext
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Track all valid networks separately
        val validNetworks = mutableSetOf<Network>()

        fun updateState() {
            _connectionState.value = if (validNetworks.isNotEmpty())
                ConnectionState.Connected
            else
                ConnectionState.Disconnected
        }

        // Set initial state
        val currentCaps = connectivityManager.getNetworkCapabilities(
            connectivityManager.activeNetwork
        )
        if (currentCaps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            && currentCaps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            connectivityManager.activeNetwork?.let { validNetworks.add(it) }
        }
        updateState()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Intentionally empty — we wait for onCapabilitiesChanged with
                // NET_CAPABILITY_VALIDATED before marking as connected
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet = networkCapabilities
                    .hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val hasValidated = networkCapabilities
                    .hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                if (hasInternet && hasValidated) {
                    validNetworks.add(network)
                } else {
                    validNetworks.remove(network)
                }
                updateState()
            }

            override fun onLost(network: Network) {
                validNetworks.remove(network)
                updateState()
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)
    }
}