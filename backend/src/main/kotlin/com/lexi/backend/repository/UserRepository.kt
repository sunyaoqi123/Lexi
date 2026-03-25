package com.lexi.backend.repository

import com.lexi.backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Int> {
    fun findByUsername(username: String): Optional<User>
    fun existsByUsername(username: String): Boolean
}
