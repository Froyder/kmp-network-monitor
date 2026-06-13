package io.github.froyder.networkmonitor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DebounceTest {

    private val timeout = 2000L

    @Test
    fun `Connected emits immediately without delay`() = runTest {
        val results = mutableListOf<ConnectionState>()
        launch {
            flow { emit(ConnectionState.Connected) }
                .debounceDisconnected(timeout)
                .collect { results.add(it) }
        }
        advanceUntilIdle()
        assertEquals(listOf<ConnectionState>(ConnectionState.Connected), results)
    }

    @Test
    fun `Unknown emits immediately without delay`() = runTest {
        val results = mutableListOf<ConnectionState>()
        launch {
            flow { emit(ConnectionState.Unknown) }
                .debounceDisconnected(timeout)
                .collect { results.add(it) }
        }
        advanceUntilIdle()
        assertEquals(listOf<ConnectionState>(ConnectionState.Unknown), results)
    }

    @Test
    fun `Disconnected is not emitted before timeout elapses`() = runTest {
        val results = mutableListOf<ConnectionState>()
        launch {
            flow {
                emit(ConnectionState.Disconnected)
                awaitCancellation()
            }
            .debounceDisconnected(timeout)
            .collect { results.add(it) }
        }
        advanceTimeBy(timeout - 1)
        assertEquals(emptyList<ConnectionState>(), results)
    }

    @Test
    fun `Disconnected is emitted once timeout elapses`() = runTest {
        val results = mutableListOf<ConnectionState>()
        launch {
            flow { emit(ConnectionState.Disconnected) }
                .debounceDisconnected(timeout)
                .collect { results.add(it) }
        }
        advanceUntilIdle()
        assertEquals(listOf<ConnectionState>(ConnectionState.Disconnected), results)
    }

    @Test
    fun `Disconnected is suppressed when Connected arrives before timeout`() = runTest {
        val results = mutableListOf<ConnectionState>()
        launch {
            flow {
                emit(ConnectionState.Disconnected)
                delay(timeout / 2)
                emit(ConnectionState.Connected)
                awaitCancellation()
            }
            .debounceDisconnected(timeout)
            .collect { results.add(it) }
        }
        advanceUntilIdle()
        assertEquals(listOf<ConnectionState>(ConnectionState.Connected), results)
    }

    @Test
    fun `Disconnected is suppressed when Unknown arrives before timeout`() = runTest {
        val results = mutableListOf<ConnectionState>()
        launch {
            flow {
                emit(ConnectionState.Disconnected)
                delay(timeout / 2)
                emit(ConnectionState.Unknown)
                awaitCancellation()
            }
            .debounceDisconnected(timeout)
            .collect { results.add(it) }
        }
        advanceUntilIdle()
        assertEquals(listOf<ConnectionState>(ConnectionState.Unknown), results)
    }

    @Test
    fun `Disconnected following Connected is also debounced`() = runTest {
        val results = mutableListOf<ConnectionState>()
        launch {
            flow {
                emit(ConnectionState.Connected)
                emit(ConnectionState.Disconnected)
                awaitCancellation()
            }
            .debounceDisconnected(timeout)
            .collect { results.add(it) }
        }

        // Connected emits at t=0; Disconnected is pending at t=2000
        runCurrent()
        assertEquals(listOf<ConnectionState>(ConnectionState.Connected), results)

        advanceTimeBy(timeout - 1)
        assertEquals(listOf<ConnectionState>(ConnectionState.Connected), results)

        // Timeout fires; Disconnected is now emitted
        advanceUntilIdle()
        assertEquals(listOf(ConnectionState.Connected, ConnectionState.Disconnected), results)
    }

    @Test
    fun `first Disconnected suppressed, second Disconnected emitted after its own timeout`() = runTest {
        // t=0:    first Disconnected → pending at t=2000
        // t=1000: Connected cancels the pending, Connected emitted
        //         second Disconnected → pending at t=3000
        val results = mutableListOf<ConnectionState>()
        launch {
            flow {
                emit(ConnectionState.Disconnected)
                delay(timeout / 2)
                emit(ConnectionState.Connected)
                emit(ConnectionState.Disconnected)
                awaitCancellation()
            }
            .debounceDisconnected(timeout)
            .collect { results.add(it) }
        }

        // Advance to t=2999: Connected already emitted, second Disconnected not yet fired
        advanceTimeBy(timeout / 2 + timeout - 1)
        assertEquals(listOf<ConnectionState>(ConnectionState.Connected), results)

        // Advance to t=3000: second Disconnected fires
        advanceUntilIdle()
        assertEquals(listOf(ConnectionState.Connected, ConnectionState.Disconnected), results)
    }
}
