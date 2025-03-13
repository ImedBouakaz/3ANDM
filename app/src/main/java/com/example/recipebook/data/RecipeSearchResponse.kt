package com.example.recipebook.data

data class RecipeSearchResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<Recipe>
)

data class RecipeSearchQuery(
    val page: Int = 1,
    val query: String = "",
    val category: String = ""
) 