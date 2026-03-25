package com.syq.lexi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.syq.lexi.data.database.WordbookEntity
import kotlin.math.ceil

@Composable
fun AddPlanDialog(
    wordbooks: List<WordbookEntity>,
    wordCounts: Map<Int, Int>,
    existingPlanWordbookIds: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (wordbookId: Int, dailyWords: Int) -> Unit
) {
    val availableWordbooks = wordbooks.filter { it.id !in existingPlanWordbookIds }

    var selectedWordbook by remember { mutableStateOf(availableWordbooks.firstOrNull()) }
    var dailyWords by remember { mutableStateOf(10f) }

    val totalWords = selectedWordbook?.let { wordCounts[it.id] ?: it.totalWords } ?: 0
    val daysNeeded = if (dailyWords > 0 && totalWords > 0)
        ceil(totalWords.toFloat() / dailyWords).toInt() else 0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("新增背诵计划", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(20.dp))

                if (availableWordbooks.isEmpty()) {
                    Text("所有单词本都已有计划了", fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("关闭") }
                    return@Column
                }

                // 选择单词本
                Text("选择单词本", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 210.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(availableWordbooks) { wb ->
                        val isSelected = wb.id == selectedWordbook?.id
                        val wbTotal = wordCounts[wb.id] ?: wb.totalWords
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedWordbook = wb }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(wb.name, fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurface)
                            Text("$wbTotal 词", fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 每日单词数滑块
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("每日单词数", fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${dailyWords.toInt()} 个/天", fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = dailyWords,
                    onValueChange = { dailyWords = it },
                    valueRange = 5f..50f,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("5", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("50", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 预计天数
                if (daysNeeded > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "预计 $daysNeeded 天背完，共 $totalWords 词",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("取消")
                    }
                    Button(
                        onClick = {
                            selectedWordbook?.let {
                                onConfirm(it.id, dailyWords.toInt())
                                onDismiss()
                            }
                        },
                        enabled = selectedWordbook != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("创建")
                    }
                }
            }
        }
    }
}
