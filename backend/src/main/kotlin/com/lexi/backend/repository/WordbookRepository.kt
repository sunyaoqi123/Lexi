package com.lexi.backend.repository

import com.lexi.backend.entity.Wordbook
import org.springframework.data.jpa.repository.JpaRepository

interface WordbookRepository : JpaRepository<Wordbook, Int> {
    fun findByUserId(userId: Int): List<Wordbook>
    fun findByIdAndUserId(id: Int, userId: Int): Wordbook?
}
