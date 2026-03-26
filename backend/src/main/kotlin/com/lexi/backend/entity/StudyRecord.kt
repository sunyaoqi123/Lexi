package com.lexi.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "study_records")
data class StudyRecord(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Int = 0,

    @Column(name = "wordbook_id", nullable = false)
    val wordbookId: Int = 0,

    @Column(name = "word_id", nullable = false)
    val wordId: Int = 0,

    @Column(name = "study_date", nullable = false)
    val studyDate: Long = System.currentTimeMillis(),

    @Column(name = "is_correct", columnDefinition = "boolean default false")
    val isCorrect: Boolean = false,

    @Column(name = "phase", columnDefinition = "int default 0")
    val phase: Int = 0,

    @Column(name = "hesitation_ms", columnDefinition = "bigint default 0")
    val hesitationMs: Long = 0
)
