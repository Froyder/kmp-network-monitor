package io.github.froyder.networkmonitor

actual object NetworkMonitorInitializer {
    actual fun initialize(context: Any) {
        // No-op on iOS — no context needed
    }
}