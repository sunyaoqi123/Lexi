package com.lexi.backend.controller

import com.lexi.backend.dto.LoginRequest
import com.lexi.backend.dto.RegisterRequest
import com.lexi.backend.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    fun register(@RequestBody req: RegisterRequest): ResponseEntity<*> {
        return try {
            ResponseEntity.ok(authService.register(req))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<*> {
        return try {
            ResponseEntity.ok(authService.login(req))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(401).body(mapOf("error" to e.message))
        }
    }
}
