package com.syq.lexi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syq.lexi.ui.viewmodel.FriendViewModel
import com.syq.lexi.notification.DailyReminderManager
import kotlinx.coroutines.delay

data class Game(val id: Int, val name: String, val description: String, val icon: String, val color: Color)
private enum class FriendPage { GAME, FRIENDS, NEW_FRIENDS }

@Composable
fun GameScreen(onMenuClick: () -> Unit, innerPadding: PaddingValues, friendViewModel: FriendViewModel) {
    val st by friendViewModel.state.collectAsState()
    val context = LocalContext.current
    val games = remember {
        listOf(
            Game(1, "单词拼写", "根据中文意思拼写英文单词", "✏️", Color(0xFF6366F1)),
            Game(2, "单词匹配", "将单词与其定义进行匹配", "🎯", Color(0xFF8B5CF6)),
            Game(3, "听音识词", "根据发音选择正确的单词", "🎧", Color(0xFFEC4899)),
            Game(4, "单词接龙", "根据前一个单词的末尾字母开始新单词", "🔗", Color(0xFFF59E0B)),
            Game(5, "快速反应", "在规定时间内选择正确的单词含义", "⚡", Color(0xFF10B981)),
            Game(6, "单词填空", "在句子中填入正确的单词", "📝", Color(0xFF3B82F6))
        )
    }
    var page by remember { mutableStateOf(FriendPage.GAME) }
    var showAdd by remember { mutableStateOf(false) }
    var remindTarget by remember { mutableStateOf<Pair<Int, String>?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            friendViewModel.refreshAll()
            friendViewModel.pollUnreadReminders { reminder ->
                DailyReminderManager.showFriendStudyReminderNotification(context, reminder.message)
            }
            delay(15000)
        }
    }

    if (showAdd) {
        AddFriendDialog(
            input = st.searchInput,
            onInputChange = friendViewModel::updateSearchInput,
            onSearch = friendViewModel::searchUser,
            onSendRequest = friendViewModel::sendRequest,
            onDismiss = { showAdd = false; friendViewModel.clearTips() },
            foundUsername = st.searchResult?.username,
            message = st.message,
            error = st.error
        )
    }

    remindTarget?.let { (friendId, friendName) ->
        AlertDialog(
            onDismissRequest = { remindTarget = null },
            title = { Text("确认提醒") },
            text = { Text("确定提醒 $friendName 背单词吗？") },
            confirmButton = {
                Button(onClick = {
                    friendViewModel.sendStudyReminder(friendId, friendName)
                    remindTarget = null
                }) { Text("确定") }
            },
            dismissButton = {
                OutlinedButton(onClick = { remindTarget = null }) { Text("取消") }
            }
        )
    }

    Column(Modifier.fillMaxSize().padding(innerPadding).background(MaterialTheme.colorScheme.background)) {
        when (page) {
            FriendPage.GAME -> {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, contentDescription = "菜单") }
                    Text("趣味游戏", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Box(Modifier.size(48.dp), contentAlignment = Alignment.TopEnd) {
                        IconButton(onClick = { page = FriendPage.FRIENDS }) { Icon(Icons.Default.AccountCircle, contentDescription = "好友") }
                        if (st.pendingCount > 0) Box(Modifier.padding(top = 10.dp, end = 10.dp).size(10.dp).background(Color(0xFFE53935), CircleShape))
                    }
                }
                Spacer(Modifier.height(16.dp))
                LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                    items(games) { GameCard(it) }
                }
            }
            FriendPage.FRIENDS -> {
                Header("我的好友", onBack = { page = FriendPage.GAME }) { IconButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, contentDescription = "加好友") } }
                if (!st.error.isNullOrBlank()) Text(st.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
                if (!st.message.isNullOrBlank()) Text(st.message!!, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp))

                val hasNew = st.pendingRequests.isNotEmpty() || st.sentRequests.isNotEmpty()
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable { page = FriendPage.NEW_FRIENDS }, shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("新的朋友", fontSize = 16.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (hasNew) Box(Modifier.size(8.dp).background(Color(0xFFE53935), CircleShape))
                            Text("查看", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Text("好友列表", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                if (st.friends.isEmpty()) {
                    Text("还没有好友，点击右上角 + 添加好友", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                } else {
                    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                        items(st.friends) { f ->
                            Card(shape = RoundedCornerShape(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(f.username)
                                    OutlinedButton(onClick = { remindTarget = f.id to f.username }) {
                                        Text("提醒背单词")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            FriendPage.NEW_FRIENDS -> {
                Header("新的朋友", onBack = { page = FriendPage.FRIENDS }) { Spacer(Modifier.size(48.dp)) }
                if (!st.error.isNullOrBlank()) Text(st.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
                if (!st.message.isNullOrBlank()) Text(st.message!!, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp))

                LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                    item { Text("收到的好友申请", fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(6.dp)) }
                    if (st.pendingRequests.isEmpty()) item { Text("暂无收到的申请", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    items(st.pendingRequests) { r ->
                        Card(shape = RoundedCornerShape(10.dp)) {
                            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                Text(r.fromUsername, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { friendViewModel.respondRequest(r.id, false) }) { Text("拒绝") }
                                    Button(onClick = { friendViewModel.respondRequest(r.id, true) }) { Text("接受") }
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)); Text("发出的好友申请", fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(6.dp)) }
                    if (st.sentRequests.isEmpty()) item { Text("暂无发出的申请", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    items(st.sentRequests) { r ->
                        val s = when (r.status) { "PENDING" -> "待处理"; "ACCEPTED" -> "已通过"; "REJECTED" -> "已拒绝"; else -> r.status }
                        Card(shape = RoundedCornerShape(10.dp)) { Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(r.toUsername); Text(s, color = MaterialTheme.colorScheme.primary) } }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(title: String, onBack: () -> Unit, right: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "返回") }
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        right()
    }
}

@Composable
private fun AddFriendDialog(
    input: String,
    onInputChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSendRequest: () -> Unit,
    onDismiss: () -> Unit,
    foundUsername: String?,
    message: String?,
    error: String?
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(20.dp)) {
                Text("添加好友", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = input, onValueChange = onInputChange, singleLine = true, label = { Text("输入用户名") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp)); Button(onClick = onSearch, modifier = Modifier.fillMaxWidth()) { Text("搜索用户") }
                if (foundUsername != null) {
                    Spacer(Modifier.height(10.dp)); Text("找到用户：$foundUsername", color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp)); Button(onClick = onSendRequest, modifier = Modifier.fillMaxWidth()) { Text("发送好友申请") }
                }
                if (!error.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                if (!message.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text(message, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp) }
                Spacer(Modifier.height(12.dp)); OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("关闭") }
            }
        }
    }
}

@Composable
fun GameCard(game: Game) {
    Card(modifier = Modifier.fillMaxWidth().height(180.dp).clickable { }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = game.color)) {
        Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(game.icon, fontSize = 48.sp, modifier = Modifier.padding(bottom = 12.dp))
            Text(game.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(game.description, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center, maxLines = 2)
        }
    }
}
