package com.example.recipebook.viewmodel

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

    fun loadStoredRecipes() {
        searchJob?.cancel()
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    searchQuery = "",
                    selectedCategory = "",
                    currentPage = 1
                )
            }

            repository.getStoredRecipes().collect { result ->
                _uiState.update { currentState ->
                    when (result) {
                        is Result.Loading -> currentState.copy(isLoading = true)
                        is Result.Success -> {
                            currentState.copy(
                                isLoading = false,
                                recipes = result.data,
                                error = null
                            )
                        }
                        is Result.Error -> {
                            currentState.copy(
                                isLoading = false,
                                error = result.exception.message
                            )
                        }
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
            _uiState.update { currentState ->
                currentState.copy(isLoading = true)
            }

            if (_uiState.value.searchQuery.isNotEmpty() || _uiState.value.selectedCategory.isNotEmpty()) {
                repository.searchRecipes(
                    RecipeSearchQuery(
                        page = _uiState.value.currentPage,
                        query = _uiState.value.searchQuery,
                        category = _uiState.value.selectedCategory
                    )
                ).collect { result ->
                    _uiState.update { currentState ->
                        when (result) {
                            is Result.Loading -> currentState
                            is Result.Success -> {
                                when (val data = result.data) {
                                    is RecipeSearchResponse -> {
                                        val newRecipes = if (refresh) {
                                            data.results
                                        } else {
                                            currentState.recipes + data.results
                                        }
                                        // Remove automatic database saving during search
                                        currentState.copy(
                                            isLoading = false,
                                            recipes = newRecipes,
                                            error = null,
                                            hasMorePages = newRecipes.size >= PAGE_SIZE
                                        )
                                    }
                                    else -> currentState.copy(
                                        isLoading = false,
                                        error = "Unexpected response type"
                                    )
                                }
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
    }

    // Permet de charger la page suivante
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

    // Clear l'écran grâce à un bouton
    fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        if (query.isEmpty()) {
            clearScreen()
            return
        }
        resetState()
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                selectedCategory = "",
                isLoading = true
            )
        }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            loadRecipes(refresh = true)
        }
    }

    fun clearScreen() {
        searchJob?.cancel()
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = false,
                recipes = emptyList(),
                error = null,
                searchQuery = "",
                selectedCategory = "",
                currentPage = 1,
                hasMorePages = true
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
} 