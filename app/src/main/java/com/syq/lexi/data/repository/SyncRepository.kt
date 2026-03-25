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

    suspend fun syncFromUserWordbooks(): SyncStatus {
        val token = authPrefs.token.first()
        if (token.isNullOrEmpty()) return SyncStatus(false, "未登录")
        val bearer = "Bearer $token"
        return try {
            var wordbooksSynced = 0
            var wordsSynced = 0
            val remoteWordbooks = api.getWordbooks(bearer)
            for (remote in remoteWordbooks) {
                val localAll = wordbookRepository.getAllWordbooks().first()
                val existing = localAll.find { it.name == remote.name }
                if (existing == null) {
                    val newWb = WordbookEntity(
                        name = remote.name, category = remote.category,
                        description = remote.description, totalWords = remote.totalWords
                    )
                    val localId = wordbookRepository.insertWordbook(newWb).toInt()
                    val remoteWords = api.getWords(bearer, remote.id)
                    if (remoteWords.isNotEmpty()) {
                        wordbookRepository.insertWords(remoteWords.map { it.toEntity(localId) })
                        wordsSynced += remoteWords.size
                    }
                    wordbooksSynced++
                    Log.d("SyncRepository", "InitSync: downloaded '${remote.name}' ${remoteWords.size} words")
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
        Log.d("SyncRepository", "syncAll: token=${if (token.isNullOrEmpty()) "EMPTY" else "OK(${token.take(10)}...)"}")
        if (token.isNullOrEmpty()) return SyncStatus(false, "未登录")
        val bearer = "Bearer $token"

        return try {
            var wordbooksSynced = 0
            var wordsSynced = 0

            // ===== 从系统词库同步（不影响用户自己创建的词库）=====
            val systemWordbooks = api.getSystemWordbooks(bearer)
            val localAll = wordbookRepository.getAllWordbooks().first()

            for (sysWb in systemWordbooks) {
                val existing = localAll.find { it.name == sysWb.name }
                val systemWords = api.getSystemWords(bearer, sysWb.id)
                Log.d("SyncRepository", "System: '${sysWb.name}' (${sysWb.id}), localMatch: ${existing?.id}, systemWords: ${systemWords.size}")

                if (existing == null) {
                    // 本地没有 → 完整下载到本地，并上传到用户服务端词库
                    val newWb = WordbookEntity(
                        name = sysWb.name, category = sysWb.category,
                        description = sysWb.description, totalWords = systemWords.size
                    )
                    val localId = wordbookRepository.insertWordbook(newWb).toInt()
                    if (systemWords.isNotEmpty()) {
                        wordbookRepository.insertWords(systemWords.map { it.toEntity(localId) })
                        wordsSynced += systemWords.size
                    }
                    // 上传到用户服务端词库
                    try {
                        val createdWb = api.createWordbook(bearer, com.syq.lexi.data.network.WordbookDto(
                            name = sysWb.name, category = sysWb.category,
                            description = sysWb.description
                        ))
                        if (systemWords.isNotEmpty()) {
                            api.syncWords(bearer, createdWb.id,
                                com.syq.lexi.data.network.SyncWordsRequest(systemWords))
                        }
                        Log.d("SyncRepository", "Uploaded '${sysWb.name}' to user server")
                    } catch (e: Exception) {
                        Log.e("SyncRepository", "Upload '${sysWb.name}' failed: ${e.message}")
                    }
                    wordbooksSynced++
                    Log.d("SyncRepository", "Downloaded new wb '${sysWb.name}' → localId=$localId, ${systemWords.size} words")
                } else {
                    // 本地已有 → 只补充系统有但本地没有的单词
                    val localWords = wordbookRepository.getWordsByWordbook(existing.id).first()
                    val localEnglish = localWords.map { it.english.lowercase() }.toSet()
                    val toDownload = systemWords.filter { it.english.lowercase() !in localEnglish }
                    if (toDownload.isNotEmpty()) {
                        wordbookRepository.insertWords(toDownload.map { it.toEntity(existing.id) })
                        wordsSynced += toDownload.size
                        Log.d("SyncRepository", "Merged '${sysWb.name}': added ${toDownload.size} words")
                    } else {
                        Log.d("SyncRepository", "'${sysWb.name}' already up to date")
                    }
                    // 把本地最新单词列表上传到服务端用户词库（确保删词操作也持久化）
                    try {
                        val remoteUserWbs = api.getWordbooks(bearer)
                        val userWb = remoteUserWbs.find { it.name == sysWb.name }
                        if (userWb != null) {
                            val latestLocal = wordbookRepository.getWordsByWordbook(existing.id).first()
                            if (latestLocal.isNotEmpty()) {
                                api.syncWords(bearer, userWb.id,
                                    com.syq.lexi.data.network.SyncWordsRequest(latestLocal.map { it.toDto() }))
                                Log.d("SyncRepository", "Uploaded local changes for '${sysWb.name}' to user server")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("SyncRepository", "Upload changes for '${sysWb.name}' failed: ${e.message}")
                    }
                }
            }

            // ===== 同步背诵计划（只下载）=====
            try {
                Log.d("SyncRepository", "Fetching study plans...")
                val remotePlans = api.getStudyPlans(bearer)
                Log.d("SyncRepository", "Got ${remotePlans.size} study plans")
                val latestLocal = wordbookRepository.getAllWordbooks().first()
                for (plan in remotePlans) {
                    val localWb = latestLocal.find { it.name == plan.wordbookName }
                    if (localWb != null) {
                        wordbookRepository.insertStudyPlan(
                            StudyPlanEntity(wordbookId = localWb.id, dailyWords = plan.dailyWords)
                        )
                    }
                }
                Log.d("SyncRepository", "Study plans synced: ${remotePlans.size}")
            } catch (e: Exception) {
                Log.e("SyncRepository", "Sync study plans failed: ${e.message}", e)
            }

            Log.d("SyncRepository", "Sync done: $wordbooksSynced wordbooks, $wordsSynced words")
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
    exampleTranslation = exampleTranslation, isMastered = isMastered, isStarred = isStarred
)

fun WordDto.toEntity(localWordbookId: Int) = WordEntity(
    wordbookId = localWordbookId, english = english, chinese = chinese,
    pronunciation = pronunciation, partOfSpeech = partOfSpeech,
    example = example, exampleTranslation = exampleTranslation,
    isMastered = isMastered, isStarred = isStarred
)
