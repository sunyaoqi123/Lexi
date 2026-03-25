package com.syq.lexi.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 模拟器用 10.0.2.2，真机用电脑局域网 IP
    // private const val BASE_URL = "http://10.0.2.2:8081/"  // 模拟器
    private const val BASE_URL = "http://172.20.10.2:8081/"  // 真机（手机热点）
    // private const val BASE_URL = "http://192.168.1.x:8081/"  // 真机 WiFi

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val api: LexiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LexiApi::class.java)
    }
}
