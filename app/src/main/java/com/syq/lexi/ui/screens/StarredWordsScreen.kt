package com.syq.lexi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syq.lexi.data.database.WordEntity
import com.syq.lexi.ui.viewmodel.WordbookViewModel

@Composable
fun StarredWordsScreen(
    wordbookId: Int,
    wordbookName: String,
    onBackClick: () -> Unit,
    innerPadding: PaddingValues,
    viewModel: WordbookViewModel
) {
    val starredWords by viewModel.getStarredWords(wordbookId).collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 顶部栏
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
            }
            Text(
                "$wordbookName · 难词",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
        }

        if (starredWords.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⭐", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "还没有收藏的难词",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "在背单词时点击 ★ 收藏难词",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Text(
                "共 ${starredWords.size} 个难词",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(starredWords, key = { it.id }) { word ->
                    StarredWordCard(
                        word = word,
                        onUnstar = { viewModel.unstarWord(word.id) },
                        onMasterAndUnstar = {
                            viewModel.markWordAsMastered(word.id)
                            viewModel.unstarWord(word.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StarredWordCard(word: WordEntity, onUnstar: () -> Unit, onMasterAndUnstar: () -> Unit = {}) {
    var showUnstarConfirm by remember { mutableStateOf(false) }
    var showMasterConfirm by remember { mutableStateOf(false) }

    if (showUnstarConfirm) {
        AlertDialog(
            onDismissRequest = { showUnstarConfirm = false },
            title = { Text("取消收藏") },
            text = { Text("确定要从难词本移除「${word.english}」吗？") },
            confirmButton = {
                TextButton(onClick = { showUnstarConfirm = false; onUnstar() }) {
                    Text("确定", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnstarConfirm = false }) { Text("取消") }
            }
        )
    }

    if (showMasterConfirm) {
        AlertDialog(
            onDismissRequest = { showMasterConfirm = false },
            title = { Text("标记为已掌握") },
            text = { Text("确定将「${word.english}」标记为已掌握并取消收藏吗？") },
            confirmButton = {
                TextButton(onClick = { showMasterConfirm = false; onMasterAndUnstar() }) {
                    Text("确定", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMasterConfirm = false }) { Text("取消") }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(word.english, fontSize = 17.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface)
                    if (word.partOfSpeech.isNotEmpty()) {
                        Text(word.partOfSpeech, fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
                if (word.pronunciation.isNotEmpty()) {
                    Text(word.pronunciation, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(4.dp))
                Text(word.chinese, fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (word.example.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(word.example, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                // 对勾按钮：标记已掌握并取消收藏
                IconButton(onClick = { showMasterConfirm = true }) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "标记已掌握",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                            .border(1.5.dp, MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(4.dp))
                            .padding(2.dp)
                    )
                }
                // 星号按钮：取消收藏
                IconButton(onClick = { showUnstarConfirm = true }) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "取消收藏",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
