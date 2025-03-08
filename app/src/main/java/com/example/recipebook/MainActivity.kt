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
import com.example.recipebook.ui.RecipeSearchScreen
import com.example.recipebook.ui.theme.RecipeBookTheme
import com.example.recipebook.viewmodel.RecipeViewModel
import com.example.recipebook.data.Recipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private var database: RecipeDatabase? = null
    private lateinit var viewModel: RecipeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Starting MainActivity initialization")

        initializeDatabase()
        
        database?.let { db ->
            viewModel = RecipeViewModel(db.recipeDao())
        }

        setContent {
            Log.d(TAG, "onCreate: Setting up Compose content")
            RecipeBookTheme {
                var showSplash by remember { mutableStateOf(true) }
                
                LaunchedEffect(true) {
                    delay(2000) // Show splash for 2 seconds
                    showSplash = false
                }

                if (showSplash) {
                    SplashScreen()
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        database?.let {
                            RecipeSearchScreen(viewModel = viewModel)
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

    private fun saveRecipe(title: String, content: String) {
        if (database == null) {
            Log.e(TAG, "saveRecipe: Database is not initialized")
            Toast.makeText(this, "Database is not initialized", Toast.LENGTH_LONG).show()
            return
        }

        if (title.isEmpty()) {
            Log.w(TAG, "saveRecipe: Empty title")
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val recipe = Recipe(
            title = title,
            ingredients = listOf(content)  // Using content as an ingredient
        )
        Log.d(TAG, "saveRecipe: Attempting to save recipe: $recipe")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                database?.recipeDao()?.insertRecipe(recipe)
                Log.d(TAG, "saveRecipe: Recipe saved successfully")
                
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(this@MainActivity, "Recipe saved successfully!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "saveRecipe: Failed to save recipe", e)
                Log.e(TAG, "saveRecipe: Stack trace: ${e.stackTrace.joinToString("\\n")}")
                
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to save recipe: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}

@Composable
fun RecipeInput(
    onSaveRecipe: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Recipe Title") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = { Text("Recipe Content") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                Log.d(TAG, "Save button clicked - Title: $title")
                onSaveRecipe(title, content)
                if (title.isNotEmpty() && content.isNotEmpty()) {
                    title = ""
                    content = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Recipe")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeInputPreview() {
    RecipeBookTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            RecipeInput(
                onSaveRecipe = { _, _ -> }
            )
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE67E22)), // Orange background color
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Replace R.drawable.your_icon with your actual icon resource
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Recipe Book",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    RecipeBookTheme {
        SplashScreen()
    }
}
