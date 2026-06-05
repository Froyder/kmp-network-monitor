package io.github.froyder.networkmonitor

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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