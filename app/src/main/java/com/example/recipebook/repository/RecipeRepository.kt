package com.example.recipebook.repository

import com.example.recipebook.api.NetworkModule
import com.example.recipebook.data.RecipeSearchQuery
import kotlinx.coroutines.flow.flow

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>() // État de chargement
}

// Référentiel pour gérer les recettes
class RecipeRepository(
    private val recipeDao: com.example.recipebook.data.RecipeDao,
    private val api: NetworkModule = NetworkModule
) {

    // Récupère les recettes stockées localement
    fun getStoredRecipes() = flow {
        emit(Result.Loading) // Émet l'état de chargement
        try {
            recipeDao.getAllRecipes().collect { recipes ->
                emit(Result.Success(recipes)) // Émet les recettes récupérées
            }
        } catch (e: Exception) {
            emit(Result.Error(e)) // Émet une erreur en cas d'exception
        }
    }

    // Recherche des recettes via l'API
    fun searchRecipes(query: RecipeSearchQuery) = flow {
        emit(Result.Loading)
        try {
            val response = query.query.takeIf { it.isNotEmpty() }?.let {
                api.recipeApiService.searchRecipes(
                    page = query.page,
                    query = it
                )
            }
            emit(Result.Success(response)) // Donne la réponse de l'API
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}
