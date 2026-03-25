package com.lexi.backend.repository

import com.lexi.backend.entity.StudyPlan
import org.springframework.data.jpa.repository.JpaRepository

interface StudyPlanRepository : JpaRepository<StudyPlan, Int> {
    fun findByUserId(userId: Int): List<StudyPlan>
    fun deleteByUserIdAndWordbookName(userId: Int, wordbookName: String)
}
