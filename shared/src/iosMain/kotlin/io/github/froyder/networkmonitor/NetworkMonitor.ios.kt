package io.github.froyder.networkmonitor

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
actual class NetworkMonitor actual constructor() : INetworkMonitor {
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Unknown)
    actual override val connectionState: Flow<ConnectionState> = _connectionState

    init {
        val monitor = nw_path_monitor_create()

        nw_path_monitor_set_update_handler(monitor) { path ->
            val status = nw_path_get_status(path)
            _connectionState.value = if (status == nw_path_status_satisfied)
                ConnectionState.Connected
            else
                ConnectionState.Disconnected
        }

        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_start(monitor)
    }
}