package io.github.froyder.networkmonitor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FakeNetworkMonitor(
    initialState: ConnectionState = ConnectionState.Unknown
) : INetworkMonitor {
    private val _state = MutableStateFlow(initialState)
    override val connectionState: Flow<ConnectionState> = _state

    fun emit(state: ConnectionState) {
        _state.value = state
    }
}

class NetworkMonitorTest {

    @Test
    fun `initial state is emitted`() = runTest {
        val monitor = FakeNetworkMonitor(ConnectionState.Connected)
        val state = monitor.connectionState.first()
        assertEquals(ConnectionState.Connected, state)
    }

    @Test
    fun `state changes from connected to disconnected`() = runTest {
        val monitor = FakeNetworkMonitor(ConnectionState.Connected)
        monitor.emit(ConnectionState.Disconnected)
        val state = monitor.connectionState.first()
        assertEquals(ConnectionState.Disconnected, state)
    }

    @Test
    fun `all connection states are valid`() = runTest {
        val monitor = FakeNetworkMonitor()
        val state = monitor.connectionState.first()
        assertTrue(
            state is ConnectionState.Connected
                    || state is ConnectionState.Disconnected
                    || state is ConnectionState.Unknown
        )
    }
}