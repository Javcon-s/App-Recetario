package com.couplerecipes.ui.detail

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.couplerecipes.CoupleRecipesApp
import com.couplerecipes.R
import com.couplerecipes.data.local.entities.*
import com.couplerecipes.databinding.FragmentRecipeDetailBinding
import com.couplerecipes.ui.RecipeViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/**
 * Fragment de detalle de receta. Muestra todos los campos de la receta
 * y permite editarla, eliminarla, marcarla como favorita o como cocinada.
 */
class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecipeViewModel by activityViewModels {
        RecipeViewModel.Factory((requireActivity().application as CoupleRecipesApp).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObserver()
        setupMenu()
    }

    private fun setupObserver() {
        viewModel.selectedRecipe.observe(viewLifecycleOwner) { recipe ->
            recipe ?: return@observe
            bindRecipeData(recipe)
        }
    }

    private fun bindRecipeData(recipe: Recipe) {
        with(binding) {
            textRecipeName.text = recipe.name
            textCategory.text = RecipeCategory.displayName(recipe.category)
            textDifficulty.text = RecipeDifficulty.displayName(recipe.difficulty)
            textCookingTime.text = "${recipe.cookingTimeMinutes} minutos"
            textAuthor.text = "Por: ${recipe.author}"
            textTimesCooked.text = "Cocinada ${recipe.timesCooked} veces"

            // Calórias si existen
            if (recipe.calories != null) {
                textCalories.text = "${recipe.calories} kcal"
                textCalories.visibility = View.VISIBLE
            } else {
                textCalories.visibility = View.GONE
            }

            // Ingredientes
            val ingredientText = recipe.getIngredientList()
                .mapIndexed { i, ing -> "• $ing" }
                .joinToString("\n")
            textIngredients.text = ingredientText

            // Pasos
            val stepsText = recipe.getStepList()
                .mapIndexed { i, step -> "${i + 1}. $step" }
                .joinToString("\n\n")
            textSteps.text = stepsText

            // Notas
            if (recipe.notes.isNotBlank()) {
                textNotes.text = recipe.notes
                cardNotes.visibility = View.VISIBLE
            } else {
                cardNotes.visibility = View.GONE
            }

            // Filtros de salud
            val filters = recipe.getHealthFilterSet()
                .map { HealthFilter.displayName(it) }
                .joinToString(" · ")
            if (filters.isNotBlank()) {
                textHealthFilters.text = filters
                textHealthFilters.visibility = View.VISIBLE
            } else {
                textHealthFilters.visibility = View.GONE
            }

            // Badge "juntos"
            chipTogether.visibility = if (recipe.madeTogather) View.VISIBLE else View.GONE

            // Favorito
            fabFavorite.setImageResource(
                if (recipe.isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
            )
            fabFavorite.setOnClickListener { viewModel.toggleFavorite(recipe) }

            // Imagen
            if (!recipe.imagePath.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(recipe.imagePath)
                    .centerCrop()
                    .placeholder(R.drawable.ic_recipe_placeholder)
                    .into(imageRecipe)
            }

            // Botón "Cocinar ahora"
            btnMarkAsCooked.setOnClickListener { showCookDialog(recipe) }
        }
    }

    private fun showCookDialog(recipe: Recipe) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("¿Cocinaron esta receta?")
            .setMessage("Registrar ${recipe.name} como cocinada")
            .setNeutralButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Solo yo") { _, _ ->
                viewModel.markAsCooked(recipe, madeTogether = false)
                Snackbar.make(binding.root, "¡Buen provecho! 🍽️", Snackbar.LENGTH_SHORT).show()
            }
            .setPositiveButton("¡Juntos! ❤️") { _, _ ->
                viewModel.markAsCooked(recipe, madeTogether = true)
                Snackbar.make(binding.root, "¡Cocinaron juntos! 💑", Snackbar.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.detail_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_edit -> {
                        findNavController().navigate(
                            R.id.action_recipeDetailFragment_to_addEditRecipeFragment
                        )
                        true
                    }
                    R.id.action_delete -> {
                        showDeleteDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showDeleteDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar receta")
            .setMessage("¿Estás seguro que quieres eliminar esta receta? Esta acción no se puede deshacer.")
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.selectedRecipe.value?.let { recipe ->
                    viewModel.deleteRecipe(recipe)
                    findNavController().navigateUp()
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
