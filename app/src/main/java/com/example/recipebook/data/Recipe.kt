package com.example.recipebook.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey
    @SerializedName("pk")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("publisher")
    val publisher: String = "",

    @SerializedName("featured_image")
    val featuredImage: String = "",

    @SerializedName("rating")
    val rating: Int = 0,

    @SerializedName("source_url")
    val sourceUrl: String = "",

    @SerializedName("description")
    val description: String = "",

    @SerializedName("cooking_instructions")
    val cookingInstructions: String? = null,

    @SerializedName("ingredients")
    val ingredients: List<String> = emptyList(),

    @SerializedName("date_added")
    val dateAdded: String = "",

    @SerializedName("date_updated")
    val dateUpdated: String = "",

    @SerializedName("long_date_added")
    val longDateAdded: Long = 0,

    @SerializedName("long_date_updated")
    val longDateUpdated: Long = 0
)


data class RecipeResponse(
    val recipe: Recipe
)