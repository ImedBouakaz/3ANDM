package com.example.recipebook.repository

import android.util.Log
import com.example.recipebook.api.NetworkModule
import com.example.recipebook.data.Recipe
import com.example.recipebook.data.RecipeDao
import com.example.recipebook.data.RecipeSearchQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val api: NetworkModule = NetworkModule
) {
    fun searchRecipes(query: RecipeSearchQuery) = flow {
        emit(Result.Loading)
        try {
            // Try to fetch from network
            val response = api.recipeApiService.searchRecipes(
                page = query.page,
                query = query.query.takeIf { it.isNotEmpty() }
            )
            
            // Cache the results
            recipeDao.insertRecipes(response.results)
            
            emit(Result.Success(response))
        } catch (e: IOException) {
            // Network error, try to load from cache
            Log.e("RecipeRepository", "Network error, loading from cache", e)
            val cachedRecipes = recipeDao.searchRecipes("%${query.query}%")
            if (cachedRecipes.isNotEmpty()) {
                emit(Result.Success(cachedRecipes))
            } else {
                emit(Result.Error(e))
            }
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
} 