package com.lexi.backend.repository

import com.lexi.backend.entity.FriendRequest
import com.lexi.backend.entity.FriendRequestStatus
import org.springframework.data.jpa.repository.JpaRepository

interface FriendRequestRepository : JpaRepository<FriendRequest, Int> {
    fun findByIdAndToUserId(id: Int, toUserId: Int): FriendRequest?
    fun findByFromUserIdOrderByCreatedAtDesc(fromUserId: Int): List<FriendRequest>
    fun findByToUserIdAndStatusOrderByCreatedAtDesc(toUserId: Int, status: FriendRequestStatus): List<FriendRequest>
    fun findByFromUserIdAndToUserIdAndStatus(fromUserId: Int, toUserId: Int, status: FriendRequestStatus): FriendRequest?
}
