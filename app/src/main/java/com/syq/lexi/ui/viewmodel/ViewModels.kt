package com.syq.lexi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import com.syq.lexi.data.model.StudyPlanModel
import com.syq.lexi.data.model.User

class HomeViewModel : ViewModel() {
    val studyPlans = mutableStateOf<List<StudyPlanModel>>(
        listOf(
            StudyPlanModel(
                id = "1",
                name = "高考词汇",
                dailyWords = 20,
                totalWords = 3500,
                wordsLearned = 1575
            ),
            StudyPlanModel(
                id = "2",
                name = "四级词汇",
                dailyWords = 15,
                totalWords = 2500,
                wordsLearned = 750
            ),
            StudyPlanModel(
                id = "3",
                name = "六级词汇",
                dailyWords = 25,
                totalWords = 5500,
                wordsLearned = 3300
            )
        )
    )

    fun addStudyPlan(plan: StudyPlanModel) {
        val currentPlans = studyPlans.value.toMutableList()
        currentPlans.add(plan)
        studyPlans.value = currentPlans
    }

    fun deleteStudyPlan(planId: String) {
        studyPlans.value = studyPlans.value.filter { it.id != planId }
    }
}

class UserViewModel : ViewModel() {
    val currentUser = mutableStateOf<User?>(null)
    val isLoggedIn = mutableStateOf(false)

    fun login(email: String, password: String) {
        // TODO: 实现登录逻辑
        currentUser.value = User(
            id = "user_1",
            username = "用户名",
            email = email,
            totalWordsLearned = 1234,
            totalWordsMastered = 567,
            streakDays = 15
        )
        isLoggedIn.value = true
    }

    fun logout() {
        currentUser.value = null
        isLoggedIn.value = false
    }

    fun register(username: String, email: String, password: String) {
        // TODO: 实现注册逻辑
        currentUser.value = User(
            id = "user_${System.currentTimeMillis()}",
            username = username,
            email = email
        )
        isLoggedIn.value = true
    }
}
