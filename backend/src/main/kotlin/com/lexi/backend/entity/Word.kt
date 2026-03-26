package com.lexi.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "words")
data class Word(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "wordbook_id", nullable = false)
    val wordbookId: Int = 0,

    @Column(nullable = false, length = 200)
    val english: String = "",

    @Column(nullable = false, length = 500)
    val chinese: String = "",

    @Column(length = 200)
    val pronunciation: String = "",

    @Column(name = "part_of_speech", length = 50)
    val partOfSpeech: String = "",

    @Column(length = 1000)
    val example: String = "",

    @Column(name = "example_translation", length = 1000)
    val exampleTranslation: String = "",

    @Column(name = "is_mastered", columnDefinition = "boolean default false")
    val isMastered: Boolean = false,

    @Column(name = "is_starred", columnDefinition = "boolean default false")
    val isStarred: Boolean = false,

    // 复习系统字段
    @Column(name = "familiarity", columnDefinition = "float default 0")
    val familiarity: Float = 0f,

    @Column(name = "review_count", columnDefinition = "int default 0")
    val reviewCount: Int = 0,

    @Column(name = "next_review_date", columnDefinition = "bigint default 0")
    val nextReviewDate: Long = 0
)
