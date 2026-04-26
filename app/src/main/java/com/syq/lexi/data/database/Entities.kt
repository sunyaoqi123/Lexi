package com.syq.lexi.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(tableName = "wordbooks")
data class WordbookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: String,
    val description: String = "",
    val totalWords: Int = 0,
    val createdDate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "words",
    foreignKeys = [
        ForeignKey(
            entity = WordbookEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordbookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [androidx.room.Index(value = ["wordbookId", "english"], unique = true)]
)
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val wordbookId: Int,
    val english: String,
    val chinese: String,
    val pronunciation: String = "",
    val partOfSpeech: String = "",
    val example: String = "",
    val exampleTranslation: String = "",
    val isMastered: Boolean = false,
    val isStarred: Boolean = false,
    val learnDate: Long = 0,
    val masteredDate: Long = 0,
    // 复习系统字段
    val familiarity: Float = 0f,        // 熟悉度 0~1
    val reviewCount: Int = 0,           // 已复习次数
    val nextReviewDate: Long = 0        // 下次复习时间（epoch ms，0=未安排）
)

@Entity(tableName = "study_plans")
data class StudyPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val wordbookId: Int,
    val dailyWords: Int,       // 每天背多少个
    val createdDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_records")
data class StudyRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val wordbookId: Int,
    val wordId: Int,
    val studyDate: Long = System.currentTimeMillis(),
    val reviewCount: Int = 0,
    val isCorrect: Boolean = false,
    // 复习系统字段
    val phase: Int = 0,             // 0=Phase1, 1=Phase2, 2=Phase3
    val hesitationMs: Long = 0,     // 题目出现到用户选择的时间差(ms)
    val durationMs: Long = 0        // 本次学习总用时(ms)
)

@Entity(tableName = "guess_what_questions")
data class GuessWhatQuestionEntity(
    @PrimaryKey
    val answer: String,
    val answerMeaning: String,
    val cluesBlob: String,
    val clueMeaningsBlob: String,
    val updatedAt: Long = System.currentTimeMillis()
)

