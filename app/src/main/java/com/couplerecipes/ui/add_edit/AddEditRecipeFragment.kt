package com.couplerecipes.ui.add_edit

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.couplerecipes.CoupleRecipesApp
import com.couplerecipes.R
import com.couplerecipes.data.local.entities.*
import com.couplerecipes.databinding.FragmentAddEditRecipeBinding
import com.couplerecipes.ui.RecipeViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar

/**
 * Fragment para crear o editar una receta.
 * Detecta si hay receta seleccionada → modo edición, sino → modo creación.
 */
class AddEditRecipeFragment : Fragment() {

    private var _binding: FragmentAddEditRecipeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecipeViewModel by activityViewModels {
        RecipeViewModel.Factory((requireActivity().application as CoupleRecipesApp).repository)
    }

    private var selectedImageUri: Uri? = null
    private var editingRecipe: Recipe? = null
    private val selectedHealthFilters = mutableSetOf<String>()

    // Selector de imagen desde galería
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this).load(it).centerCrop().into(binding.imageRecipe)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropdowns()
        setupHealthFilterChips()
        setupImagePicker()
        setupSaveButton()
        observeEditingRecipe()
    }

    private fun setupDropdowns() {
        // Categorías
        val categories = RecipeCategory.all().map { RecipeCategory.displayName(it) }
        val catAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, categories)
        binding.dropdownCategory.setAdapter(catAdapter)

        // Dificultad
        val difficulties = listOf("Fácil", "Medio", "Difícil")
        val diffAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, difficulties)
        binding.dropdownDifficulty.setAdapter(diffAdapter)

        // Autor
        val authors = listOf(RecipeAuthor.ME, RecipeAuthor.PARTNER)
        val authorAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, authors)
        binding.dropdownAuthor.setAdapter(authorAdapter)
        binding.dropdownAuthor.setText(RecipeAuthor.ME, false) // Default
    }

    private fun setupHealthFilterChips() {
        HealthFilter.all().forEach { filter ->
            val chip = Chip(requireContext()).apply {
                text = HealthFilter.displayName(filter)
                isCheckable = true
                tag = filter
                setOnCheckedChangeListener { _, checked ->
                    if (checked) selectedHealthFilters.add(filter)
                    else selectedHealthFilters.remove(filter)
                }
            }
            binding.chipGroupHealth.addView(chip)
        }
    }

    private fun setupImagePicker() {
        binding.imageRecipe.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        binding.btnAddImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            if (validateForm()) saveRecipe()
        }
    }

    private fun observeEditingRecipe() {
        // Si hay receta seleccionada, estamos en modo edición
        viewModel.selectedRecipe.observe(viewLifecycleOwner) { recipe ->
            if (recipe != null && editingRecipe == null) {
                editingRecipe = recipe
                populateForm(recipe)
                requireActivity().title = "Editar receta"
            } else if (recipe == null) {
                requireActivity().title = "Nueva receta"
            }
        }
    }

    private fun populateForm(recipe: Recipe) {
        with(binding) {
            editRecipeName.setText(recipe.name)
            editIngredients.setText(recipe.ingredients)
            editSteps.setText(recipe.steps)
            editCookingTime.setText(recipe.cookingTimeMinutes.toString())
            editCalories.setText(recipe.calories?.toString() ?: "")
            editNotes.setText(recipe.notes)

            dropdownCategory.setText(RecipeCategory.displayName(recipe.category), false)
            dropdownDifficulty.setText(RecipeDifficulty.displayName(recipe.difficulty), false)
            dropdownAuthor.setText(recipe.author, false)

            // Restaurar filtros de salud
            recipe.getHealthFilterSet().forEach { filter ->
                selectedHealthFilters.add(filter)
                for (i in 0 until chipGroupHealth.childCount) {
                    val chip = chipGroupHealth.getChildAt(i) as? Chip
                    if (chip?.tag == filter) chip.isChecked = true
                }
            }

            // Imagen
            if (!recipe.imagePath.isNullOrEmpty()) {
                Glide.with(requireContext()).load(recipe.imagePath).centerCrop().into(imageRecipe)
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        with(binding) {
            if (editRecipeName.text.isNullOrBlank()) {
                textInputName.error = "El nombre es requerido"
                isValid = false
            } else textInputName.error = null

            if (editIngredients.text.isNullOrBlank()) {
                textInputIngredients.error = "Agrega al menos un ingrediente"
                isValid = false
            } else textInputIngredients.error = null

            if (editSteps.text.isNullOrBlank()) {
                textInputSteps.error = "Agrega al menos un paso"
                isValid = false
            } else textInputSteps.error = null

            if (editCookingTime.text.isNullOrBlank()) {
                textInputCookingTime.error = "El tiempo es requerido"
                isValid = false
            } else textInputCookingTime.error = null

            if (dropdownCategory.text.isNullOrBlank()) {
                textInputCategory.error = "Selecciona una categoría"
                isValid = false
            } else textInputCategory.error = null
        }
        return isValid
    }

    private fun saveRecipe() {
        val categoryDisplay = binding.dropdownCategory.text.toString()
        val category = RecipeCategory.all().find {
            RecipeCategory.displayName(it) == categoryDisplay
        } ?: RecipeCategory.LUNCH

        val difficultyDisplay = binding.dropdownDifficulty.text.toString()
        val difficulty = when (difficultyDisplay) {
            "Fácil" -> RecipeDifficulty.EASY
            "Difícil" -> RecipeDifficulty.HARD
            else -> RecipeDifficulty.MEDIUM
        }

        val calories = binding.editCalories.text.toString().toIntOrNull()
        val healthFiltersStr = selectedHealthFilters.joinToString(",")

        val recipe = Recipe(
            id = editingRecipe?.id ?: 0,
            name = binding.editRecipeName.text.toString().trim(),
            ingredients = binding.editIngredients.text.toString().trim(),
            steps = binding.editSteps.text.toString().trim(),
            cookingTimeMinutes = binding.editCookingTime.text.toString().toInt(),
            difficulty = difficulty,
            category = category,
            healthFilters = healthFiltersStr,
            author = binding.dropdownAuthor.text.toString(),
            imagePath = selectedImageUri?.toString() ?: editingRecipe?.imagePath,
            isFavorite = editingRecipe?.isFavorite ?: false,
            madeTogather = editingRecipe?.madeTogather ?: false,
            createdAt = editingRecipe?.createdAt ?: System.currentTimeMillis(),
            lastCookedAt = editingRecipe?.lastCookedAt,
            timesCooked = editingRecipe?.timesCooked ?: 0,
            calories = calories,
            notes = binding.editNotes.text.toString().trim()
        )

        if (editingRecipe == null) {
            viewModel.insertRecipe(recipe) {
                requireActivity().runOnUiThread {
                    Snackbar.make(binding.root, "¡Receta guardada! 🎉", Snackbar.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        } else {
            viewModel.updateRecipe(recipe)
            Snackbar.make(binding.root, "Receta actualizada ✓", Snackbar.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
