package com.syq.lexi.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syq.lexi.data.auth.AuthPreferences
import com.syq.lexi.data.database.LexiDatabase
import com.syq.lexi.data.network.LoginRequest
import com.syq.lexi.data.network.RegisterRequest
import com.syq.lexi.data.network.RetrofitClient
import com.syq.lexi.data.repository.WordbookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val token: String, val username: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val context: Context) : ViewModel() {

    private val authPrefs = AuthPreferences(context)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username.asStateFlow()

    init {
        viewModelScope.launch {
            _token.value = authPrefs.token.first()
            _username.value = authPrefs.username.first()
        }
    }

    fun register(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("用户名和密码不能为空")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val resp = RetrofitClient.api.register(RegisterRequest(username, password))
                authPrefs.saveAuth(resp.token, resp.username)
                _token.value = resp.token
                _username.value = resp.username
                _authState.value = AuthState.Success(resp.token, resp.username)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "注册失败")
            }
        }
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("用户名和密码不能为空")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val resp = RetrofitClient.api.login(LoginRequest(username, password))
                authPrefs.saveAuth(resp.token, resp.username)
                _token.value = resp.token
                _username.value = resp.username
                _authState.value = AuthState.Success(resp.token, resp.username)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "登录失败")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            // 清空本地 Room 数据库（防止多账号数据串）
            clearLocalData()
            authPrefs.clearAuth()
            _token.value = null
            _username.value = null
            _authState.value = AuthState.Idle
        }
    }

    private suspend fun clearLocalData() {
        try {
            val db = LexiDatabase.getDatabase(context)
            val repo = WordbookRepository(
                db.wordbookDao(), db.wordDao(),
                db.studyRecordDao(), db.studyPlanDao()
            )
            val wordbooks = repo.getAllWordbooks().first()
            for (wb in wordbooks) {
                repo.deleteWordsByWordbook(wb.id)
                repo.deleteWordbook(wb)
            }
            repo.deleteAllPlans()
        } catch (e: Exception) {
            android.util.Log.e("AuthViewModel", "Error clearing local data", e)
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
