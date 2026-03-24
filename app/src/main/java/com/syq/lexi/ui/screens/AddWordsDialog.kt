package com.syq.lexi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun AddWordsDialog(
    wordbookName: String,
    onDismiss: () -> Unit,
    onAdd: (words: List<ParsedWord>) -> Unit
) {
    val wordText = remember { mutableStateOf("") }
    val previewWords = remember(wordText.value) { parseWordText(wordText.value) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    "添加单词",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "添加到：$wordbookName",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 单词文本输入
                TextField(
                    value = wordText.value,
                    onValueChange = { wordText.value = it },
                    label = { Text("粘贴或输入单词（每行一个）") },
                    placeholder = { Text("apple 苹果\nbanana 香蕉\ncherry 樱桃") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = 15,
                    singleLine = false
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 格式提示
                Text(
                    "支持格式：\n• 英文 中文（空格）\n• 英文,中文（逗号）\n• 英文，中文（中文逗号）\n• 英文-中文（横线）",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 预览识别结果
                if (previewWords.isNotEmpty()) {
                    Text(
                        "已识别 ${previewWords.size} 个单词：",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    previewWords.take(3).forEach { word ->
                        Text(
                            "• ${word.english} → ${word.chinese}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (previewWords.size > 3) {
                        Text(
                            "... 等共 ${previewWords.size} 个",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (wordText.value.isNotEmpty()) {
                    Text(
                        "未识别到单词，请检查格式",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消")
                    }

                    Button(
                        onClick = {
                            if (previewWords.isNotEmpty()) {
                                onAdd(previewWords)
                                onDismiss()
                            }
                        },
                        enabled = previewWords.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("添加（${previewWords.size}词）")
                    }
                }
            }
        }
    }
}
