package com.lexi.backend.repository

import com.lexi.backend.entity.GameResult
import org.springframework.data.jpa.repository.JpaRepository

interface GameResultRepository : JpaRepository<GameResult, Int> {
    fun findByUserIdAndGameKeyAndGroupSignatureOrderByCreatedAtDesc(
        userId: Int,
        gameKey: String,
        groupSignature: String
    ): List<GameResult>

    fun findByUserIdAndGameKeyOrderByCreatedAtDesc(userId: Int, gameKey: String): List<GameResult>
    fun findByUserIdOrderByCreatedAtDesc(userId: Int): List<GameResult>
}
