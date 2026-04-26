package com.lexi.backend.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.lexi.backend.dto.GuessWhatQuestionDto
import com.lexi.backend.entity.GuessWhatQuestion
import com.lexi.backend.repository.GuessWhatQuestionRepository
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class GuessWhatQuestionService(
    private val repo: GuessWhatQuestionRepository,
    private val objectMapper: ObjectMapper
) {
    data class Seed(
        val answer: String,
        val answerMeaning: String,
        val clues: List<String>,
        val clueMeanings: List<String>
    )

    @PostConstruct
    @Transactional
    fun ensureSeeded() {
        val seeds = seedQuestions()
        if (repo.count() >= seeds.size.toLong()) return

        repo.deleteAll()
        val now = LocalDateTime.now()
        val entities = seeds.map {
            GuessWhatQuestion(
                answer = it.answer,
                answerMeaning = it.answerMeaning,
                cluesJson = objectMapper.writeValueAsString(it.clues),
                clueMeaningsJson = objectMapper.writeValueAsString(it.clueMeanings),
                createdAt = now,
                updatedAt = now
            )
        }
        repo.saveAll(entities)
    }

    fun listQuestions(limit: Int): List<GuessWhatQuestionDto> {
        val max = limit.coerceIn(1, 200)
        return repo.findAll()
            .shuffled()
            .take(max)
            .mapNotNull { e ->
                val clues: List<String> = parseList(e.cluesJson)
                val clueMeanings: List<String> = parseList(e.clueMeaningsJson)
                if (clues.isEmpty() || clueMeanings.size != clues.size || e.answerMeaning.isBlank()) null
                else GuessWhatQuestionDto(
                    answer = e.answer,
                    clues = clues,
                    clueMeanings = clueMeanings,
                    answerMeaning = e.answerMeaning
                )
            }
    }

    private fun parseList(json: String): List<String> = try {
        objectMapper.readValue<List<String>>(json)
    } catch (_: Exception) {
        emptyList()
    }

    private fun seedQuestions(): List<Seed> = listOf(
        Seed("football", "足球", listOf("grass", "goal", "offside"), listOf("草地", "球门", "越位")),
        Seed("basketball", "篮球", listOf("hoop", "dribble", "court"), listOf("篮筐", "运球", "球场")),
        Seed("baseball", "棒球", listOf("bat", "pitcher", "home run"), listOf("球棒", "投手", "本垒打")),
        Seed("swimming", "游泳", listOf("pool", "stroke", "goggles"), listOf("泳池", "泳姿", "护目镜")),
        Seed("running", "跑步", listOf("marathon", "track", "sneakers"), listOf("马拉松", "跑道", "运动鞋")),
        Seed("cycling", "骑行", listOf("pedal", "helmet", "chain"), listOf("踏板", "头盔", "链条")),
        Seed("hospital", "医院", listOf("doctor", "nurse", "emergency"), listOf("医生", "护士", "急诊")),
        Seed("library", "图书馆", listOf("books", "quiet", "borrow"), listOf("书籍", "安静", "借阅")),
        Seed("airport", "机场", listOf("passport", "terminal", "boarding"), listOf("护照", "航站楼", "登机")),
        Seed("kitchen", "厨房", listOf("cook", "fridge", "pan"), listOf("烹饪", "冰箱", "平底锅")),
        Seed("classroom", "教室", listOf("teacher", "blackboard", "desk"), listOf("老师", "黑板", "书桌")),
        Seed("office", "办公室", listOf("meeting", "computer", "printer"), listOf("会议", "电脑", "打印机")),
        Seed("factory", "工厂", listOf("machine", "worker", "assembly"), listOf("机器", "工人", "装配")),
        Seed("supermarket", "超市", listOf("cart", "checkout", "aisle"), listOf("购物车", "收银台", "货架通道")),
        Seed("restaurant", "餐馆", listOf("menu", "waiter", "bill"), listOf("菜单", "服务员", "账单")),
        Seed("breakfast", "早餐", listOf("morning", "eggs", "toast"), listOf("早晨", "鸡蛋", "吐司")),
        Seed("homework", "家庭作业", listOf("student", "assignment", "deadline"), listOf("学生", "作业任务", "截止日期")),
        Seed("vacation", "假期", listOf("travel", "hotel", "relax"), listOf("旅行", "酒店", "放松")),
        Seed("rainbow", "彩虹", listOf("rain", "sunlight", "colors"), listOf("下雨", "阳光", "颜色")),
        Seed("thunder", "雷声", listOf("storm", "lightning", "loud"), listOf("暴风雨", "闪电", "响亮")),
        Seed("winter", "冬天", listOf("snow", "cold", "jacket"), listOf("雪", "寒冷", "外套")),
        Seed("summer", "夏天", listOf("hot", "beach", "ice cream"), listOf("炎热", "海滩", "冰淇淋")),
        Seed("spring", "春天", listOf("flowers", "warm", "breeze"), listOf("花朵", "温暖", "微风")),
        Seed("autumn", "秋天", listOf("leaves", "harvest", "cool"), listOf("树叶", "收获", "凉爽")),
        Seed("banana", "香蕉", listOf("yellow", "peel", "fruit"), listOf("黄色", "果皮", "水果")),
        Seed("orange", "橙子", listOf("citrus", "juice", "vitamin c"), listOf("柑橘", "果汁", "维生素C")),
        Seed("strawberry", "草莓", listOf("red", "sweet", "seeds"), listOf("红色", "甜", "籽")),
        Seed("watermelon", "西瓜", listOf("summer", "green", "slice"), listOf("夏季", "绿色", "切片")),
        Seed("pineapple", "菠萝", listOf("tropical", "spiky", "sweet"), listOf("热带", "带刺", "甜")),
        Seed("carrot", "胡萝卜", listOf("orange", "vegetable", "rabbit"), listOf("橙色", "蔬菜", "兔子")),
        Seed("potato", "土豆", listOf("fries", "starch", "mashed"), listOf("薯条", "淀粉", "土豆泥")),
        Seed("tomato", "番茄", listOf("salad", "red", "sauce"), listOf("沙拉", "红色", "酱")),
        Seed("onion", "洋葱", listOf("layers", "tear", "flavor"), listOf("层", "流泪", "风味")),
        Seed("garlic", "大蒜", listOf("clove", "smell", "seasoning"), listOf("蒜瓣", "气味", "调味")),
        Seed("computer", "电脑", listOf("keyboard", "screen", "mouse"), listOf("键盘", "屏幕", "鼠标")),
        Seed("internet", "互联网", listOf("website", "browser", "online"), listOf("网站", "浏览器", "在线")),
        Seed("password", "密码", listOf("login", "secure", "account"), listOf("登录", "安全", "账号")),
        Seed("battery", "电池", listOf("charge", "power", "phone"), listOf("充电", "电力", "手机")),
        Seed("camera", "相机", listOf("photo", "lens", "flash"), listOf("照片", "镜头", "闪光灯")),
        Seed("notebook", "笔记本", listOf("pages", "write", "spiral"), listOf("页面", "书写", "线圈")),
        Seed("backpack", "背包", listOf("school", "zipper", "carry"), listOf("学校", "拉链", "携带")),
        Seed("umbrella", "雨伞", listOf("rain", "fold", "handle"), listOf("下雨", "折叠", "手柄")),
        Seed("traffic", "交通", listOf("cars", "road", "jam"), listOf("汽车", "道路", "拥堵")),
        Seed("subway", "地铁", listOf("station", "train", "underground"), listOf("车站", "列车", "地下")),
        Seed("bicycle", "自行车", listOf("wheel", "pedal", "helmet"), listOf("车轮", "踏板", "头盔")),
        Seed("mountain", "山", listOf("high", "climb", "peak"), listOf("高", "攀登", "山峰")),
        Seed("river", "河流", listOf("water", "flow", "bridge"), listOf("水", "流动", "桥")),
        Seed("forest", "森林", listOf("trees", "wild", "green"), listOf("树木", "野生", "绿色")),
        Seed("desert", "沙漠", listOf("sand", "dry", "camel"), listOf("沙子", "干燥", "骆驼")),
        Seed("island", "岛屿", listOf("sea", "beach", "boat"), listOf("海洋", "海滩", "船")),
        Seed("planet", "行星", listOf("space", "orbit", "gravity"), listOf("太空", "轨道", "引力")),
        Seed("galaxy", "星系", listOf("stars", "milky way", "universe"), listOf("恒星", "银河", "宇宙")),
        Seed("teacher", "老师", listOf("lesson", "homework", "school"), listOf("课程", "作业", "学校")),
        Seed("student", "学生", listOf("study", "exam", "class"), listOf("学习", "考试", "课堂")),
        Seed("doctor", "医生", listOf("hospital", "medicine", "patient"), listOf("医院", "药物", "病人")),
        Seed("engineer", "工程师", listOf("design", "build", "technology"), listOf("设计", "建造", "技术")),
        Seed("artist", "艺术家", listOf("paint", "gallery", "creative"), listOf("绘画", "画廊", "创意")),
        Seed("musician", "音乐家", listOf("instrument", "melody", "concert"), listOf("乐器", "旋律", "音乐会")),
        Seed("writer", "作家", listOf("novel", "story", "publish"), listOf("小说", "故事", "出版")),
        Seed("farmer", "农民", listOf("field", "crops", "tractor"), listOf("田地", "作物", "拖拉机")),
        Seed("police", "警察", listOf("law", "uniform", "patrol"), listOf("法律", "制服", "巡逻")),
        Seed("firefighter", "消防员", listOf("fire", "rescue", "helmet"), listOf("火灾", "救援", "头盔"))
    )
}
