package com.example.recipebook

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.room.Database
import com.example.recipebook.ui.theme.RecipeBookTheme
import com.example.recipebook.Recipe
import com.example.recipebook.RecipeDatabase

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private var database: RecipeDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Starting MainActivity initialization")

        initializeDatabase()

        setContent {
            Log.d(TAG, "onCreate: Setting up Compose content")
            RecipeBookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RecipeInput(
                        onSaveRecipe = { title, content ->
                            Log.d(TAG, "Attempting to save recipe - Title: $title")
                            saveRecipe(title, content)
                        }
                    )
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

        if (title.isEmpty() || content.isEmpty()) {
            Log.w(TAG, "saveRecipe: Empty title or content")
            Toast.makeText(this, "Title and content cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val recipe = Recipe(title = title, content = content)
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

/* TODO
Add buttons for editing and deleting recipes
Add a search bar to find recipes through API
UI/UX improvements
Save from API
 */