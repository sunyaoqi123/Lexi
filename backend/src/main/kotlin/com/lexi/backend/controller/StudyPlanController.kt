package com.lexi.backend.controller

import com.lexi.backend.config.JwtUtil
import com.lexi.backend.service.StudyPlanService
import org.springframework.web.bind.annotation.*

data class StudyPlanDto(
    val id: Int = 0,
    val wordbookName: String,
    val dailyWords: Int
)

@RestController
@RequestMapping("/api/study-plans")
class StudyPlanController(
    private val studyPlanService: StudyPlanService,
    private val jwtUtil: JwtUtil
) {
    private fun userId(token: String) =
        jwtUtil.getUserIdFromToken(token.removePrefix("Bearer ").trim()) ?: throw IllegalArgumentException("无效token")

    @GetMapping
    fun getAll(@RequestHeader("Authorization") token: String): List<StudyPlanDto> {
        val uid = userId(token)
        return studyPlanService.getAll(uid).map {
            StudyPlanDto(it.id, it.wordbookName, it.dailyWords)
        }
    }

    @PostMapping
    fun save(
        @RequestHeader("Authorization") token: String,
        @RequestBody req: StudyPlanDto
    ): StudyPlanDto {
        val uid = userId(token)
        val saved = studyPlanService.save(uid, req.wordbookName, req.dailyWords)
        return StudyPlanDto(saved.id, saved.wordbookName, saved.dailyWords)
    }

    @DeleteMapping
    fun delete(
        @RequestHeader("Authorization") token: String,
        @RequestParam wordbookName: String
    ) {
        val uid = userId(token)
        studyPlanService.delete(uid, wordbookName)
    }
}
