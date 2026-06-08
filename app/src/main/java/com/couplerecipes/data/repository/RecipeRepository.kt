package com.couplerecipes.data.repository

import androidx.lifecycle.LiveData
import com.couplerecipes.data.local.dao.RecipeDao
import com.couplerecipes.data.local.entities.Recipe

/**
 * Repositorio que actúa como fuente única de verdad (Single Source of Truth).
 * La UI siempre interactúa con el repositorio, nunca directamente con el DAO.
 */
class RecipeRepository(private val recipeDao: RecipeDao) {

    // ==================== OBSERVABLES (LiveData) ====================

    val allRecipes: LiveData<List<Recipe>> = recipeDao.getAllRecipes()
    val favoriteRecipes: LiveData<List<Recipe>> = recipeDao.getFavoriteRecipes()
    val madeTogetherRecipes: LiveData<List<Recipe>> = recipeDao.getMadeTogetherRecipes()
    val cookingHistory: LiveData<List<Recipe>> = recipeDao.getCookingHistory()
    val totalCount: LiveData<Int> = recipeDao.getTotalCount()
    val madeTogetherCount: LiveData<Int> = recipeDao.getMadeTogetherCount()
    val mostCooked: LiveData<List<Recipe>> = recipeDao.getMostCooked()

    // ==================== OPERACIONES CRUD ====================

    suspend fun insert(recipe: Recipe): Long = recipeDao.insertRecipe(recipe)

    suspend fun update(recipe: Recipe) = recipeDao.updateRecipe(recipe)

    suspend fun delete(recipe: Recipe) = recipeDao.deleteRecipe(recipe)

    suspend fun deleteById(id: Long) = recipeDao.deleteRecipeById(id)

    // ==================== CONSULTAS ====================

    fun getById(id: Long): LiveData<Recipe?> = recipeDao.getRecipeById(id)

    fun searchRecipes(
        query: String = "",
        category: String = "",
        author: String = ""
    ): LiveData<List<Recipe>> = recipeDao.searchRecipes(query, category, author)

    fun getByCategory(category: String): LiveData<List<Recipe>> =
        recipeDao.getByCategory(category)

    fun getByHealthFilter(filter: String): LiveData<List<Recipe>> =
        recipeDao.getByHealthFilter(filter)

    // ==================== ACCIONES ESPECIALES ====================

    suspend fun toggleFavorite(recipe: Recipe) {
        recipeDao.updateFavoriteStatus(recipe.id, !recipe.isFavorite)
    }

    suspend fun markAsCooked(id: Long, madeTogether: Boolean) {
        recipeDao.markAsCooked(
            id = id,
            timestamp = System.currentTimeMillis(),
            madeTogether = madeTogether
        )
    }
}
