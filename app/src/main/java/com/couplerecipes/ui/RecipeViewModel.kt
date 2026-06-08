package com.couplerecipes.ui

import androidx.lifecycle.*
import com.couplerecipes.data.local.entities.Recipe
import com.couplerecipes.data.repository.RecipeRepository
import kotlinx.coroutines.launch

/**
 * ViewModel compartido entre los fragments de la aplicación.
 * Contiene la lógica de negocio y expone LiveData a la UI.
 */
class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    // ==================== ESTADO DE BÚSQUEDA / FILTRO ====================

    private val _searchQuery = MutableLiveData("")
    private val _selectedCategory = MutableLiveData("")
    private val _selectedAuthor = MutableLiveData("")
    private val _selectedHealthFilter = MutableLiveData("")

    val searchQuery: LiveData<String> = _searchQuery
    val selectedCategory: LiveData<String> = _selectedCategory

    // ==================== LISTAS REACTIVAS ====================

    /** Lista principal filtrada según búsqueda activa */
    val recipes: LiveData<List<Recipe>> = MediatorLiveData<List<Recipe>>().apply {
        fun update() {
            val query = _searchQuery.value ?: ""
            val category = _selectedCategory.value ?: ""
            val author = _selectedAuthor.value ?: ""
            removeSource(repository.allRecipes)
            addSource(repository.searchRecipes(query, category, author)) { value = it }
        }
        addSource(_searchQuery) { update() }
        addSource(_selectedCategory) { update() }
        addSource(_selectedAuthor) { update() }
        update()
    }

    val favoriteRecipes: LiveData<List<Recipe>> = repository.favoriteRecipes
    val cookingHistory: LiveData<List<Recipe>> = repository.cookingHistory
    val madeTogetherRecipes: LiveData<List<Recipe>> = repository.madeTogetherRecipes
    val totalCount: LiveData<Int> = repository.totalCount
    val madeTogetherCount: LiveData<Int> = repository.madeTogetherCount
    val mostCooked: LiveData<List<Recipe>> = repository.mostCooked

    // ==================== RECETA SELECCIONADA (para detalle/edición) ====================

    private val _selectedRecipeId = MutableLiveData<Long>()

    val selectedRecipe: LiveData<Recipe?> = _selectedRecipeId.switchMap { id ->
        repository.getById(id)
    }

    fun selectRecipe(id: Long) {
        _selectedRecipeId.value = id
    }

    // ==================== OPERACIONES CRUD ====================

    fun insertRecipe(recipe: Recipe, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insert(recipe)
            onResult(id)
        }
    }

    fun updateRecipe(recipe: Recipe) {
        viewModelScope.launch { repository.update(recipe) }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch { repository.delete(recipe) }
    }

    // ==================== ACCIONES ESPECIALES ====================

    fun toggleFavorite(recipe: Recipe) {
        viewModelScope.launch { repository.toggleFavorite(recipe) }
    }

    fun markAsCooked(recipe: Recipe, madeTogether: Boolean) {
        viewModelScope.launch { repository.markAsCooked(recipe.id, madeTogether) }
    }

    // ==================== FILTROS ====================

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setCategory(category: String) { _selectedCategory.value = category }
    fun setAuthor(author: String) { _selectedAuthor.value = author }
    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = ""
        _selectedAuthor.value = ""
        _selectedHealthFilter.value = ""
    }

    // ==================== FACTORY ====================

    class Factory(private val repository: RecipeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RecipeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
