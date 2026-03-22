package com.syq.lexi.data.model

// 用户模型
data class User(
    val id: String,
    val username: String,
    val email: String,
    val avatar: String = "",
    val totalWordsLearned: Int = 0,
    val totalWordsMastered: Int = 0,
    val streakDays: Int = 0
)

// 单词模型
data class Word(
    val id: String,
    val english: String,
    val chinese: String,
    val pronunciation: String = "",
    val partOfSpeech: String = "",
    val example: String = "",
    val isMastered: Boolean = false
)

// 背诵计划模型
data class StudyPlanModel(
    val id: String,
    val name: String,
    val dailyWords: Int,
    val totalWords: Int,
    val wordsLearned: Int = 0,
    val createdDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

// 单词本模型
data class WordbookModel(
    val id: String,
    val name: String,
    val category: String,
    val words: List<Word> = emptyList(),
    val masteredCount: Int = 0,
    val createdDate: Long = System.currentTimeMillis()
)

// 游戏记录模型
data class GameRecord(
    val id: String,
    val gameType: String,
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val playDate: Long = System.currentTimeMillis(),
    val duration: Long = 0
)
