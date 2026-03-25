package com.lexi.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "system_wordbooks")
data class SystemWordbook(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(nullable = false, length = 100)
    val name: String = "",

    @Column(length = 50)
    val category: String = "",

    @Column(length = 500)
    val description: String = ""
)
