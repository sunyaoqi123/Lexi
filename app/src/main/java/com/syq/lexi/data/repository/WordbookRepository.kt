package com.syq.lexi.data.repository

import com.syq.lexi.data.database.WordbookDao
import com.syq.lexi.data.database.WordDao
import com.syq.lexi.data.database.StudyRecordDao
import com.syq.lexi.data.database.WordbookEntity
import com.syq.lexi.data.database.WordEntity
import com.syq.lexi.data.database.StudyRecordEntity
import kotlinx.coroutines.flow.Flow

class WordbookRepository(
    private val wordbookDao: WordbookDao,
    private val wordDao: WordDao,
    private val studyRecordDao: StudyRecordDao
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

    fun getWordsByWordbook(wordbookId: Int): Flow<List<WordEntity>> {
        return wordDao.getWordsByWordbook(wordbookId)
    }

    fun getWordById(id: Int): Flow<WordEntity> {
        return wordDao.getWordById(id)
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

    // Study Record 操作
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
}
