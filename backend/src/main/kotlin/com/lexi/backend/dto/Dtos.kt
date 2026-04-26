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

data class FriendUserDto(
    val id: Int,
    val username: String
)

data class SendFriendRequestDto(
    val targetUsername: String
)

data class FriendRequestDto(
    val id: Int,
    val fromUserId: Int,
    val fromUsername: String,
    val toUserId: Int,
    val toUsername: String,
    val status: String,
    val createdAt: String
)

data class FriendReminderDto(
    val id: Int,
    val fromUserId: Int,
    val fromUsername: String,
    val toUserId: Int,
    val toUsername: String,
    val message: String,
    val createdAt: String
)

data class SendFriendReminderDto(
    val friendUserId: Int
)

data class GameResultUploadDto(
    val gameKey: String,
    val groupSignature: String,
    val pairCount: Int,
    val elapsedSeconds: Int,
    val errors: Int,
    val accuracy: Float
)

data class FriendLeaderboardEntryDto(
    val userId: Int,
    val username: String,
    val clearedGroups: Int,
    val avgSeconds: Float,
    val accuracy: Float,
    val metricValue: Float,
    val rank: Int
)

data class GameLeaderboardDto(
    val gameKey: String,
    val metric: String,
    val entries: List<FriendLeaderboardEntryDto>,
    val myRank: Int?
)

data class GuessWhatQuestionDto(
    val answer: String,
    val clues: List<String>,
    val clueMeanings: List<String>,
    val answerMeaning: String
)

