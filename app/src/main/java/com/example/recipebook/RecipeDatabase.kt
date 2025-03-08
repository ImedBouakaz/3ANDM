package com.example.recipebook

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import androidx.room.TypeConverters
import com.example.recipebook.data.Recipe
import com.example.recipebook.data.RecipeDao
import com.example.recipebook.data.Converters

private const val TAG = "RecipeDatabase"

@Database(entities = [Recipe::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: RecipeDatabase? = null

        fun getDatabase(context: Context): RecipeDatabase {
            Log.d(TAG, "getDatabase: Attempting to get database instance")
            
            if (INSTANCE != null) {
                Log.d(TAG, "getDatabase: Returning existing instance")
                return INSTANCE!!
            }

            return synchronized(this) {
                Log.d(TAG, "getDatabase: Inside synchronized block")
                
                val instance = INSTANCE ?: try {
                    Log.d(TAG, "getDatabase: Building new database instance")
                    
                    Room.databaseBuilder(
                        context.applicationContext,
                        RecipeDatabase::class.java,
                        "recipe_database"
                    )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                    .also { newInstance ->
                        Log.d(TAG, "getDatabase: New instance built successfully")
                        INSTANCE = newInstance
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "getDatabase: Error building database", e)
                    Log.e(TAG, "getDatabase: Stack trace: ${e.stackTrace.joinToString("\\n")}")
                    throw RuntimeException("Failed to create database: ${e.message}", e)
                }

                Log.d(TAG, "getDatabase: Returning instance")
                instance
            }
        }
    }
}

