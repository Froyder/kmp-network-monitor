# KMP Network Monitor

[![Maven Central](https://img.shields.io/maven-central/v/io.github.froyder/kmp-network-monitor)](https://central.sonatype.com/artifact/io.github.froyder/kmp-network-monitor)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-multiplatform-orange.svg)](https://kotlinlang.org/docs/multiplatform.html)

A lightweight Kotlin Multiplatform library for observing network connectivity on Android and iOS.

## Features

- 🌐 Real-time network state monitoring
- 📱 Supports Android and iOS
- 🔄 Reactive API via Kotlin `Flow`
- 🧪 Testable via `INetworkMonitor` interface
- ⚡ Instant `Connected` reaction, debounced `Disconnected` to filter brief spikes

## Installation

```toml
# gradle/libs.versions.toml
[versions]
kmp-network-monitor = "1.0.0"

[libraries]
kmp-network-monitor = { module = "io.github.froyder:kmp-network-monitor", version.ref = "kmp-network-monitor" }
```

```kotlin
// build.gradle.kts
commonMain.dependencies {
    implementation(libs.kmp.network.monitor)
}
```

## Setup

### Android

Initialize the library once in your `Application.onCreate()` or `MainActivity.onCreate()`:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    NetworkMonitorInitializer.initialize(this)
}
```

### iOS

No setup required — the library works out of the box on iOS.

## Usage

### Observing state with `NetworkMonitorProvider` (recommended)

`NetworkMonitorProvider` is a singleton that provides a shared, debounced `StateFlow`.
Use this in your UI layer:

```kotlin
// In a Composable
val connectionState by NetworkMonitorProvider.connectionState.collectAsState()

when (connectionState) {
    is ConnectionState.Connected    -> Text("Online")
    is ConnectionState.Disconnected -> Text("Offline")
    is ConnectionState.Unknown      -> Text("Checking...")
}
```

### Using `NetworkMonitor` directly

For manual control or ViewModel usage:

```kotlin
val monitor = NetworkMonitor()

viewModelScope.launch {
    monitor.connectionState.collect { state ->
        when (state) {
            is ConnectionState.Connected    -> println("Online")
            is ConnectionState.Disconnected -> println("Offline")
            is ConnectionState.Unknown      -> println("Checking...")
        }
    }
}
```

### Testing

Inject `INetworkMonitor` into your classes and use `FakeNetworkMonitor` in tests:

```kotlin
// Production
class MyViewModel(
    private val networkMonitor: INetworkMonitor = NetworkMonitorProvider.connectionState
)

// Test
val fake = FakeNetworkMonitor(initialState = ConnectionState.Connected)
fake.emit(ConnectionState.Disconnected) // simulate network loss
```

## Connection States

| State | Description |
|---|---|
| `Connected` | Device has an active, validated internet connection |
| `Disconnected` | Device has no internet connection |
| `Unknown` | Connection state has not yet been determined |

## Requirements

| Platform | Minimum version |
|---|---|
| Android | API 26 (Android 8.0) |
| iOS | iOS 12.0 |

## License

```
Copyright 2026 Ilia Froyder

Licensed under the Apache License, Version 2.0
```