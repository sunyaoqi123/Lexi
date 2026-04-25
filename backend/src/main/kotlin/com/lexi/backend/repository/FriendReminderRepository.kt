package com.lexi.backend.repository

import com.lexi.backend.entity.FriendReminder
import org.springframework.data.jpa.repository.JpaRepository

interface FriendReminderRepository : JpaRepository<FriendReminder, Int> {
    fun findByToUserIdAndIsReadOrderByCreatedAtDesc(toUserId: Int, isRead: Boolean): List<FriendReminder>
    fun findByIdAndToUserId(id: Int, toUserId: Int): FriendReminder?
}
