package com.couplerecipes.ui.home

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.couplerecipes.CoupleRecipesApp
import com.couplerecipes.R
import com.couplerecipes.data.local.entities.RecipeCategory
import com.couplerecipes.databinding.FragmentHomeBinding
import com.couplerecipes.ui.RecipeViewModel
import com.google.android.material.chip.Chip

/**
 * Fragment principal: lista de recetas con búsqueda y filtros de categoría.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecipeViewModel by activityViewModels {
        RecipeViewModel.Factory((requireActivity().application as CoupleRecipesApp).repository)
    }

    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupCategoryChips()
        setupObservers()
        setupFab()
        setupMenu()
    }

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(
            onRecipeClick = { recipe ->
                viewModel.selectRecipe(recipe.id)
                findNavController().navigate(R.id.action_homeFragment_to_recipeDetailFragment)
            },
            onFavoriteClick = { recipe -> viewModel.toggleFavorite(recipe) }
        )
        binding.recyclerViewRecipes.adapter = recipeAdapter
    }

    private fun setupCategoryChips() {
        // Chip "Todas"
        val allChip = Chip(requireContext()).apply {
            text = "Todas"
            isCheckable = true
            isChecked = true
        }
        allChip.setOnCheckedChangeListener { _, checked ->
            if (checked) viewModel.setCategory("")
        }
        binding.chipGroupCategories.addView(allChip)

        // Chips por categoría
        RecipeCategory.all().forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = RecipeCategory.displayName(category)
                isCheckable = true
                tag = category
            }
            chip.setOnCheckedChangeListener { _, checked ->
                if (checked) viewModel.setCategory(category)
            }
            binding.chipGroupCategories.addView(chip)
        }
        binding.chipGroupCategories.isSingleSelection = true
    }

    private fun setupObservers() {
        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            recipeAdapter.submitList(recipes)
            binding.emptyView.root.visibility =
                if (recipes.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.totalCount.observe(viewLifecycleOwner) { count ->
            binding.textRecipeCount.text = "$count recetas"
        }
    }

    private fun setupFab() {
        // Central button removed as requested
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_menu, menu)
                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView
                searchView.queryHint = "Buscar recetas..."
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?) = true
                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.setSearchQuery(newText ?: "")
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_add_recipe -> {
                        findNavController().navigate(R.id.action_homeFragment_to_addEditRecipeFragment)
                        true
                    }
                    R.id.action_filter -> {
                        showFilterDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showFilterDialog() {
        FilterBottomSheet().show(parentFragmentManager, FilterBottomSheet.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
