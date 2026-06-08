package com.couplerecipes.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.couplerecipes.R
import com.couplerecipes.data.local.entities.Recipe
import com.couplerecipes.data.local.entities.RecipeCategory
import com.couplerecipes.data.local.entities.RecipeDifficulty
import com.couplerecipes.databinding.ItemRecipeBinding

/**
 * Adapter para la lista de recetas usando ListAdapter con DiffUtil
 * para actualizaciones eficientes de la lista.
 */
class RecipeAdapter(
    private val onRecipeClick: (Recipe) -> Unit,
    private val onFavoriteClick: (Recipe) -> Unit
) : ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecipeViewHolder(
        private val binding: ItemRecipeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            with(binding) {
                textRecipeName.text = recipe.name
                textCategory.text = RecipeCategory.displayName(recipe.category)
                textDifficulty.text = RecipeDifficulty.displayName(recipe.difficulty)
                textCookingTime.text = "${recipe.cookingTimeMinutes} min"
                textAuthor.text = recipe.author

                // Ícono de favorito
                val favIcon = if (recipe.isFavorite) R.drawable.ic_favorite
                              else R.drawable.ic_favorite_border
                btnFavorite.setImageResource(favIcon)

                // Badge "juntos"
                if (recipe.madeTogather) {
                    chipTogether.visibility = android.view.View.VISIBLE
                } else {
                    chipTogether.visibility = android.view.View.GONE
                }

                // Imagen de la receta (Glide)
                if (!recipe.imagePath.isNullOrEmpty()) {
                    Glide.with(imageRecipe.context)
                        .load(recipe.imagePath)
                        .centerCrop()
                        .placeholder(R.drawable.ic_recipe_placeholder)
                        .into(imageRecipe)
                } else {
                    imageRecipe.setImageResource(R.drawable.ic_recipe_placeholder)
                }

                // Listeners
                root.setOnClickListener { onRecipeClick(recipe) }
                btnFavorite.setOnClickListener { onFavoriteClick(recipe) }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe) =
            oldItem == newItem
    }
}
