package com.lexi.backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

enum class FriendRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

@Entity
@Table(name = "friend_requests")
data class FriendRequest(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "from_user_id", nullable = false)
    val fromUserId: Int = 0,

    @Column(name = "to_user_id", nullable = false)
    val toUserId: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val status: FriendRequestStatus = FriendRequestStatus.PENDING,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
