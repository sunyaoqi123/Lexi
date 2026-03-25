package com.lexi.backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "study_plans")
data class StudyPlan(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Int = 0,

    @Column(name = "wordbook_name", nullable = false, length = 100)
    val wordbookName: String = "",

    @Column(name = "daily_words", nullable = false)
    val dailyWords: Int = 10,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
