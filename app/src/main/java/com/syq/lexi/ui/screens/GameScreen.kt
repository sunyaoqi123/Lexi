package com.syq.lexi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syq.lexi.data.database.LexiDatabase
import com.syq.lexi.data.database.WordEntity
import com.syq.lexi.notification.DailyReminderManager
import com.syq.lexi.ui.viewmodel.FriendViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

data class Game(val id:Int,val name:String,val description:String,val icon:String,val color:Color)
data class MatchWordItem(val id:Int,val english:String,val chinese:String)
data class WordPlacement(
    val start: Pair<Int, Int>,
    val end: Pair<Int, Int>,
    val axis: String,
    val reversed: Boolean
)

data class FivesMode(val id:String,val title:String,val chances:Int,val wordLength:Int)

private object FivesCandidatesCache {
    private var wordsHash: Int = 0
    private var c4: List<String> = emptyList()
    private var c5: List<String> = emptyList()
    private var c6: List<String> = emptyList()

    fun ensure(words: List<WordEntity>) {
        val cleaned = words.map { it.english.trim().lowercase() }
            .filter { it.all { ch -> ch.isLetter() } }
            .distinct()
        val newHash = cleaned.joinToString("|").hashCode()
        if (newHash != wordsHash) {
            wordsHash = newHash
            c4 = cleaned.filter { it.length == 4 }.shuffled().take(500)
            c5 = cleaned.filter { it.length == 5 }.shuffled().take(500)
            c6 = cleaned.filter { it.length == 6 }.shuffled().take(500)
        }
    }

    fun getCandidates(length: Int): List<String> = when (length) {
        4 -> c4
        5 -> c5
        6 -> c6
        else -> emptyList()
    }
}

private enum class Page{GAME,FRIENDS,NEW_FRIENDS,WORD_MATCH,HELLO_WORD,LETTER_REORDER,FIVES,WORD_SEARCH,GUESS_WHAT,LEADERBOARD}

@Composable
fun GameScreen(onMenuClick:()->Unit,innerPadding:PaddingValues,friendViewModel:FriendViewModel,onInGameDetailChanged:(Boolean)->Unit){
    val st by friendViewModel.state.collectAsState();val context=LocalContext.current
    var page by remember{mutableStateOf(Page.GAME)};var showAdd by remember{mutableStateOf(false)}
    var remindTarget by remember{mutableStateOf<Pair<Int,String>?>(null)}
    val games=remember{listOf(
        Game(1,"单词连连看","匹配英文与中文释义","🧩",Color(0xFF6366F1)),
        Game(2,"Hello Word","根据提示拼出完整单词","🔠",Color(0xFF8B5CF6)),
        Game(3,"字母重组","点击交换字母还原单词","🎧",Color(0xFFEC4899)),
        Game(4,"Fives","6次机会猜5字母单词","🔗",Color(0xFFF59E0B)),
        Game(5,"Word Search","在字母网格中找出目标词","⚡",Color(0xFF10B981)),
        Game(6,"GuessWhat","根据提示猜出秘密单词","📝",Color(0xFF3B82F6)))}

    LaunchedEffect(page){ onInGameDetailChanged(page in setOf(Page.WORD_MATCH, Page.HELLO_WORD, Page.LETTER_REORDER, Page.FIVES, Page.WORD_SEARCH, Page.GUESS_WHAT)) }
    LaunchedEffect(Unit){while(true){friendViewModel.refreshAll();friendViewModel.pollUnreadReminders{DailyReminderManager.showFriendStudyReminderNotification(context,it.message)};delay(15000)}}
    if(showAdd)AddFriendDialog(st.searchInput,friendViewModel::updateSearchInput,friendViewModel::searchUser,friendViewModel::sendRequest,{showAdd=false;friendViewModel.clearTips()},st.searchResult?.username,st.message,st.error)
    remindTarget?.let{(id,name)->AlertDialog(onDismissRequest={remindTarget=null},title={Text("确认提醒")},text={Text("确定提醒 $name 背单词吗？")},confirmButton={Button(onClick={friendViewModel.sendStudyReminder(id,name);remindTarget=null}){Text("确定")}},dismissButton={OutlinedButton(onClick={remindTarget=null}){Text("取消")}})}

    Column(Modifier.fillMaxSize().padding(innerPadding).background(MaterialTheme.colorScheme.background)){
        when(page){
            Page.GAME->{
                Row(Modifier.fillMaxWidth().padding(16.dp),Arrangement.SpaceBetween,Alignment.CenterVertically){
                    IconButton(onClick=onMenuClick){Icon(Icons.Default.Menu,"菜单")};Text("趣味游戏",fontSize=20.sp,fontWeight=FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick={page=Page.LEADERBOARD}){ Text("🏆", fontSize = 20.sp) }
                        Box(Modifier.size(48.dp),contentAlignment=Alignment.TopEnd){IconButton(onClick={page=Page.FRIENDS}){Icon(Icons.Default.AccountCircle,"好友")};if(st.pendingCount>0)Box(Modifier.padding(top=10.dp,end=10.dp).size(10.dp).background(Color(0xFFE53935),CircleShape))}
                    }
                }
                Spacer(Modifier.height(16.dp))
                LazyVerticalGrid(columns=GridCells.Fixed(2),modifier=Modifier.fillMaxSize().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(16.dp),horizontalArrangement=Arrangement.spacedBy(16.dp),contentPadding=PaddingValues(bottom=16.dp)){items(games){g->GameCard(g){if(g.id==1)page=Page.WORD_MATCH; if(g.id==2)page=Page.HELLO_WORD; if(g.id==3)page=Page.LETTER_REORDER; if(g.id==4)page=Page.FIVES; if(g.id==5)page=Page.WORD_SEARCH; if(g.id==6)page=Page.GUESS_WHAT}}}
            }
            Page.FRIENDS->{
                Header("我的好友",{page=Page.GAME}){IconButton(onClick={showAdd=true}){Icon(Icons.Default.Add,"加好友")}}
                Card(Modifier.fillMaxWidth().padding(16.dp).clickable{page=Page.NEW_FRIENDS},shape=RoundedCornerShape(12.dp)){Row(Modifier.fillMaxWidth().padding(12.dp),Arrangement.SpaceBetween){Text("新的朋友");Text("查看",color=MaterialTheme.colorScheme.primary)}}
                Text("好友列表",fontWeight=FontWeight.SemiBold,modifier=Modifier.padding(horizontal=16.dp))
                LazyColumn(Modifier.fillMaxSize().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(8.dp),contentPadding=PaddingValues(bottom=16.dp)){items(st.friends){f->Card(shape=RoundedCornerShape(10.dp)){Row(Modifier.fillMaxWidth().padding(12.dp),Arrangement.SpaceBetween,Alignment.CenterVertically){Text(f.username);OutlinedButton(onClick={remindTarget=f.id to f.username}){Text("提醒背单词")}}}}}
            }
            Page.NEW_FRIENDS->{
                Header("新的朋友",{page=Page.FRIENDS}){Spacer(Modifier.size(48.dp))}
                LazyColumn(Modifier.fillMaxSize().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(8.dp),contentPadding=PaddingValues(bottom=16.dp)){
                    item{Text("收到的好友申请",fontWeight=FontWeight.SemiBold)}
                    items(st.pendingRequests){r->Card(shape=RoundedCornerShape(10.dp)){Column(Modifier.fillMaxWidth().padding(12.dp)){Text(r.fromUsername);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(onClick={friendViewModel.respondRequest(r.id,false)}){Text("拒绝")};Button(onClick={friendViewModel.respondRequest(r.id,true)}){Text("接受")}}}}}
                    item{Text("发出的好友申请",fontWeight=FontWeight.SemiBold)}
                    items(st.sentRequests){r->val s=when(r.status){"PENDING"->"待处理";"ACCEPTED"->"已通过";"REJECTED"->"已拒绝";else->r.status};Card(shape=RoundedCornerShape(10.dp)){Row(Modifier.fillMaxWidth().padding(12.dp),Arrangement.SpaceBetween){Text(r.toUsername);Text(s)}}}
                }
            }
            Page.LEADERBOARD->GameLeaderboardScreen(friendViewModel=friendViewModel,onBack={page=Page.GAME})
            Page.HELLO_WORD->HelloWordGame(onBack={page=Page.GAME}, friendViewModel=friendViewModel)
            Page.LETTER_REORDER->LetterReorderGame(onBack={page=Page.GAME}, friendViewModel=friendViewModel)
            Page.FIVES->FivesGame(onBack={page=Page.GAME}, friendViewModel=friendViewModel)
            Page.WORD_SEARCH->WordSearchGame(onBack={page=Page.GAME})
            Page.GUESS_WHAT->GuessWhatGame(onBack={page=Page.GAME})
            Page.WORD_MATCH->WordMatchGame(onBack={page=Page.GAME}, onBackToMenu={page=Page.GAME}, friendViewModel=friendViewModel)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HelloWordGame(onBack:()->Unit, friendViewModel: FriendViewModel){
    val context=LocalContext.current
    var sourceItems by remember{mutableStateOf<List<MatchWordItem>>(emptyList())}
    var queue by remember{mutableStateOf<List<MatchWordItem>>(emptyList())}
    var totalCount by remember{mutableIntStateOf(0)}
    var solvedCount by remember{mutableIntStateOf(0)}
    var selectedLetters by remember{mutableStateOf<List<Char>>(emptyList())}
    var selectedIndices by remember{mutableStateOf<List<Int>>(emptyList())}
    var shuffledPool by remember{mutableStateOf<List<Char>>(emptyList())}
    var elapsed by remember{mutableIntStateOf(0)}
    var errors by remember{mutableIntStateOf(0)}
    var skipped by remember{mutableIntStateOf(0)}
    var isLoading by remember{mutableStateOf(true)}
    var showVictory by remember{mutableStateOf(false)}
    var showSkipDialog by remember{mutableStateOf(false)}
    var showRulesDialog by remember{mutableStateOf(false)}
    var reloadToken by remember{mutableIntStateOf(0)}
    var myRank by remember{mutableStateOf<Int?>(null)}
    var uploadedThisRound by remember{mutableStateOf(false)}

    fun setupQuestion(){
        val target = queue.firstOrNull()?.english?.lowercase().orEmpty().filter { it.isLetter() }
        selectedLetters = emptyList()
        selectedIndices = emptyList()
        shuffledPool = target.toList().shuffled()
    }

    fun moveCurrentToEnd(){
        if(queue.isEmpty()) return
        val m = queue.toMutableList()
        val cur = m.removeAt(0)
        m.add(cur)
        queue = m
        setupQuestion()
    }

    LaunchedEffect(reloadToken){
        isLoading = true
        val all = LexiDatabase.getDatabase(context).wordDao().getAllWords().first()
        val unique = all.distinctBy { "${it.english.trim().lowercase()}|${it.chinese.trim()}" }
            .filter { it.english.count { ch -> ch.isLetter() } in 3..12 }
            .shuffled()
            .take(10)
            .mapIndexed { i, w -> MatchWordItem(i+1, w.english, w.chinese) }
        sourceItems = unique
        queue = unique
        totalCount = unique.size
        solvedCount = 0
        elapsed = 0
        errors = 0
        skipped = 0
        showVictory = false
        showSkipDialog = false
        myRank = null
        uploadedThisRound = false
        setupQuestion()
        isLoading = false
    }

    val running = !isLoading && queue.isNotEmpty() && !showVictory && !showSkipDialog && !showRulesDialog
    LaunchedEffect(running){ while(running){ delay(1000); elapsed++ } }

    LaunchedEffect(showVictory){
        if(showVictory && !uploadedThisRound && totalCount > 0){
            uploadedThisRound = true
            val signature = sourceItems.sortedBy { it.id }.joinToString("|") { "${it.english}=${it.chinese}" }
            val weightedErrors = errors + skipped
            friendViewModel.uploadGameResult(
                gameKey = "hello_word",
                groupSignature = signature,
                pairCount = totalCount,
                elapsedSeconds = elapsed,
                errors = weightedErrors
            ) { rank -> myRank = rank }
        }
    }

    Header("Hello Word", onBack){ IconButton(onClick={showRulesDialog=true}){ Text("💡") } }
    if(isLoading){ Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){ CircularProgressIndicator() }; return }
    if(queue.isEmpty() && !showVictory){ Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){ Text("单词不足，至少需要 10 个单词") }; return }

    val item = queue.firstOrNull()
    val target = item?.english?.lowercase()?.filter { it.isLetter() }.orEmpty()

    Column(
        Modifier.fillMaxSize().padding(horizontal=16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text("提示：${item?.chinese?.lowercase() ?: ""}", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        Text("进度：${solvedCount}/${totalCount}   用时：${elapsed}s   错误：$errors   跳过：$skipped", color = MaterialTheme.colorScheme.onSurfaceVariant)
        GameRuleHint("看提示拼单词，可清空或跳过，完成全部题目即通关")
        GameRuleHint("Use hint to spell word. You can clear/skip. Finish all to win.")
        Spacer(Modifier.height(22.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            target.forEachIndexed { idx, _ ->
                Box(
                    modifier = Modifier.size(38.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) { Text(selectedLetters.getOrNull(idx)?.toString() ?: "_", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
            }
        }

        Spacer(Modifier.height(22.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            shuffledPool.forEachIndexed { idx, ch ->
                val used = selectedIndices.contains(idx)
                Box(
                    modifier = Modifier.size(44.dp)
                        .background(if (used) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable(enabled = !used) {
                            if (selectedLetters.size < target.length) {
                                val nextLetters = selectedLetters + ch
                                val nextIndices = selectedIndices + idx
                                selectedLetters = nextLetters
                                selectedIndices = nextIndices
                                if (nextLetters.size == target.length) {
                                    val guess = nextLetters.joinToString("")
                                    if (guess == target) {
                                        val m = queue.toMutableList()
                                        if (m.isNotEmpty()) m.removeAt(0)
                                        queue = m
                                        solvedCount++
                                        if (solvedCount >= totalCount) {
                                            showVictory = true
                                        } else {
                                            setupQuestion()
                                        }
                                    } else {
                                        errors++
                                        selectedLetters = emptyList()
                                        selectedIndices = emptyList()
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) { Text(ch.toString(), color = if (used) MaterialTheme.colorScheme.onSurfaceVariant else Color.White, fontWeight = FontWeight.Bold) }
            }
        }

        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick={ selectedLetters = emptyList(); selectedIndices = emptyList() }){ Text("清空") }
            TextButton(onClick={ if (target.isNotEmpty()) showSkipDialog = true }){ Text("跳过") }
        }
    }

    if(showRulesDialog){
        AlertDialog(
            onDismissRequest={showRulesDialog=false},
            title={ Text("Hello Word 规则") },
            text={ Text("根据中文提示拼出完整英文单词。可清空重选，也可跳过并查看答案。规则弹窗打开时计时暂停。") },
            confirmButton={ TextButton(onClick={showRulesDialog=false}){ Text("知道了") } }
        )
    }

    if(showSkipDialog){
        AlertDialog(
            onDismissRequest={showSkipDialog=false},
            title={ Text("已跳过") },
            text={ Text("正确答案：${target.lowercase()}") },
            confirmButton={
                TextButton(onClick={
                    skipped++
                    showSkipDialog=false
                    moveCurrentToEnd()
                }){ Text("下一个") }
            }
        )
    }

    if(showVictory){
        AlertDialog(
            onDismissRequest={showVictory=false},
            title={ Text("挑战成功") },
            text={ Text("完成 ${totalCount} 题\n用时 ${elapsed}s\n错误 $errors 次\n跳过 $skipped 次\n" + if(myRank!=null) "好友第 ${myRank} 名" else "好友排名统计中...") },
            confirmButton={ TextButton(onClick={reloadToken++;showVictory=false}){ Text("再来一局") } },
            dismissButton={ TextButton(onClick={showVictory=false;onBack()}){ Text("返回菜单") } }
        )
    }
}

@Composable
private fun WordMatchGame(onBack:()->Unit, onBackToMenu:()->Unit, friendViewModel: FriendViewModel){
    val context=LocalContext.current
    var sourceWords by remember{mutableStateOf<List<MatchWordItem>>(emptyList())}
    var left by remember{mutableStateOf<List<MatchWordItem>>(emptyList())}
    var right by remember{mutableStateOf<List<MatchWordItem>>(emptyList())}
    val leftPairColor=remember{mutableStateMapOf<Int,Color>()}
    val rightPairColor=remember{mutableStateMapOf<Int,Color>()}
    val leftToRight=remember{mutableStateMapOf<Int,Int>()}
    var selectedSide by remember{mutableStateOf("")}
    var selectedId by remember{mutableIntStateOf(-1)}
    var elapsed by remember{mutableIntStateOf(0)}
    var errors by remember{mutableIntStateOf(0)}
    var reloadToken by remember{mutableIntStateOf(0)}
    var pulseId by remember{mutableIntStateOf(-1)}
    var myRank by remember{mutableStateOf<Int?>(null)}
    var uploadedThisRound by remember{mutableStateOf(false)}
    var showVictory by remember{mutableStateOf(false)}
    var isLoadingWords by remember { mutableStateOf(true) }
    var showRulesDialog by remember { mutableStateOf(false) }

    val pairColors=listOf(Color(0xFFDDF4FF),Color(0xFFE6F7E7),Color(0xFFFFF4D8),Color(0xFFFFE8F1),Color(0xFFEDE7FF),Color(0xFFFFEDD5),Color(0xFFE0F2FE),Color(0xFFDCFCE7),Color(0xFFFEF9C3),Color(0xFFFCE7F3))
    fun pairColorForWordId(id:Int)=pairColors[(id-1)%pairColors.size]

    fun resetKeepWords(){
        left=sourceWords.shuffled(); right=sourceWords.shuffled()
        leftPairColor.clear(); rightPairColor.clear(); leftToRight.clear()
        selectedSide=""; selectedId=-1
        elapsed=0; errors=0; showVictory=false; pulseId=-1; myRank=null; uploadedThisRound=false
    }

    LaunchedEffect(reloadToken){
        isLoadingWords = true
        val all=LexiDatabase.getDatabase(context).wordDao().getAllWords().first()
        val unique = all.distinctBy { "${it.english.trim().lowercase()}|${it.chinese.trim()}" }
        if(unique.size>=10){
            sourceWords=unique.shuffled().take(10).mapIndexed{i,w->MatchWordItem(i+1,w.english,w.chinese)}
            resetKeepWords()
        } else {
            sourceWords=emptyList(); left=emptyList(); right=emptyList()
        }
        isLoadingWords = false
    }
    val running=left.isNotEmpty()&&leftToRight.size<left.size&&!showRulesDialog
    LaunchedEffect(running){while(running){delay(1000);elapsed++}}
    LaunchedEffect(leftToRight.size,left.size){
        if(left.isNotEmpty()&&leftToRight.size==left.size){
            showVictory=true
            if(!uploadedThisRound){
                uploadedThisRound=true
                val signature = sourceWords.sortedBy { it.id }.joinToString("|") { "${it.english}=${it.chinese}" }
                friendViewModel.uploadGameResult(
                    gameKey = "word_match",
                    groupSignature = signature,
                    pairCount = left.size,
                    elapsedSeconds = elapsed,
                    errors = errors
                ) { rank -> myRank = rank }
            }
        }
    }
    LaunchedEffect(pulseId){ if(pulseId!=-1){ delay(180); pulseId=-1 } }

    val bg = MaterialTheme.colorScheme.surface
    fun isLeftPaired(id:Int)=leftToRight.containsKey(id)
    fun isRightPaired(id:Int)=leftToRight.values.contains(id)
    fun clearSel(){selectedSide="";selectedId=-1}

    fun leftBg(id:Int)=when{isLeftPaired(id)->leftPairColor[id]?:bg;selectedSide=="L"&&selectedId==id->pairColorForWordId(id);else->bg}
    fun rightBg(id:Int)=when{isRightPaired(id)->rightPairColor[id]?:bg;selectedSide=="R"&&selectedId==id->pairColorForWordId(id);else->bg}

    fun clickLeft(id:Int){
        if(isLeftPaired(id))return
        if(selectedSide.isEmpty()){selectedSide="L";selectedId=id;return}
        if(selectedSide=="L"){if(selectedId==id)clearSel() else selectedId=id;return}
        val rid=selectedId
        if(id==rid){val c=pairColorForWordId(id);leftToRight[id]=rid;leftPairColor[id]=c;rightPairColor[rid]=c;pulseId=id}else{errors++}
        clearSel()
    }

    fun clickRight(id:Int){
        if(isRightPaired(id))return
        if(selectedSide.isEmpty()){selectedSide="R";selectedId=id;return}
        if(selectedSide=="R"){if(selectedId==id)clearSel() else selectedId=id;return}
        val lid=selectedId
        if(lid==id){val c=pairColorForWordId(id);leftToRight[lid]=id;leftPairColor[lid]=c;rightPairColor[id]=c;pulseId=id}else{errors++}
        clearSel()
    }

    Header("单词连连看",onBack){IconButton(onClick={showRulesDialog=true}){ Text("💡") }}
    if(isLoadingWords){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){CircularProgressIndicator()};return}
    if(left.isEmpty()){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text("单词不足，至少需要 10 个单词")};return}
    Row(Modifier.fillMaxWidth().padding(horizontal=16.dp),Arrangement.SpaceBetween){Text("进度：${leftToRight.size}/${left.size}",fontWeight=FontWeight.SemiBold);Text("用时：${elapsed}s",fontWeight=FontWeight.SemiBold)}
    Row(Modifier.fillMaxWidth().padding(horizontal=16.dp),Arrangement.SpaceBetween,Alignment.CenterVertically){
        Text("错误：$errors",fontWeight=FontWeight.SemiBold,color=MaterialTheme.colorScheme.error)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
            TextButton(onClick={ resetKeepWords() }){ Text("同组重开") }
            TextButton(onClick={ reloadToken++ }){ Text("新抽10词") }
        }
    }
    GameRuleHint("左右配对英文与中文，全部连对即通关；规则弹窗打开时计时暂停")
    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxSize().padding(horizontal=16.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)){
        LazyColumn(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp),contentPadding=PaddingValues(bottom=16.dp)){items(left){w->Card(shape=RoundedCornerShape(10.dp),colors=CardDefaults.cardColors(containerColor=leftBg(w.id)),modifier=Modifier.fillMaxWidth().scale(if(pulseId==w.id)1.06f else 1f).clickable{clickLeft(w.id)}){Text(w.english,Modifier.padding(12.dp),fontWeight=FontWeight.Medium)}}}
        LazyColumn(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp),contentPadding=PaddingValues(bottom=16.dp)){items(right){w->Card(shape=RoundedCornerShape(10.dp),colors=CardDefaults.cardColors(containerColor=rightBg(w.id)),modifier=Modifier.fillMaxWidth().scale(if(pulseId==w.id)1.06f else 1f).clickable{clickRight(w.id)}){Text(w.chinese,Modifier.padding(12.dp),fontWeight=FontWeight.Medium)}}}
    }
    if(showRulesDialog){
        AlertDialog(onDismissRequest={showRulesDialog=false},title={Text("单词连连看 规则")},text={Text("从左列英文和右列中文中点击配对。配对错误会计入错误次数。全部配对完成即通关。规则弹窗打开时倒计时暂停。")},confirmButton={TextButton(onClick={showRulesDialog=false}){Text("知道了")}})
    }

    if(showVictory){
        AlertDialog(
            onDismissRequest={showVictory=false},
            title={ Text("挑战成功", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, fontSize = 22.sp) },
            text={
                Text(
                    "用时 ${elapsed}s · 错误 $errors 次" + if (myRank != null) "\n\n好友第 ${myRank} 名" else "",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            confirmButton={
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick={resetKeepWords();showVictory=false}){Text("同组重开")}
                        TextButton(onClick={reloadToken++;showVictory=false}){Text("新抽10词")}
                        TextButton(onClick={showVictory=false;onBackToMenu()}){Text("返回菜单")}
                    }
                }
            },
            dismissButton={}
        )
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LetterReorderGame(onBack:()->Unit, friendViewModel: FriendViewModel){
    val context=LocalContext.current
    var items by remember{mutableStateOf<List<MatchWordItem>>(emptyList())}
    var idx by remember{mutableIntStateOf(0)}
    var letters by remember{mutableStateOf<List<Char>>(emptyList())}
    var picked by remember{mutableIntStateOf(-1)}
    var elapsed by remember{mutableIntStateOf(0)}
    var errors by remember{mutableIntStateOf(0)}
    var skips by remember{mutableIntStateOf(0)}
    var loading by remember{mutableStateOf(true)}
    var win by remember{mutableStateOf(false)}
    var showSkipDialog by remember{mutableStateOf(false)}
    var showRulesDialog by remember{mutableStateOf(false)}
    var sourceItems by remember{mutableStateOf<List<MatchWordItem>>(emptyList())}
    var myRank by remember{mutableStateOf<Int?>(null)}
    var uploadedThisRound by remember{mutableStateOf(false)}

    fun resetRound(i:Int){
        val t=items.getOrNull(i)?.english?.lowercase()?.filter{it.isLetter()}.orEmpty()
        if(t.isEmpty()) return
        var shuffled=t.toList().shuffled(); var guard=0
        while(shuffled.joinToString("")==t&&guard<6){shuffled=t.toList().shuffled();guard++}
        letters=shuffled
        picked=-1
    }

    fun moveCurrentToEnd(){
        if(items.isEmpty()) return
        val m=items.toMutableList()
        val cur=m.removeAt(idx)
        m.add(cur)
        items=m
        if(idx>=items.size) idx=0
        resetRound(idx)
    }

    LaunchedEffect(Unit){
        loading=true
        val all=LexiDatabase.getDatabase(context).wordDao().getAllWords().first()
        items=all.distinctBy{"${it.english.trim().lowercase()}|${it.chinese.trim()}"}
            .filter{it.english.count{c->c.isLetter()} in 4..9}
            .shuffled().take(10).mapIndexed{i,w->MatchWordItem(i+1,w.english,w.chinese)}
        sourceItems = items
        idx=0; elapsed=0; errors=0; skips=0; win=false; showSkipDialog=false; myRank=null; uploadedThisRound=false; resetRound(0); loading=false
    }

    val running=!loading&&!win&&items.isNotEmpty()&&!showSkipDialog&&!showRulesDialog
    LaunchedEffect(running){while(running){delay(1000);elapsed++}}

    LaunchedEffect(win){
        if(win && !uploadedThisRound && sourceItems.isNotEmpty()){
            uploadedThisRound = true
            val signature = sourceItems.sortedBy { it.id }.joinToString("|") { "${it.english}=${it.chinese}" }
            val weightedErrors = errors + skips
            friendViewModel.uploadGameResult(
                gameKey = "letter_reorder",
                groupSignature = signature,
                pairCount = sourceItems.size,
                elapsedSeconds = elapsed,
                errors = weightedErrors
            ) { rank -> myRank = rank }
        }
    }

    Header("字母重组",onBack){IconButton(onClick={showRulesDialog=true}){ Text("💡") }}
    if(loading){Box(Modifier.fillMaxSize(),Alignment.Center){CircularProgressIndicator()};return}
    if(items.isEmpty()){Box(Modifier.fillMaxSize(),Alignment.Center){Text("单词不足，至少需要 10 个单词")};return}

    val item=items[idx]
    val target=item.english.lowercase().filter{it.isLetter()}
    Column(Modifier.fillMaxSize().padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
        Text("提示：${item.chinese}",fontWeight=FontWeight.SemiBold,fontSize=20.sp)
        Text("进度：${idx+1}/${items.size}   用时：${elapsed}s   错误：$errors   跳过：$skips",color=MaterialTheme.colorScheme.onSurfaceVariant)
        GameRuleHint("交换字母后点击提交判定；可跳过并显示答案，全部完成即通关")
        Spacer(Modifier.height(16.dp))
        FlowRow(modifier=Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp,Alignment.CenterHorizontally),verticalArrangement=Arrangement.spacedBy(8.dp)){
            letters.forEachIndexed{i,c->
                val on=i==picked
                Box(
                    Modifier.size(46.dp)
                        .background(Color(0xFFF2F2F2), RoundedCornerShape(8.dp))
                        .border(1.dp, if(on) MaterialTheme.colorScheme.primary else Color(0xFFBDBDBD), RoundedCornerShape(8.dp))
                        .clickable{
                            if(picked==-1) picked=i
                            else if(picked==i) picked=-1
                            else{
                                val m=letters.toMutableList(); val t=m[picked]; m[picked]=m[i]; m[i]=t; letters=m; picked=-1
                            }
                        },
                    contentAlignment=Alignment.Center
                ){
                    Text(c.toString(),color=MaterialTheme.colorScheme.onSurface,fontWeight=FontWeight.Bold,fontSize=20.sp)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
            TextButton(onClick={resetRound(idx)}){Text("重排本题")}
            TextButton(onClick={showSkipDialog=true}){Text("跳过")}
            Button(onClick={
                if(letters.joinToString("")==target){
                    if(idx==items.lastIndex) win=true else { idx++; resetRound(idx) }
                } else {
                    errors++
                }
            }){Text("提交")}
        }
    }

    if(showRulesDialog){
        AlertDialog(onDismissRequest={showRulesDialog=false},title={Text("字母重组 规则")},text={Text("根据中文提示，通过交换字母还原单词。只有点击提交才判断对错。可跳过并查看正确答案。规则弹窗打开时倒计时暂停。")},confirmButton={TextButton(onClick={showRulesDialog=false}){Text("知道了")}})
    }

    if(showSkipDialog){
        AlertDialog(
            onDismissRequest={showSkipDialog=false},
            title={Text("已跳过")},
            text={Text("正确答案：$target")},
            confirmButton={TextButton(onClick={skips++;showSkipDialog=false;moveCurrentToEnd()}){Text("下一个")}}
        )
    }

    if(win){
        AlertDialog(onDismissRequest={win=false},title={Text("挑战成功")},text={Text("完成 ${items.size} 题\n用时 ${elapsed}s\n错误 $errors 次\n跳过 $skips 次\n" + if(myRank!=null) "好友第 ${myRank} 名" else "好友排名统计中...")},confirmButton={TextButton(onClick={win=false;onBack()}){Text("返回菜单")}})
    }
}


@Composable
private fun FivesGame(onBack:()->Unit, friendViewModel: FriendViewModel){
    val context=LocalContext.current
    val modes = remember {
        listOf(
            FivesMode("fives_4", "5次机会猜4字母", 5, 4),
            FivesMode("fives_5", "6次机会猜5字母", 6, 5),
            FivesMode("fives_6", "7次机会猜6字母", 7, 6)
        )
    }

    var selectedMode by remember { mutableStateOf<FivesMode?>(null) }
    var cands by remember{mutableStateOf<List<String>>(emptyList())}
    var ans by remember{mutableStateOf("")}
    var tries by remember{mutableStateOf<List<String>>(emptyList())}
    var input by remember{mutableStateOf("")}
    var elapsed by remember{mutableIntStateOf(0)}
    var loading by remember{mutableStateOf(true)}
    var win by remember{mutableStateOf(false)}
    var lose by remember{mutableStateOf(false)}
    var showRulesDialog by remember{mutableStateOf(false)}
    var myRank by remember { mutableStateOf<Int?>(null) }
    var uploadedThisRound by remember { mutableStateOf(false) }

    fun restart(){
        if(cands.isNotEmpty()) ans=cands.random()
        tries=emptyList(); input=""; elapsed=0; win=false; lose=false; myRank=null; uploadedThisRound=false
    }

    fun marks(g:String,a:String):List<Int>{
        val n = a.length
        val r=MutableList(n){0}
        val rem=a.toMutableList()
        for(i in 0 until n){ if(g[i]==a[i]){ r[i]=2; rem[i]='*' } }
        for(i in 0 until n){ if(r[i]==0){ val k=rem.indexOf(g[i]); if(k>=0){ r[i]=1; rem[k]='*' } } }
        return r
    }

    LaunchedEffect(Unit){
        loading=true
        val all=LexiDatabase.getDatabase(context).wordDao().getAllWords().first()
        FivesCandidatesCache.ensure(all)
        loading=false
    }

    LaunchedEffect(selectedMode){
        val mode = selectedMode ?: return@LaunchedEffect
        cands = FivesCandidatesCache.getCandidates(mode.wordLength)
        restart()
    }

    val running=!loading&&selectedMode!=null&&!win&&!lose&&!showRulesDialog
    LaunchedEffect(running){while(running){delay(1000);elapsed++}}

    LaunchedEffect(win){
        val mode = selectedMode ?: return@LaunchedEffect
        if(win && !uploadedThisRound){
            uploadedThisRound = true
            val wrongGuesses = (tries.size - 1).coerceAtLeast(0)
            friendViewModel.uploadGameResult(
                gameKey = mode.id,
                groupSignature = "len=${mode.wordLength}|chances=${mode.chances}",
                pairCount = 1,
                elapsedSeconds = elapsed,
                errors = wrongGuesses
            ) { rank -> myRank = rank }
        }
    }

    Header("Fives",onBack){IconButton(onClick={showRulesDialog=true}){ Text("💡") }}
    if(showRulesDialog){
        AlertDialog(onDismissRequest={showRulesDialog=false},title={Text("Fives 规则")},text={Text("先选择模式：4字母5次、5字母6次、6字母7次。\n每次输入后点提交：绿色=字母和位置都正确；黄色=字母存在但位置错误；灰色=该字母不在答案中。\n在限定次数内猜中即胜利，否则失败并显示答案。\n规则弹窗打开时倒计时暂停。")},confirmButton={TextButton(onClick={showRulesDialog=false}){Text("知道了")}})
    }
    if(loading){Box(Modifier.fillMaxSize(),Alignment.Center){CircularProgressIndicator()};return}

    if(selectedMode==null){
        Column(Modifier.fillMaxSize().padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
            Text("选择模式", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(18.dp))
            modes.forEach { mode ->
                Button(onClick={ selectedMode = mode },shape = RoundedCornerShape(8.dp),modifier = Modifier.fillMaxWidth(0.78f).padding(vertical = 6.dp)) { Text(mode.title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold) }
            }
        }
        return
    }

    val mode = selectedMode!!
    if(ans.isBlank()){
        Box(Modifier.fillMaxSize(),Alignment.Center){Text("暂无可用${mode.wordLength}字母词")}
        return
    }

    Column(Modifier.fillMaxSize().padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally){
        Text("${mode.chances}次机会猜中${mode.wordLength}字母单词",color=MaterialTheme.colorScheme.onSurfaceVariant)
        Text("用时：${elapsed}s",color=MaterialTheme.colorScheme.onSurfaceVariant)
        GameRuleHint("绿色位置正确，黄色字母存在但位置错，灰色表示字母不存在")
        Spacer(Modifier.height(12.dp))

        repeat(mode.chances){row->
            val g=tries.getOrNull(row)
            val m=if(g!=null)marks(g,ans) else List(mode.wordLength){-1}
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                repeat(mode.wordLength){c->
                    val ch=g?.getOrNull(c)?.uppercase() ?: ""
                    val bg=when(m[c]){2->Color(0xFF2E7D32);1->Color(0xFFF9A825);0->Color(0xFF757575);else->MaterialTheme.colorScheme.surfaceVariant}
                    Box(Modifier.size(44.dp).background(bg,RoundedCornerShape(8.dp)),Alignment.Center){Text(ch,color=if(m[c]==-1)MaterialTheme.colorScheme.onSurface else Color.White,fontWeight=FontWeight.Bold)}
                }
            }
            Spacer(Modifier.height(6.dp))
        }

        OutlinedTextField(value=input,onValueChange={input=it.lowercase().filter{c->c.isLetter()}.take(mode.wordLength)},singleLine=true,label={Text("输入${mode.wordLength}字母")})
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick={ val g=input; if(g.length==mode.wordLength){ val next=tries+g; tries=next; input=""; if(g==ans)win=true else if(next.size>=mode.chances)lose=true } }){Text("提交")}
            TextButton(onClick={ selectedMode=null }){Text("切换模式")}
        }
    }

    if(win){
        AlertDialog(
            onDismissRequest={win=false},
            title={Text("猜中了")},
            text={Text("答案：${ans.uppercase()}\n用时 ${elapsed}s\n" + if(myRank!=null) "好友第 ${myRank} 名" else "好友排名统计中...")},
            confirmButton={TextButton(onClick={restart()}){Text("再来一局")}},
            dismissButton={TextButton(onClick={win=false;onBack()}){Text("返回菜单")}}
        )
    }
    if(lose){
        AlertDialog(onDismissRequest={lose=false},title={Text("挑战结束")},text={Text("正确答案：${ans.uppercase()}")},confirmButton={TextButton(onClick={restart()}){Text("再来一局")}},dismissButton={TextButton(onClick={lose=false;onBack()}){Text("返回菜单")}})
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordSearchGame(onBack:()->Unit){
    val context = LocalContext.current
    val size = 7

    var targetItems by remember { mutableStateOf<List<MatchWordItem>>(emptyList()) }
    var grid by remember { mutableStateOf(List(size){MutableList(size){'a'}}) }
    var selected by remember { mutableStateOf<List<Pair<Int,Int>>>(emptyList()) }
    var foundEnglish by remember { mutableStateOf<Set<String>>(emptySet()) }
    var foundCells by remember { mutableStateOf<Set<Pair<Int,Int>>>(emptySet()) }
    var expectedPaths by remember { mutableStateOf<Map<String, List<Pair<Int,Int>>>>(emptyMap()) }
    var placements by remember { mutableStateOf<Map<String, WordPlacement>>(emptyMap()) }
    var elapsed by remember { mutableIntStateOf(0) }
    var win by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var reloadToken by remember { mutableIntStateOf(0) }
    var cachedPool by remember { mutableStateOf<List<MatchWordItem>>(emptyList()) }
    var poolLoaded by remember { mutableStateOf(false) }
    var gaveUp by remember { mutableStateOf(false) }
    var missedCells by remember { mutableStateOf<Set<Pair<Int,Int>>>(emptySet()) }

    fun normalizeEnglish(s:String)=s.lowercase().filter { it.isLetter() }

    data class BuildResult(
        val board: List<MutableList<Char>>,
        val paths: Map<String, List<Pair<Int,Int>>>,
        val placements: Map<String, WordPlacement>
    )

    fun tryPlaceWords(words: List<String>): BuildResult? {
        if (words.isEmpty()) return null
        val board = MutableList(size){ MutableList(size){ '#' } }
        val paths = mutableMapOf<String, List<Pair<Int,Int>>>()
        val placements = mutableMapOf<String, WordPlacement>()

        fun canPlace(word:String, row:Int, col:Int, horizontal:Boolean):Boolean {
            return word.indices.all { i ->
                val r = if (horizontal) row else row + i
                val c = if (horizontal) col + i else col
                r in 0 until size && c in 0 until size && (board[r][c] == '#' || board[r][c] == word[i])
            }
        }

        fun doPlace(raw:String, placeWord:String, row:Int, col:Int, horizontal:Boolean, reversed:Boolean) {
            val placedCells = mutableListOf<Pair<Int,Int>>()
            placeWord.indices.forEach { i ->
                val r = if (horizontal) row else row + i
                val c = if (horizontal) col + i else col
                board[r][c] = placeWord[i]
                placedCells += (r to c)
            }
            val canonicalPath = if (reversed) placedCells.reversed() else placedCells
            paths[raw] = canonicalPath
            placements[raw] = WordPlacement(
                start = canonicalPath.first(),
                end = canonicalPath.last(),
                axis = if (horizontal) "H" else "V",
                reversed = reversed
            )
        }

        val sortedWords = words.distinct().sortedByDescending { it.length }
        for (raw in sortedWords) {
            val reversed = (0..1).random() == 1
            val placeWord = if (reversed) raw.reversed() else raw
            var placed = false
            repeat(120) {
                val horizontal = (0..1).random() == 1
                val maxRow = if (horizontal) size - 1 else size - placeWord.length
                val maxCol = if (horizontal) size - placeWord.length else size - 1
                if (maxRow < 0 || maxCol < 0) return@repeat
                val row = (0..maxRow).random()
                val col = (0..maxCol).random()
                if (canPlace(placeWord, row, col, horizontal)) {
                    doPlace(raw, placeWord, row, col, horizontal, reversed)
                    placed = true
                    return@repeat
                }
            }
            if (!placed) return null
        }

        for (r in 0 until size) {
            for (c in 0 until size) {
                if (board[r][c] == '#') board[r][c] = listOf('q','x','z','v','j','k','w').random()
            }
        }

        fun findOccurrences(word: String): List<List<Pair<Int,Int>>> {
            val result = mutableListOf<List<Pair<Int,Int>>>()
            val rev = word.reversed()

            for (r in 0 until size) {
                for (cStart in 0..(size - word.length)) {
                    val cells = word.indices.map { i -> r to (cStart + i) }
                    val s = cells.joinToString("") { (rr,cc) -> board[rr][cc].toString() }
                    if (s == word || s == rev) result += cells
                }
            }

            for (c in 0 until size) {
                for (rStart in 0..(size - word.length)) {
                    val cells = word.indices.map { i -> (rStart + i) to c }
                    val s = cells.joinToString("") { (rr,cc) -> board[rr][cc].toString() }
                    if (s == word || s == rev) result += cells
                }
            }
            return result
        }

        fun samePath(a: List<Pair<Int,Int>>, b: List<Pair<Int,Int>>): Boolean = (a == b || a == b.reversed())

        val allAnswerCells = paths.values.flatten().toSet()
        val rareLetters = listOf('q','x','z','v','j','k','w')

        repeat(120) {
            var changed = false
            for (word in words.distinct()) {
                val answerPath = paths[word] ?: return null
                val extras = findOccurrences(word).filterNot { samePath(it, answerPath) }
                for (occ in extras) {
                    val mutableCell = occ.firstOrNull { it !in allAnswerCells } ?: return null
                    val (rr, cc) = mutableCell
                    val current = board[rr][cc]
                    board[rr][cc] = rareLetters.firstOrNull { it != current } ?: 'q'
                    changed = true
                }
            }
            if (!changed) return@repeat
        }

        // final strict check: each target word has exactly one valid occurrence (its answer path)
        val ok = words.distinct().all { word ->
            val answerPath = paths[word] ?: return null
            val occ = findOccurrences(word)
            occ.size == 1 && samePath(occ.first(), answerPath)
        }
        if (!ok) return null

        return BuildResult(board, paths, placements)
    }

    fun buildFrom(items: List<MatchWordItem>) {
        val clean = items
            .map { MatchWordItem(it.id, normalizeEnglish(it.english), it.chinese.trim()) }
            .filter { it.english.length in 3..size && it.chinese.isNotBlank() }
            .distinctBy { it.english }

        var builtGrid: BuildResult? = null
        var builtItems: List<MatchWordItem> = emptyList()

        val maxPick = minOf(4, clean.size)
        for (pick in maxPick downTo 3) {
            repeat(120) {
                val sample = clean.shuffled().take(pick)
                val tryGrid = tryPlaceWords(sample.map { it.english })
                if (tryGrid != null) {
                    builtGrid = tryGrid
                    builtItems = sample
                    return@repeat
                }
            }
            if (builtGrid != null) break
        }

        if (builtGrid == null) {
            val fallback = listOf(
                MatchWordItem(1, "cat", "猫"),
                MatchWordItem(2, "dog", "狗"),
                MatchWordItem(3, "sun", "太阳")
            )
            builtItems = fallback
            var fb: BuildResult? = null
            repeat(80) {
                val attempt = tryPlaceWords(fallback.map { it.english })
                if (attempt != null) {
                    fb = attempt
                    return@repeat
                }
            }
            builtGrid = fb ?: BuildResult(
                board = List(size) { MutableList(size) { 'x' } },
                paths = mapOf(
                    "cat" to listOf(0 to 0, 0 to 1, 0 to 2),
                    "dog" to listOf(2 to 0, 2 to 1, 2 to 2),
                    "sun" to listOf(4 to 0, 4 to 1, 4 to 2)
                ),
                placements = mapOf(
                    "cat" to WordPlacement(0 to 0, 0 to 2, "H", false),
                    "dog" to WordPlacement(2 to 0, 2 to 2, "H", false),
                    "sun" to WordPlacement(4 to 0, 4 to 2, "H", false)
                )
            )
            if (fb == null) {
                val m = builtGrid!!.board.map { it.toMutableList() }
                m[0][0]='c';m[0][1]='a';m[0][2]='t'
                m[2][0]='d';m[2][1]='o';m[2][2]='g'
                m[4][0]='s';m[4][1]='u';m[4][2]='n'
                builtGrid = BuildResult(m, builtGrid!!.paths, builtGrid!!.placements)
            }
        }

        targetItems = builtItems
        grid = builtGrid!!.board
        expectedPaths = builtGrid!!.paths
        placements = builtGrid!!.placements
        selected = emptyList()
        foundEnglish = emptySet()
        foundCells = emptySet()
        gaveUp = false
        missedCells = emptySet()
        elapsed = 0
        win = false
        loading = false
    }

    LaunchedEffect(reloadToken){
        loading = true
        val all = LexiDatabase.getDatabase(context).wordDao().getAllWords().first()
        val pool = all.mapIndexed { i, w -> MatchWordItem(i+1, w.english, w.chinese) }
        buildFrom(pool)
    }

    val running = !loading && !win && !gaveUp && !showRulesDialog
    LaunchedEffect(running){ while(running){ delay(1000); elapsed++ } }

    Header("Word Search",onBack){IconButton(onClick={showRulesDialog=true}){ Text("💡") }}
    if(loading){ Box(Modifier.fillMaxSize(), Alignment.Center){ CircularProgressIndicator() }; return }

    Column(Modifier.fillMaxSize().padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally){
        Text("目标释义（在下方找英文）", color=MaterialTheme.colorScheme.onSurfaceVariant)
        Text("已找到：${foundEnglish.size}/${targetItems.size}   用时：${elapsed}s",color=MaterialTheme.colorScheme.onSurfaceVariant)
        GameRuleHint("7×7网格；按点选顺序取词；每个目标词仅一条正确路径（词间可共享格子）")
        Spacer(Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            targetItems.forEach { item ->
                val hit = foundEnglish.contains(item.english)
                val miss = gaveUp && !hit
                val chipBg = when {
                    hit -> Color(0xFF43A047)
                    miss -> Color(0xFFE53935)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                Surface(
                    color = chipBg,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(item.chinese, color = if (hit || miss) Color.White else MaterialTheme.colorScheme.onSurface)
                        if (hit || miss) {
                            Text(item.english, fontSize = 11.sp, color = Color.White.copy(alpha = 0.95f))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement=Arrangement.spacedBy(6.dp)){
            repeat(size){ r ->
                Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                    repeat(size){ c ->
                        val cell = r to c
                        val picking = selected.contains(cell)
                        val done = foundCells.contains(cell)
                        val miss = missedCells.contains(cell)
                        val bg = when {
                            done -> Color(0xFF43A047)
                            miss -> Color(0xFFE53935)
                            picking -> Color(0xFFFFB300)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        Box(
                            Modifier.size(38.dp)
                                .background(bg, RoundedCornerShape(8.dp))
                                .clickable { selected = if (picking) selected - cell else selected + cell },
                            Alignment.Center
                        ){
                            Text(grid[r][c].toString(), color = if (done || miss || picking) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        fun lineWordBySelectionOrder(cells: List<Pair<Int,Int>>): String? {
            if (cells.isEmpty()) return null
            if (cells.size == 1) return grid[cells[0].first][cells[0].second].toString()

            val dr0 = cells[1].first - cells[0].first
            val dc0 = cells[1].second - cells[0].second
            val validFirstStep = (dr0 == 0 && (dc0 == 1 || dc0 == -1)) || (dc0 == 0 && (dr0 == 1 || dr0 == -1))
            if (!validFirstStep) return null

            for (i in 1 until cells.size) {
                val dr = cells[i].first - cells[i-1].first
                val dc = cells[i].second - cells[i-1].second
                if (dr != dr0 || dc != dc0) return null
            }

            return cells.joinToString("") { (rr,cc) -> grid[rr][cc].toString() }
        }

        val now = lineWordBySelectionOrder(selected)
        Text("当前：${now ?: "(请选择同行或同列连续字母，按点选顺序显示)"}")

        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
            TextButton(onClick={selected=emptyList()}){Text("清空")}
            TextButton(onClick={reloadToken++}){Text("换一组")}
            TextButton(
                onClick={
                    if (!gaveUp) {
                        gaveUp = true
                        selected = emptyList()
                        val missWords = targetItems.map { it.english }.filter { !foundEnglish.contains(it) }.toSet()
                        missedCells = expectedPaths.filterKeys { missWords.contains(it) }
                            .values
                            .flatten()
                            .toSet()
                    }
                },
                enabled = !gaveUp
            ){Text("放弃")}
            if (gaveUp) {
                Button(onClick={onBack()}, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))){Text("退出", color = Color.White)}
            } else {
                Button(onClick={
                    val word = lineWordBySelectionOrder(selected)
                    if (word != null) {
                        val hit = targetItems.firstOrNull { item ->
                            val path = expectedPaths[item.english] ?: return@firstOrNull false
                            (selected == path || selected == path.reversed()) && !foundEnglish.contains(item.english)
                        }?.english
                        if (hit != null) {
                            foundEnglish = foundEnglish + hit
                            foundCells = foundCells + selected.toSet()
                            selected = emptyList()
                            if (foundEnglish.size == targetItems.size) win = true
                        }
                    }
                }){Text("确认")}
            }
        }
    }

    if(showRulesDialog){
        AlertDialog(
            onDismissRequest={showRulesDialog=false},
            title={Text("Word Search 规则")},
            text={Text("上方显示中文释义列表，你需要在 7×7 字母网格中找出对应英文单词。\n选择方式必须是同一行或同一列的连续字母，支持反向点选。\n“当前”按你的点选顺序显示。\n每个目标词在网格中仅有一条正确路径（不同目标词可以共享格子）。\n点击“放弃”会暂停计时，并将未找到词与网格答案标红；此时确认位会变为红色“退出”。\n选好后点“确认”，命中会高亮并计入进度；全部找齐即通关。\n规则弹窗打开时倒计时暂停。")},
            confirmButton={TextButton(onClick={showRulesDialog=false}){Text("知道了")}}
        )
    }

    if(win){
        AlertDialog(
            onDismissRequest={win=false},
            title={Text("挑战成功")},
            text={Text("全部找齐，用时 ${elapsed}s")},
            confirmButton={TextButton(onClick={reloadToken++}){Text("再来一局")}},
            dismissButton={TextButton(onClick={win=false;onBack()}){Text("返回菜单")}}
        )
    }
}


@Composable
private fun GuessWhatGame(onBack:()->Unit){
    data class Q(val answer:String,val clues:List<String>)
    val qs=remember{listOf(Q("football",listOf("grass","goal","offside")),Q("hospital",listOf("doctor","nurse","emergency")),Q("library",listOf("books","quiet","borrow")),Q("airport",listOf("plane","passport","terminal")),Q("kitchen",listOf("cook","pan","fridge")))}
    var idx by remember{mutableIntStateOf(0)};var clue by remember{mutableIntStateOf(1)};var input by remember{mutableStateOf("")};var errors by remember{mutableIntStateOf(0)};var elapsed by remember{mutableIntStateOf(0)};var win by remember{mutableStateOf(false)};var showRulesDialog by remember{mutableStateOf(false)}
    fun reset(){idx=0;clue=1;input="";errors=0;elapsed=0;win=false}
    val running=!win&&!showRulesDialog
    LaunchedEffect(running){while(running){delay(1000);elapsed++}}
    Header("GuessWhat",onBack){IconButton(onClick={showRulesDialog=true}){ Text("💡") }}
    val q=qs[idx]
    Column(Modifier.fillMaxSize().padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
        Text("第 ${idx+1}/${qs.size} 题",color=MaterialTheme.colorScheme.onSurfaceVariant);Text("用时：${elapsed}s   错误：$errors",color=MaterialTheme.colorScheme.onSurfaceVariant);GameRuleHint("线索词是提示，不是备选答案；输入它们共同指向的目标英文单词") ;Spacer(Modifier.height(10.dp))
        q.clues.take(clue).forEach{Text("• $it",fontSize=20.sp,fontWeight=FontWeight.SemiBold)}
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){TextButton(onClick={if(clue<q.clues.size)clue++}){Text("更多提示")};TextButton(onClick={clue=1}){Text("重置提示")}}
        OutlinedTextField(value=input,onValueChange={input=it.lowercase().trim()},singleLine=true,label={Text("输入答案")});Spacer(Modifier.height(8.dp))
        Button(onClick={if(input==q.answer){if(idx==qs.lastIndex)win=true else{idx++;clue=1;input=""}} else errors++}){Text("提交")}
    }
    if(showRulesDialog){
        AlertDialog(
            onDismissRequest={showRulesDialog=false},
            title={Text("GuessWhat 详细规则")},
            text={
                Text(
                    "玩法目标：\n" +
                    "每一题都有一个隐藏的目标英文单词（answer）。你看到的多个词（如 grass / goal / offside）只是线索，不是答案选项。\n\n" +
                    "你要做什么：\n" +
                    "根据当前显示的线索，输入它们共同指向的那个英文单词，然后点击“提交”。\n\n" +
                    "例子：\n" +
                    "线索：grass / goal / offside\n" +
                    "应输入：football\n\n" +
                    "判定规则：\n" +
                    "1) 提交后若输入与目标词完全一致（当前为小写匹配）则本题通过，进入下一题；\n" +
                    "2) 若不一致，错误次数 +1，本题继续；\n" +
                    "3) 完成最后一题后通关。\n\n" +
                    "提示系统：\n" +
                    "- “更多提示”：在本题增加一条线索，帮助缩小范围；\n" +
                    "- “重置提示”：把本题线索数量恢复到初始状态。\n\n" +
                    "计时说明：\n" +
                    "- 计时从开局开始，通关结束；\n" +
                    "- 规则弹窗打开时计时暂停。"
                )
            },
            confirmButton={TextButton(onClick={showRulesDialog=false}){Text("知道了")}}
        )
    }
    if(win){AlertDialog(onDismissRequest={win=false},title={Text("挑战成功")},text={Text("完成 ${qs.size} 题\n用时 ${elapsed}s\n错误 $errors 次")},confirmButton={TextButton(onClick={reset()}){Text("再来一局")}},dismissButton={TextButton(onClick={win=false;onBack()}){Text("返回菜单")}})}
}

@Composable
private fun GameLeaderboardScreen(friendViewModel: FriendViewModel, onBack: () -> Unit) {
    val games = listOf(
        "word_match" to "单词连连看",
        "hello_word" to "Hello Word",
        "letter_reorder" to "字母重组",
        "fives_4" to "Fives 4字母",
        "fives_5" to "Fives 5字母",
        "fives_6" to "Fives 6字母",
        "word_search" to "Word Search",
        "guess_what" to "GuessWhat"
    )

    var gameKey by remember { mutableStateOf("word_match") }
    var gameMenuExpanded by remember { mutableStateOf(false) }
    var metric by remember { mutableStateOf("cleared") }
    val st by friendViewModel.state.collectAsState()
    val board = st.leaderboardByMetric[metric]
    val isFives = gameKey.startsWith("fives_")

    LaunchedEffect(gameKey) {
        if (isFives && metric == "accuracy") metric = "cleared"
    }
    LaunchedEffect(gameKey, metric) { friendViewModel.fetchGameLeaderboard(gameKey, metric) }

    Header("好友排行榜", onBack) {
        Box {
            TextButton(onClick = { gameMenuExpanded = true }) {
                Text(games.firstOrNull { it.first == gameKey }?.second ?: "选择游戏")
            }
            DropdownMenu(expanded = gameMenuExpanded, onDismissRequest = { gameMenuExpanded = false }) {
                games.forEach { (key, label) ->
                    DropdownMenuItem(text = { Text(label) }, onClick = { gameKey = key; gameMenuExpanded = false })
                }
            }
        }
    }

    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = metric == "cleared", onClick = { metric = "cleared" }, label = { Text("通关局数") })
        FilterChip(selected = metric == "avg_time", onClick = { metric = "avg_time" }, label = { Text("平均用时") })
        if (!isFives) {
            FilterChip(selected = metric == "accuracy", onClick = { metric = "accuracy" }, label = { Text("正确率") })
        }
    }
    Spacer(Modifier.height(10.dp))

    if (st.isLeaderboardLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val entries = board?.entries ?: emptyList()
    if (entries.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无好友参与", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
        items(entries) { e ->
            Card(shape = RoundedCornerShape(12.dp)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("#${e.rank}  ${e.username}", fontWeight = FontWeight.SemiBold)
                    Text(
                        when (metric) {
                            "cleared" -> "${e.clearedGroups}局"
                            "avg_time" -> "${"%.1f".format(e.avgSeconds)}s"
                            else -> "${"%.1f".format(e.accuracy * 100)}%"
                        },
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}


@Composable
private fun GameRuleHint(text:String){
    Text(
        text = "规则：$text",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
    )
}

@Composable
private fun Header(title:String,onBack:()->Unit,right:@Composable ()->Unit){Row(Modifier.fillMaxWidth().padding(horizontal=8.dp,vertical=8.dp),Arrangement.SpaceBetween,Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"返回")};Text(title,fontSize=20.sp,fontWeight=FontWeight.Bold);right()}}

@Composable
private fun AddFriendDialog(input:String,onInputChange:(String)->Unit,onSearch:()->Unit,onSendRequest:()->Unit,onDismiss:()->Unit,foundUsername:String?,message:String?,error:String?){androidx.compose.ui.window.Dialog(onDismissRequest=onDismiss){Surface(shape=RoundedCornerShape(20.dp),color=MaterialTheme.colorScheme.surface){Column(Modifier.padding(20.dp)){Text("添加好友",fontSize=18.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(12.dp));OutlinedTextField(value=input,onValueChange=onInputChange,singleLine=true,label={Text("输入用户名")},modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(10.dp));Button(onClick=onSearch,modifier=Modifier.fillMaxWidth()){Text("搜索用户")};if(foundUsername!=null){Spacer(Modifier.height(10.dp));Text("找到用户：$foundUsername",color=MaterialTheme.colorScheme.primary);Spacer(Modifier.height(8.dp));Button(onClick=onSendRequest,modifier=Modifier.fillMaxWidth()){Text("发送好友申请")}};if(!error.isNullOrBlank()){Spacer(Modifier.height(8.dp));Text(error,color=MaterialTheme.colorScheme.error,fontSize=12.sp)};if(!message.isNullOrBlank()){Spacer(Modifier.height(8.dp));Text(message,color=MaterialTheme.colorScheme.primary,fontSize=12.sp)};Spacer(Modifier.height(12.dp));OutlinedButton(onClick=onDismiss,modifier=Modifier.fillMaxWidth()){Text("关闭")}}}}}

@Composable
fun GameCard(game:Game,onClick:()->Unit){Card(modifier=Modifier.fillMaxWidth().height(180.dp).clickable(onClick=onClick),shape=RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(containerColor=game.color)){Column(Modifier.fillMaxSize().padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text(game.icon,fontSize=48.sp,modifier=Modifier.padding(bottom=12.dp));Text(game.name,fontSize=16.sp,fontWeight=FontWeight.Bold,color=Color.White,textAlign=TextAlign.Center);Spacer(Modifier.height(8.dp));Text(game.description,fontSize=12.sp,color=Color.White.copy(alpha=0.8f),textAlign=TextAlign.Center,maxLines=2)}}}
