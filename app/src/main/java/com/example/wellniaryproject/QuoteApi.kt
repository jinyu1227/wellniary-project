package com.example.wellniaryproject

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// 返回的 JSON 是 List<Quote>
data class Quote(
    val q: String, // Quote 内容
    val a: String  // 作者
)

interface ZenQuoteService {
    @GET("api/random")
    suspend fun getRandomQuote(): List<Quote>
}

object QuoteApi {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://zenquotes.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: ZenQuoteService = retrofit.create(ZenQuoteService::class.java)
}
