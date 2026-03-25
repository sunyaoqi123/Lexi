package com.syq.lexi.data.network

import retrofit2.http.*

interface LexiApi {

    @POST("api/auth/register")
    suspend fun register(@Body req: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): AuthResponse

    @GET("api/wordbooks")
    suspend fun getWordbooks(@Header("Authorization") token: String): List<WordbookDto>

    @POST("api/wordbooks")
    suspend fun createWordbook(
        @Header("Authorization") token: String,
        @Body req: WordbookDto
    ): WordbookDto

    @DELETE("api/wordbooks/{id}")
    suspend fun deleteWordbook(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    )

    @GET("api/wordbooks/{id}/words")
    suspend fun getWords(
        @Header("Authorization") token: String,
        @Path("id") wordbookId: Int
    ): List<WordDto>

    @GET("api/system/wordbooks")
    suspend fun getSystemWordbooks(
        @Header("Authorization") token: String
    ): List<WordbookDto>

    @GET("api/system/wordbooks/{id}/words")
    suspend fun getSystemWords(
        @Header("Authorization") token: String,
        @Path("id") wordbookId: Int
    ): List<WordDto>

    @POST("api/wordbooks/{id}/words/sync")
    suspend fun syncWords(
        @Header("Authorization") token: String,
        @Path("id") wordbookId: Int,
        @Body req: SyncWordsRequest
    ): SyncResult

    @PATCH("api/wordbooks/{wordbookId}/words/{wordId}/mastered")
    suspend fun updateMastered(
        @Header("Authorization") token: String,
        @Path("wordbookId") wordbookId: Int,
        @Path("wordId") wordId: Int,
        @Query("mastered") mastered: Boolean
    )

    @PATCH("api/wordbooks/{wordbookId}/words/{wordId}/starred")
    suspend fun updateStarred(
        @Header("Authorization") token: String,
        @Path("wordbookId") wordbookId: Int,
        @Path("wordId") wordId: Int,
        @Query("starred") starred: Boolean
    )

    @DELETE("api/wordbooks/{wordbookId}/words/{wordId}")
    suspend fun deleteWord(
        @Header("Authorization") token: String,
        @Path("wordbookId") wordbookId: Int,
        @Path("wordId") wordId: Int
    )

    // 背诵计划
    @GET("api/study-plans")
    suspend fun getStudyPlans(@Header("Authorization") token: String): List<StudyPlanDto>

    @POST("api/study-plans")
    suspend fun saveStudyPlan(
        @Header("Authorization") token: String,
        @Body req: StudyPlanDto
    ): StudyPlanDto

    @DELETE("api/study-plans")
    suspend fun deleteStudyPlan(
        @Header("Authorization") token: String,
        @Query("wordbookName") wordbookName: String
    )
}
