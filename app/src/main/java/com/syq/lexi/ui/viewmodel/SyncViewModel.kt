package com.syq.lexi.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syq.lexi.data.repository.SyncRepository
import com.syq.lexi.data.repository.WordbookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(val wordbooksSynced: Int, val wordsSynced: Int) : SyncState()
    data class Error(val message: String) : SyncState()
}

class SyncViewModel(
    private val context: Context,
    private val wordbookRepository: WordbookRepository
) : ViewModel() {

    private val syncRepository = SyncRepository(context, wordbookRepository)

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    fun syncAll(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing
            val result = syncRepository.syncAll()
            _syncState.value = if (result.success) {
                Log.d("SyncViewModel", "Sync success: ${result.wordbooksSynced} wordbooks, ${result.wordsSynced} words")
                SyncState.Success(result.wordbooksSynced, result.wordsSynced)
            } else {
                Log.e("SyncViewModel", "Sync error: ${result.message}")
                SyncState.Error(result.message)
            }
            onComplete?.invoke()
        }
    }

    fun syncFromUserWordbooks(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing
            val result = syncRepository.syncFromUserWordbooks()
            _syncState.value = if (result.success) {
                Log.d("SyncViewModel", "InitSync success: ${result.wordbooksSynced} wordbooks, ${result.wordsSynced} words")
                SyncState.Success(result.wordbooksSynced, result.wordsSynced)
            } else {
                Log.e("SyncViewModel", "InitSync error: ${result.message}")
                SyncState.Error(result.message)
            }
            onComplete?.invoke()
        }
    }

    fun resetState() {
        _syncState.value = SyncState.Idle
    }
}
