package com.example.wellniaryproject


import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NutritionixService {
    @Headers(
        "x-app-id: f99300ac",
        "x-app-key: 8ace2c6bc5df74eed33c2275333cbcb0",
        "Content-Type: application/json"
    )
    @POST("v2/natural/nutrients")
    suspend fun getNutritionInfo(@Body body: NutritionRequest): NutritionResponse
}

data class NutritionRequest(
    val query: String
)

data class NutritionResponse(
    val foods: List<FoodItem>
)

data class FoodItem(
    val food_name: String,
    val nf_calories: Float,
    val nf_protein: Float,
    val nf_total_fat: Float
)
