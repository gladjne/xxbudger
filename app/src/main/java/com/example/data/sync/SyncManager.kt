// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.data.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SyncState {
    IDLE,
    SYNCING,
    SYNCED,
    OFFLINE,
    ERROR
}

object SyncManager {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var delayJob: Job? = null

    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    fun updateState(state: SyncState) {
        delayJob?.cancel()
        _syncState.value = state

        if (state == SyncState.SYNCED) {
            delayJob = scope.launch {
                delay(2500) // 2.5 seconds (between 2 to 3)
                if (_syncState.value == SyncState.SYNCED) {
                    _syncState.value = SyncState.IDLE
                }
            }
        } else if (state == SyncState.ERROR) {
            delayJob = scope.launch {
                delay(2500)
                if (_syncState.value == SyncState.ERROR) {
                    _syncState.value = SyncState.IDLE
                }
            }
        }
    }
}
