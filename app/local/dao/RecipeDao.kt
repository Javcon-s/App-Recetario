package com.couplerecipes.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.couplerecipes.data.local.entities.Recipe

/**
 * DAO (Data Access Object) para operaciones de base de datos con recetas.
 * Todas las consultas retornan LiveData para observación reactiva en la UI.
 */
@Dao
interface RecipeDao {

    // ==================== INSERTAR / ACTUALIZAR / ELIMINAR ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipeById(id: Long)

    // ==================== CONSULTAS PRINCIPALES ====================

    /** Todas las recetas ordenadas por fecha de creación */
    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    fun getAllRecipes(): LiveData<List<Recipe>>

    /** Receta por ID */
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getRecipeById(id: Long): LiveData<Recipe?>

    /** Solo recetas favoritas */
    @Query("SELECT * FROM recipes WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteRecipes(): LiveData<List<Recipe>>

    /** Recetas cocinadas juntos */
    @Query("SELECT * FROM recipes WHERE madeTogather = 1 ORDER BY lastCookedAt DESC")
    fun getMadeTogetherRecipes(): LiveData<List<Recipe>>

    /** Historial: recetas cocinadas al menos una vez, ordenadas por última vez cocinada */
    @Query("SELECT * FROM recipes WHERE timesCooked > 0 ORDER BY lastCookedAt DESC")
    fun getCookingHistory(): LiveData<List<Recipe>>

    // ==================== BÚSQUEDA Y FILTRADO ====================

    /** Buscar por nombre (case-insensitive) */
    @Query("SELECT * FROM recipes WHERE name LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchByName(query: String): LiveData<List<Recipe>>

    /** Filtrar por categoría */
    @Query("SELECT * FROM recipes WHERE category = :category ORDER BY createdAt DESC")
    fun getByCategory(category: String): LiveData<List<Recipe>>

    /** Filtrar por autor */
    @Query("SELECT * FROM recipes WHERE author = :author ORDER BY createdAt DESC")
    fun getByAuthor(author: String): LiveData<List<Recipe>>

    /** Búsqueda combinada por nombre y categoría */
    @Query("""
        SELECT * FROM recipes 
        WHERE (:query = '' OR name LIKE '%' || :query || '%')
        AND (:category = '' OR category = :category)
        AND (:author = '' OR author = :author)
        ORDER BY createdAt DESC
    """)
    fun searchRecipes(
        query: String = "",
        category: String = "",
        author: String = ""
    ): LiveData<List<Recipe>>

    /** Recetas con un filtro saludable específico */
    @Query("SELECT * FROM recipes WHERE healthFilters LIKE '%' || :filter || '%' ORDER BY createdAt DESC")
    fun getByHealthFilter(filter: String): LiveData<List<Recipe>>

    // ==================== ESTADÍSTICAS ====================

    /** Total de recetas */
    @Query("SELECT COUNT(*) FROM recipes")
    fun getTotalCount(): LiveData<Int>

    /** Total de recetas cocinadas juntos */
    @Query("SELECT COUNT(*) FROM recipes WHERE madeTogather = 1")
    fun getMadeTogetherCount(): LiveData<Int>

    /** Recetas más cocinadas */
    @Query("SELECT * FROM recipes ORDER BY timesCooked DESC LIMIT :limit")
    fun getMostCooked(limit: Int = 5): LiveData<List<Recipe>>

    // ==================== ACTUALIZACIONES ESPECÍFICAS ====================

    /** Marcar/desmarcar favorito */
    @Query("UPDATE recipes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    /** Registrar que se cocinó la receta */
    @Query("""
        UPDATE recipes 
        SET timesCooked = timesCooked + 1, 
            lastCookedAt = :timestamp,
            madeTogather = CASE WHEN :madeTogether = 1 THEN 1 ELSE madeTogather END
        WHERE id = :id
    """)
    suspend fun markAsCooked(id: Long, timestamp: Long, madeTogether: Boolean)
}
