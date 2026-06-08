package com.couplerecipes

import android.app.Application
import com.couplerecipes.data.local.RecipeDatabase
import com.couplerecipes.data.repository.RecipeRepository

/**
 * Application class. Inicializa la base de datos y el repositorio
 * como dependencias accesibles desde los ViewModels mediante ViewModelFactory.
 */
class CoupleRecipesApp : Application() {

    /** Instancia de la base de datos (lazy = se crea solo cuando se necesita) */
    val database: RecipeDatabase by lazy {
        RecipeDatabase.getDatabase(this)
    }

    /** Repositorio accesible globalmente */
    val repository: RecipeRepository by lazy {
        RecipeRepository(database.recipeDao())
    }
}
