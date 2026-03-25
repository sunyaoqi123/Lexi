package com.syq.lexi.data.network

data class RegisterRequest(val username: String, val password: String)
data class LoginRequest(val username: String, val password: String)
data class AuthResponse(val token: String, val username: String)

data class WordbookDto(
    val id: Int = 0,
    val name: String,
    val category: String = "",
    val description: String = "",
    val totalWords: Int = 0
)

data class WordDto(
    val id: Int = 0,
    val english: String,
    val chinese: String,
    val pronunciation: String = "",
    val partOfSpeech: String = "",
    val example: String = "",
    val exampleTranslation: String = "",
    val isMastered: Boolean = false,
    val isStarred: Boolean = false
)

data class SyncWordsRequest(val words: List<WordDto>)

data class SyncResult(val added: Int, val merged: Int, val skipped: Int)

data class StudyPlanDto(
    val id: Int = 0,
    val wordbookName: String,
    val dailyWords: Int
)
