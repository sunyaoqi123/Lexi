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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syq.lexi.data.database.StudyPlanEntity
import com.syq.lexi.data.database.WordbookEntity
import kotlin.math.ceil

@Composable
fun HomeScreen(
    onMenuClick: () -> Unit,
    innerPadding: PaddingValues,
    wordbooks: List<WordbookEntity> = emptyList(),
    wordCounts: Map<Int, Int> = emptyMap(),
    masteredCounts: Map<Int, Int> = emptyMap(),
    starredCounts: Map<Int, Int> = emptyMap(),
    plans: List<StudyPlanEntity> = emptyList(),
    onStartLearning: (WordbookEntity, Int) -> Unit = { _, _ -> },
    onStartStarredLearning: (WordbookEntity, Int) -> Unit = { _, _ -> },
    onStartReview: (WordbookEntity, Int) -> Unit = { _, _ -> },
    dueReviewCounts: Map<Int, Int> = emptyMap(),
    onAddPlan: (wordbookId: Int, dailyWords: Int) -> Unit = { _, _ -> },
    onDeletePlan: (StudyPlanEntity) -> Unit = {}
) {
    var selectedWordbook by remember(wordbooks) { mutableStateOf(wordbooks.firstOrNull()) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var showAddPlanDialog by remember { mutableStateOf(false) }
    var selectedGroupSize by remember { mutableIntStateOf(10) }
    val groupSizeOptions = listOf(5, 10, 15, 20)
    val planWordbookIds = plans.map { it.wordbookId }.toSet()

    Column(
        modifier = Modifier.fillMaxSize().padding(innerPadding)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, "菜单") }
            Text("Lexi", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Box(modifier = Modifier.size(48.dp))
        }

        if (wordbooks.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("还没有单词本", fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("前往单词本页面添加或创建单词本", fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp))
                }
            }
        } else {
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Row(modifier = Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .clickable { dropdownExpanded = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("当前单词本", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(selectedWordbook?.name ?: "请选择", fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        selectedWordbook?.let {
                            Text(it.category, fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(4.dp))
                        }
                        Icon(Icons.Default.KeyboardArrowDown, "选择",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                DropdownMenu(expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .heightIn(max = 260.dp)) {
                    wordbooks.forEach { wb ->
                        DropdownMenuItem(text = {
                            Column {
                                Text(wb.name, fontSize = 15.sp,
                                    fontWeight = if (wb.id == selectedWordbook?.id)
                                        FontWeight.Bold else FontWeight.Normal,
                                    color = if (wb.id == selectedWordbook?.id)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface)
                                Text(wb.category, fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }, onClick = { selectedWordbook = wb; dropdownExpanded = false })
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("每组单词数", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    groupSizeOptions.forEach { size ->
                        val isSelected = size == selectedGroupSize
                        Box(modifier = Modifier
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(20.dp))
                            .clickable { selectedGroupSize = size }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center) {
                            Text("$size", fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White
                                        else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
            Spacer(Modifier.height(40.dp))
            // 开始背单词 + 练习难词按钮
            val starredCount = starredCounts[selectedWordbook?.id] ?: 0
            val dueReviewCount = dueReviewCounts[selectedWordbook?.id] ?: 0
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val btnColor = if (dueReviewCount > 0) Color(0xFFFFB300) else MaterialTheme.colorScheme.primary
                Box(modifier = Modifier.size(180.dp)
                    .background(btnColor, CircleShape)
                    .clickable {
                        selectedWordbook?.let {
                            if (dueReviewCount > 0) onStartReview(it, selectedGroupSize)
                            else onStartLearning(it, selectedGroupSize)
                        }
                    },
                    contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (dueReviewCount > 0) {
                            Text("复习", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("$dueReviewCount 个待复习", fontSize = 14.sp, color = Color.White)
                        } else {
                            Text("开始", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("背单词", fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
                if (dueReviewCount > 0) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                            .clickable { selectedWordbook?.let { onStartLearning(it, selectedGroupSize) } }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("学新词", fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (starredCount > 0) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFFFB300).copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                            .clickable { selectedWordbook?.let { onStartStarredLearning(it, selectedGroupSize) } }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp))
                        Text("练习难词 ($starredCount)", fontSize = 14.sp,
                            color = Color(0xFFFFB300), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("我的背诵计划", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { showAddPlanDialog = true }, Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, "新增计划", tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(10.dp))
            if (plans.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), Alignment.Center) {
                    Text("点击 + 新增背诵计划", fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)
                    .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)) {
                    items(plans) { plan ->
                        val wb = wordbooks.find { it.id == plan.wordbookId }
                        if (wb != null) {
                            val total = wordCounts[wb.id] ?: wb.totalWords
                            val mastered = masteredCounts[wb.id] ?: 0
                            val days = if (plan.dailyWords > 0 && total > 0)
                                ceil(total.toFloat() / plan.dailyWords).toInt() else 0
                            PlanCard(name = wb.name, category = wb.category,
                                totalWords = total, masteredWords = mastered,
                                dailyWords = plan.dailyWords, daysNeeded = days,
                                onDelete = { onDeletePlan(plan) })
                        }
                    }
                }
            }
        }
    }
    if (showAddPlanDialog) {
        AddPlanDialog(wordbooks = wordbooks, wordCounts = wordCounts,
            existingPlanWordbookIds = planWordbookIds,
            onDismiss = { showAddPlanDialog = false },
            onConfirm = { wid, dw -> onAddPlan(wid, dw) })
    }
}

@Composable
fun PlanCard(
    name: String,
    category: String,
    totalWords: Int,
    masteredWords: Int = 0,
    dailyWords: Int = 0,
    daysNeeded: Int = 0,
    onDelete: (() -> Unit)? = null
) {
    val progress = if (totalWords > 0) masteredWords.toFloat() / totalWords else 0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text(category, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("$masteredWords / ${totalWords}词", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary)
                    if (dailyWords > 0) {
                        Text("${dailyWords}词/天 · 约${daysNeeded}天", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(8.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    RoundedCornerShape(4.dp))) {
                Box(modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(8.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)))
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("${(progress * 100).toInt()}%已掌握", fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (onDelete != null) {
                    Text("删除计划", fontSize = 11.sp, color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.clickable { onDelete() })
                }
            }
        }
    }
}
