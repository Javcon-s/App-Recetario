package com.couplerecipes.data.local.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Entidad principal de la base de datos Room.
 * Representa una receta saludable con todos sus atributos.
 */
@Parcelize
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Nombre de la receta */
    val name: String,

    /** Lista de ingredientes separados por comas */
    val ingredients: String,

    /** Pasos de preparación numerados */
    val steps: String,

    /** Tiempo de cocción en minutos */
    val cookingTimeMinutes: Int,

    /** Nivel de dificultad: EASY, MEDIUM, HARD */
    val difficulty: String,

    /** Categoría: BREAKFAST, LUNCH, DINNER, SNACK */
    val category: String,

    /** Filtros saludables: LOW_CALORIE, VEGETARIAN, VEGAN, GLUTEN_FREE */
    val healthFilters: String = "",

    /** Autor: "Yo" o "Mi pareja" */
    val author: String,

    /** Si fue cocinada juntos */
    val madeTogather: Boolean = false,

    /** Si está en favoritos */
    val isFavorite: Boolean = false,

    /** Ruta de la imagen local (opcional) */
    val imagePath: String? = null,

    /** Timestamp de creación */
    val createdAt: Long = System.currentTimeMillis(),

    /** Timestamp de la última vez cocinada */
    val lastCookedAt: Long? = null,

    /** Contador de veces cocinada */
    val timesCooked: Int = 0,

    /** Calorías aproximadas (opcional) */
    val calories: Int? = null,

    /** Notas adicionales */
    val notes: String = ""
) : Parcelable {

    /** Devuelve la lista de filtros saludables como Set */
    fun getHealthFilterSet(): Set<String> {
        return if (healthFilters.isBlank()) emptySet()
        else healthFilters.split(",").map { it.trim() }.toSet()
    }

    /** Devuelve los ingredientes como lista */
    fun getIngredientList(): List<String> {
        return ingredients.split("\n").filter { it.isNotBlank() }
    }

    /** Devuelve los pasos como lista */
    fun getStepList(): List<String> {
        return steps.split("\n").filter { it.isNotBlank() }
    }
}

// Constantes para los valores de categoría y dificultad
object RecipeCategory {
    const val BREAKFAST = "BREAKFAST"
    const val LUNCH = "LUNCH"
    const val DINNER = "DINNER"
    const val SNACK = "SNACK"

    fun displayName(category: String): String = when (category) {
        BREAKFAST -> "Desayuno"
        LUNCH -> "Almuerzo"
        DINNER -> "Cena"
        SNACK -> "Snack"
        else -> category
    }

    fun all() = listOf(BREAKFAST, LUNCH, DINNER, SNACK)
}

object RecipeDifficulty {
    const val EASY = "EASY"
    const val MEDIUM = "MEDIUM"
    const val HARD = "HARD"

    fun displayName(difficulty: String): String = when (difficulty) {
        EASY -> "Fácil"
        MEDIUM -> "Medio"
        HARD -> "Difícil"
        else -> difficulty
    }
}

object HealthFilter {
    const val LOW_CALORIE = "LOW_CALORIE"
    const val VEGETARIAN = "VEGETARIAN"
    const val VEGAN = "VEGAN"
    const val GLUTEN_FREE = "GLUTEN_FREE"
    const val HIGH_PROTEIN = "HIGH_PROTEIN"

    fun displayName(filter: String): String = when (filter) {
        LOW_CALORIE -> "Bajas calorías"
        VEGETARIAN -> "Vegetariana"
        VEGAN -> "Vegana"
        GLUTEN_FREE -> "Sin gluten"
        HIGH_PROTEIN -> "Alta proteína"
        else -> filter
    }

    fun all() = listOf(LOW_CALORIE, VEGETARIAN, VEGAN, GLUTEN_FREE, HIGH_PROTEIN)
}

object RecipeAuthor {
    const val ME = "Yo"
    const val PARTNER = "Mi pareja"
}
