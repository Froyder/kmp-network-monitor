package io.github.froyder.networkmonitor

import kotlinx.coroutines.flow.Flow

/**
 * Observes the device's network connection state.
 * Use [NetworkMonitor] for the platform implementation,
 * or implement this interface directly for testing with a fake.
 */
interface INetworkMonitor {
    /** A [kotlinx.coroutines.flow.Flow] emitting [ConnectionState] updates. */
    val connectionState: Flow<ConnectionState>
}

expect class NetworkMonitor() : INetworkMonitor {
    override val connectionState: Flow<ConnectionState>
}