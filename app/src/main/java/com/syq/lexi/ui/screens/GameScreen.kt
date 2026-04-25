package com.syq.lexi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.syq.lexi.notification.DailyReminderManager
import com.syq.lexi.ui.viewmodel.FriendViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

data class Game(val id:Int,val name:String,val description:String,val icon:String,val color:Color)
data class MatchWordItem(val id:Int,val english:String,val chinese:String)
private enum class Page{GAME,FRIENDS,NEW_FRIENDS,WORD_MATCH,LEADERBOARD}

@Composable
fun GameScreen(onMenuClick:()->Unit,innerPadding:PaddingValues,friendViewModel:FriendViewModel,onInGameDetailChanged:(Boolean)->Unit){
    val st by friendViewModel.state.collectAsState();val context=LocalContext.current
    var page by remember{mutableStateOf(Page.GAME)};var showAdd by remember{mutableStateOf(false)}
    var remindTarget by remember{mutableStateOf<Pair<Int,String>?>(null)}
    val games=remember{listOf(
        Game(1,"单词连连看","匹配英文与中文释义","🧩",Color(0xFF6366F1)),
        Game(2,"单词匹配","将单词与其定义进行匹配","🎯",Color(0xFF8B5CF6)),
        Game(3,"听音识词","根据发音选择正确的单词","🎧",Color(0xFFEC4899)),
        Game(4,"单词接龙","根据前一个单词的末尾字母开始新单词","🔗",Color(0xFFF59E0B)),
        Game(5,"快速反应","在规定时间内选择正确的单词含义","⚡",Color(0xFF10B981)),
        Game(6,"单词填空","在句子中填入正确的单词","📝",Color(0xFF3B82F6)))}

    LaunchedEffect(page){ onInGameDetailChanged(page == Page.WORD_MATCH) }
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
                LazyVerticalGrid(columns=GridCells.Fixed(2),modifier=Modifier.fillMaxSize().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(16.dp),horizontalArrangement=Arrangement.spacedBy(16.dp),contentPadding=PaddingValues(bottom=16.dp)){items(games){g->GameCard(g){if(g.id==1)page=Page.WORD_MATCH}}}
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
            Page.WORD_MATCH->WordMatchGame(onBack={page=Page.GAME}, onBackToMenu={page=Page.GAME}, friendViewModel=friendViewModel)
        }
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
    val running=left.isNotEmpty()&&leftToRight.size<left.size
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

    Header("单词连连看",onBack){Spacer(Modifier.size(48.dp))}
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
    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxSize().padding(horizontal=16.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)){
        LazyColumn(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp),contentPadding=PaddingValues(bottom=16.dp)){items(left){w->Card(shape=RoundedCornerShape(10.dp),colors=CardDefaults.cardColors(containerColor=leftBg(w.id)),modifier=Modifier.fillMaxWidth().scale(if(pulseId==w.id)1.06f else 1f).clickable{clickLeft(w.id)}){Text(w.english,Modifier.padding(12.dp),fontWeight=FontWeight.Medium)}}}
        LazyColumn(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp),contentPadding=PaddingValues(bottom=16.dp)){items(right){w->Card(shape=RoundedCornerShape(10.dp),colors=CardDefaults.cardColors(containerColor=rightBg(w.id)),modifier=Modifier.fillMaxWidth().scale(if(pulseId==w.id)1.06f else 1f).clickable{clickRight(w.id)}){Text(w.chinese,Modifier.padding(12.dp),fontWeight=FontWeight.Medium)}}}
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

@Composable
private fun GameLeaderboardScreen(friendViewModel: FriendViewModel, onBack: () -> Unit) {
    var gameKey by remember { mutableStateOf("word_match") }
    var metric by remember { mutableStateOf("cleared") }
    val st by friendViewModel.state.collectAsState()
    val board = st.leaderboardByMetric[metric]

    LaunchedEffect(gameKey, metric) { friendViewModel.fetchGameLeaderboard(gameKey, metric) }

    Header("好友排行榜", onBack) { Spacer(Modifier.size(48.dp)) }
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = gameKey == "word_match", onClick = { gameKey = "word_match" }, label = { Text("单词连连看") })
    }
    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = metric == "cleared", onClick = { metric = "cleared" }, label = { Text("通关组数") })
        FilterChip(selected = metric == "avg_time", onClick = { metric = "avg_time" }, label = { Text("平均用时") })
        FilterChip(selected = metric == "accuracy", onClick = { metric = "accuracy" }, label = { Text("正确率") })
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
                            "cleared" -> "${e.clearedGroups}组"
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
private fun Header(title:String,onBack:()->Unit,right:@Composable ()->Unit){Row(Modifier.fillMaxWidth().padding(horizontal=8.dp,vertical=8.dp),Arrangement.SpaceBetween,Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"返回")};Text(title,fontSize=20.sp,fontWeight=FontWeight.Bold);right()}}

@Composable
private fun AddFriendDialog(input:String,onInputChange:(String)->Unit,onSearch:()->Unit,onSendRequest:()->Unit,onDismiss:()->Unit,foundUsername:String?,message:String?,error:String?){androidx.compose.ui.window.Dialog(onDismissRequest=onDismiss){Surface(shape=RoundedCornerShape(20.dp),color=MaterialTheme.colorScheme.surface){Column(Modifier.padding(20.dp)){Text("添加好友",fontSize=18.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(12.dp));OutlinedTextField(value=input,onValueChange=onInputChange,singleLine=true,label={Text("输入用户名")},modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(10.dp));Button(onClick=onSearch,modifier=Modifier.fillMaxWidth()){Text("搜索用户")};if(foundUsername!=null){Spacer(Modifier.height(10.dp));Text("找到用户：$foundUsername",color=MaterialTheme.colorScheme.primary);Spacer(Modifier.height(8.dp));Button(onClick=onSendRequest,modifier=Modifier.fillMaxWidth()){Text("发送好友申请")}};if(!error.isNullOrBlank()){Spacer(Modifier.height(8.dp));Text(error,color=MaterialTheme.colorScheme.error,fontSize=12.sp)};if(!message.isNullOrBlank()){Spacer(Modifier.height(8.dp));Text(message,color=MaterialTheme.colorScheme.primary,fontSize=12.sp)};Spacer(Modifier.height(12.dp));OutlinedButton(onClick=onDismiss,modifier=Modifier.fillMaxWidth()){Text("关闭")}}}}}

@Composable
fun GameCard(game:Game,onClick:()->Unit){Card(modifier=Modifier.fillMaxWidth().height(180.dp).clickable(onClick=onClick),shape=RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(containerColor=game.color)){Column(Modifier.fillMaxSize().padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text(game.icon,fontSize=48.sp,modifier=Modifier.padding(bottom=12.dp));Text(game.name,fontSize=16.sp,fontWeight=FontWeight.Bold,color=Color.White,textAlign=TextAlign.Center);Spacer(Modifier.height(8.dp));Text(game.description,fontSize=12.sp,color=Color.White.copy(alpha=0.8f),textAlign=TextAlign.Center,maxLines=2)}}}
