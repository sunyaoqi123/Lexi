package com.syq.lexi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syq.lexi.data.database.StudyPlanEntity
import com.syq.lexi.data.database.StudyPlanDao
import com.syq.lexi.data.database.WordbookEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudyPlanViewModel(private val studyPlanDao: StudyPlanDao) : ViewModel() {

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

    fun addPlan(wordbookId: Int, dailyWords: Int) {
        viewModelScope.launch {
            try {
                val plan = StudyPlanEntity(
                    wordbookId = wordbookId,
                    dailyWords = dailyWords
                )
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
