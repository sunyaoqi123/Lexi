package com.lexi.backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "game_results")
data class GameResult(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Int = 0,

    @Column(name = "game_key", nullable = false, length = 50)
    val gameKey: String = "",

    @Column(name = "group_signature", nullable = false, length = 1024)
    val groupSignature: String = "",

    @Column(name = "pair_count", nullable = false)
    val pairCount: Int = 0,

    @Column(name = "elapsed_seconds", nullable = false)
    val elapsedSeconds: Int = 0,

    @Column(name = "errors", nullable = false)
    val errors: Int = 0,

    @Column(name = "accuracy", nullable = false)
    val accuracy: Float = 0f,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
