package com.couplerecipes.ui.history

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.couplerecipes.CoupleRecipesApp
import com.couplerecipes.R
import com.couplerecipes.databinding.FragmentHistoryBinding
import com.couplerecipes.ui.RecipeViewModel
import com.couplerecipes.ui.home.RecipeAdapter

/**
 * Fragment del historial de recetas cocinadas.
 * Muestra estadísticas de pareja y las recetas cocinadas juntos.
 */
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecipeViewModel by activityViewModels {
        RecipeViewModel.Factory((requireActivity().application as CoupleRecipesApp).repository)
    }

    private lateinit var historyAdapter: RecipeAdapter
    private lateinit var togetherAdapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupObservers()
        setupFab()
        setupMenu()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_menu, menu)
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

    private fun setupFab() {
        // Central button removed as requested
    }

    private fun setupAdapters() {
        val navigateToDetail: (com.couplerecipes.data.local.entities.Recipe) -> Unit = { recipe ->
            viewModel.selectRecipe(recipe.id)
            findNavController().navigate(R.id.action_historyFragment_to_recipeDetailFragment)
        }
        val onFavorite: (com.couplerecipes.data.local.entities.Recipe) -> Unit = { recipe ->
            viewModel.toggleFavorite(recipe)
        }

        historyAdapter = RecipeAdapter(navigateToDetail, onFavorite)
        togetherAdapter = RecipeAdapter(navigateToDetail, onFavorite)

        binding.recyclerViewHistory.adapter = historyAdapter
        binding.recyclerViewTogether.adapter = togetherAdapter
    }

    private fun setupObservers() {
        // Estadísticas de pareja
        viewModel.totalCount.observe(viewLifecycleOwner) { count ->
            binding.textTotalRecipes.text = count.toString()
        }

        viewModel.madeTogetherCount.observe(viewLifecycleOwner) { count ->
            binding.textTogetherCount.text = count.toString()
        }

        // Historial completo
        viewModel.cookingHistory.observe(viewLifecycleOwner) { recipes ->
            historyAdapter.submitList(recipes)
            binding.textHistoryEmpty.visibility =
                if (recipes.isEmpty()) View.VISIBLE else View.GONE
        }

        // Solo cocinadas juntos
        viewModel.madeTogetherRecipes.observe(viewLifecycleOwner) { recipes ->
            togetherAdapter.submitList(recipes)
            binding.textTogetherEmpty.visibility =
                if (recipes.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
