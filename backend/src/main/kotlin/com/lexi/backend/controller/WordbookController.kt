package com.lexi.backend.controller

import com.lexi.backend.dto.SyncWordsRequest
import com.lexi.backend.dto.WordbookRequest
import com.lexi.backend.service.WordbookService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/wordbooks")
class WordbookController(private val wordbookService: WordbookService) {

    @GetMapping
    fun getAll(@AuthenticationPrincipal userId: Int) =
        ResponseEntity.ok(wordbookService.getAll(userId))

    @PostMapping
    fun create(
        @AuthenticationPrincipal userId: Int,
        @RequestBody req: WordbookRequest
    ) = ResponseEntity.ok(wordbookService.create(userId, req))

    @DeleteMapping("/{id}")
    fun delete(
        @AuthenticationPrincipal userId: Int,
        @PathVariable id: Int
    ): ResponseEntity<*> {
        return try {
            wordbookService.delete(userId, id)
            ResponseEntity.ok(mapOf("message" to "删除成功"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/{id}/words")
    fun getWords(
        @AuthenticationPrincipal userId: Int,
        @PathVariable id: Int
    ): ResponseEntity<*> {
        return try {
            ResponseEntity.ok(wordbookService.getWords(userId, id))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PostMapping("/{id}/words/sync")
    fun syncWords(
        @AuthenticationPrincipal userId: Int,
        @PathVariable id: Int,
        @RequestBody req: SyncWordsRequest
    ): ResponseEntity<*> {
        return try {
            ResponseEntity.ok(wordbookService.syncWords(userId, id, req.words))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PatchMapping("/{wordbookId}/words/{wordId}/mastered")
    fun updateMastered(
        @AuthenticationPrincipal userId: Int,
        @PathVariable wordbookId: Int,
        @PathVariable wordId: Int,
        @RequestParam mastered: Boolean
    ): ResponseEntity<*> {
        return try {
            wordbookService.updateMastered(userId, wordbookId, wordId, mastered)
            ResponseEntity.ok(mapOf("message" to "更新成功"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @DeleteMapping("/{wordbookId}/words/{wordId}")
    fun deleteWord(
        @AuthenticationPrincipal userId: Int,
        @PathVariable wordbookId: Int,
        @PathVariable wordId: Int
    ): ResponseEntity<*> {
        return try {
            wordbookService.deleteWord(userId, wordbookId, wordId)
            ResponseEntity.ok(mapOf("message" to "删除成功"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}
