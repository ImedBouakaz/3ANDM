package com.example.recipebook.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipebook.api.NetworkModule
import com.example.recipebook.data.Recipe
import com.example.recipebook.data.RecipeDao
import com.example.recipebook.data.RecipeSearchQuery
import com.example.recipebook.repository.RecipeRepository
import com.example.recipebook.repository.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

class RecipeViewModel(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadRecipes()
    }

    fun loadRecipes(refresh: Boolean = false) {
        if (refresh) {
            _uiState.value = _uiState.value.copy(currentPage = 1)
        }

        viewModelScope.launch {
            repository.searchRecipes(
                RecipeSearchQuery(
                    page = _uiState.value.currentPage,
                    query = _uiState.value.searchQuery,
                    category = _uiState.value.selectedCategory
                )
            ).collect { result ->
                _uiState.value = when (result) {
                    is Result.Loading -> _uiState.value.copy(isLoading = true)
                    is Result.Success -> {
                        val recipes = if (refresh) {
                            result.data
                        } else {
                            _uiState.value.recipes + result.data
                        }
                        _uiState.value.copy(
                            isLoading = false,
                            recipes = recipes,
                            error = null,
                            hasMorePages = recipes.size >= 30
                        )
                    }
                    is Result.Error -> _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message
                    )
                }
            }
        }
    }

    fun loadNextPage() {
        if (!_uiState.value.isLoading && _uiState.value.hasMorePages) {
            _uiState.value = _uiState.value.copy(currentPage = _uiState.value.currentPage + 1)
            loadRecipes()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // Debounce search
            loadRecipes(refresh = true)
        }
    }

    fun onCategorySelected(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        loadRecipes(refresh = true)
    }

    fun selectRecipe(recipe: Recipe) {
        _uiState.value = _uiState.value.copy(selectedRecipe = recipe)
    }

    fun clearSelectedRecipe() {
        _uiState.value = _uiState.value.copy(selectedRecipe = null)
    }
} 