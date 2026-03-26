package com.syq.lexi.data.repository

import android.content.Context
import android.util.Log
import com.syq.lexi.data.auth.AuthPreferences
import com.syq.lexi.data.database.StudyPlanEntity
import com.syq.lexi.data.database.WordEntity
import com.syq.lexi.data.database.WordbookEntity
import com.syq.lexi.data.network.RetrofitClient
import com.syq.lexi.data.network.WordDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class SyncStatus(
    val success: Boolean,
    val message: String,
    val wordbooksSynced: Int = 0,
    val wordsSynced: Int = 0
)

class SyncRepository(
    private val context: Context,
    private val wordbookRepository: WordbookRepository
) {
    private val authPrefs = AuthPreferences(context)
    private val api = RetrofitClient.api
    private val syncMutex = Mutex()

    suspend fun syncFromUserWordbooks(): SyncStatus = syncMutex.withLock {
        val token = authPrefs.token.first()
        if (token.isNullOrEmpty()) return SyncStatus(false, "未登录")
        val bearer = "Bearer $token"
        return try {
            var wordbooksSynced = 0
            var wordsSynced = 0
            val remoteWordbooks = api.getWordbooks(bearer)

            if (remoteWordbooks.isEmpty()) {
                // 新用户：从系统词库初始化个人词库
                Log.d("SyncRepository", "New user, initializing from system wordbooks")
                val systemWordbooks = api.getSystemWordbooks(bearer)
                for (sysWb in systemWordbooks) {
                    val systemWords = api.getSystemWords(bearer, sysWb.id)
                    // 上传到个人服务端词库
                    val createdWb = api.createWordbook(bearer, com.syq.lexi.data.network.WordbookDto(
                        name = sysWb.name, category = sysWb.category, description = sysWb.description
                    ))
                    if (systemWords.isNotEmpty()) {
                        api.syncWords(bearer, createdWb.id, com.syq.lexi.data.network.SyncWordsRequest(systemWords))
                    }
                    // 下载到本地 Room
                    val localId = wordbookRepository.insertWordbook(
                        WordbookEntity(name = sysWb.name, category = sysWb.category,
                            description = sysWb.description, totalWords = systemWords.size)
                    ).toInt()
                    if (systemWords.isNotEmpty()) {
                        wordbookRepository.insertWords(systemWords.map { it.toEntity(localId) })
                        wordsSynced += systemWords.size
                    }
                    wordbooksSynced++
                    Log.d("SyncRepository", "InitSync: created '${sysWb.name}' with ${systemWords.size} words")
                }
            } else {
                // 已有个人词库：下载到本地 Room，更新复习数据
                val localAll = wordbookRepository.getAllWordbooks().first()
                for (remote in remoteWordbooks) {
                    val existing = localAll.find { it.name == remote.name }
                    if (existing == null) {
                        // 本地没有 → 下载
                        val remoteWords = api.getWords(bearer, remote.id)
                        val localId = wordbookRepository.insertWordbook(
                            WordbookEntity(name = remote.name, category = remote.category,
                                description = remote.description, totalWords = remote.totalWords)
                        ).toInt()
                        if (remoteWords.isNotEmpty()) {
                            wordbookRepository.insertWords(remoteWords.map { it.toEntity(localId) })
                            wordsSynced += remoteWords.size
                        }
                        wordbooksSynced++
                        Log.d("SyncRepository", "InitSync: downloaded '${remote.name}' ${remoteWords.size} words")
                    } else {
                        // 本地已有 → 更新复习数据
                        val remoteWords = api.getWords(bearer, remote.id)
                        val localWords = wordbookRepository.getWordsByWordbook(existing.id).first()
                        var updatedCount = 0
                        for (remoteWord in remoteWords) {
                            val localWord = localWords.find { it.english.equals(remoteWord.english, ignoreCase = true) }
                            if (localWord != null && remoteWord.reviewCount > 0) {
                                wordbookRepository.updateReviewData(
                                    localWord.id, remoteWord.familiarity,
                                    remoteWord.reviewCount, remoteWord.nextReviewDate
                                )
                                updatedCount++
                            }
                        }
                        Log.d("SyncRepository", "InitSync: updated review data for '${remote.name}', $updatedCount words")
                    }
                }
            }
            SyncStatus(true, "初始化成功", wordbooksSynced, wordsSynced)
        } catch (e: Exception) {
            Log.e("SyncRepository", "InitSync failed", e)
            SyncStatus(false, e.message ?: "初始化失败")
        }
    }

    suspend fun syncAll(): SyncStatus {
        val token = authPrefs.token.first()
        Log.d("SyncRepository", "syncAll: token=${if (token.isNullOrEmpty()) "EMPTY" else "OK(${token.take(10)}...)"})")
        if (token.isNullOrEmpty()) return SyncStatus(false, "未登录")
        val bearer = "Bearer $token"

        return try {
            var wordbooksSynced = 0
            var wordsSynced = 0

            val systemWordbooks = api.getSystemWordbooks(bearer)
            val userWordbooks = api.getWordbooks(bearer)
            val localAll = wordbookRepository.getAllWordbooks().first()

            for (sysWb in systemWordbooks) {
                val systemWords = api.getSystemWords(bearer, sysWb.id)
                val userWb = userWordbooks.find { it.name == sysWb.name }
                val localWb = localAll.find { it.name == sysWb.name }

                if (userWb == null) {
                    // 个人词库没有这个词库 → 创建并下载到本地
                    val createdWb = api.createWordbook(bearer, com.syq.lexi.data.network.WordbookDto(
                        name = sysWb.name, category = sysWb.category, description = sysWb.description
                    ))
                    if (systemWords.isNotEmpty()) {
                        api.syncWords(bearer, createdWb.id, com.syq.lexi.data.network.SyncWordsRequest(systemWords))
                    }
                    val localId = if (localWb == null) {
                        wordbookRepository.insertWordbook(WordbookEntity(
                            name = sysWb.name, category = sysWb.category,
                            description = sysWb.description, totalWords = systemWords.size
                        )).toInt()
                    } else localWb.id
                    if (systemWords.isNotEmpty()) {
                        wordbookRepository.insertWords(systemWords.map { it.toEntity(localId) })
                        wordsSynced += systemWords.size
                    }
                    wordbooksSynced++
                    Log.d("SyncRepository", "syncAll: created '${sysWb.name}' with ${systemWords.size} words")
                } else {
                    // 个人词库已有 → 补充系统有但个人没有的单词
                    val userWords = api.getWords(bearer, userWb.id)
                    val userEnglish = userWords.map { it.english.lowercase() }.toSet()
                    val toAdd = systemWords.filter { it.english.lowercase() !in userEnglish }
                    if (toAdd.isNotEmpty()) {
                        api.syncWords(bearer, userWb.id, com.syq.lexi.data.network.SyncWordsRequest(toAdd))
                        wordsSynced += toAdd.size
                        Log.d("SyncRepository", "syncAll: added ${toAdd.size} words to '${sysWb.name}'")
                    }
                    // 本地也补充
                    if (localWb != null) {
                        val localWords = wordbookRepository.getWordsByWordbook(localWb.id).first()
                        val localEnglish = localWords.map { it.english.lowercase() }.toSet()
                        val toAddLocal = systemWords.filter { it.english.lowercase() !in localEnglish }
                        if (toAddLocal.isNotEmpty()) {
                            wordbookRepository.insertWords(toAddLocal.map { it.toEntity(localWb.id) })
                        }
                    }
                    if (toAdd.isEmpty()) Log.d("SyncRepository", "syncAll: '${sysWb.name}' already up to date")
                }
            }

            // 同步背诵计划
            try {
                val remotePlans = api.getStudyPlans(bearer)
                val latestLocal = wordbookRepository.getAllWordbooks().first()
                for (plan in remotePlans) {
                    val localWb = latestLocal.find { it.name == plan.wordbookName }
                    if (localWb != null) {
                        wordbookRepository.insertStudyPlan(
                            StudyPlanEntity(wordbookId = localWb.id, dailyWords = plan.dailyWords)
                        )
                    }
                }
                Log.d("SyncRepository", "syncAll: study plans synced: ${remotePlans.size}")
            } catch (e: Exception) {
                Log.e("SyncRepository", "Sync study plans failed: ${e.message}")
            }

            Log.d("SyncRepository", "syncAll done: $wordbooksSynced wordbooks, $wordsSynced words")
            SyncStatus(true, "同步成功", wordbooksSynced, wordsSynced)
        } catch (e: Exception) {
            Log.e("SyncRepository", "Sync failed", e)
            SyncStatus(false, e.message ?: "同步失败")
        }
    }
}

fun WordEntity.toDto() = com.syq.lexi.data.network.WordDto(
    english = english, chinese = chinese, pronunciation = pronunciation,
    partOfSpeech = partOfSpeech, example = example,
    exampleTranslation = exampleTranslation, isMastered = isMastered, isStarred = isStarred,
    familiarity = familiarity, reviewCount = reviewCount, nextReviewDate = nextReviewDate
)

fun WordDto.toEntity(localWordbookId: Int) = WordEntity(
    wordbookId = localWordbookId, english = english, chinese = chinese,
    pronunciation = pronunciation, partOfSpeech = partOfSpeech,
    example = example, exampleTranslation = exampleTranslation,
    isMastered = isMastered, isStarred = isStarred
)
