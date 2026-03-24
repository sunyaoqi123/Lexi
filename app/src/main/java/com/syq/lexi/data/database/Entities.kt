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
    ]
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
    val learnDate: Long = 0,
    val masteredDate: Long = 0
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
    val isCorrect: Boolean = false
)
