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

    @DELETE("api/wordbooks/{wordbookId}/words/{wordId}")
    suspend fun deleteWord(
        @Header("Authorization") token: String,
        @Path("wordbookId") wordbookId: Int,
        @Path("wordId") wordId: Int
    )
}
