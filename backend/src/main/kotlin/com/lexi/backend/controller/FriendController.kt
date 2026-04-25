package com.lexi.backend.controller

import com.lexi.backend.dto.SendFriendRequestDto
import com.lexi.backend.dto.SendFriendReminderDto
import com.lexi.backend.service.FriendService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/friends")
class FriendController(private val friendService: FriendService) {

    @GetMapping("/search")
    fun searchUser(
        @AuthenticationPrincipal userId: Int,
        @RequestParam username: String
    ): ResponseEntity<*> {
        val user = friendService.searchUser(userId, username)
        return if (user == null) {
            ResponseEntity.ok(mapOf("found" to false))
        } else {
            ResponseEntity.ok(mapOf("found" to true, "user" to user))
        }
    }

    @PostMapping("/requests")
    fun sendRequest(
        @AuthenticationPrincipal userId: Int,
        @RequestBody req: SendFriendRequestDto
    ): ResponseEntity<*> {
        return try {
            friendService.sendRequest(userId, req.targetUsername)
            ResponseEntity.ok(mapOf("message" to "好友申请已发送"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/requests/pending")
    fun pendingRequests(@AuthenticationPrincipal userId: Int): ResponseEntity<*> {
        return ResponseEntity.ok(friendService.pendingRequests(userId))
    }

    @GetMapping("/requests/sent")
    fun sentRequests(@AuthenticationPrincipal userId: Int): ResponseEntity<*> {
        return ResponseEntity.ok(friendService.mySentRequests(userId))
    }

    @PatchMapping("/requests/{id}")
    fun respondRequest(
        @AuthenticationPrincipal userId: Int,
        @PathVariable id: Int,
        @RequestParam accept: Boolean
    ): ResponseEntity<*> {
        return try {
            friendService.respondRequest(userId, id, accept)
            ResponseEntity.ok(mapOf("message" to if (accept) "已接受好友申请" else "已拒绝好友申请"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping
    fun friends(@AuthenticationPrincipal userId: Int): ResponseEntity<*> {
        return ResponseEntity.ok(friendService.friends(userId))
    }

    @PostMapping("/reminders")
    fun sendStudyReminder(
        @AuthenticationPrincipal userId: Int,
        @RequestBody req: SendFriendReminderDto
    ): ResponseEntity<*> {
        return try {
            friendService.sendStudyReminder(userId, req.friendUserId)
            ResponseEntity.ok(mapOf("message" to "提醒已发送"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/reminders/unread")
    fun unreadReminders(@AuthenticationPrincipal userId: Int): ResponseEntity<*> {
        return ResponseEntity.ok(friendService.unreadReminders(userId))
    }

    @PatchMapping("/reminders/{id}/read")
    fun markReminderRead(
        @AuthenticationPrincipal userId: Int,
        @PathVariable id: Int
    ): ResponseEntity<*> {
        return try {
            friendService.markReminderRead(userId, id)
            ResponseEntity.ok(mapOf("message" to "已读"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}
