package com.syq.lexi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.syq.lexi.data.database.WordEntity
import com.syq.lexi.ui.viewmodel.WordbookViewModel

@Composable
fun StudyScreen(
    wordbookId: Int,
    wordbookName: String,
    onMenuClick: () -> Unit,
    onBackClick: () -> Unit,
    innerPadding: PaddingValues,
    viewModel: WordbookViewModel? = null
) {
    val allWords = viewModel?.words?.collectAsState()?.value ?: emptyList()
    val searchQuery = remember { mutableStateOf("") }
    val filteredWords = if (searchQuery.value.isEmpty()) allWords
    else allWords.filter { word ->
        word.english.contains(searchQuery.value, ignoreCase = true) ||
        word.chinese.contains(searchQuery.value, ignoreCase = false)
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(innerPadding)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "返回") }
            Text(wordbookName, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f), maxLines = 1)
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, "菜单") }
        }
        TextField(value = searchQuery.value, onValueChange = { searchQuery.value = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text("搜索单词...") }, singleLine = true,
            trailingIcon = {
                if (searchQuery.value.isNotEmpty())
                    IconButton(onClick = { searchQuery.value = "" }) {
                        Icon(Icons.Default.Clear, "清除")
                    }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(8.dp))
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("共${filteredWords.size}词", fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("已掌握${filteredWords.count { it.isMastered }}词", fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(12.dp))
        if (filteredWords.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(16.dp), Alignment.Center) {
                Text("未找到匹配的单词", fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)) {
                items(filteredWords, key = { it.id }) { word ->
                    WordCard(
                        word = word,
                        onMasteredToggle = { isMastered ->
                            if (isMastered) viewModel?.markWordAsMastered(word.id)
                            else viewModel?.markWordAsUnmastered(word.id)
                        },
                        onDelete = { viewModel?.deleteWord(word) }
                    )
                }
            }
        }
    }
}

@Composable
fun WordCard(
    word: WordEntity,
    onMasteredToggle: (Boolean) -> Unit = {},
    onDelete: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDetail by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (word.isMastered)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onMasteredToggle(!word.isMastered) }
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(word.english, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (word.pronunciation.isNotEmpty())
                        Text(word.pronunciation, fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(enabled = false) {}
                ) {
                    if (word.isMastered)
                        Text("\u2713", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Text("⋮", fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("详情") },
                                onClick = { showMenu = false; showDetail = true }
                            )
                            DropdownMenuItem(
                                text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                                onClick = { showMenu = false; showDeleteConfirm = true }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(word.chinese, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }

    if (showDetail) {
        Dialog(onDismissRequest = { showDetail = false }) {
            Card(shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(word.english, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    if (word.pronunciation.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(word.pronunciation, fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    if (word.partOfSpeech.isNotEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text(word.partOfSpeech, fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    TextButton(onClick = { showDetail = false },
                        modifier = Modifier.align(Alignment.End)) { Text("关闭") }
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
                TextButton(onClick = { showDeleteConfirm = false; onDelete?.invoke() }) {
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
fun SwipeableWordCard(
    word: WordEntity,
    onMasteredToggle: (Boolean) -> Unit = {},
    onDelete: () -> Unit = {}
) {
    WordCard(word = word, onMasteredToggle = onMasteredToggle, onDelete = onDelete)
}
