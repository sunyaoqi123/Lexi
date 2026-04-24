package com.syq.lexi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syq.lexi.ui.viewmodel.AuthViewModel
import com.syq.lexi.ui.viewmodel.SyncState
import com.syq.lexi.ui.viewmodel.SyncViewModel

@Composable
fun DrawerContent(
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    authViewModel: AuthViewModel? = null,
    syncViewModel: SyncViewModel? = null,
    onSummaryClick: () -> Unit = {}
) {
    val username by authViewModel?.username?.collectAsState() ?: remember { mutableStateOf<String?>(null) }
    val token by authViewModel?.token?.collectAsState() ?: remember { mutableStateOf<String?>(null) }
    val isLoggedIn = !token.isNullOrEmpty()
    val syncState by syncViewModel?.syncState?.collectAsState() ?: remember { mutableStateOf<SyncState>(SyncState.Idle) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showLogoutDialog = false }
        ) {
            androidx.compose.material3.Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("退出登录", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(8.dp))
                    Text("确定要退出登录吗？", fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center)
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        androidx.compose.material3.OutlinedButton(
                            onClick = { showLogoutDialog = false },
                            modifier = Modifier.weight(1f)
                        ) { Text("取消", fontSize = 15.sp) }
                        androidx.compose.material3.Button(
                            onClick = { showLogoutDialog = false; authViewModel?.logout() },
                            modifier = Modifier.weight(1f),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) { Text("确定", fontSize = 15.sp, color = Color.White) }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        if (isLoggedIn) {
            UserProfile(username = username ?: "")
            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))
        } else {
            GuestSection()
            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 同步按钮（仅登录后显示）
        if (isLoggedIn && syncViewModel != null) {
            val isSyncing = syncState is SyncState.Syncing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isSyncing) { syncViewModel.syncAll() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "同步",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        if (isSyncing) "同步中..." else "同步单词库",
                        fontSize = 16.sp,
                        color = if (isSyncing) MaterialTheme.colorScheme.onSurfaceVariant
                               else MaterialTheme.colorScheme.primary
                    )
                    when (val s = syncState) {
                        is SyncState.Success -> Text(
                            "上次同步: ${s.wordbooksSynced}本 ${s.wordsSynced}词",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        is SyncState.Error -> Text(
                            "同步失败: ${s.message}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                        else -> {}
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        DrawerMenuItem(icon = Icons.Default.Refresh, label = "学习总结", onClick = onSummaryClick)
        Spacer(modifier = Modifier.height(8.dp))
        DrawerMenuItem(icon = Icons.Default.Settings, label = "设置", onClick = {})
        Spacer(modifier = Modifier.height(8.dp))
        DrawerMenuItem(icon = Icons.Default.Info, label = "关于", onClick = {})
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "深色模式",
                    modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface)
                Text("深色模式", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Switch(checked = isDarkTheme, onCheckedChange = { onToggleDarkTheme() })
        }

        Spacer(modifier = Modifier.weight(1f))

        if (isLoggedIn) {
            DrawerMenuItem(
                icon = Icons.Default.ExitToApp,
                label = "退出登录",
                onClick = { showLogoutDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun UserProfile(username: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.size(80.dp).background(
                color = MaterialTheme.colorScheme.primary, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AccountCircle, contentDescription = "用户头像",
                modifier = Modifier.size(60.dp), tint = Color.White)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(username, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text("已登录", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun GuestSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.size(80.dp).background(
                color = MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AccountCircle, contentDescription = "未登录",
                modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("未登录", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun DrawerMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
