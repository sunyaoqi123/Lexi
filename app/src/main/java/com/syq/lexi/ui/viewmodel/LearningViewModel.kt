package com.syq.lexi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syq.lexi.data.auth.AuthPreferences
import com.syq.lexi.data.database.StudyRecordEntity
import com.syq.lexi.data.database.WordEntity
import com.syq.lexi.data.repository.WordbookRepository
import com.syq.lexi.data.network.RetrofitClient
import com.syq.lexi.util.ReviewAlgorithm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// 学习阶段
enum class LearningPhase {
    PHASE1_WORD_TO_MEANING,  // 看单词选释义
    PHASE2_MEANING_TO_WORD,  // 看释义选单词
    PHASE3_SPELL_WORD,       // 看释义拼写单词
    COMPLETED                // 全部完成
}

// 单题状态
data class QuizQuestion(
    val word: WordEntity,           // 目标单词
    val options: List<String>,      // 四个选项
    val correctAnswer: String,      // 正确答案
    val phase: LearningPhase
)

// 学习会话状态
data class LearningSessionState(
    val wordbookId: Int = 0,
    val wordbookName: String = "",
    val phase: LearningPhase = LearningPhase.PHASE1_WORD_TO_MEANING,
    val currentQuestion: QuizQuestion? = null,
    val currentIndex: Int = 0,      // 已答对的数量（进度）
    val totalInRound: Int = 0,       // 固定为 sessionWordCount
    val sessionWordCount: Int = 0,   // 原始单词数（固定不变）
    val correctCount: Int = 0,       // 本轮答对数
    val selectedAnswer: String? = null,
    val isAnswered: Boolean = false,
    val isCorrect: Boolean = false,
    val spellInput: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class LearningViewModel(private val repository: WordbookRepository, private val context: android.content.Context? = null) : ViewModel() {

    private val authPrefs = context?.let { AuthPreferences(it) }
    private val api = RetrofitClient.api

    private val _state = MutableStateFlow(LearningSessionState())
    val state: StateFlow<LearningSessionState> = _state.asStateFlow()

    private var starredOnly: Boolean = false
    private var sessionWords: List<WordEntity> = emptyList()
    private var allWords: List<WordEntity> = emptyList()

    private var phase1Queue: MutableList<WordEntity> = mutableListOf()
    private var phase2Queue: MutableList<WordEntity> = mutableListOf()
    private var phase3Queue: MutableList<WordEntity> = mutableListOf()

    private var currentQueueIndex: Int = 0
    private var correctInPhase: Int = 0

    // 计时与答题记录
    private var questionStartTime: Long = 0L
    private val answerRecords = mutableMapOf<Int, MutableList<Triple<Boolean, Long, Int>>>()

    // 学完后的回调：(wordId, familiarity, reviewCount, nextReviewDate)
    var onReviewDataUpdated: ((Int, Float, Int, Long) -> Unit)? = null

    fun startSession(wordbookId: Int, wordbookName: String, groupSize: Int = 10, starredOnly: Boolean = false, reviewOnly: Boolean = false) {
        viewModelScope.launch {
            try {
                this@LearningViewModel.starredOnly = starredOnly
                _state.value = LearningSessionState(
                    wordbookId = wordbookId,
                    wordbookName = wordbookName,
                    isLoading = true
                )
                answerRecords.clear()
                allWords = repository.getWordsByWordbook(wordbookId).first()

                if (allWords.size < 4) {
                    _state.value = _state.value.copy(isLoading = false,
                        errorMessage = "单词本中至少需要4个单词才能开始学习")
                    return@launch
                }

                val candidateWords: List<WordEntity>
                if (starredOnly) {
                    candidateWords = allWords.filter { it.isStarred }
                } else if (reviewOnly) {
                    // 纯复习模式：只取到期复习词，按紧迫程度排序（nextReviewDate 最早的优先）
                    val dueWords = repository.getDueReviewWords(wordbookId)
                    Log.d("LearningViewModel", "reviewOnly: dueWords=${dueWords.size}, groupSize=$groupSize")
                    val actualSize = minOf(dueWords.size, groupSize)
                    candidateWords = dueWords.take(actualSize)
                } else {
                    // 优先复习到期词，不足则补充新词
                    val dueWords = repository.getDueReviewWords(wordbookId)
                    val newWords = repository.getNewWords(wordbookId)
                    val reviewPart = dueWords.shuffled().take(groupSize)
                    val remaining = groupSize - reviewPart.size
                    val newPart = newWords.filter { w -> reviewPart.none { it.id == w.id } }
                        .shuffled().take(remaining)
                    candidateWords = reviewPart + newPart
                }

                if (candidateWords.isEmpty()) {
                    _state.value = _state.value.copy(isLoading = false,
                        errorMessage = if (starredOnly) "没有收藏的难词" else if (reviewOnly) "没有需要复习的单词" else "没有未掌握的单词")
                    return@launch
                }

                sessionWords = if (starredOnly) {
                    val actualSize = minOf(groupSize, candidateWords.size)
                    candidateWords.shuffled().take(actualSize)
                } else if (reviewOnly) {
                    // 纯复习：取 min(到期词数, groupSize)
                    candidateWords
                } else {
                    if (candidateWords.size >= groupSize) candidateWords.take(groupSize)
                    else candidateWords
                }

                phase1Queue = sessionWords.shuffled().toMutableList()
                phase2Queue = mutableListOf()
                phase3Queue = mutableListOf()
                currentQueueIndex = 0
                correctInPhase = 0

                _state.value = LearningSessionState(
                    wordbookId = wordbookId,
                    wordbookName = wordbookName,
                    phase = LearningPhase.PHASE1_WORD_TO_MEANING,
                    totalInRound = phase1Queue.size,
                    sessionWordCount = sessionWords.size,
                    isLoading = false
                )
                loadNextQuestion()
            } catch (e: Exception) {
                Log.e("LearningViewModel", "Error starting session", e)
                _state.value = _state.value.copy(isLoading = false,
                    errorMessage = "加载失败：${e.message}")
            }
        }
    }

    private fun loadNextQuestion() {
        val queue = getCurrentQueue()
        if (currentQueueIndex >= queue.size) {
            advancePhase()
            return
        }
        val word = queue[currentQueueIndex]
        val question = buildQuestion(word, _state.value.phase)
        questionStartTime = System.currentTimeMillis()
        _state.value = _state.value.copy(
            currentQuestion = question,
            currentIndex = correctInPhase,
            totalInRound = _state.value.sessionWordCount,
            selectedAnswer = null,
            isAnswered = false,
            isCorrect = false,
            spellInput = ""
        )
    }

    private fun getCurrentQueue(): List<WordEntity> = when (_state.value.phase) {
        LearningPhase.PHASE1_WORD_TO_MEANING -> phase1Queue
        LearningPhase.PHASE2_MEANING_TO_WORD -> phase2Queue
        LearningPhase.PHASE3_SPELL_WORD -> phase3Queue
        LearningPhase.COMPLETED -> emptyList()
    }

    private fun buildQuestion(word: WordEntity, phase: LearningPhase): QuizQuestion {
        val others = allWords.filter { it.id != word.id }.shuffled().take(3)

        return when (phase) {
            LearningPhase.PHASE1_WORD_TO_MEANING -> {
                val options = (others.map { it.chinese } + word.chinese).shuffled()
                QuizQuestion(word, options, word.chinese, phase)
            }
            LearningPhase.PHASE2_MEANING_TO_WORD -> {
                val options = (others.map { it.english } + word.english).shuffled()
                QuizQuestion(word, options, word.english, phase)
            }
            LearningPhase.PHASE3_SPELL_WORD -> {
                QuizQuestion(word, emptyList(), word.english, phase)
            }
            LearningPhase.COMPLETED -> QuizQuestion(word, emptyList(), "", phase)
        }
    }

    fun submitAnswer(answer: String) {
        val state = _state.value
        if (state.isAnswered) return
        val question = state.currentQuestion ?: return
        val isCorrect = answer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
        val hesitationMs = System.currentTimeMillis() - questionStartTime
        val phaseInt = when (state.phase) {
            LearningPhase.PHASE1_WORD_TO_MEANING -> 0
            LearningPhase.PHASE2_MEANING_TO_WORD -> 1
            LearningPhase.PHASE3_SPELL_WORD -> 2
            else -> 0
        }
        // 记录答题数据
        answerRecords.getOrPut(question.word.id) { mutableListOf() }
            .add(Triple(isCorrect, hesitationMs, phaseInt))

        if (isCorrect) {
            correctInPhase++
        } else {
            when (state.phase) {
                LearningPhase.PHASE1_WORD_TO_MEANING -> phase1Queue.add(question.word)
                LearningPhase.PHASE2_MEANING_TO_WORD -> phase2Queue.add(question.word)
                LearningPhase.PHASE3_SPELL_WORD -> phase3Queue.add(question.word)
                else -> {}
            }
        }
        _state.value = _state.value.copy(
            selectedAnswer = answer,
            isAnswered = true,
            isCorrect = isCorrect,
            currentIndex = correctInPhase
        )
    }

    fun updateSpellInput(input: String) {
        _state.value = _state.value.copy(spellInput = input)
    }

    fun nextQuestion() {
        currentQueueIndex++
        loadNextQuestion()
    }

    private fun advancePhase() {
        when (_state.value.phase) {
            LearningPhase.PHASE1_WORD_TO_MEANING -> {
                phase2Queue = sessionWords.shuffled().toMutableList()
                currentQueueIndex = 0
                correctInPhase = 0
                _state.value = _state.value.copy(
                    phase = LearningPhase.PHASE2_MEANING_TO_WORD,
                    totalInRound = phase2Queue.size,
                    currentIndex = 0
                )
                loadNextQuestion()
            }
            LearningPhase.PHASE2_MEANING_TO_WORD -> {
                phase3Queue = sessionWords.shuffled().toMutableList()
                currentQueueIndex = 0
                correctInPhase = 0
                _state.value = _state.value.copy(
                    phase = LearningPhase.PHASE3_SPELL_WORD,
                    totalInRound = phase3Queue.size,
                    currentIndex = 0
                )
                loadNextQuestion()
            }
            LearningPhase.PHASE3_SPELL_WORD -> finishSession()
            LearningPhase.COMPLETED -> {}
        }
    }

    private suspend fun syncReviewToServer(word: WordEntity, familiarity: Float, reviewCount: Int, nextReviewDate: Long) {
        try {
            val token = authPrefs?.token?.first() ?: return
            val bearer = "Bearer $token"
            val wordbookEntity = repository.getWordsByWordbook(word.wordbookId).first()
                .let { repository.getAllWordbooks().first().find { wb -> wb.id == word.wordbookId } } ?: return
            val remoteWordbooks = api.getWordbooks(bearer)
            val remoteWb = remoteWordbooks.find { it.name == wordbookEntity.name } ?: return
            val remoteWords = api.getWords(bearer, remoteWb.id)
            val remoteWord = remoteWords.find { it.english.equals(word.english, ignoreCase = true) } ?: return
            api.updateReviewData(bearer, remoteWb.id, remoteWord.id, familiarity, reviewCount, nextReviewDate)
            Log.d("LearningViewModel", "syncReviewToServer: '${word.english}' familiarity=$familiarity reviewCount=$reviewCount")
        } catch (e: Exception) {
            Log.e("LearningViewModel", "syncReviewToServer failed for '${word.english}': ${e.message}")
        }
    }

    fun getRemainingReviewCount(wordbookId: Int): kotlinx.coroutines.flow.Flow<Int> =
        repository.getDueReviewCount(wordbookId)

    fun resetState() {
        _state.value = LearningSessionState(isLoading = false)
        sessionWords = emptyList()
        allWords = emptyList()
        phase1Queue = mutableListOf()
        phase2Queue = mutableListOf()
        phase3Queue = mutableListOf()
        currentQueueIndex = 0
        correctInPhase = 0
        answerRecords.clear()
    }

    fun toggleStar(wordId: Int, isStarred: Boolean, onSyncToRemote: ((Int, Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                if (isStarred) {
                    repository.unstarWord(wordId)
                } else {
                    repository.starWord(wordId)
                    // 收藏时立即取消已掌握状态
                    repository.markWordAsUnmastered(wordId)
                }
                val newStarred = !isStarred
                // 更新 allWords 和 sessionWords 里的状态
                allWords = allWords.map {
                    if (it.id == wordId) it.copy(isStarred = newStarred) else it
                }
                sessionWords = sessionWords.map {
                    if (it.id == wordId) it.copy(isStarred = newStarred) else it
                }
                // 刷新当前题目
                val q = _state.value.currentQuestion
                if (q != null && q.word.id == wordId) {
                    _state.value = _state.value.copy(
                        currentQuestion = q.copy(word = q.word.copy(isStarred = newStarred))
                    )
                }
                // 同步到服务端（通过 WordbookViewModel）
                onSyncToRemote?.invoke(wordId, newStarred)
            } catch (e: Exception) {
                Log.e("LearningViewModel", "Error toggling star", e)
            }
        }
    }

    private fun finishSession() {
        viewModelScope.launch {
            try {
                val wordbookId = _state.value.wordbookId
                Log.d("LearningViewModel", "finishSession: sessionWords=${sessionWords.size}, answerRecords=${answerRecords.size}")
                sessionWords.forEach { word ->
                    val records = answerRecords[word.id] ?: emptyList()
                    Log.d("LearningViewModel", "word=${word.english} records=${records.size}")
                    records.forEach { (isCorrect, hesitationMs, phase) ->
                        repository.insertStudyRecord(
                            StudyRecordEntity(
                                wordbookId = wordbookId,
                                wordId = word.id,
                                isCorrect = isCorrect,
                                hesitationMs = hesitationMs,
                                phase = phase
                            )
                        )
                    }
                    // 计算熟悉度并更新复习数据
                    val recentRecords = repository.getRecentRecordsByWord(word.id)
                    val familiarity = ReviewAlgorithm.calcFamiliarity(recentRecords)
                    val newReviewCount = word.reviewCount + 1
                    val nextReviewDate = ReviewAlgorithm.calcNextReviewDate(newReviewCount, familiarity)
                    repository.updateReviewData(word.id, familiarity, newReviewCount, nextReviewDate)
                    // 非难词模式：完成三阶段即标记已掌握
                    if (!starredOnly) {
                        repository.markWordAsMastered(word.id)
                    }
                    // 通知外部同步复习数据到服务端
                    Log.d("LearningViewModel", "onReviewDataUpdated callback: ${onReviewDataUpdated != null}, wordId=${word.id}")
                    onReviewDataUpdated?.invoke(word.id, familiarity, newReviewCount, nextReviewDate)
                    // 直接调用 API 同步到服务端
                    syncReviewToServer(word, familiarity, newReviewCount, nextReviewDate)
                }
                _state.value = _state.value.copy(phase = LearningPhase.COMPLETED)
            } catch (e: Exception) {
                Log.e("LearningViewModel", "Error finishing session", e)
                _state.value = _state.value.copy(phase = LearningPhase.COMPLETED)
            }
        }
    }
}
