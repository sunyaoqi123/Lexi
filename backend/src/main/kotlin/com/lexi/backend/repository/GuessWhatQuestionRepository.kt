package com.lexi.backend.repository

import com.lexi.backend.entity.GuessWhatQuestion
import org.springframework.data.jpa.repository.JpaRepository

interface GuessWhatQuestionRepository : JpaRepository<GuessWhatQuestion, Int> {
    fun existsByAnswer(answer: String): Boolean
}
