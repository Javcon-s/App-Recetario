package com.couplerecipes.ui.favorites

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.couplerecipes.CoupleRecipesApp
import com.couplerecipes.R
import com.couplerecipes.databinding.FragmentFavoritesBinding
import com.couplerecipes.ui.RecipeViewModel
import com.couplerecipes.ui.home.RecipeAdapter

/**
 * Fragment que muestra las recetas marcadas como favoritas.
 */
class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecipeViewModel by activityViewModels {
        RecipeViewModel.Factory((requireActivity().application as CoupleRecipesApp).repository)
    }

    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupFab()
        setupMenu()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_menu, menu)
                // Ocultar búsqueda y filtro si no son necesarios en Favoritos, 
                // o simplemente dejarlos si el ViewModel los maneja.
                // Por ahora solo aseguramos que el botón de añadir funcione.
                menu.findItem(R.id.action_search)?.isVisible = false
                menu.findItem(R.id.action_filter)?.isVisible = false
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_add_recipe -> {
                        findNavController().navigate(R.id.addEditRecipeFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(
            onRecipeClick = { recipe ->
                viewModel.selectRecipe(recipe.id)
                findNavController().navigate(R.id.action_favoritesFragment_to_recipeDetailFragment)
            },
            onFavoriteClick = { recipe -> viewModel.toggleFavorite(recipe) }
        )
        binding.recyclerViewFavorites.adapter = recipeAdapter
    }

    private fun setupObservers() {
        viewModel.favoriteRecipes.observe(viewLifecycleOwner) { recipes ->
            recipeAdapter.submitList(recipes)
            binding.emptyView.root.visibility =
                if (recipes.isEmpty()) View.VISIBLE else View.GONE
            binding.textFavoriteCount.text = "${recipes.size} favoritas"
        }
    }

    private fun setupFab() {
        // Central button removed as requested
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
