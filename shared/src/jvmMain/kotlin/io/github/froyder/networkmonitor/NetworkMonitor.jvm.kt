package io.github.froyder.networkmonitor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

actual class NetworkMonitor actual constructor() : INetworkMonitor {
    actual override val connectionState: Flow<ConnectionState> =
        MutableStateFlow(ConnectionState.Unknown)
}