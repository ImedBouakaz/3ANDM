package com.example.recipebook.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebook.data.Recipe
import com.example.recipebook.data.RecipeSearchQuery
import com.example.recipebook.data.RecipeSearchResponse
import com.example.recipebook.repository.RecipeRepository
import com.example.recipebook.repository.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipeUiState(
    val isLoading: Boolean = false,
    val recipes: List<Recipe> = emptyList(),
    val error: String? = null,
    val currentPage: Int = 1,
    val hasMorePages: Boolean = true,
    val selectedRecipe: Recipe? = null,
    val searchQuery: String = "",
    val selectedCategory: String = ""
)

sealed class RecipeError {
    data class NetworkError(val message: String) : RecipeError()
    data class DatabaseError(val message: String) : RecipeError()
    data class ValidationError(val message: String) : RecipeError()
}

class RecipeViewModel(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null

    private companion object {
        const val PAGE_SIZE = 30
        const val SEARCH_DEBOUNCE_MS = 300L
    }

    init {
        loadStoredRecipes()
    }

    fun loadStoredRecipes() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    searchQuery = "",
                    currentPage = 1
                )
            }

            repository.getStoredRecipes().collect { result ->
                _uiState.update { currentState ->
                    when (result) {
                        is Result.Loading -> currentState.copy(isLoading = true)
                        is Result.Success -> currentState.copy(
                            isLoading = false,
                            recipes = result.data,
                            error = null
                        )
                        is Result.Error -> currentState.copy(
                            isLoading = false,
                            error = result.exception.message
                        )
                    }
                }
            }
        }
    }

    fun loadRecipes(refresh: Boolean = false) {
        if (refresh) {
            _uiState.update { currentState ->
                currentState.copy(currentPage = 1)
            }
        }

        viewModelScope.launch {
            repository.searchRecipes(
                RecipeSearchQuery(
                    page = _uiState.value.currentPage,
                    query = _uiState.value.searchQuery,
                    category = _uiState.value.selectedCategory
                )
            ).collect { result ->
                _uiState.update { currentState ->
                    when (result) {
                        is Result.Loading -> currentState.copy(isLoading = true)
                        is Result.Success -> {
                            val newRecipes = when (result.data) {
                                is RecipeSearchResponse -> {
                                    if (refresh) result.data.results
                                    else currentState.recipes + result.data.results
                                }
                                is List<*> -> {
                                    @Suppress("UNCHECKED_CAST")
                                    if (refresh) result.data as List<Recipe>
                                    else currentState.recipes + (result.data as List<Recipe>)
                                }
                                else -> emptyList()
                            }
                            currentState.copy(
                                isLoading = false,
                                recipes = newRecipes,
                                error = null,
                                hasMorePages = newRecipes.size >= 30
                            )
                        }
                        is Result.Error -> currentState.copy(
                            isLoading = false,
                            error = result.exception.message
                        )
                    }
                }
            }
        }
    }

    fun loadNextPage() {
        if (!_uiState.value.isLoading && _uiState.value.hasMorePages) {
            _uiState.update { currentState ->
                currentState.copy(currentPage = currentState.currentPage + 1)
            }
            loadRecipes()
        }
    }

    fun resetState() {
        _uiState.update { currentState ->
            currentState.copy(
                recipes = emptyList(),
                currentPage = 1,
                hasMorePages = true,
                error = null,
                isLoading = false
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        resetState()
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                selectedCategory = "" // Clear category when searching
            )
        }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            loadRecipes(refresh = true)
        }
    }

    fun onCategorySelected(category: String) {
        searchJob?.cancel()
        resetState()
        _uiState.update { currentState ->
            currentState.copy(
                selectedCategory = category,
                searchQuery = "" // Clear search query when selecting category
            )
        }
        loadRecipes(refresh = true)
    }

    fun selectRecipe(recipe: Recipe) {
        _uiState.update { currentState ->
            currentState.copy(selectedRecipe = recipe)
        }
    }

    fun clearSelectedRecipe() {
        _uiState.update { currentState ->
            currentState.copy(selectedRecipe = null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
} 