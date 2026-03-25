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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syq.lexi.ui.viewmodel.AuthViewModel

@Composable
fun DrawerContent(
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    authViewModel: AuthViewModel? = null
) {
    val username by authViewModel?.username?.collectAsState() ?: androidx.compose.runtime.mutableStateOf(null).let {
        androidx.compose.runtime.remember { it }
        it
    }.let { androidx.compose.runtime.derivedStateOf { it.value } }.let {
        androidx.compose.runtime.remember { it }
    }.let { androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) } }
    val token by authViewModel?.token?.collectAsState() ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    val isLoggedIn = !token.isNullOrEmpty()

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
                onClick = { authViewModel?.logout() }
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
