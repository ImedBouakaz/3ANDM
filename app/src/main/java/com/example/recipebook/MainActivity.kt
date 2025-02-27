package com.example.recipebook

import com.example.recipebook.RecipeDao
import com.example.recipebook.RecipeDatabase
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var database: RecipeDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize database
        database = RecipeDatabase.getDatabase(this)

        // Set up save button click listener
        saveButton.setOnClickListener {
            saveRecipe()
        }
    }

    // Function to save recipe to database
    private fun saveRecipe() {
        val title = titleEditText.text.toString()
        val content = contentEditText.text.toString()

        if (title.isNotEmpty() && content.isNotEmpty()) {
            val recipe = Recipe(title = title, content = content)

            // Using coroutine to perform database operation on background thread
            CoroutineScope(Dispatchers.IO).launch {
                database.recipeDao().insertRecipe(recipe)
            }

            // Clear input fields after saving
            titleEditText.text.clear()
            contentEditText.text.clear()
        }
    }
}
@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                TextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Recipe Title") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    placeholder = { Text("Recipe Content") }
                )

                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Recipe")
                }
            }
        }
    }
}
