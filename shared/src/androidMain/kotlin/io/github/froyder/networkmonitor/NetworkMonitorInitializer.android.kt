package io.github.froyder.networkmonitor

import android.content.Context

actual object NetworkMonitorInitializer {
    internal lateinit var appContext: Context

    actual fun initialize(context: Any) {
        appContext = (context as Context).applicationContext
    }
}