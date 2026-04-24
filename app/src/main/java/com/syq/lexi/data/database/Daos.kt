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

    @Insert(onConflict = androidx.room.OnConflictStrategy.IGNORE)
    suspend fun insertWords(words: List<WordEntity>)

    @Update
    suspend fun updateWord(word: WordEntity)

    @Delete
    suspend fun deleteWord(word: WordEntity)

    @Query("SELECT * FROM words")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE wordbookId = :wordbookId")
    fun getWordsByWordbook(wordbookId: Int): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE id = :id")
    fun getWordById(id: Int): Flow<WordEntity>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordByIdOnce(id: Int): WordEntity?

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

    @Query("UPDATE words SET isStarred = 1 WHERE id = :wordId")
    suspend fun starWord(wordId: Int)

    @Query("UPDATE words SET isStarred = 0 WHERE id = :wordId")
    suspend fun unstarWord(wordId: Int)

    @Query("SELECT * FROM words WHERE wordbookId = :wordbookId AND isStarred = 1")
    fun getStarredWords(wordbookId: Int): Flow<List<WordEntity>>

    @Query("SELECT COUNT(*) FROM words WHERE wordbookId = :wordbookId AND isStarred = 1")
    fun getStarredCount(wordbookId: Int): Flow<Int>

    @Query("SELECT * FROM words WHERE wordbookId = :wordbookId AND LOWER(english) = LOWER(:english) LIMIT 1")
    suspend fun getWordByEnglish(wordbookId: Int, english: String): WordEntity?

    @Query("UPDATE words SET chinese = :chinese WHERE id = :wordId")
    suspend fun updateChinese(wordId: Int, chinese: String)

    @Query("DELETE FROM words WHERE wordbookId = :wordbookId")
    suspend fun deleteWordsByWordbook(wordbookId: Int)

    // 复习系统
    @Query("UPDATE words SET familiarity = :familiarity, reviewCount = :reviewCount, nextReviewDate = :nextReviewDate WHERE id = :wordId")
    suspend fun updateReviewData(wordId: Int, familiarity: Float, reviewCount: Int, nextReviewDate: Long)

    @Query("SELECT * FROM words WHERE wordbookId = :wordbookId AND nextReviewDate > 0 AND nextReviewDate <= :now AND isMastered = 0 ORDER BY nextReviewDate ASC")
    suspend fun getDueReviewWords(wordbookId: Int, now: Long): List<WordEntity>

    @Query("SELECT COUNT(*) FROM words WHERE wordbookId = :wordbookId AND nextReviewDate > 0 AND nextReviewDate <= :now AND isMastered = 0")
    fun getDueReviewCount(wordbookId: Int, now: Long): Flow<Int>

    @Query("SELECT * FROM words WHERE wordbookId = :wordbookId AND isMastered = 0 AND (nextReviewDate = 0 OR nextReviewDate > :now)")
    suspend fun getNewWords(wordbookId: Int, now: Long): List<WordEntity>
}

@Dao
interface StudyPlanDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: StudyPlanEntity): Long

    @Delete
    suspend fun deletePlan(plan: StudyPlanEntity)

    @Query("SELECT * FROM study_plans")
    fun getAllPlans(): Flow<List<StudyPlanEntity>>

    @Query("SELECT * FROM study_plans WHERE wordbookId = :wordbookId LIMIT 1")
    fun getPlanByWordbook(wordbookId: Int): Flow<StudyPlanEntity?>

    @Query("DELETE FROM study_plans WHERE wordbookId = :wordbookId")
    suspend fun deletePlanByWordbookId(wordbookId: Int)

    @Query("DELETE FROM study_plans")
    suspend fun deleteAllPlans()
}

@Dao
interface StudyRecordDao {
    @Insert
    suspend fun insertRecord(record: StudyRecordEntity): Long

    @Query("SELECT * FROM study_records ORDER BY studyDate DESC")
    fun getAllRecords(): Flow<List<StudyRecordEntity>>

    @Query("SELECT * FROM study_records WHERE wordbookId = :wordbookId ORDER BY studyDate DESC")
    fun getRecordsByWordbook(wordbookId: Int): Flow<List<StudyRecordEntity>>

    @Query("SELECT * FROM study_records WHERE wordId = :wordId ORDER BY studyDate DESC")
    fun getRecordsByWord(wordId: Int): Flow<List<StudyRecordEntity>>

    @Query("DELETE FROM study_records WHERE studyDate < :date")
    suspend fun deleteOldRecords(date: Long)

    // 统计查询
    @Query("SELECT COUNT(*) FROM study_records WHERE studyDate >= :startOfDay")
    fun getTodayStudyCount(startOfDay: Long): Flow<Int>

    @Query("SELECT COUNT(DISTINCT wordId) FROM study_records WHERE studyDate >= :startOfDay AND isCorrect = 1")
    fun getTodayCorrectCount(startOfDay: Long): Flow<Int>

    @Query("SELECT * FROM study_records WHERE wordId = :wordId ORDER BY studyDate DESC LIMIT :limit")
    suspend fun getRecentRecordsByWord(wordId: Int, limit: Int = 10): List<StudyRecordEntity>
}
