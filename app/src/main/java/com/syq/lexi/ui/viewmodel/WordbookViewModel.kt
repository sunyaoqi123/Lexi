package com.syq.lexi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syq.lexi.data.database.WordbookEntity
import com.syq.lexi.data.database.WordEntity
import com.syq.lexi.data.repository.WordbookRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class WordbookViewModel(private val repository: WordbookRepository) : ViewModel() {

    private val _wordbooks = MutableStateFlow<List<WordbookEntity>>(emptyList())
    val wordbooks: StateFlow<List<WordbookEntity>> = _wordbooks.asStateFlow()

    private val _selectedWordbookId = MutableStateFlow<Int?>(null)
    val selectedWordbook: StateFlow<WordbookEntity?> = _selectedWordbookId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.getWordbookById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // 用 flatMapLatest 保证切换单词本时旧 Flow 自动取消
    val words: StateFlow<List<WordEntity>> = _selectedWordbookId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getWordsByWordbook(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val masteredCount: StateFlow<Int> = _selectedWordbookId
        .flatMapLatest { id ->
            if (id == null) flowOf(0)
            else repository.getMasteredCount(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 所有单词本的实际词数（实时从数据库查询）
    private val _wordCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val wordCounts: StateFlow<Map<Int, Int>> = _wordCounts.asStateFlow()

    // 所有单词本的已掌握词数（实时从数据库查询）
    private val _masteredCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val masteredCounts: StateFlow<Map<Int, Int>> = _masteredCounts.asStateFlow()

    init {
        Log.d("WordbookViewModel", "ViewModel initialized")
        loadAllWordbooks()
        loadWordCounts()
    }

    private fun loadWordCounts() {
        viewModelScope.launch {
            _wordbooks.collect { wordbookList ->
                wordbookList.forEach { wordbook ->
                    if (!_wordCounts.value.containsKey(wordbook.id)) {
                        launch {
                            repository.getWordCount(wordbook.id).collect { count ->
                                _wordCounts.value = _wordCounts.value + (wordbook.id to count)
                            }
                        }
                        launch {
                            repository.getMasteredCount(wordbook.id).collect { count ->
                                _masteredCounts.value = _masteredCounts.value + (wordbook.id to count)
                            }
                        }
                    }
                }
            }
        }
    }

    fun loadAllWordbooks() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
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
        Log.d("WordbookViewModel", "Selecting wordbook: ${wordbook.id} - ${wordbook.name}")
        _selectedWordbookId.value = wordbook.id
    }

    fun markWordAsMastered(wordId: Int) {
        viewModelScope.launch {
            try {
                repository.markWordAsMastered(wordId)
            } catch (e: Exception) {
                Log.e("WordbookViewModel", "Error marking word as mastered", e)
            }
        }
    }

    fun markWordAsUnmastered(wordId: Int) {
        viewModelScope.launch {
            try {
                repository.markWordAsUnmastered(wordId)
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

    fun addWordbook(name: String, category: String, description: String, words: List<com.syq.lexi.ui.screens.ParsedWord> = emptyList()) {
        viewModelScope.launch {
            try {
                val newWordbook = com.syq.lexi.data.database.WordbookEntity(
                    name = name,
                    category = category,
                    description = description,
                    totalWords = words.size
                )
                val wordbookId = repository.insertWordbook(newWordbook).toInt()
                if (words.isNotEmpty()) {
                    val wordEntities = words.map { parsed ->
                        com.syq.lexi.data.database.WordEntity(
                            wordbookId = wordbookId,
                            english = parsed.english,
                            chinese = parsed.chinese
                        )
                    }
                    repository.insertWords(wordEntities)
                }
            } catch (e: Exception) {
                Log.e("WordbookViewModel", "Error adding wordbook", e)
            }
        }
    }

    fun addWordsToWordbook(wordbookId: Int, words: List<com.syq.lexi.ui.screens.ParsedWord>) {
        viewModelScope.launch {
            try {
                val wordEntities = words.map { parsed ->
                    com.syq.lexi.data.database.WordEntity(
                        wordbookId = wordbookId,
                        english = parsed.english,
                        chinese = parsed.chinese
                    )
                }
                repository.insertWords(wordEntities)

                // 更新单词本的 totalWords 字段
                val wordbook = repository.getWordbookById(wordbookId).first()
                repository.updateWordbook(wordbook.copy(totalWords = wordbook.totalWords + words.size))

                Log.d("WordbookViewModel", "Added ${words.size} words to wordbook $wordbookId")
            } catch (e: Exception) {
                Log.e("WordbookViewModel", "Error adding words to wordbook", e)
            }
        }
    }
}
