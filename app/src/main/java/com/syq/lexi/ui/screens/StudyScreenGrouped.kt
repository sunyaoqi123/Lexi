package com.syq.lexi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.syq.lexi.data.database.WordEntity
import com.syq.lexi.ui.viewmodel.WordbookViewModel
import kotlinx.coroutines.launch

// 所有26个字母
private val ALL_LETTERS = ('A'..'Z').toList()

@Composable
fun StudyScreenGrouped(
    wordbookId: Int,
    wordbookName: String,
    onMenuClick: () -> Unit,
    onBackClick: () -> Unit,
    innerPadding: PaddingValues,
    viewModel: WordbookViewModel? = null
) {
    val allWords = viewModel?.words?.collectAsState()?.value ?: emptyList()
    val searchQuery = remember { mutableStateOf("") }
    val showAddWordsDialog = remember { mutableStateOf(false) }

    val filteredWords = if (searchQuery.value.isEmpty()) {
        allWords
    } else {
        allWords.filter { word ->
            word.english.contains(searchQuery.value, ignoreCase = true) ||
            word.chinese.contains(searchQuery.value, ignoreCase = false)
        }
    }

    // 按首字母分组（只含有单词的字母）
    val groupedWords = remember(filteredWords) {
        filteredWords.sortedBy { it.english }
            .groupBy { it.english.firstOrNull()?.uppercaseChar() ?: '?' }
            .toSortedMap()
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 当前可见首字母 - 直接依赖 listState 和 groupedWords
    val currentVisibleLetter = remember(groupedWords) {
        derivedStateOf {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            var currentLetter = groupedWords.keys.firstOrNull() ?: 'A'
            var currentIndex = 0
            for ((letter, words) in groupedWords) {
                if (currentIndex + words.size > firstVisibleIndex) {
                    currentLetter = letter
                    break
                }
                currentIndex += words.size + 1
            }
            currentLetter
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // 顶部栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
                Text(
                    wordbookName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                IconButton(onClick = { showAddWordsDialog.value = true }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "添加单词"
                    )
                }
            }

            // 添加单词对话框
            if (showAddWordsDialog.value) {
                val context = androidx.compose.ui.platform.LocalContext.current
                AddWordsDialog(
                    wordbookName = wordbookName,
                    onDismiss = { showAddWordsDialog.value = false },
                    onAdd = { words ->
                        viewModel?.addWordsToWordbook(wordbookId, words) { added, merged, skipped ->
                            val parts = mutableListOf<String>()
                            if (added > 0) parts.add("新增${added}个单词")
                            if (skipped > 0) parts.add("${skipped}个重复单词")
                            if (merged > 0) parts.add("新增${merged}个释义")
                            val msg = parts.joinToString("，")
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

        // 搜索框
        TextField(
            value = searchQuery.value,
            onValueChange = { searchQuery.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("搜索单词...") },
            singleLine = true,
            trailingIcon = {
                if (searchQuery.value.isNotEmpty()) {
                    IconButton(onClick = { searchQuery.value = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "清除")
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 统计
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "共${filteredWords.size}词",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "已掌握${filteredWords.count { it.isMastered }}词",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

            if (filteredWords.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (searchQuery.value.isEmpty()) "快来添加词汇吧！" else "未找到匹配的单词",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    groupedWords.forEach { (letter, words) ->
                        item {
                            Text(
                                letter.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(words) { word ->
                            SimpleWordCard(
                                word = word,
                                onMasteredToggle = { isMastered ->
                                    if (isMastered) viewModel?.markWordAsMastered(word.id)
                                    else viewModel?.markWordAsUnmastered(word.id)
                                },
                                onDelete = { viewModel?.deleteWord(word) }
                            )
                            // 每个单词之间的间距
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }

        // 右侧全字母导航栏
        LetterNavigationBar(
            availableLetters = groupedWords.keys.toSet(),
            currentLetter = currentVisibleLetter.value,
            onLetterClick = { letter ->
                if (groupedWords.containsKey(letter)) {
                    scope.launch {
                        var index = 0
                        for ((l, words) in groupedWords) {
                            if (l == letter) {
                                listState.animateScrollToItem(index)
                                break
                            }
                            index += words.size + 1
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun LetterNavigationBar(
    availableLetters: Set<Char>,
    currentLetter: Char,
    onLetterClick: (Char) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(24.dp)
            .padding(vertical = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ALL_LETTERS.forEach { letter ->
            val isAvailable = availableLetters.contains(letter)
            val isCurrent = letter == currentLetter
            Text(
                letter.toString(),
                fontSize = if (isCurrent) 14.sp else 10.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isCurrent -> MaterialTheme.colorScheme.primary
                    isAvailable -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                },
                modifier = Modifier
                    .clickable(enabled = isAvailable) { onLetterClick(letter) }
                    .padding(vertical = 1.dp)
            )
        }
    }
}

@Composable
fun SimpleWordCard(
    word: WordEntity,
    onMasteredToggle: (Boolean) -> Unit = {},
    onDelete: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDetail by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMasteredToggle(!word.isMastered) }
            .background(
                color = if (word.isMastered)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                word.english,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(100.dp)
            )
            Text(
                word.chinese,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
        if (word.isMastered) {
            Text(
                "✓",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Box {
            IconButton(onClick = { showMenu = true }) {
                Text("⋮", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error)
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("详情") },
                    onClick = { showMenu = false; showDetail = true })
                DropdownMenuItem(
                    text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                    onClick = { showMenu = false; showDeleteConfirm = true })
            }
        }
    }

    if (showDetail) {
        Dialog(onDismissRequest = { showDetail = false }) {
            androidx.compose.material3.Card(
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(word.english, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    if (word.pronunciation.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(word.pronunciation, fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(word.chinese, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    if (word.example.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text("例句", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text(word.example, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        if (word.exampleTranslation.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(word.exampleTranslation, fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    androidx.compose.material3.TextButton(
                        onClick = { showDetail = false },
                        modifier = Modifier.align(Alignment.End)
                    ) { Text("关闭") }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除单词") },
            text = { Text("确定要删除「" + word.english + "」吗？") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showDeleteConfirm = false; onDelete?.invoke() }
                ) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showDeleteConfirm = false }
                ) { Text("取消") }
            }
        )
    }
}
