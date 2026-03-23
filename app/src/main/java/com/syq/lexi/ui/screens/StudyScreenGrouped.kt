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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syq.lexi.data.database.WordEntity
import com.syq.lexi.ui.viewmodel.WordbookViewModel
import kotlinx.coroutines.launch

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
    
    // 根据搜索词过滤单词
    val filteredWords = if (searchQuery.value.isEmpty()) {
        allWords
    } else {
        allWords.filter { word ->
            word.english.contains(searchQuery.value, ignoreCase = true) ||
            word.chinese.contains(searchQuery.value, ignoreCase = false)
        }
    }
    
    // 按首字母分组
    val groupedWords = remember(filteredWords) {
        filteredWords.sortedBy { it.english }
            .groupBy { it.english.firstOrNull()?.uppercaseChar() ?: '?' }
            .toSortedMap()
    }
    
    val letters = remember(groupedWords) { groupedWords.keys.toList() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // 获取当前可见的首字母
    val currentVisibleLetter = remember {
        derivedStateOf {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            var currentLetter = 'A'
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

            Spacer(modifier = Modifier.height(12.dp))

            // 单词统计
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

            Spacer(modifier = Modifier.height(12.dp))

            // 单词列表
            if (filteredWords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "未找到匹配的单词",
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
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    groupedWords.forEach { (letter, words) ->
                        item {
                            // 字母标题
                            Text(
                                letter.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        items(words) { word ->
                            SimpleWordCard(
                                word = word,
                                onMasteredToggle = { isMastered ->
                                    if (isMastered) {
                                        viewModel?.markWordAsMastered(word.id)
                                    } else {
                                        viewModel?.markWordAsUnmastered(word.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // 右侧字母导航栏
        LetterNavigationBar(
            letters = letters,
            currentLetter = currentVisibleLetter.value,
            onLetterClick = { letter ->
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
        )
    }
}

@Composable
fun LetterNavigationBar(
    letters: List<Char>,
    currentLetter: Char,
    onLetterClick: (Char) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(24.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        letters.forEach { letter ->
            val isCurrentLetter = letter == currentLetter
            Text(
                letter.toString(),
                fontSize = if (isCurrentLetter) 14.sp else 10.sp,
                fontWeight = if (isCurrentLetter) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrentLetter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clickable { onLetterClick(letter) }
                    .padding(2.dp)
            )
        }
    }
}

@Composable
fun SimpleWordCard(
    word: WordEntity,
    onMasteredToggle: (Boolean) -> Unit = {}
) {
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
            .padding(12.dp),
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
    }
}
