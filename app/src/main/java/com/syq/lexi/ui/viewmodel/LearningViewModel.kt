package com.syq.lexi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syq.lexi.data.database.WordEntity
import com.syq.lexi.data.repository.WordbookRepository
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

class LearningViewModel(private val repository: WordbookRepository) : ViewModel() {

    private val _state = MutableStateFlow(LearningSessionState())
    val state: StateFlow<LearningSessionState> = _state.asStateFlow()

    // 是否只练习难词模式
    private var starredOnly: Boolean = false

    // 当前10个目标单词
    private var sessionWords: List<WordEntity> = emptyList()
    // 单词本所有单词（用于生成干扰选项）
    private var allWords: List<WordEntity> = emptyList()

    // 各阶段待完成的单词队列（答错的重复）
    private var phase1Queue: MutableList<WordEntity> = mutableListOf()
    private var phase2Queue: MutableList<WordEntity> = mutableListOf()
    private var phase3Queue: MutableList<WordEntity> = mutableListOf()

    // 当前题目在队列中的索引
    private var currentQueueIndex: Int = 0
    // 当前阶段已答对数量（用于进度显示）
    private var correctInPhase: Int = 0

    fun startSession(wordbookId: Int, wordbookName: String, groupSize: Int = 10, starredOnly: Boolean = false) {
        viewModelScope.launch {
            try {
                this@LearningViewModel.starredOnly = starredOnly
                // 立刻清空旧状态，防止闪烁
                _state.value = LearningSessionState(
                    wordbookId = wordbookId,
                    wordbookName = wordbookName,
                    isLoading = true
                )
                allWords = repository.getWordsByWordbook(wordbookId).first()

                if (allWords.size < 4) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "单词本中至少需要4个单词才能开始学习"
                    )
                    return@launch
                }

                // 根据模式选词
                val candidateWords = if (starredOnly) {
                    allWords.filter { it.isStarred }
                } else {
                    allWords.filter { !it.isMastered }.shuffled()
                }

                if (candidateWords.isEmpty()) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = if (starredOnly) "没有收藏的难词" else "没有未掌握的单词"
                    )
                    return@launch
                }

                sessionWords = if (starredOnly) {
                    // 练习难词：groupSize 不能超过难词总数
                    val actualSize = minOf(groupSize, candidateWords.size)
                    candidateWords.shuffled().take(actualSize)
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
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "加载失败：${e.message}"
                )
            }
        }
    }

    private fun loadNextQuestion() {
        val queue = getCurrentQueue()
        if (currentQueueIndex >= queue.size) {
            // 本阶段完成，进入下一阶段
            advancePhase()
            return
        }

        val word = queue[currentQueueIndex]
        val question = buildQuestion(word, _state.value.phase)

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

        if (isCorrect) {
            correctInPhase++
        } else {
            // 答错：加入本阶段队列末尾，下轮重复
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
            LearningPhase.PHASE3_SPELL_WORD -> {
                // 全部完成，标记已掌握
                markSessionWordsAsMastered()
            }
            LearningPhase.COMPLETED -> {}
        }
    }

    fun resetState() {
        _state.value = LearningSessionState(isLoading = false)
        sessionWords = emptyList()
        allWords = emptyList()
        phase1Queue = mutableListOf()
        phase2Queue = mutableListOf()
        phase3Queue = mutableListOf()
        currentQueueIndex = 0
        correctInPhase = 0
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

    private fun markSessionWordsAsMastered() {
        viewModelScope.launch {
            try {
                // 难词模式下不自动标记已掌握
                if (!starredOnly) {
                    sessionWords.forEach { word ->
                        repository.markWordAsMastered(word.id)
                    }
                }
                _state.value = _state.value.copy(
                    phase = LearningPhase.COMPLETED
                )
            } catch (e: Exception) {
                Log.e("LearningViewModel", "Error marking words as mastered", e)
            }
        }
    }
}
