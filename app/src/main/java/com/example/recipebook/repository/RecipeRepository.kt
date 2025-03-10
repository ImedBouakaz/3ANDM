package com.example.recipebook.repository

import android.util.Log
import com.example.recipebook.api.NetworkModule
import com.example.recipebook.data.Recipe
import com.example.recipebook.data.RecipeSearchQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.io.IOException
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
            val response = api.recipeApiService.searchRecipes(
                page = query.page,
                query = query.query.takeIf { it.isNotEmpty() }
            )

            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun searchLocalRecipes(query: String) = flow {
        emit(Result.Loading)
        try {
            val cachedRecipes = recipeDao.searchRecipes("%$query%")
            emit(Result.Success(cachedRecipes))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    fun getRecipeDetails(id: String) = flow {
        emit(Result.Loading)
        try {
            // Try to fetch from network
            val response = api.recipeApiService.getRecipe(id)
            // Cache the recipe
            recipeDao.insertRecipe(response.recipe)
            emit(Result.Success(response.recipe))
        } catch (e: IOException) {
            // Network error, try to load from cache
            Log.e("RecipeRepository", "Network error, loading from cache", e)
            val cachedRecipe = recipeDao.getRecipeById(id)
            if (cachedRecipe != null) {
                emit(Result.Success(cachedRecipe))
            } else {
                emit(Result.Error(e))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    // New function to explicitly save recipes
    suspend fun saveRecipes(recipes: List<Recipe>) {
        recipeDao.insertRecipes(recipes)
    }
} 