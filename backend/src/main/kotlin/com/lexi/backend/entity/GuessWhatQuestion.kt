package com.lexi.backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "guess_what_questions")
data class GuessWhatQuestion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(nullable = false, unique = true, length = 80)
    val answer: String = "",

    @Column(name = "answer_meaning", nullable = false, length = 255)
    val answerMeaning: String = "",

    @Column(name = "clues_json", nullable = false, columnDefinition = "TEXT")
    val cluesJson: String = "[]",

    @Column(name = "clue_meanings_json", nullable = false, columnDefinition = "TEXT")
    val clueMeaningsJson: String = "[]",

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
