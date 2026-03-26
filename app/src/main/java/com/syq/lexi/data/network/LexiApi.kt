package com.syq.lexi.data.network

import retrofit2.http.*

interface LexiApi {

    @POST("api/auth/register")
    suspend fun register(@Body req: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): AuthResponse

    // 获取个人词库列表
    @GET("api/wordbooks")
    suspend fun getWordbooks(@Header("Authorization") token: String): List<WordbookDto>

    // 创建词库
    @POST("api/wordbooks")
    suspend fun createWordbook(
        @Header("Authorization") token: String,
        @Body req: WordbookDto
    ): WordbookDto

    // 删除词库
    @DELETE("api/wordbooks/{id}")
    suspend fun deleteWordbook(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    )

    // 获取词库单词
    @GET("api/wordbooks/{id}/words")
    suspend fun getWords(
        @Header("Authorization") token: String,
        @Path("id") wordbookId: Int
    ): List<WordDto>

    // 获取系统词库列表
    @GET("api/system/wordbooks")
    suspend fun getSystemWordbooks(
        @Header("Authorization") token: String
    ): List<WordbookDto>

    // 获取系统词库单词
    @GET("api/system/wordbooks/{id}/words")
    suspend fun getSystemWords(
        @Header("Authorization") token: String,
        @Path("id") wordbookId: Int
    ): List<WordDto>

    // 批量同步单词
    @POST("api/wordbooks/{id}/words/sync")
    suspend fun syncWords(
        @Header("Authorization") token: String,
        @Path("id") wordbookId: Int,
        @Body req: SyncWordsRequest
    ): SyncResult

    // 更新掌握状态
    @PATCH("api/wordbooks/{wordbookId}/words/{wordId}/mastered")
    suspend fun updateMastered(
        @Header("Authorization") token: String,
        @Path("wordbookId") wordbookId: Int,
        @Path("wordId") wordId: Int,
        @Query("mastered") mastered: Boolean
    )

    // 更新收藏状态
    @PATCH("api/wordbooks/{wordbookId}/words/{wordId}/starred")
    suspend fun updateStarred(
        @Header("Authorization") token: String,
        @Path("wordbookId") wordbookId: Int,
        @Path("wordId") wordId: Int,
        @Query("starred") starred: Boolean
    )

    // 更新复习数据
    @PATCH("api/wordbooks/{wordbookId}/words/{wordId}/review")
    suspend fun updateReviewData(
        @Header("Authorization") token: String,
        @Path("wordbookId") wordbookId: Int,
        @Path("wordId") wordId: Int,
        @Query("familiarity") familiarity: Float,
        @Query("reviewCount") reviewCount: Int,
        @Query("nextReviewDate") nextReviewDate: Long
    )

    // 删除单词
    @DELETE("api/wordbooks/{wordbookId}/words/{wordId}")
    suspend fun deleteWord(
        @Header("Authorization") token: String,
        @Path("wordbookId") wordbookId: Int,
        @Path("wordId") wordId: Int
    )

    // 获取背诵计划
    @GET("api/study-plans")
    suspend fun getStudyPlans(@Header("Authorization") token: String): List<StudyPlanDto>

    // 保存背诵计划
    @POST("api/study-plans")
    suspend fun saveStudyPlan(
        @Header("Authorization") token: String,
        @Body req: StudyPlanDto
    ): StudyPlanDto

    // 删除背诵计划
    @DELETE("api/study-plans")
    suspend fun deleteStudyPlan(
        @Header("Authorization") token: String,
        @Query("wordbookName") wordbookName: String
    )
}
