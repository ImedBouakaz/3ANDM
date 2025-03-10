package com.example.recipebook

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.recipebook.data.Recipe
import com.example.recipebook.ui.screens.RecipeListScreen
import com.example.recipebook.ui.screens.SplashScreen
import com.example.recipebook.ui.screens.RecipeDetailScreen
import com.example.recipebook.ui.theme.RecipeBookTheme
import com.example.recipebook.viewmodel.RecipeViewModel
import com.example.recipebook.repository.RecipeRepository
import com.example.recipebook.data.RecipeDatabase
import kotlinx.coroutines.delay

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private var database: RecipeDatabase? = null
    private lateinit var viewModel: RecipeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Starting MainActivity initialization")

        initializeDatabase()

        database?.let { db ->
            val repository = RecipeRepository(db.recipeDao())
            viewModel = RecipeViewModel(repository)
        }

        setContent {
            Log.d(TAG, "onCreate: Setting up Compose content")
            RecipeBookTheme {
                var showSplash by remember { mutableStateOf(true) }
                var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }

                LaunchedEffect(true) {
                    delay(2000) // Show splash for 2 seconds
                    showSplash = false
                }

                if (showSplash) {
                    SplashScreen()
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
//                        color = Color(0xFFFB8A4E)
                    ) {
                        database?.let {
                            if (selectedRecipe != null) {
                                RecipeDetailScreen(
                                    recipe = selectedRecipe!!,
                                    onBackClick = { selectedRecipe = null }
                                )
                            } else {
                                RecipeListScreen(
                                    viewModel = viewModel,
                                    onRecipeClick = { recipe ->
                                        selectedRecipe = recipe
                                    }
                                )
                            }
                        } ?: run {
                            Toast.makeText(
                                this@MainActivity,
                                "Database initialization failed",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun initializeDatabase() {
        try {
            Log.d(TAG, "initializeDatabase: Starting database initialization")
            database = RecipeDatabase.getDatabase(applicationContext)
            Log.d(TAG, "initializeDatabase: Database initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "initializeDatabase: Failed to initialize database", e)
            Log.e(TAG, "initializeDatabase: Stack trace: ${e.stackTrace.joinToString("\\n")}")
            Toast.makeText(
                this,
                "Database initialization failed: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
