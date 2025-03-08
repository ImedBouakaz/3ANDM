package com.example.recipebook.api

import com.example.recipebook.BuildConfig
import com.example.recipebook.data.RecipeResponse
import com.example.recipebook.data.RecipeSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Header

interface RecipeApiService {
    companion object {
        private const val TOKEN_PREFIX = "Token "
        private const val BASE_URL = "https://food2fork.ca/"
        private const val AUTH_TOKEN = BuildConfig.API_TOKEN
    }

    @GET("api/recipe/search/")
    suspend fun searchRecipes(
        @Query("page") page: Int,
        @Query("query") query: String? = null,
        @Header("Authorization") token: String = TOKEN_PREFIX + AUTH_TOKEN
    ): RecipeSearchResponse

    @GET("api/recipe/get/")
    suspend fun getRecipe(
        @Query("id") id: String,
        @Header("Authorization") token: String = TOKEN_PREFIX + AUTH_TOKEN
    ): RecipeResponse
} 