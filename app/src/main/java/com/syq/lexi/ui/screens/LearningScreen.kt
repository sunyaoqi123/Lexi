package com.syq.lexi.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syq.lexi.ui.viewmodel.LearningPhase
import com.syq.lexi.ui.viewmodel.LearningViewModel
import com.syq.lexi.ui.viewmodel.QuizQuestion

@Composable
fun LearningScreen(
    wordbookId: Int,
    wordbookName: String,
    groupSize: Int = 10,
    starredOnly: Boolean = false,
    reviewOnly: Boolean = false,
    onBackClick: () -> Unit,
    innerPadding: PaddingValues,
    viewModel: LearningViewModel,
    onStarChanged: ((wordId: Int, isStarred: Boolean) -> Unit)? = null
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(wordbookId, starredOnly, reviewOnly) {
        viewModel.startSession(wordbookId, wordbookName, groupSize, starredOnly, reviewOnly)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
            }
            Text(wordbookName, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f), maxLines = 1)
            // 星标按钮：有当前题目时显示
            val currentWord = state.currentQuestion?.word
            if (currentWord != null && state.phase != LearningPhase.COMPLETED) {
                // 对勾按钮：仅练习难词模式显示，点击标记已掌握并取消收藏
                if (starredOnly) {
                    val showMasterConfirmState = remember { mutableStateOf(false) }
                    val showMasterConfirm = showMasterConfirmState.value
                    if (showMasterConfirm) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = { showMasterConfirmState.value = false },
                            title = { Text("标记为已掌握") },
                            text = { Text("确定将「${currentWord.english}」标记为已掌握并取消收藏吗？") },
                            confirmButton = {
                                androidx.compose.material3.TextButton(onClick = {
                                    showMasterConfirmState.value = false
                                    onStarChanged?.invoke(currentWord.id, true) // unstar
                                    viewModel.toggleStar(currentWord.id, true, onStarChanged)
                                }) { Text("确定", color = MaterialTheme.colorScheme.primary) }
                            },
                            dismissButton = {
                                androidx.compose.material3.TextButton(onClick = { showMasterConfirmState.value = false }) { Text("取消") }
                            }
                        )
                    }
                    IconButton(onClick = { showMasterConfirmState.value = true }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "标记已掌握",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                                .border(1.5.dp, MaterialTheme.colorScheme.outline,
                                    androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                .padding(2.dp)
                        )
                    }
                }
                IconButton(onClick = { viewModel.toggleStar(currentWord.id, currentWord.isStarred, onStarChanged) }) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = if (currentWord.isStarred) "取消收藏" else "收藏难词",
                        tint = if (currentWord.isStarred) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
            PhaseIndicator(phase = state.phase)
        }
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
                    Button(onClick = onBackClick) { Text("返回") }
                }
            }
            state.phase == LearningPhase.COMPLETED -> {
                val remainingReview by viewModel.getRemainingReviewCount(wordbookId).collectAsState(initial = 0)
                CompletedScreen(
                    wordbookName = wordbookName,
                    count = state.sessionWordCount,
                    remainingReviewCount = remainingReview,
                    isReviewMode = reviewOnly,
                    onBackClick = onBackClick,
                    onContinue = {
                        if (reviewOnly && remainingReview == 0) {
                            // 没有更多复习词，切换到学新词模式
                            viewModel.startSession(wordbookId, wordbookName, groupSize, starredOnly, false)
                        } else {
                            viewModel.startSession(wordbookId, wordbookName, groupSize, starredOnly, reviewOnly)
                        }
                    }
                )
            }
            state.currentQuestion != null -> {
                val progress = if (state.totalInRound > 0) state.currentIndex.toFloat() / state.totalInRound else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text("${state.currentIndex} / ${state.totalInRound}", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    textAlign = TextAlign.End)
                AnimatedContent(targetState = state.currentQuestion,
                    transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "question") { question ->
                    question?.let {
                        when (state.phase) {
                            LearningPhase.PHASE1_WORD_TO_MEANING -> Phase1Screen(it,
                                state.selectedAnswer, state.isAnswered, state.isCorrect,
                                { ans -> viewModel.submitAnswer(ans) }, { viewModel.nextQuestion() })
                            LearningPhase.PHASE2_MEANING_TO_WORD -> Phase2Screen(it,
                                state.selectedAnswer, state.isAnswered, state.isCorrect,
                                { ans -> viewModel.submitAnswer(ans) }, { viewModel.nextQuestion() })
                            LearningPhase.PHASE3_SPELL_WORD -> Phase3Screen(it,
                                state.spellInput, state.isAnswered, state.isCorrect,
                                { viewModel.updateSpellInput(it) },
                                { viewModel.submitAnswer(state.spellInput) },
                                { viewModel.nextQuestion() })
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Phase1Screen(
    question: QuizQuestion,
    selectedAnswer: String?,
    isAnswered: Boolean,
    isCorrect: Boolean,
    onAnswer: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text("选择正确的释义", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))
        Box(modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
            .padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(question.word.english, fontSize = 36.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                if (question.word.pronunciation.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(question.word.pronunciation, fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        question.options.forEach { option ->
            OptionButton(text = option, isSelected = selectedAnswer == option,
                isAnswered = isAnswered, isCorrect = option == question.correctAnswer,
                onClick = { if (!isAnswered) onAnswer(option) })
            Spacer(modifier = Modifier.height(12.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        if (isAnswered) ResultBar(isCorrect, question.correctAnswer, onNext)
    }
}

@Composable
fun Phase2Screen(
    question: QuizQuestion,
    selectedAnswer: String?,
    isAnswered: Boolean,
    isCorrect: Boolean,
    onAnswer: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text("选择对应的单词", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))
        Box(modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
            .padding(32.dp), contentAlignment = Alignment.Center) {
            Text(question.word.chinese, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.height(32.dp))
        question.options.forEach { option ->
            OptionButton(text = option, isSelected = selectedAnswer == option,
                isAnswered = isAnswered, isCorrect = option == question.correctAnswer,
                onClick = { if (!isAnswered) onAnswer(option) })
            Spacer(modifier = Modifier.height(12.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        if (isAnswered) ResultBar(isCorrect, question.correctAnswer, onNext)
    }
}

@Composable
fun Phase3Screen(
    question: QuizQuestion,
    spellInput: String,
    isAnswered: Boolean,
    isCorrect: Boolean,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(question) { focusRequester.requestFocus() }
    Column(modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text("拼写对应的单词", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))
        Box(modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
            .padding(32.dp), contentAlignment = Alignment.Center) {
            Text(question.word.chinese, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("提示：共 ${question.correctAnswer.length} 个字母", fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = spellInput, onValueChange = { if (!isAnswered) onInputChange(it) },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            label = { Text("输入英文单词") }, singleLine = true, enabled = !isAnswered,
            isError = isAnswered && !isCorrect,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (!isAnswered) onSubmit() }),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (!isAnswered) {
            Button(onClick = onSubmit, enabled = spellInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth()) { Text("确认", fontSize = 16.sp) }
        }
        Spacer(modifier = Modifier.weight(1f))
        if (isAnswered) ResultBar(isCorrect, question.correctAnswer, onNext)
    }
}

@Composable
fun OptionButton(
    text: String, isSelected: Boolean, isAnswered: Boolean,
    isCorrect: Boolean, onClick: () -> Unit
) {
    val bgColor = when {
        isAnswered && isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.15f)
        isAnswered && isSelected && !isCorrect -> Color(0xFFF44336).copy(alpha = 0.15f)
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val borderColor = when {
        isAnswered && isCorrect -> Color(0xFF4CAF50)
        isAnswered && isSelected && !isCorrect -> Color(0xFFF44336)
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(bgColor).border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = !isAnswered, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f))
        if (isAnswered && isCorrect)
            Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
        else if (isAnswered && isSelected && !isCorrect)
            Icon(Icons.Default.Close, null, tint = Color(0xFFF44336), modifier = Modifier.size(20.dp))
    }
}

@Composable
fun ResultBar(isCorrect: Boolean, correctAnswer: String, onNext: () -> Unit) {
    val bgColor = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(bgColor).padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(if (isCorrect) "正确！" else "答错了", fontSize = 16.sp,
                fontWeight = FontWeight.Bold, color = Color.White)
            if (!isCorrect) Text("正确答案：$correctAnswer", fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f))
        }
        Button(onClick = onNext, colors = ButtonDefaults.buttonColors(
            containerColor = Color.White, contentColor = bgColor),
            shape = RoundedCornerShape(20.dp)) {
            Text("继续", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CompletedScreen(wordbookName: String, count: Int, remainingReviewCount: Int = 0, isReviewMode: Boolean = false, onBackClick: () -> Unit, onContinue: () -> Unit = {}) {
    val hasMoreReview = remainingReviewCount > 0
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎉", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text("太棒了！", fontSize = 28.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))
        if (isReviewMode) {
            if (hasMoreReview) {
                Text("恭喜完成了 $wordbookName 的 $count 个单词的复习",
                    fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text("还有 $remainingReviewCount 个单词需要复习",
                    fontSize = 14.sp, color = Color(0xFFFFB300), textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium)
            } else {
                Text("恭喜完成了 $wordbookName 的所有复习任务！",
                    fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center)
            }
        } else {
            Text("你已完成 $wordbookName 中 $count 个单词的学习",
                fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text("这些单词已标记为已掌握！", fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.height(40.dp))
        if (isReviewMode && hasMoreReview) {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
            ) {
                Text("继续复习", fontSize = 16.sp, color = Color.White)
            }
        } else if (!isReviewMode) {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("继续背诵", fontSize = 16.sp)
            }
        } else {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("开始学新词", fontSize = 16.sp)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
            Text("返回首页", fontSize = 16.sp)
        }
    }
}

@Composable
fun PhaseIndicator(phase: LearningPhase) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
        listOf(LearningPhase.PHASE1_WORD_TO_MEANING, LearningPhase.PHASE2_MEANING_TO_WORD,
            LearningPhase.PHASE3_SPELL_WORD).forEachIndexed { index, p ->
            val isActive = phase == p
            val isDone = when (phase) {
                LearningPhase.PHASE2_MEANING_TO_WORD -> index == 0
                LearningPhase.PHASE3_SPELL_WORD -> index < 2
                LearningPhase.COMPLETED -> true
                else -> false
            }
            Box(modifier = Modifier.size(if (isActive) 10.dp else 8.dp).background(
                color = if (isDone || isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape))
        }
    }
}