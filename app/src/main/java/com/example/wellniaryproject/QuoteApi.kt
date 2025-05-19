package com.example.wellniaryproject

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class Quote(
    val q: String,
    val a: String
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
