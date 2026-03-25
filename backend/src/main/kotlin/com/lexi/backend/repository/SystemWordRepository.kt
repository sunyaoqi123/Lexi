package com.lexi.backend.repository

import com.lexi.backend.entity.SystemWord
import org.springframework.data.jpa.repository.JpaRepository

interface SystemWordRepository : JpaRepository<SystemWord, Int> {
    fun findByWordbookId(wordbookId: Int): List<SystemWord>
}
