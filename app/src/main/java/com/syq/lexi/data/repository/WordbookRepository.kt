package com.syq.lexi.data.repository

import com.syq.lexi.data.database.WordbookDao
import com.syq.lexi.data.database.WordDao
import com.syq.lexi.data.database.StudyRecordDao
import com.syq.lexi.data.database.StudyPlanDao
import com.syq.lexi.data.database.WordbookEntity
import com.syq.lexi.data.database.WordEntity
import com.syq.lexi.data.database.StudyRecordEntity
import kotlinx.coroutines.flow.Flow

class WordbookRepository(
    private val wordbookDao: WordbookDao,
    private val wordDao: WordDao,
    private val studyRecordDao: StudyRecordDao,
    private val studyPlanDao: StudyPlanDao? = null
) {
    // Wordbook 操作
    suspend fun insertWordbook(wordbook: WordbookEntity): Long {
        return wordbookDao.insertWordbook(wordbook)
    }

    suspend fun updateWordbook(wordbook: WordbookEntity) {
        wordbookDao.updateWordbook(wordbook)
    }

    suspend fun deleteWordbook(wordbook: WordbookEntity) {
        wordbookDao.deleteWordbook(wordbook)
    }

    fun getAllWordbooks(): Flow<List<WordbookEntity>> {
        return wordbookDao.getAllWordbooks()
    }

    fun getWordbookById(id: Int): Flow<WordbookEntity> {
        return wordbookDao.getWordbookById(id)
    }

    fun getWordbooksByCategory(category: String): Flow<List<WordbookEntity>> {
        return wordbookDao.getWordbooksByCategory(category)
    }

    // Word 操作
    suspend fun insertWord(word: WordEntity): Long {
        return wordDao.insertWord(word)
    }

    suspend fun insertWords(words: List<WordEntity>) {
        wordDao.insertWords(words)
    }

    suspend fun updateWord(word: WordEntity) {
        wordDao.updateWord(word)
    }

    suspend fun deleteWord(word: WordEntity) {
        wordDao.deleteWord(word)
    }

    suspend fun deleteWordsByWordbook(wordbookId: Int) {
        wordDao.deleteWordsByWordbook(wordbookId)
    }

    // 带查重的批量添加：已存在同英文单词时合并释义
    suspend fun addWordsWithDedup(
        wordbookId: Int,
        words: List<WordEntity>
    ): Triple<Int, Int, Int> { // added, merged, skipped
        var added = 0; var merged = 0; var skipped = 0
        for (word in words) {
            val existing = wordDao.getWordByEnglish(wordbookId, word.english)
            if (existing == null) {
                wordDao.insertWord(word)
                added++
            } else {
                val newMeanings = word.chinese.split("[,，]".toRegex())
                    .map { it.trim() }.filter { it.isNotEmpty() }
                val existingMeanings = existing.chinese.split("[,，]".toRegex())
                    .map { it.trim() }.filter { it.isNotEmpty() }
                val toAdd = newMeanings.filter { it !in existingMeanings }
                if (toAdd.isEmpty()) {
                    skipped++
                } else {
                    val merged_chinese = (existingMeanings + toAdd).joinToString("，")
                    wordDao.updateChinese(existing.id, merged_chinese)
                    merged++
                }
            }
        }
        return Triple(added, merged, skipped)
    }

    fun getWordsByWordbook(wordbookId: Int): Flow<List<WordEntity>> {
        return wordDao.getWordsByWordbook(wordbookId)
    }

    fun getWordById(id: Int): Flow<WordEntity> {
        return wordDao.getWordById(id)
    }

    suspend fun getWordByIdOnce(id: Int): WordEntity? {
        return wordDao.getWordByIdOnce(id)
    }

    fun getUnmasteredWords(wordbookId: Int): Flow<List<WordEntity>> {
        return wordDao.getUnmasteredWords(wordbookId)
    }

    fun getMasteredWords(wordbookId: Int): Flow<List<WordEntity>> {
        return wordDao.getMasteredWords(wordbookId)
    }

    fun getWordCount(wordbookId: Int): Flow<Int> {
        return wordDao.getWordCount(wordbookId)
    }

    fun getMasteredCount(wordbookId: Int): Flow<Int> {
        return wordDao.getMasteredCount(wordbookId)
    }

    suspend fun markWordAsMastered(wordId: Int) {
        wordDao.markWordAsMastered(wordId)
    }

    suspend fun markWordAsUnmastered(wordId: Int) {
        wordDao.markWordAsUnmastered(wordId)
    }

    suspend fun updateReviewData(wordId: Int, familiarity: Float, reviewCount: Int, nextReviewDate: Long) {
        val word = wordDao.getWordByIdOnce(wordId) ?: return
        wordDao.updateWord(word.copy(familiarity = familiarity, reviewCount = reviewCount, nextReviewDate = nextReviewDate))
    }

    suspend fun getDueReviewWords(wordbookId: Int): List<WordEntity> {
        return wordDao.getDueReviewWords(wordbookId, System.currentTimeMillis())
    }

    suspend fun getNewWords(wordbookId: Int): List<WordEntity> {
        return wordDao.getNewWords(wordbookId, System.currentTimeMillis())
    }

    fun getDueReviewCount(wordbookId: Int): Flow<Int> {
        return wordDao.getDueReviewCount(wordbookId, System.currentTimeMillis())
    }

    suspend fun starWord(wordId: Int) {
        wordDao.starWord(wordId)
    }

    suspend fun unstarWord(wordId: Int) {
        wordDao.unstarWord(wordId)
    }

    fun getStarredWords(wordbookId: Int): Flow<List<WordEntity>> {
        return wordDao.getStarredWords(wordbookId)
    }

    fun getStarredCount(wordbookId: Int): Flow<Int> {
        return wordDao.getStarredCount(wordbookId)
    }

    suspend fun getRecentRecordsByWord(wordId: Int, limit: Int = 10): List<StudyRecordEntity> {
        return studyRecordDao.getRecentRecordsByWord(wordId, limit)
    }

    suspend fun insertStudyRecord(record: StudyRecordEntity): Long {
        return studyRecordDao.insertRecord(record)
    }

    fun getRecordsByWordbook(wordbookId: Int): Flow<List<StudyRecordEntity>> {
        return studyRecordDao.getRecordsByWordbook(wordbookId)
    }

    fun getRecordsByWord(wordId: Int): Flow<List<StudyRecordEntity>> {
        return studyRecordDao.getRecordsByWord(wordId)
    }

    suspend fun deleteOldRecords(date: Long) {
        studyRecordDao.deleteOldRecords(date)
    }

    suspend fun deleteAllPlans() {
        studyPlanDao?.deleteAllPlans()
    }

    suspend fun insertStudyPlan(plan: com.syq.lexi.data.database.StudyPlanEntity) {
        studyPlanDao?.insertPlan(plan)
    }
}
