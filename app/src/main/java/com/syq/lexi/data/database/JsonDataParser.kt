package com.syq.lexi.data.database

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.InputStreamReader

data class WordbookJson(
    val id: Int,
    val name: String,
    val category: String,
    val description: String,
    val totalWords: Int
)

data class WordJson(
    val wordbookId: Int,
    val english: String,
    val chinese: String,
    val pronunciation: String,
    val partOfSpeech: String,
    val example: String,
    val exampleTranslation: String
)

object JsonDataParser {
    private const val TAG = "JsonDataParser"

    fun parseWordsJson(context: Context): Pair<List<WordbookJson>, List<WordJson>> {
        return try {
            Log.d(TAG, "Starting to parse words.json")
            val inputStream = context.resources.openRawResource(com.syq.lexi.R.raw.words)
            val jsonString = InputStreamReader(inputStream).use { it.readText() }
            Log.d(TAG, "JSON file read successfully, length: ${jsonString.length}")
            
            val jsonObject = JSONObject(jsonString)

            val wordbooks = mutableListOf<WordbookJson>()
            val words = mutableListOf<WordJson>()

            // 解析单词本
            val wordbooksArray = jsonObject.getJSONArray("wordbooks")
            Log.d(TAG, "Found ${wordbooksArray.length()} wordbooks")
            
            for (i in 0 until wordbooksArray.length()) {
                val wb = wordbooksArray.getJSONObject(i)
                wordbooks.add(
                    WordbookJson(
                        id = wb.getInt("id"),
                        name = wb.getString("name"),
                        category = wb.getString("category"),
                        description = wb.getString("description"),
                        totalWords = wb.getInt("totalWords")
                    )
                )
            }

            // 解析单词
            val wordsArray = jsonObject.getJSONArray("words")
            Log.d(TAG, "Found ${wordsArray.length()} words")
            
            for (i in 0 until wordsArray.length()) {
                val w = wordsArray.getJSONObject(i)
                words.add(
                    WordJson(
                        wordbookId = w.getInt("wordbookId"),
                        english = w.getString("english"),
                        chinese = w.getString("chinese"),
                        pronunciation = w.getString("pronunciation"),
                        partOfSpeech = w.getString("partOfSpeech"),
                        example = w.getString("example"),
                        exampleTranslation = w.getString("exampleTranslation")
                    )
                )
            }

            Log.d(TAG, "Parsing completed successfully")
            Pair(wordbooks, words)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing JSON", e)
            Pair(emptyList(), emptyList())
        }
    }

    fun convertToEntities(
        wordbooks: List<WordbookJson>,
        words: List<WordJson>
    ): Pair<List<WordbookEntity>, List<WordEntity>> {
        val wordbookEntities = wordbooks.map {
            WordbookEntity(
                id = it.id,
                name = it.name,
                category = it.category,
                description = it.description,
                totalWords = it.totalWords
            )
        }

        val wordEntities = words.map {
            WordEntity(
                wordbookId = it.wordbookId,
                english = it.english,
                chinese = it.chinese,
                pronunciation = it.pronunciation,
                partOfSpeech = it.partOfSpeech,
                example = it.example,
                exampleTranslation = it.exampleTranslation
            )
        }

        Log.d(TAG, "Converted ${wordbookEntities.size} wordbooks and ${wordEntities.size} words to entities")
        return Pair(wordbookEntities, wordEntities)
    }
}
