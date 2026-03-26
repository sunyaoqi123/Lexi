package com.lexi.backend.dto

data class RegisterRequest(val username: String, val password: String)
data class LoginRequest(val username: String, val password: String)
data class AuthResponse(val token: String, val username: String)

data class WordbookRequest(
    val name: String,
    val category: String = "",
    val description: String = ""
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
    val isStarred: Boolean = false,
    // 复习系统字段
    val familiarity: Float = 0f,
    val reviewCount: Int = 0,
    val nextReviewDate: Long = 0
)

data class SyncWordsRequest(val words: List<WordDto>)

data class SyncResult(
    val added: Int,
    val merged: Int,
    val skipped: Int
)
