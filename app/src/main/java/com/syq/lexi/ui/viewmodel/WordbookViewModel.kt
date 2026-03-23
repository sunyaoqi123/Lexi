package com.syq.lexi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syq.lexi.data.database.WordbookEntity
import com.syq.lexi.data.database.WordEntity
import com.syq.lexi.data.repository.WordbookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WordbookViewModel(private val repository: WordbookRepository) : ViewModel() {
    
    private val _wordbooks = MutableStateFlow<List<WordbookEntity>>(emptyList())
    val wordbooks: StateFlow<List<WordbookEntity>> = _wordbooks.asStateFlow()

    private val _selectedWordbook = MutableStateFlow<WordbookEntity?>(null)
    val selectedWordbook: StateFlow<WordbookEntity?> = _selectedWordbook.asStateFlow()

    private val _words = MutableStateFlow<List<WordEntity>>(emptyList())
    val words: StateFlow<List<WordEntity>> = _words.asStateFlow()

    private val _masteredCount = MutableStateFlow(0)
    val masteredCount: StateFlow<Int> = _masteredCount.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        Log.d("WordbookViewModel", "ViewModel initialized")
        loadAllWordbooks()
    }

    fun loadAllWordbooks() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("WordbookViewModel", "Loading wordbooks...")
                repository.getAllWordbooks().collect { wordbookList ->
                    Log.d("WordbookViewModel", "Wordbooks loaded: ${wordbookList.size}")
                    _wordbooks.value = wordbookList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("WordbookViewModel", "Error loading wordbooks", e)
                _isLoading.value = false
            }
        }
    }

    fun selectWordbook(wordbook: WordbookEntity) {
        _selectedWordbook.value = wordbook
        loadWordsForWordbook(wordbook.id)
        loadMasteredCount(wordbook.id)
    }

    private fun loadWordsForWordbook(wordbookId: Int) {
        viewModelScope.launch {
            try {
                repository.getWordsByWordbook(wordbookId).collect { wordList ->
                    Log.d("WordbookViewModel", "Words loaded for wordbook $wordbookId: ${wordList.size}")
                    _words.value = wordList
                }
            } catch (e: Exception) {
                Log.e("WordbookViewModel", "Error loading words", e)
            }
        }
    }

    private fun loadMasteredCount(wordbookId: Int) {
        viewModelScope.launch {
            try {
                repository.getMasteredCount(wordbookId).collect { count ->
                    _masteredCount.value = count
                }
            } catch (e: Exception) {
                Log.e("WordbookViewModel", "Error loading mastered count", e)
            }
        }
    }

    fun markWordAsMastered(wordId: Int) {
        viewModelScope.launch {
            try {
                repository.markWordAsMastered(wordId)
                _selectedWordbook.value?.id?.let { loadMasteredCount(it) }
            } catch (e: Exception) {
                Log.e("WordbookViewModel", "Error marking word as mastered", e)
            }
        }
    }

    fun markWordAsUnmastered(wordId: Int) {
        viewModelScope.launch {
            try {
                repository.markWordAsUnmastered(wordId)
                _selectedWordbook.value?.id?.let { loadMasteredCount(it) }
            } catch (e: Exception) {
                Log.e("WordbookViewModel", "Error marking word as unmastered", e)
            }
        }
    }

    fun getUnmasteredWords(wordbookId: Int): Flow<List<WordEntity>> {
        return repository.getUnmasteredWords(wordbookId)
    }

    fun getMasteredWords(wordbookId: Int): Flow<List<WordEntity>> {
        return repository.getMasteredWords(wordbookId)
    }

    fun addWordbook(name: String, category: String, description: String) {
        viewModelScope.launch {
            try {
                val newWordbook = WordbookEntity(
                    name = name,
                    category = category,
                    description = description,
                    totalWords = 0
                )
                repository.insertWordbook(newWordbook)
                loadAllWordbooks()
            } catch (e: Exception) {
                Log.e("WordbookViewModel", "Error adding wordbook", e)
            }
        }
    }
}
