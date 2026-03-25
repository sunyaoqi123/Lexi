package com.lexi.backend.service

import com.lexi.backend.config.JwtUtil
import com.lexi.backend.dto.AuthResponse
import com.lexi.backend.dto.LoginRequest
import com.lexi.backend.dto.RegisterRequest
import com.lexi.backend.entity.User
import com.lexi.backend.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val systemDataService: SystemDataService
) {
    fun register(req: RegisterRequest): AuthResponse {
        if (userRepository.existsByUsername(req.username))
            throw IllegalArgumentException("用户名已存在")
        val user = userRepository.save(
            User(username = req.username, password = passwordEncoder.encode(req.password))
        )
        // 新用户注册后自动初始化系统默认单词本
        systemDataService.initUserData(user.id)
        val token = jwtUtil.generateToken(user.id, user.username)
        return AuthResponse(token, user.username)
    }

    fun login(req: LoginRequest): AuthResponse {
        val user = userRepository.findByUsername(req.username)
            .orElseThrow { IllegalArgumentException("用户名或密码错误") }
        if (!passwordEncoder.matches(req.password, user.password))
            throw IllegalArgumentException("用户名或密码错误")
        // 登录时也检查是否需要初始化（兼容老用户）
        systemDataService.initUserData(user.id)
        val token = jwtUtil.generateToken(user.id, user.username)
        return AuthResponse(token, user.username)
    }
}
