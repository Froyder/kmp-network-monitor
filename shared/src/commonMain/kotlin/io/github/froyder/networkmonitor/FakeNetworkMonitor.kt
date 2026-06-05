package io.github.froyder.networkmonitor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * A fake implementation of [INetworkMonitor] for use in tests.
 *
 * Usage:
 * ```kotlin
 * val fake = FakeNetworkMonitor(initialState = ConnectionState.Connected)
 * fake.emit(ConnectionState.Disconnected) // simulate network loss
 * ```
 */
class FakeNetworkMonitor(
    initialState: ConnectionState = ConnectionState.Unknown
) : INetworkMonitor {
    private val _state = MutableStateFlow(initialState)
    override val connectionState: Flow<ConnectionState> = _state

    /** Simulate a network state change. */
    fun emit(state: ConnectionState) {
        _state.value = state
    }
}
