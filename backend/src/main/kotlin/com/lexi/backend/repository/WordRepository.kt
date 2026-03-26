package com.lexi.backend.repository

import com.lexi.backend.entity.Word
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface WordRepository : JpaRepository<Word, Int> {
    fun findByWordbookId(wordbookId: Int): List<Word>
    fun deleteByWordbookId(wordbookId: Int)
    fun findByWordbookIdAndEnglishIgnoreCase(wordbookId: Int, english: String): Word?

    @Modifying
    @Query("UPDATE Word w SET w.isMastered = :mastered WHERE w.id = :id")
    fun updateMastered(id: Int, mastered: Boolean): Int

    @Modifying
    @Query("UPDATE Word w SET w.isStarred = :starred WHERE w.id = :id")
    fun updateStarred(id: Int, starred: Boolean): Int

    @Modifying
    @Query("UPDATE Word w SET w.familiarity = :familiarity, w.reviewCount = :reviewCount, w.nextReviewDate = :nextReviewDate WHERE w.id = :id")
    fun updateReviewData(id: Int, familiarity: Float, reviewCount: Int, nextReviewDate: Long): Int

    fun findByWordbookIdAndIsStarredTrue(wordbookId: Int): List<Word>
}
