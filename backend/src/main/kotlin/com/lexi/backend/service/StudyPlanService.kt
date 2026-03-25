package com.lexi.backend.service

import com.lexi.backend.entity.StudyPlan
import com.lexi.backend.repository.StudyPlanRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class StudyPlanService(private val studyPlanRepository: StudyPlanRepository) {

    fun getAll(userId: Int): List<StudyPlan> =
        studyPlanRepository.findByUserId(userId)

    @Transactional
    fun save(userId: Int, wordbookName: String, dailyWords: Int): StudyPlan {
        // 同一用户同一单词本只保留一条计划
        studyPlanRepository.deleteByUserIdAndWordbookName(userId, wordbookName)
        return studyPlanRepository.save(
            StudyPlan(userId = userId, wordbookName = wordbookName, dailyWords = dailyWords)
        )
    }

    @Transactional
    fun delete(userId: Int, wordbookName: String) {
        studyPlanRepository.deleteByUserIdAndWordbookName(userId, wordbookName)
    }
}
