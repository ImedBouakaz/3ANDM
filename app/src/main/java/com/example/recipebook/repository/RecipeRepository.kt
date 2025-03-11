package com.example.recipebook.repository

import com.example.recipebook.api.NetworkModule
import com.example.recipebook.data.RecipeSearchQuery
import kotlinx.coroutines.flow.flow

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

class RecipeRepository(
    private val recipeDao: com.example.recipebook.data.RecipeDao,
    private val api: NetworkModule = NetworkModule
) {

    fun getStoredRecipes() = flow {
        emit(Result.Loading)
        try {
            recipeDao.getAllRecipes().collect { recipes ->
                emit(Result.Success(recipes))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun searchRecipes(query: RecipeSearchQuery) = flow {
        emit(Result.Loading)
        try {
            // Try to fetch from network
            val response = query.query.takeIf { it.isNotEmpty() }?.let {
                api.recipeApiService.searchRecipes(
                    page = query.page,
                    query = it
                )
            }

            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}