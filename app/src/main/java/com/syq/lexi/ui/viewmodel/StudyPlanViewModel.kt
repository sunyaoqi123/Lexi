package com.syq.lexi.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syq.lexi.data.auth.AuthPreferences
import com.syq.lexi.data.database.StudyPlanEntity
import com.syq.lexi.data.database.StudyPlanDao
import com.syq.lexi.data.database.WordbookEntity
import com.syq.lexi.data.network.RetrofitClient
import com.syq.lexi.data.network.StudyPlanDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StudyPlanViewModel(
    private val studyPlanDao: StudyPlanDao,
    private val context: Context? = null
) : ViewModel() {

    private val authPrefs = context?.let { AuthPreferences(it) }
    private val api = RetrofitClient.api

    private val _plans = MutableStateFlow<List<StudyPlanEntity>>(emptyList())
    val plans: StateFlow<List<StudyPlanEntity>> = _plans.asStateFlow()

    init {
        loadPlans()
    }

    private fun loadPlans() {
        viewModelScope.launch {
            studyPlanDao.getAllPlans().collect { planList ->
                _plans.value = planList
            }
        }
    }

    fun addPlan(wordbook: WordbookEntity, dailyWords: Int) {
        viewModelScope.launch {
            try {
                val plan = StudyPlanEntity(wordbookId = wordbook.id, dailyWords = dailyWords)
                studyPlanDao.insertPlan(plan)
                // 同步到服务端
                val token = authPrefs?.token?.first()
                if (token != null) {
                    api.saveStudyPlan(
                        "Bearer $token",
                        StudyPlanDto(wordbookName = wordbook.name, dailyWords = dailyWords)
                    )
                }
            } catch (e: Exception) {
                Log.e("StudyPlanViewModel", "Error adding plan", e)
            }
        }
    }

    fun deletePlan(plan: StudyPlanEntity, wordbookName: String) {
        viewModelScope.launch {
            try {
                studyPlanDao.deletePlan(plan)
                // 同步到服务端
                val token = authPrefs?.token?.first()
                if (token != null) {
                    api.deleteStudyPlan("Bearer $token", wordbookName)
                }
            } catch (e: Exception) {
                Log.e("StudyPlanViewModel", "Error deleting plan", e)
            }
        }
    }

    // 旧接口兼容，仅用于不需要同步的场景
    fun addPlan(wordbookId: Int, dailyWords: Int) {
        viewModelScope.launch {
            try {
                val plan = StudyPlanEntity(wordbookId = wordbookId, dailyWords = dailyWords)
                studyPlanDao.insertPlan(plan)
            } catch (e: Exception) {
                Log.e("StudyPlanViewModel", "Error adding plan", e)
            }
        }
    }

    fun deletePlan(plan: StudyPlanEntity) {
        viewModelScope.launch {
            try {
                studyPlanDao.deletePlan(plan)
            } catch (e: Exception) {
                Log.e("StudyPlanViewModel", "Error deleting plan", e)
            }
        }
    }
}
