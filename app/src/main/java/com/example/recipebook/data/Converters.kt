package com.example.recipebook.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    /* Cette méthode convertit une chaîne JSON en une liste de chaînes.
     L'annotation @TypeConverter indique que cette méthode doit être utilisée par Room
     pour convertir des données d'un type à un autre. */
    @TypeConverter
    fun fromString(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        // Utiliser Gson pour analyser la chaîne JSON en une liste de chaînes
        return Gson().fromJson(value, listType)
    }

    /* Cette méthode convertit une liste de chaînes en une chaîne JSON.
     pour convertir des données d'un type à un autre. */
    @TypeConverter
    fun fromList(list: List<String>): String {
        // Utiliser Gson pour convertir la liste de chaînes en une chaîne JSON
        return Gson().toJson(list)
    }
}
