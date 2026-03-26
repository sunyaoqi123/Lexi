package com.lexi.backend.repository

import com.lexi.backend.entity.StudyRecord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StudyRecordRepository : JpaRepository<StudyRecord, Int> {
    fun findByUserIdAndWordId(userId: Int, wordId: Int): List<StudyRecord>
    fun findByUserIdOrderByStudyDateDesc(userId: Int): List<StudyRecord>
}
