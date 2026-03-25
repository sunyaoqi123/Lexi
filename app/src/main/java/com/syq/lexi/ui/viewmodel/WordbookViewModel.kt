package com.syq.lexi.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syq.lexi.data.auth.AuthPreferences
import com.syq.lexi.data.database.WordbookEntity
import com.syq.lexi.data.database.WordEntity
import com.syq.lexi.data.network.RetrofitClient
import com.syq.lexi.data.network.SyncWordsRequest
import com.syq.lexi.data.network.WordbookDto
import com.syq.lexi.data.repository.SyncRepository
import com.syq.lexi.data.repository.WordbookRepository
import com.syq.lexi.data.repository.toDto
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
class WordbookViewModel(
    private val repository: WordbookRepository,
    private val context: Context? = null
) : ViewModel() {

    private val authPrefs = context?.let { AuthPreferences(it) }
    private val api = RetrofitClient.api

    private val _wordbooks = MutableStateFlow<List<WordbookEntity>>(emptyList())
    val wordbooks: StateFlow<List<WordbookEntity>> = _wordbooks.asStateFlow()

    private val _selectedWordbookId = MutableStateFlow<Int?>(null)
    val selectedWordbook: StateFlow<WordbookEntity?> = _selectedWordbookId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.getWordbookById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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

    private val _wordCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val wordCounts: StateFlow<Map<Int, Int>> = _wordCounts.asStateFlow()

    private val _masteredCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val masteredCounts: StateFlow<Map<Int, Int>> = _masteredCounts.asStateFlow()

    private val _starredCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val starredCounts: StateFlow<Map<Int, Int>> = _starredCounts.asStateFlow()

    init {
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
                        launch {
                            repository.getStarredCount(wordbook.id).collect { count ->
                                _starredCounts.value = _starredCounts.value + (wordbook.id to count)
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
        // 先置 null 再设新 id，避免切换时短暂显示旧数据
        _selectedWordbookId.value = null
        _selectedWordbookId.value = wordbook.id
    }

    fun markWordAsMastered(wordId: Int) {
        viewModelScope.launch {
            try {
                repository.markWordAsMastered(wordId)
                // 同步到服务端
                syncMasteredToRemote(wordId, true)
            } catch (e: Exception) {
                Log.e("WordbookViewModel", "Error marking mastered", e)
            }
        }
    }

    fun markWordAsUnmastered(wordId: Int) {
        viewModelScope.launch {
            try {
                repository.markWordAsUnmastered(wordId)
                // 同步到服务端
                syncMasteredToRemote(wordId, false)
            } catch (e: Exception) {
                Log.e("WordbookViewModel", "Error marking unmastered", e)
            }
        }
    }

    private suspend fun syncMasteredToRemote(wordId: Int, mastered: Boolean) {
        try {
            val token = authPrefs?.token?.first() ?: return
            val bearer = "Bearer $token"
            val word = repository.getWordByIdOnce(wordId) ?: return
            val wordbook = repository.getWordbookById(word.wordbookId).first()
            val remoteWordbooks = api.getWordbooks(bearer)
            val remote = remoteWordbooks.find { it.name == wordbook.name } ?: return
            val remoteWords = api.getWords(bearer, remote.id)
            val remoteWord = remoteWords.find { it.english.equals(word.english, ignoreCase = true) } ?: return
            api.updateMastered(bearer, remote.id, remoteWord.id, mastered)
        } catch (e: Exception) {
            Log.e("WordbookViewModel", "Error syncing mastered to remote", e)
        }
    }

    fun getUnmasteredWords(wordbookId: Int): Flow<List<WordEntity>> =
        repository.getUnmasteredWords(wordbookId)

    fun getMasteredWords(wordbookId: Int): Flow<List<WordEntity>> =
        repository.getMasteredWords(wordbookId)

    fun getStarredWords(wordbookId: Int): Flow<List<WordEntity>> =
        repository.getStarredWords(wordbookId)

    fun starWord(wordId: Int) {
        viewModelScope.launch {
            try {
                repository.starWord(wordId)
                // 收藏时立即取消已掌握状态
                repository.markWordAsUnmastered(wordId)
                syncStarToRemote(wordId, true)
                syncMasteredToRemote(wordId, false)
            } catch (e: Exception) {
                Log.e("WordbookViewModel", "Error starring word", e)
            }
        }
    }

    fun unstarWord(wordId: Int) {
        viewModelScope.launch {
            try {
                repository.unstarWord(wordId)
                syncStarToRemote(wordId, false)
            } catch (e: Exception) {
                Log.e("WordbookViewModel", "Error unstarring word", e)
            }
        }
    }

    private suspend fun syncStarToRemote(wordId: Int, starred: Boolean) {
        try {
            val token = authPrefs?.token?.first()
            Log.d("WordbookViewModel", "syncStarToRemote: token=${if (token.isNullOrEmpty()) "EMPTY" else "OK(${token.take(10)}...)"}")
            if (token.isNullOrEmpty()) {
                Log.w("WordbookViewModel", "syncStarToRemote: no token, skip")
                return
            }
            val bearer = "Bearer $token"
            val word = repository.getWordByIdOnce(wordId) ?: run {
                Log.w("WordbookViewModel", "syncStarToRemote: word $wordId not found")
                return
            }
            val wordbook = repository.getWordbookById(word.wordbookId).first()
            val remoteWordbooks = api.getWordbooks(bearer)
            val remote = remoteWordbooks.find { it.name == wordbook.name } ?: run {
                Log.w("WordbookViewModel", "syncStarToRemote: remote wordbook '${wordbook.name}' not found")
                return
            }
            val remoteWords = api.getWords(bearer, remote.id)
            val remoteWord = remoteWords.find { it.english.equals(word.english, ignoreCase = true) } ?: run {
                Log.w("WordbookViewModel", "syncStarToRemote: remote word '${word.english}' not found")
                return
            }
            api.updateStarred(bearer, remote.id, remoteWord.id, starred)
            Log.d("WordbookViewModel", "syncStarToRemote: '${word.english}' starred=$starred OK (remoteWordId=${remoteWord.id})")
        } catch (e: Exception) {
            Log.e("WordbookViewModel", "Error syncing starred to remote", e)
        }
    }

    fun addWordbook(name: String, category: String, description: String,
                    words: List<com.syq.lexi.ui.screens.ParsedWord> = emptyList()) {
        viewModelScope.launch {
            try {
                // 1. 本地保存
                val newWordbook = WordbookEntity(name = name, category = category,
                    description = description, totalWords = words.size)
                val localId = repository.insertWordbook(newWordbook).toInt()
                if (words.isNotEmpty()) {
                    val wordEntities = words.map {
                        WordEntity(wordbookId = localId, english = it.english, chinese = it.chinese)
                    }
                    repository.insertWords(wordEntities)
                }

                // 2. 同步到服务端
                val token = authPrefs?.token?.first() ?: return@launch
                val bearer = "Bearer $token"
                val created = api.createWordbook(bearer,
                    WordbookDto(name = name, category = category, description = description))
                if (words.isNotEmpty()) {
                    val localWords = repository.getWordsByWordbook(localId).first()
                    api.syncWords(bearer, created.id, SyncWordsRequest(localWords.map { it.toDto() }))
                }
                Log.d("WordbookViewModel", "Wordbook added locally and remotely: $name")
            } catch (e: Exception) {
                Log.e("WordbookViewModel", "Error adding wordbook", e)
            }
        }
    }

    fun addWordsToWordbook(wordbookId: Int, words: List<com.syq.lexi.ui.screens.ParsedWord>,
                           onResult: ((added: Int, merged: Int, skipped: Int) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val wordEntities = words.map {
                    WordEntity(wordbookId = wordbookId, english = it.english, chinese = it.chinese)
                }
                val (added, merged, skipped) = repository.addWordsWithDedup(wordbookId, wordEntities)
                val wordbook = repository.getWordbookById(wordbookId).first()
                repository.updateWordbook(wordbook.copy(totalWords = wordbook.totalWords + added))

                // 同步到服务端
                val token = authPrefs?.token?.first()
                if (token != null) {
                    val bearer = "Bearer $token"
                    val remoteWordbooks = api.getWordbooks(bearer)
                    val remote = remoteWordbooks.find { it.name == wordbook.name }
                    if (remote != null) {
                        val localWords = repository.getWordsByWordbook(wordbookId).first()
                        api.syncWords(bearer, remote.id, SyncWordsRequest(localWords.map { it.toDto() }))
                    }
                }
                onResult?.invoke(added, merged, skipped)
            } catch (e: Exception) {
                Log.e("WordbookViewModel", "Error adding words", e)
            }
        }
    }

    fun deleteWord(word: WordEntity) {
        viewModelScope.launch {
            try {
                repository.deleteWord(word)
                val wordbook = repository.getWordbookById(word.wordbookId).first()
                if (wordbook.totalWords > 0)
                    repository.updateWordbook(wordbook.copy(totalWords = wordbook.totalWords - 1))

                // 同步删除到服务端
                val token = authPrefs?.token?.first()
                if (token != null) {
                    val bearer = "Bearer $token"
                    val remoteWordbooks = api.getWordbooks(bearer)
                    val remote = remoteWordbooks.find { it.name == wordbook.name }
                    if (remote != null) {
                        val remoteWords = api.getWords(bearer, remote.id)
                        val remoteWord = remoteWords.find {
                            it.english.equals(word.english, ignoreCase = true)
                        }
                        if (remoteWord != null)
                            api.deleteWord(bearer, remote.id, remoteWord.id)
                    }
                }
                Log.d("WordbookViewModel", "Deleted word: ${word.english}")
            } catch (e: Exception) {
                Log.e("WordbookViewModel", "Error deleting word", e)
            }
        }
    }

    fun deleteWordbookWithWords(wordbook: WordbookEntity) {
        viewModelScope.launch {
            try {
                // 1. 本地删除
                repository.deleteWordsByWordbook(wordbook.id)
                repository.deleteWordbook(wordbook)

                // 2. 同步删除到服务端
                val token = authPrefs?.token?.first()
                if (token != null) {
                    val bearer = "Bearer $token"
                    val remoteWordbooks = api.getWordbooks(bearer)
                    val remote = remoteWordbooks.find { it.name == wordbook.name }
                    if (remote != null)
                        api.deleteWordbook(bearer, remote.id)
                }
                Log.d("WordbookViewModel", "Deleted wordbook: ${wordbook.name}")
            } catch (e: Exception) {
                Log.e("WordbookViewModel", "Error deleting wordbook", e)
            }
        }
    }
}
