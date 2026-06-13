package io.github.froyder.networkmonitor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
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
): kotlinx.coroutines.flow.Flow<ConnectionState> = channelFlow {
    var pendingJob: Job? = null
    collect { state ->
        when (state) {
            is ConnectionState.Connected -> {
                pendingJob?.cancel()
                pendingJob = null
                send(state)
            }
            is ConnectionState.Disconnected -> {
                pendingJob?.cancel()
                pendingJob = launch {
                    delay(timeoutMillis)
                    send(state)
                }
            }
            is ConnectionState.Unknown -> {
                pendingJob?.cancel()
                pendingJob = null
                send(state)
            }
        }
    }
}