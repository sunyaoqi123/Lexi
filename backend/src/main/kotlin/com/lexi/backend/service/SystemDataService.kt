package com.lexi.backend.service

import com.lexi.backend.entity.SystemWord
import com.lexi.backend.entity.SystemWordbook
import com.lexi.backend.entity.Word
import com.lexi.backend.entity.Wordbook
import com.lexi.backend.repository.SystemWordRepository
import com.lexi.backend.repository.SystemWordbookRepository
import com.lexi.backend.repository.WordRepository
import com.lexi.backend.repository.WordbookRepository
import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SystemDataService(
    private val systemWordbookRepository: SystemWordbookRepository,
    private val systemWordRepository: SystemWordRepository,
    private val wordbookRepository: WordbookRepository,
    private val wordRepository: WordRepository
) {

    @PostConstruct
    fun initSystemData() {
        if (systemWordbookRepository.count() > 0) return

        // 高考词汇
        val gaokao = systemWordbookRepository.save(SystemWordbook(
            name = "高考词汇", category = "英语考试", description = "高考英语必备核心词汇"))
        saveWords(gaokao.id, listOf(
            Triple("ability", "能力；才能", "/əˈbɪləti/"),
            Triple("absent", "缺席的；不在的", "/ˈæbsənt/"),
            Triple("accept", "接受；承认", "/əkˈsept/"),
            Triple("accident", "事故；意外", "/ˈæksɪdənt/"),
            Triple("achieve", "达到；完成", "/əˈtʃiːv/"),
            Triple("action", "行动；动作", "/ˈækʃn/"),
            Triple("active", "积极的；活跃的", "/ˈæktɪv/"),
            Triple("activity", "活动；行动", "/ækˈtɪvəti/"),
            Triple("actually", "实际上；事实上", "/ˈæktʃuəli/"),
            Triple("addition", "加法；附加物", "/əˈdɪʃn/"),
            Triple("address", "地址；演讲", "/əˈdres/"),
            Triple("admire", "钦佩；赞赏", "/ədˈmaɪər/"),
            Triple("adult", "成人；成年的", "/ˈædʌlt/"),
            Triple("advantage", "优势；好处", "/ədˈvɑːntɪdʒ/"),
            Triple("adventure", "冒险；奇遇", "/ədˈventʃər/"),
            Triple("affect", "影响；感动", "/əˈfekt/"),
            Triple("afford", "负担得起", "/əˈfɔːrd/"),
            Triple("afraid", "害怕的；担心的", "/əˈfreɪd/"),
            Triple("agree", "同意；赞成", "/əˈɡriː/"),
            Triple("aim", "目标；瞄准", "/eɪm/")
        ))

        // 四级词汇
        val cet4 = systemWordbookRepository.save(SystemWordbook(
            name = "四级词汇", category = "英语考试", description = "大学英语四级考试核心词汇"))
        saveWords(cet4.id, listOf(
            Triple("abandon", "放弃；遗弃", "/əˈbændən/"),
            Triple("abstract", "抽象的；摘要", "/ˈæbstrækt/"),
            Triple("academic", "学术的", "/ˌækəˈdemɪk/"),
            Triple("access", "进入；使用权", "/ˈækses/"),
            Triple("accomplish", "完成；实现", "/əˈkʌmplɪʃ/"),
            Triple("accurate", "准确的", "/ˈækjərət/"),
            Triple("acquire", "获得；学到", "/əˈkwaɪər/"),
            Triple("adapt", "适应；改编", "/əˈdæpt/"),
            Triple("adequate", "足够的", "/ˈædɪkwət/"),
            Triple("advance", "前进；提前", "/ədˈvɑːns/"),
            Triple("aggressive", "侵略的；好斗的", "/əˈɡresɪv/"),
            Triple("aid", "帮助；援助", "/eɪd/"),
            Triple("alter", "改变；修改", "/ˈɔːltər/"),
            Triple("ambiguous", "模糊的；不明确的", "/æmˈbɪɡjuəs/"),
            Triple("analyze", "分析；解析", "/ˈænəlaɪz/"),
            Triple("anxiety", "焦虑；担心", "/æŋˈzaɪəti/"),
            Triple("apparent", "明显的；表面的", "/əˈpærənt/"),
            Triple("appeal", "呼吁；吸引力", "/əˈpiːl/"),
            Triple("appreciate", "欣赏；感激", "/əˈpriːʃieɪt/"),
            Triple("approach", "方法；接近", "/əˈprəʊtʃ/")
        ))

        // 六级词汇
        val cet6 = systemWordbookRepository.save(SystemWordbook(
            name = "六级词汇", category = "英语考试", description = "大学英语六级考试核心词汇"))
        saveWords(cet6.id, listOf(
            Triple("abolish", "废除；取消", "/əˈbɒlɪʃ/"),
            Triple("abrupt", "突然的；粗鲁的", "/əˈbrʌpt/"),
            Triple("abundance", "丰富；充裕", "/əˈbʌndəns/"),
            Triple("accelerate", "加速；促进", "/əkˈseləreɪt/"),
            Triple("accommodate", "容纳；适应", "/əˈkɒmədeɪt/"),
            Triple("accumulate", "积累；聚积", "/əˈkjuːmjəleɪt/"),
            Triple("acknowledge", "承认；答谢", "/əkˈnɒlɪdʒ/"),
            Triple("advocate", "提倡；拥护者", "/ˈædvəkeɪt/"),
            Triple("aesthetic", "审美的；美学的", "/iːsˈθetɪk/"),
            Triple("aggregate", "总计；聚集", "/ˈæɡrɪɡət/"),
            Triple("allegation", "指控；陈述", "/ˌæləˈɡeɪʃn/"),
            Triple("alleviate", "减轻；缓和", "/əˈliːvieɪt/"),
            Triple("allocate", "分配；拨给", "/ˈæləkeɪt/"),
            Triple("ambivalent", "矛盾的；犹豫的", "/æmˈbɪvələnt/"),
            Triple("amplify", "放大；扩大", "/ˈæmplɪfaɪ/"),
            Triple("analogy", "类比；类推", "/əˈnælədʒi/"),
            Triple("anecdote", "轶事；趣闻", "/ˈænɪkdəʊt/"),
            Triple("anticipate", "预期；期待", "/ænˈtɪsɪpeɪt/"),
            Triple("apprehend", "理解；逮捕", "/ˌæprɪˈhend/"),
            Triple("articulate", "表达清晰的", "/ɑːˈtɪkjələt/")
        ))

        // 雅思词汇
        val ielts = systemWordbookRepository.save(SystemWordbook(
            name = "雅思词汇", category = "英语考试", description = "雅思考试高频核心词汇"))
        saveWords(ielts.id, listOf(
            Triple("abide", "遵守；忍受", "/əˈbaɪd/"),
            Triple("abnormal", "不正常的", "/æbˈnɔːml/"),
            Triple("abolition", "废除；废止", "/ˌæbəˈlɪʃn/"),
            Triple("absorb", "吸收；吸引", "/əbˈzɔːb/"),
            Triple("abstract", "抽象概念；摘要", "/ˈæbstrækt/"),
            Triple("accessible", "可进入的；易得到的", "/əkˈsesəbl/"),
            Triple("accommodate", "提供住所；适应", "/əˈkɒmədeɪt/"),
            Triple("accountable", "负有责任的", "/əˈkaʊntəbl/"),
            Triple("accumulation", "积累；堆积", "/əˌkjuːmjəˈleɪʃn/"),
            Triple("accurate", "精确的；准确的", "/ˈækjərət/"),
            Triple("achievement", "成就；成绩", "/əˈtʃiːvmənt/"),
            Triple("acknowledge", "承认；致谢", "/əkˈnɒlɪdʒ/"),
            Triple("acquisition", "获得；收购", "/ˌækwɪˈzɪʃn/"),
            Triple("adequate", "充分的；适当的", "/ˈædɪkwət/"),
            Triple("adjacent", "邻近的；毗连的", "/əˈdʒeɪsnt/"),
            Triple("administration", "管理；行政", "/ədˌmɪnɪˈstreɪʃn/"),
            Triple("adopt", "采用；收养", "/əˈdɒpt/"),
            Triple("adverse", "不利的；有害的", "/ˈædvɜːs/"),
            Triple("affect", "影响；作用于", "/əˈfekt/"),
            Triple("aggregate", "总计；合计", "/ˈæɡrɪɡət/")
        ))

        // 托福词汇
        val toefl = systemWordbookRepository.save(SystemWordbook(
            name = "托福词汇", category = "英语考试", description = "托福考试高频核心词汇"))
        saveWords(toefl.id, listOf(
            Triple("abdicate", "退位；放弃", "/ˈæbdɪkeɪt/"),
            Triple("aberrant", "异常的；偏离的", "/æˈberənt/"),
            Triple("abridge", "删节；缩短", "/əˈbrɪdʒ/"),
            Triple("abscond", "潜逃；逃跑", "/əbˈskɒnd/"),
            Triple("abstain", "戒除；弃权", "/əbˈsteɪn/"),
            Triple("accolade", "荣誉；赞扬", "/ˈækəleɪd/"),
            Triple("acrimony", "尖刻；激烈", "/ˈækrɪməni/"),
            Triple("acumen", "敏锐；聪明", "/ˈækjəmən/"),
            Triple("adept", "熟练的；内行", "/əˈdept/"),
            Triple("admonish", "告诫；劝告", "/ədˈmɒnɪʃ/"),
            Triple("adulterate", "掺假；掺杂", "/əˈdʌltəreɪt/"),
            Triple("adversity", "逆境；不幸", "/ədˈvɜːsəti/"),
            Triple("affable", "和蔼的；友善的", "/ˈæfəbl/"),
            Triple("affluent", "富裕的；丰富的", "/ˈæfluənt/"),
            Triple("aggravate", "加重；恶化", "/ˈæɡrəveɪt/"),
            Triple("alacrity", "敏捷；乐意", "/əˈlækrəti/"),
            Triple("alienate", "疏远；离间", "/ˈeɪliəneɪt/"),
            Triple("allude", "暗指；提及", "/əˈluːd/"),
            Triple("altruistic", "利他的；无私的", "/ˌæltruˈɪstɪk/"),
            Triple("ambiguous", "模棱两可的", "/æmˈbɪɡjuəs/")
        ))
    }

    private fun saveWords(wordbookId: Int, words: List<Triple<String, String, String>>) {
        words.forEach { (eng, chn, pron) ->
            systemWordRepository.save(SystemWord(
                wordbookId = wordbookId, english = eng,
                chinese = chn, pronunciation = pron
            ))
        }
    }

    // 新用户首次登录时，把系统单词本复制到用户名下
    @Transactional
    fun initUserData(userId: Int) {
        val existingWordbooks = wordbookRepository.findByUserId(userId)
        if (existingWordbooks.isNotEmpty()) return

        val systemWordbooks = systemWordbookRepository.findAll()
        for (swb in systemWordbooks) {
            val userWordbook = wordbookRepository.save(
                Wordbook(
                    userId = userId,
                    name = swb.name,
                    category = swb.category,
                    description = swb.description,
                    updatedAt = LocalDateTime.now()
                )
            )
            val systemWords = systemWordRepository.findByWordbookId(swb.id)
            val userWords = systemWords.map { sw ->
                Word(
                    wordbookId = userWordbook.id,
                    english = sw.english,
                    chinese = sw.chinese,
                    pronunciation = sw.pronunciation,
                    partOfSpeech = sw.partOfSpeech,
                    example = sw.example,
                    exampleTranslation = sw.exampleTranslation,
                    isMastered = false
                )
            }
            wordRepository.saveAll(userWords)
            wordbookRepository.save(userWordbook.copy(totalWords = userWords.size))
        }
    }
}
