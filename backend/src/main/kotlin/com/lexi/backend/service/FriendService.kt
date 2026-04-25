package com.lexi.backend.service

import com.lexi.backend.dto.FriendRequestDto
import com.lexi.backend.dto.FriendReminderDto
import com.lexi.backend.dto.FriendUserDto
import com.lexi.backend.entity.FriendRelation
import com.lexi.backend.entity.FriendReminder
import com.lexi.backend.entity.FriendRequest
import com.lexi.backend.entity.FriendRequestStatus
import com.lexi.backend.repository.FriendRelationRepository
import com.lexi.backend.repository.FriendReminderRepository
import com.lexi.backend.repository.FriendRequestRepository
import com.lexi.backend.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter

@Service
class FriendService(
    private val userRepository: UserRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val friendRelationRepository: FriendRelationRepository,
    private val friendReminderRepository: FriendReminderRepository
) {
    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun searchUser(currentUserId: Int, username: String): FriendUserDto? {
        val target = userRepository.findByUsername(username).orElse(null) ?: return null
        if (target.id == currentUserId) return null
        return FriendUserDto(target.id, target.username)
    }

    @Transactional
    fun sendRequest(currentUserId: Int, targetUsername: String) {
        val target = userRepository.findByUsername(targetUsername).orElseThrow {
            IllegalArgumentException("未找到该用户")
        }
        if (target.id == currentUserId) throw IllegalArgumentException("不能添加自己为好友")

        val alreadyFriends = friendRelationRepository.existsByUserIdAndFriendUserId(currentUserId, target.id)
        if (alreadyFriends) throw IllegalArgumentException("你们已经是好友")

        val existingPending = friendRequestRepository.findByFromUserIdAndToUserIdAndStatus(
            currentUserId,
            target.id,
            FriendRequestStatus.PENDING
        )
        if (existingPending != null) throw IllegalArgumentException("好友申请已发送，请等待对方处理")

        friendRequestRepository.save(
            FriendRequest(
                fromUserId = currentUserId,
                toUserId = target.id,
                status = FriendRequestStatus.PENDING
            )
        )
    }

    fun pendingRequests(currentUserId: Int): List<FriendRequestDto> {
        return friendRequestRepository.findByToUserIdAndStatusOrderByCreatedAtDesc(currentUserId, FriendRequestStatus.PENDING)
            .map { toDto(it) }
    }

    fun mySentRequests(currentUserId: Int): List<FriendRequestDto> {
        return friendRequestRepository.findByFromUserIdOrderByCreatedAtDesc(currentUserId).map { toDto(it) }
    }

    fun friends(currentUserId: Int): List<FriendUserDto> {
        val rels = friendRelationRepository.findByUserIdOrderByCreatedAtDesc(currentUserId)
        return rels.mapNotNull { rel ->
            userRepository.findById(rel.friendUserId).orElse(null)?.let { FriendUserDto(it.id, it.username) }
        }
    }

    @Transactional
    fun sendStudyReminder(currentUserId: Int, friendUserId: Int) {
        val isFriend = friendRelationRepository.existsByUserIdAndFriendUserId(currentUserId, friendUserId)
        if (!isFriend) throw IllegalArgumentException("对方不是你的好友")
        if (currentUserId == friendUserId) throw IllegalArgumentException("不能提醒自己")

        friendReminderRepository.save(FriendReminder(fromUserId = currentUserId, toUserId = friendUserId))
    }

    fun unreadReminders(currentUserId: Int): List<FriendReminderDto> {
        return friendReminderRepository.findByToUserIdAndIsReadOrderByCreatedAtDesc(currentUserId, false)
            .map { reminder ->
                val fromUsername = userRepository.findById(reminder.fromUserId).orElse(null)?.username ?: "好友"
                val toUsername = userRepository.findById(reminder.toUserId).orElse(null)?.username ?: "你"
                FriendReminderDto(
                    id = reminder.id,
                    fromUserId = reminder.fromUserId,
                    fromUsername = fromUsername,
                    toUserId = reminder.toUserId,
                    toUsername = toUsername,
                    message = "${fromUsername}提醒你该背单词啦",
                    createdAt = reminder.createdAt.format(fmt)
                )
            }
    }

    @Transactional
    fun markReminderRead(currentUserId: Int, reminderId: Int) {
        val reminder = friendReminderRepository.findByIdAndToUserId(reminderId, currentUserId)
            ?: throw IllegalArgumentException("提醒不存在")
        if (!reminder.isRead) {
            friendReminderRepository.save(reminder.copy(isRead = true))
        }
    }

    @Transactional
    fun respondRequest(currentUserId: Int, requestId: Int, accept: Boolean) {
        val req = friendRequestRepository.findByIdAndToUserId(requestId, currentUserId)
            ?: throw IllegalArgumentException("好友申请不存在")
        if (req.status != FriendRequestStatus.PENDING) {
            throw IllegalArgumentException("该申请已处理")
        }

        val newStatus = if (accept) FriendRequestStatus.ACCEPTED else FriendRequestStatus.REJECTED
        friendRequestRepository.save(req.copy(status = newStatus, updatedAt = java.time.LocalDateTime.now()))

        if (accept) {
            if (!friendRelationRepository.existsByUserIdAndFriendUserId(req.fromUserId, req.toUserId)) {
                friendRelationRepository.save(FriendRelation(userId = req.fromUserId, friendUserId = req.toUserId))
            }
            if (!friendRelationRepository.existsByUserIdAndFriendUserId(req.toUserId, req.fromUserId)) {
                friendRelationRepository.save(FriendRelation(userId = req.toUserId, friendUserId = req.fromUserId))
            }
        }
    }

    private fun toDto(req: FriendRequest): FriendRequestDto {
        val fromUsername = userRepository.findById(req.fromUserId).orElse(null)?.username ?: ""
        val toUsername = userRepository.findById(req.toUserId).orElse(null)?.username ?: ""
        return FriendRequestDto(
            id = req.id,
            fromUserId = req.fromUserId,
            fromUsername = fromUsername,
            toUserId = req.toUserId,
            toUsername = toUsername,
            status = req.status.name,
            createdAt = req.createdAt.format(fmt)
        )
    }
}
