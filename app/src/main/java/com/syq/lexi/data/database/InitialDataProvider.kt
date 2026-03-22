package com.syq.lexi.data.database

object InitialDataProvider {
    fun getInitialWordbooks(): List<WordbookEntity> {
        return listOf(
            WordbookEntity(
                name = "高考词汇",
                category = "高考",
                description = "高考必背词汇",
                totalWords = 3500
            ),
            WordbookEntity(
                name = "四级词汇",
                category = "四级",
                description = "英语四级考试词汇",
                totalWords = 2500
            ),
            WordbookEntity(
                name = "六级词汇",
                category = "六级",
                description = "英语六级考试词汇",
                totalWords = 5500
            ),
            WordbookEntity(
                name = "雅思词汇",
                category = "雅思",
                description = "雅思考试词汇",
                totalWords = 4000
            ),
            WordbookEntity(
                name = "托福词汇",
                category = "托福",
                description = "托福考试词汇",
                totalWords = 4500
            )
        )
    }

    fun getInitialWords(): List<WordEntity> {
        return listOf(
            // 高考词汇示例
            WordEntity(
                wordbookId = 1,
                english = "abandon",
                chinese = "放弃",
                pronunciation = "əˈbændən",
                partOfSpeech = "v.",
                example = "Don't abandon your dreams.",
                exampleTranslation = "不要放弃你的梦想。"
            ),
            WordEntity(
                wordbookId = 1,
                english = "ability",
                chinese = "能力",
                pronunciation = "əˈbɪləti",
                partOfSpeech = "n.",
                example = "She has the ability to succeed.",
                exampleTranslation = "她有成功的能力。"
            ),
            WordEntity(
                wordbookId = 1,
                english = "abolish",
                chinese = "废除",
                pronunciation = "əˈbɑːlɪʃ",
                partOfSpeech = "v.",
                example = "The government decided to abolish the old law.",
                exampleTranslation = "政府决定废除旧法律。"
            ),
            // 四级词汇示例
            WordEntity(
                wordbookId = 2,
                english = "academic",
                chinese = "学术的",
                pronunciation = "ˌækəˈdemɪk",
                partOfSpeech = "adj.",
                example = "He has strong academic performance.",
                exampleTranslation = "他的学术成绩很好。"
            ),
            WordEntity(
                wordbookId = 2,
                english = "accelerate",
                chinese = "加速",
                pronunciation = "əkˈseləreɪt",
                partOfSpeech = "v.",
                example = "The car accelerated quickly.",
                exampleTranslation = "汽车迅速加速。"
            ),
            // 六级词汇示例
            WordEntity(
                wordbookId = 3,
                english = "accommodate",
                chinese = "容纳",
                pronunciation = "əˈkɑːmədeɪt",
                partOfSpeech = "v.",
                example = "The hotel can accommodate 500 guests.",
                exampleTranslation = "这家酒店可以容纳500位客人。"
            ),
            WordEntity(
                wordbookId = 3,
                english = "accumulate",
                chinese = "积累",
                pronunciation = "əˈkjuːmjəleɪt",
                partOfSpeech = "v.",
                example = "Knowledge accumulates over time.",
                exampleTranslation = "知识随着时间积累。"
            ),
            // 雅思词汇示例
            WordEntity(
                wordbookId = 4,
                english = "acknowledge",
                chinese = "承认",
                pronunciation = "əkˈnɑːlɪdʒ",
                partOfSpeech = "v.",
                example = "He acknowledged his mistake.",
                exampleTranslation = "他承认了他的错误。"
            ),
            // 托福词汇示例
            WordEntity(
                wordbookId = 5,
                english = "acquire",
                chinese = "获得",
                pronunciation = "əˈkwaɪər",
                partOfSpeech = "v.",
                example = "She acquired new skills through training.",
                exampleTranslation = "她通过培训获得了新技能。"
            )
        )
    }
}
