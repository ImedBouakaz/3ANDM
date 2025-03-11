package com.example.recipebook.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.recipebook.data.Recipe
import com.example.recipebook.viewmodel.RecipeViewModel
import androidx.compose.ui.graphics.Color
import com.example.recipebook.data.decodeHtml

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    viewModel: RecipeViewModel,
    onRecipeClick: (Recipe) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar with Home button
        TopAppBar(
            title = { Text("Yumm") },
            actions = {
                // Database button
                IconButton(onClick = { viewModel.loadStoredRecipes() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = "Show saved recipes"
                    )
                }
                // Home button (clear screen)
                IconButton(onClick = { viewModel.clearScreen() }) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Clear screen"
                    )
                }
            }
        )

        // Search Bar
        SearchBar(
            query = uiState.searchQuery,
            onQueryChange = viewModel::onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Ingredient Filter Chips
        IngredientFilters(
            selectedIngredient = uiState.searchQuery,
            onIngredientSelected = viewModel::onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth()
        )

        // Recipe List
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading && uiState.recipes.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                RecipeList(
                    recipes = uiState.recipes,
                    onRecipeClick = onRecipeClick,
                    onLoadMore = viewModel::loadNextPage,
                    isLoadingMore = uiState.isLoading
                )
            }

            // Error Message
            uiState.error?.let { error ->
                ErrorMessage(
                    message = error,
                    onRetry = { viewModel.loadRecipes(refresh = true) },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun IngredientFilters(
    selectedIngredient: String,
    onIngredientSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ingredients = listOf(
        "Chicken",
        "Beef",
        "Soup",
        "Dessert",
        "Vegetarian",
        "French",
        "Salad",
        "Fish",
        "Pasta"
    )

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ingredients) { ingredient ->
            FilterChip(
                selected = selectedIngredient == ingredient,
                onClick = { onIngredientSelected(ingredient) },
                label = { Text(ingredient) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color(
                        alpha = 26,  // 10% opacity
                        red = 251,
                        green = 138,
                        blue = 78
                    ),
                    labelColor = Color(
                        alpha = 255,
                        red = 251,
                        green = 138,
                        blue = 78
                    ),
                    selectedContainerColor = Color(
                        alpha = 255,
                        red = 251,
                        green = 138,
                        blue = 78
                    ),
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(
                    alpha = 26,
                    red = 251,
                    green = 138,
                    blue = 78
                )
            ),
        placeholder = { Text("Search recipes...") },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(
                    alpha = 255,
                    red = 251,
                    green = 138,
                    blue = 78
                )
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color(
                alpha = 255,
                red = 251,
                green = 138,
                blue = 78
            ),
            unfocusedIndicatorColor = Color(
                alpha = 26,
                red = 251,
                green = 138,
                blue = 78
            ),
            focusedPlaceholderColor = Color(
                alpha = 128,
                red = 251,
                green = 138,
                blue = 78
            ),
            unfocusedPlaceholderColor = Color(
                alpha = 128,
                red = 251,
                green = 138,
                blue = 78
            ),
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        )
    )
}

@Composable
private fun RecipeList(
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    onLoadMore: () -> Unit,
    isLoadingMore: Boolean,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Implement infinite scrolling
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf false

            lastVisibleItem.index >= recipes.size - 1
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !isLoadingMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = recipes,
            key = { recipe -> recipe.id }
        ) { recipe ->
            RecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe) })
        }

        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(
                alpha = 255,
                red = 251,
                green = 138,
                blue = 78
            )
        )
    ) {
        Column {
            AsyncImage(
                model = recipe.featuredImage,
                contentDescription = decodeHtml(recipe.title),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = decodeHtml(recipe.title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "by ${decodeHtml(recipe.publisher)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )

                if (recipe.rating > 0) {
                    Text(
                        text = "Rating: ${recipe.rating}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message)
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(
                    alpha = 255,
                    red = 251,
                    green = 138,
                    blue = 78
                )
            )
        ) {
            Text("Retry")
        }
    }
}