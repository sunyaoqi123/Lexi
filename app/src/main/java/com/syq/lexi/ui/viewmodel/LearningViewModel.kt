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
    val currentIndex: Int = 0,
    val totalInRound: Int = 0,       // 本轮题目数（含重复答错的）
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

    fun startSession(wordbookId: Int, wordbookName: String, groupSize: Int = 10) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                allWords = repository.getWordsByWordbook(wordbookId).first()

                if (allWords.size < 4) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "单词本中至少需要4个单词才能开始学习"
                    )
                    return@launch
                }

                // 优先选取未掌握的单词，取前 groupSize 个
                val unmastered = allWords.filter { !it.isMastered }.shuffled()
                sessionWords = if (unmastered.size >= groupSize) {
                    unmastered.take(groupSize)
                } else if (unmastered.isNotEmpty()) {
                    unmastered
                } else {
                    allWords.shuffled().take(groupSize)
                }

                phase1Queue = sessionWords.shuffled().toMutableList()
                phase2Queue = mutableListOf()
                phase3Queue = mutableListOf()
                currentQueueIndex = 0

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
            currentIndex = currentQueueIndex,
            totalInRound = queue.size,
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

        if (!isCorrect) {
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
            isCorrect = isCorrect
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

    private fun markSessionWordsAsMastered() {
        viewModelScope.launch {
            try {
                sessionWords.forEach { word ->
                    repository.markWordAsMastered(word.id)
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
