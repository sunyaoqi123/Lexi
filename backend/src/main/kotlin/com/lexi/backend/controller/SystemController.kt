package com.lexi.backend.controller

import com.lexi.backend.entity.SystemWord
import com.lexi.backend.entity.SystemWordbook
import com.lexi.backend.repository.SystemWordRepository
import com.lexi.backend.repository.SystemWordbookRepository
import com.lexi.backend.service.SystemDataService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/system")
class SystemController(
    private val systemWordbookRepository: SystemWordbookRepository,
    private val systemWordRepository: SystemWordRepository,
    private val systemDataService: SystemDataService
) {

    @GetMapping("/wordbooks")
    fun getSystemWordbooks(@AuthenticationPrincipal userId: Int): ResponseEntity<List<SystemWordbook>> {
        return ResponseEntity.ok(systemWordbookRepository.findAll())
    }

    @GetMapping("/wordbooks/{id}/words")
    fun getSystemWords(
        @AuthenticationPrincipal userId: Int,
        @PathVariable id: Int
    ): ResponseEntity<List<SystemWord>> {
        return ResponseEntity.ok(systemWordRepository.findByWordbookId(id))
    }
}
