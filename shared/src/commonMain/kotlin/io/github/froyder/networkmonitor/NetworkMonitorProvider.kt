package io.github.froyder.networkmonitor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

object NetworkMonitorProvider {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val monitor = NetworkMonitor()

    val connectionState: StateFlow<ConnectionState> = monitor.connectionState
        .debounceDisconnected(2000L)
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = ConnectionState.Unknown
        )
}

/**
 * Emits [ConnectionState.Connected] and [ConnectionState.Unknown] instantly.
 * Delays [ConnectionState.Disconnected] by [timeoutMillis] to filter
 * brief disconnection spikes during network switching.
 */
fun kotlinx.coroutines.flow.Flow<ConnectionState>.debounceDisconnected(
    timeoutMillis: Long
): kotlinx.coroutines.flow.Flow<ConnectionState> = flow {
    var pendingDisconnected = false
    collect { state ->
        when (state) {
            is ConnectionState.Connected -> {
                pendingDisconnected = false
                emit(state)
            }
            is ConnectionState.Disconnected -> {
                pendingDisconnected = true
                delay(timeoutMillis)
                if (pendingDisconnected) emit(state)
            }
            is ConnectionState.Unknown -> {
                pendingDisconnected = false
                emit(state)
            }
        }
    }
}