package com.lexi.backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "friend_reminders")
data class FriendReminder(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "from_user_id", nullable = false)
    val fromUserId: Int = 0,

    @Column(name = "to_user_id", nullable = false)
    val toUserId: Int = 0,

    @Column(name = "is_read", nullable = false)
    val isRead: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
