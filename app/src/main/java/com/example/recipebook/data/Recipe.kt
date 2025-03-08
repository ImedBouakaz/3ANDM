package com.example.recipebook.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
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
    
    @SerializedName("ingredients")
    val ingredients: List<String> = emptyList(),
    
    @SerializedName("date_added")
    val dateAdded: String = "",
    
    @SerializedName("date_updated")
    val dateUpdated: String = ""
)

data class RecipeResponse(
    val recipe: Recipe
) 