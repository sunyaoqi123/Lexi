package com.lexi.backend.repository

import com.lexi.backend.entity.FriendRelation
import org.springframework.data.jpa.repository.JpaRepository

interface FriendRelationRepository : JpaRepository<FriendRelation, Int> {
    fun findByUserIdOrderByCreatedAtDesc(userId: Int): List<FriendRelation>
    fun existsByUserIdAndFriendUserId(userId: Int, friendUserId: Int): Boolean
}
