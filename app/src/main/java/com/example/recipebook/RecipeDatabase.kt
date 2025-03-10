package com.example.recipebook.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import androidx.room.TypeConverters

private const val TAG = "RecipeDatabase"

@Database(entities = [Recipe::class], version = 2, exportSchema = false)
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
                        .build()
                        .also { newInstance ->
                            Log.d(TAG, "getDatabase: New instance built successfully")
                            INSTANCE = newInstance
                        }
                } catch (e: Exception) {
                    Log.e(TAG, "La création de la base de données a échouée", e)
                    Log.e(TAG, "Message d'erreur : ${e.message}")
                    Log.e(TAG, "Stack trace: ${e.stackTrace.joinToString("\n")}")
                    throw e
                }

                Log.d(TAG, "getDatabase: Returning instance")
                instance
            }
        }
    }
} 