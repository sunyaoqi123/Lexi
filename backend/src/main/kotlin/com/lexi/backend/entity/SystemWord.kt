package com.lexi.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "system_words")
data class SystemWord(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "wordbook_id", nullable = false)
    val wordbookId: Int = 0,

    @Column(nullable = false, length = 200)
    val english: String = "",

    @Column(nullable = false, length = 500)
    val chinese: String = "",

    @Column(length = 200)
    val pronunciation: String = "",

    @Column(name = "part_of_speech", length = 50)
    val partOfSpeech: String = "",

    @Column(length = 1000)
    val example: String = "",

    @Column(name = "example_translation", length = 1000)
    val exampleTranslation: String = ""
)
