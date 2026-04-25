package com.syq.lexi.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syq.lexi.data.auth.AuthPreferences
import com.syq.lexi.data.network.FriendReminderDto
import com.syq.lexi.data.network.FriendRequestDto
import com.syq.lexi.data.network.FriendUserDto
import com.syq.lexi.data.network.RetrofitClient
import com.syq.lexi.data.network.SendFriendReminderDto
import com.syq.lexi.data.network.SendFriendRequestDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class FriendUiState(
    val searchInput: String = "",
    val searchResult: FriendUserDto? = null,
    val isSearching: Boolean = false,
    val pendingRequests: List<FriendRequestDto> = emptyList(),
    val sentRequests: List<FriendRequestDto> = emptyList(),
    val friends: List<FriendUserDto> = emptyList(),
    val unreadReminders: List<FriendReminderDto> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
) {
    val pendingCount: Int get() = pendingRequests.size
}

class FriendViewModel(private val context: Context) : ViewModel() {
    private val authPrefs = AuthPreferences(context)
    private val api = RetrofitClient.api

    private val _state = MutableStateFlow(FriendUiState())
    val state: StateFlow<FriendUiState> = _state.asStateFlow()

    init {
        refreshAll()
    }

    fun updateSearchInput(value: String) {
        _state.value = _state.value.copy(searchInput = value, error = null, message = null)
    }

    fun refreshAll() {
        viewModelScope.launch {
            val token = authPrefs.token.first() ?: return@launch
            val bearer = authPrefs.bearerToken(token)
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val pending = api.getPendingFriendRequests(bearer)
                val sent = api.getSentFriendRequests(bearer)
                val friends = api.getFriends(bearer)
                val reminders = api.getUnreadFriendReminders(bearer)
                _state.value = _state.value.copy(
                    isLoading = false,
                    pendingRequests = pending,
                    sentRequests = sent,
                    friends = friends,
                    unreadReminders = reminders
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "加载好友数据失败")
            }
        }
    }

    fun searchUser() {
        val username = _state.value.searchInput.trim()
        if (username.isBlank()) {
            _state.value = _state.value.copy(error = "请输入用户名")
            return
        }
        viewModelScope.launch {
            val token = authPrefs.token.first() ?: return@launch
            val bearer = authPrefs.bearerToken(token)
            try {
                _state.value = _state.value.copy(isSearching = true, error = null, message = null)
                val resp = api.searchFriendUser(bearer, username)
                _state.value = _state.value.copy(
                    isSearching = false,
                    searchResult = if (resp.found) resp.user else null,
                    error = if (resp.found) null else "未找到该用户"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSearching = false, error = e.message ?: "搜索失败")
            }
        }
    }

    fun sendRequest() {
        val target = _state.value.searchResult ?: run {
            _state.value = _state.value.copy(error = "请先搜索并选择用户")
            return
        }
        viewModelScope.launch {
            val token = authPrefs.token.first() ?: return@launch
            val bearer = authPrefs.bearerToken(token)
            try {
                api.sendFriendRequest(bearer, SendFriendRequestDto(target.username))
                _state.value = _state.value.copy(message = "好友申请已发送", error = null, searchResult = null)
                refreshAll()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "发送好友申请失败")
            }
        }
    }

    fun sendStudyReminder(friendUserId: Int, friendUsername: String) {
        viewModelScope.launch {
            val token = authPrefs.token.first() ?: return@launch
            val bearer = authPrefs.bearerToken(token)
            try {
                api.sendFriendStudyReminder(bearer, SendFriendReminderDto(friendUserId))
                _state.value = _state.value.copy(message = "已提醒 $friendUsername 背单词", error = null)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "发送提醒失败")
            }
        }
    }

    fun pollUnreadReminders(onReminder: (FriendReminderDto) -> Unit) {
        viewModelScope.launch {
            val token = authPrefs.token.first() ?: return@launch
            val bearer = authPrefs.bearerToken(token)
            try {
                val reminders = api.getUnreadFriendReminders(bearer)
                reminders.forEach { reminder ->
                    onReminder(reminder)
                    api.markFriendReminderRead(bearer, reminder.id)
                }
                if (reminders.isNotEmpty()) {
                    refreshAll()
                }
            } catch (_: Exception) {
            }
        }
    }

    fun respondRequest(requestId: Int, accept: Boolean) {
        viewModelScope.launch {
            val token = authPrefs.token.first() ?: return@launch
            val bearer = authPrefs.bearerToken(token)
            try {
                api.respondFriendRequest(bearer, requestId, accept)
                _state.value = _state.value.copy(message = if (accept) "已接受好友申请" else "已拒绝好友申请", error = null)
                refreshAll()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "处理申请失败")
            }
        }
    }

    fun clearTips() {
        _state.value = _state.value.copy(message = null, error = null)
    }
}
