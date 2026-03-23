package com.syq.lexi.data.database

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DataInitializer {
    private const val PREFS_NAME = "lexi_prefs"
    private const val KEY_DATA_INITIALIZED = "is_data_initialized"

    suspend fun initializeDataIfNeeded(context: Context, forceReinit: Boolean = false) {
        withContext(Dispatchers.IO) {
            try {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val isInitialized = prefs.getBoolean(KEY_DATA_INITIALIZED, false)

                if (!isInitialized || forceReinit) {
                    initializeData(context)
                    prefs.edit().putBoolean(KEY_DATA_INITIALIZED, true).apply()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun initializeData(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val database = LexiDatabase.getDatabase(context)
                val wordbookDao = database.wordbookDao()
                val wordDao = database.wordDao()

                // 解析 JSON 数据
                val (wordbooks, words) = JsonDataParser.parseWordsJson(context)
                val (wordbookEntities, wordEntities) = JsonDataParser.convertToEntities(wordbooks, words)

                // 插入单词本
                for (wordbook in wordbookEntities) {
                    wordbookDao.insertWordbook(wordbook)
                }

                // 插入单词
                wordDao.insertWords(wordEntities)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
