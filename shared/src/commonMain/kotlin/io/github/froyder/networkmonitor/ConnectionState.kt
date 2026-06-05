package io.github.froyder.networkmonitor

/**
 * Represents the current network connection state.
 */
sealed class ConnectionState {
    /** Device has an active, validated internet connection. */
    object Connected : ConnectionState()

    /** Device has no internet connection. */
    object Disconnected : ConnectionState()

    /** Connection state has not yet been determined. */
    object Unknown : ConnectionState()
}