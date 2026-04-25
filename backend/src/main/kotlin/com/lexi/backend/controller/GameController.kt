package com.lexi.backend.controller

import com.lexi.backend.dto.GameResultUploadDto
import com.lexi.backend.service.FriendService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/games")
class GameController(private val friendService: FriendService) {

    @PostMapping("/results")
    fun uploadGameResult(
        @AuthenticationPrincipal userId: Int,
        @RequestBody req: GameResultUploadDto
    ): ResponseEntity<*> {
        return try {
            friendService.uploadGameResult(userId, req)
            ResponseEntity.ok(mapOf("message" to "结果已记录"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/leaderboard")
    fun gameLeaderboard(
        @AuthenticationPrincipal userId: Int,
        @RequestParam gameKey: String,
        @RequestParam metric: String
    ): ResponseEntity<*> {
        return ResponseEntity.ok(friendService.gameLeaderboard(userId, gameKey, metric))
    }
}
