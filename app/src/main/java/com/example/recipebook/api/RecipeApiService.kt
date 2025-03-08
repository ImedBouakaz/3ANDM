package com.example.recipebook.api

import com.example.recipebook.data.RecipeResponse
import com.example.recipebook.data.RecipeSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface RecipeApiService {
    @GET("api/recipe/search/")
    suspend fun searchRecipes(
        @Query("page") page: Int = 1,
        @Query("query") query: String? = null
    ): RecipeSearchResponse

    @GET("api/recipe/get/")
    suspend fun getRecipe(
        @Query("id") id: String
    ): RecipeResponse
} 