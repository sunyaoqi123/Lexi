package com.lexi.backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "friends")
data class FriendRelation(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Int = 0,

    @Column(name = "friend_user_id", nullable = false)
    val friendUserId: Int = 0,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
