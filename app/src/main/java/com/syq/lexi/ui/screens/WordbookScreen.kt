package com.syq.lexi.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syq.lexi.data.database.WordbookEntity
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun WordbookScreen(
    onMenuClick: () -> Unit,
    innerPadding: PaddingValues,
    viewModel: com.syq.lexi.ui.viewmodel.WordbookViewModel? = null,
    onWordbookClick: (WordbookEntity) -> Unit = {}
) {
    val wordbooks = viewModel?.wordbooks?.collectAsState()?.value ?: emptyList()
    val isLoading = viewModel?.isLoading?.collectAsState()?.value ?: false
    val wordCounts = viewModel?.wordCounts?.collectAsState()?.value ?: emptyMap()
    val showImportDialog = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(innerPadding)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "菜单")
            }
            Text("选择单词本", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showImportDialog.value = true }) {
                Icon(Icons.Default.Add, contentDescription = "导入单词本")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)) {
                items(wordbooks, key = { it.id }) { wordbook ->
                    SwipeableWordbookCard(
                        wordbook = wordbook,
                        actualWordCount = wordCounts[wordbook.id] ?: wordbook.totalWords,
                        onCardClick = { onWordbookClick(wordbook) },
                        onDelete = { viewModel?.deleteWordbookWithWords(wordbook) }
                    )
                }
            }
        }
    }

    if (showImportDialog.value) {
        val context = androidx.compose.ui.platform.LocalContext.current
        ImportWordbookDialog(
            onDismiss = { showImportDialog.value = false },
            onImport = { name, category, description, words ->
                viewModel?.addWordbook(name = name, category = category,
                    description = description, words = words)
                android.widget.Toast.makeText(context,
                    "单词本\"$name\"创建成功！共${words.size}个单词",
                    android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun SwipeableWordbookCard(
    wordbook: WordbookEntity,
    actualWordCount: Int = wordbook.totalWords,
    onCardClick: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember(wordbook.id) { Animatable(0f) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val actionWidthDp = 120.dp

    // 用 height(IntrinsicSize.Min) 让 Box 高度由卡片内容决定，按钮用 fillMaxHeight 撑满
    Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
        .clip(RoundedCornerShape(12.dp))) {
        // 底层删除按钮，fillMaxHeight 和卡片等高
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(actionWidthDp)
                .fillMaxHeight()
                .background(
                    MaterialTheme.colorScheme.error,
                    RoundedCornerShape(12.dp)
                )
                .clickable {
                    scope.launch { offsetX.animateTo(0f, tween(200)) }
                    showDeleteConfirm = true
                },
            contentAlignment = Alignment.Center
        ) {
            Text("删除", color = Color.White, fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold)
        }

        // 上层卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(wordbook.id) {
                    val maxOffsetPx = -actionWidthDp.toPx()
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var totalX = 0f
                        var totalY = 0f
                        var dragging = false
                        horizontalDrag(down.id) { change ->
                            val dx = change.positionChange().x
                            val dy = change.positionChange().y
                            totalX += dx
                            totalY += dy
                            if (!dragging && abs(totalX) > abs(totalY) && abs(totalX) > 8f)
                                dragging = true
                            if (dragging) {
                                change.consume()
                                scope.launch {
                                    offsetX.snapTo((offsetX.value + dx).coerceIn(maxOffsetPx, 0f))
                                }
                            }
                        }
                        scope.launch {
                            if (dragging) {
                                if (offsetX.value < maxOffsetPx / 2)
                                    offsetX.animateTo(maxOffsetPx, tween(200))
                                else
                                    offsetX.animateTo(0f, tween(200))
                            } else if (abs(totalX) < 8f && abs(totalY) < 8f) {
                                if (offsetX.value != 0f)
                                    offsetX.animateTo(0f, tween(200))
                                else
                                    onCardClick()
                            }
                        }
                    }
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(wordbook.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("共${actualWordCount}词", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("分类: ${wordbook.category}", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(wordbook.description, fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("← 左滑删除", fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                }
                Icon(Icons.Default.ArrowForward, contentDescription = "进入",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除单词本") },
            text = { Text("确定要删除「${wordbook.name}」吗？内部所有单词也会一并删除，操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") }
            }
        )
    }
}

@Composable
fun WordbookCard(
    wordbook: WordbookEntity,
    actualWordCount: Int = wordbook.totalWords,
    onCardClick: () -> Unit = {}
) {
    SwipeableWordbookCard(wordbook = wordbook, actualWordCount = actualWordCount,
        onCardClick = onCardClick)
}
