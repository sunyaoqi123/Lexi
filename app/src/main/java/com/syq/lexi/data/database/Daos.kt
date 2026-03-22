package com.syq.lexi.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WordbookDao {
    @Insert
    suspend fun insertWordbook(wordbook: WordbookEntity): Long

    @Update
    suspend fun updateWordbook(wordbook: WordbookEntity)

    @Delete
    suspend fun deleteWordbook(wordbook: WordbookEntity)

    @Query("SELECT * FROM wordbooks")
    fun getAllWordbooks(): Flow<List<WordbookEntity>>

    @Query("SELECT * FROM wordbooks WHERE id = :id")
    fun getWordbookById(id: Int): Flow<WordbookEntity>

    @Query("SELECT * FROM wordbooks WHERE category = :category")
    fun getWordbooksByCategory(category: String): Flow<List<WordbookEntity>>
}

@Dao
interface WordDao {
    @Insert
    suspend fun insertWord(word: WordEntity): Long

    @Insert
    suspend fun insertWords(words: List<WordEntity>)

    @Update
    suspend fun updateWord(word: WordEntity)

    @Delete
    suspend fun deleteWord(word: WordEntity)

    @Query("SELECT * FROM words WHERE wordbookId = :wordbookId")
    fun getWordsByWordbook(wordbookId: Int): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE id = :id")
    fun getWordById(id: Int): Flow<WordEntity>

    @Query("SELECT * FROM words WHERE wordbookId = :wordbookId AND isMastered = 0")
    fun getUnmasteredWords(wordbookId: Int): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE wordbookId = :wordbookId AND isMastered = 1")
    fun getMasteredWords(wordbookId: Int): Flow<List<WordEntity>>

    @Query("SELECT COUNT(*) FROM words WHERE wordbookId = :wordbookId")
    fun getWordCount(wordbookId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM words WHERE wordbookId = :wordbookId AND isMastered = 1")
    fun getMasteredCount(wordbookId: Int): Flow<Int>

    @Query("UPDATE words SET isMastered = 1, masteredDate = :date WHERE id = :wordId")
    suspend fun markWordAsMastered(wordId: Int, date: Long = System.currentTimeMillis())

    @Query("UPDATE words SET isMastered = 0 WHERE id = :wordId")
    suspend fun markWordAsUnmastered(wordId: Int)
}

@Dao
interface StudyRecordDao {
    @Insert
    suspend fun insertRecord(record: StudyRecordEntity): Long

    @Query("SELECT * FROM study_records WHERE wordbookId = :wordbookId ORDER BY studyDate DESC")
    fun getRecordsByWordbook(wordbookId: Int): Flow<List<StudyRecordEntity>>

    @Query("SELECT * FROM study_records WHERE wordId = :wordId ORDER BY studyDate DESC")
    fun getRecordsByWord(wordId: Int): Flow<List<StudyRecordEntity>>

    @Query("DELETE FROM study_records WHERE studyDate < :date")
    suspend fun deleteOldRecords(date: Long)
}
