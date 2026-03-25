package com.lexi.backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "wordbooks")
data class Wordbook(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Int = 0,

    @Column(nullable = false, length = 100)
    val name: String = "",

    @Column(length = 50)
    val category: String = "",

    @Column(length = 500)
    val description: String = "",

    @Column(name = "total_words")
    val totalWords: Int = 0,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
