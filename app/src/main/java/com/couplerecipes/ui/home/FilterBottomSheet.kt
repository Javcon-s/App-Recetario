package com.couplerecipes.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.couplerecipes.CoupleRecipesApp
import com.couplerecipes.data.local.entities.HealthFilter
import com.couplerecipes.data.local.entities.RecipeAuthor
import com.couplerecipes.databinding.BottomSheetFilterBinding
import com.couplerecipes.ui.RecipeViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip

/**
 * Bottom Sheet para filtros avanzados: autor y filtros de salud.
 */
class FilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecipeViewModel by activityViewModels {
        RecipeViewModel.Factory((requireActivity().application as CoupleRecipesApp).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAuthorChips()
        setupHealthChips()
        setupButtons()
    }

    private fun setupAuthorChips() {
        listOf("Todos", RecipeAuthor.ME, RecipeAuthor.PARTNER).forEach { author ->
            val chip = Chip(requireContext()).apply {
                text = author
                isCheckable = true
            }
            chip.setOnCheckedChangeListener { _, checked ->
                if (checked) viewModel.setAuthor(if (author == "Todos") "" else author)
            }
            binding.chipGroupAuthor.addView(chip)
        }
        binding.chipGroupAuthor.isSingleSelection = true
        // Seleccionar "Todos" por defecto
        (binding.chipGroupAuthor.getChildAt(0) as Chip).isChecked = true
    }

    private fun setupHealthChips() {
        HealthFilter.all().forEach { filter ->
            val chip = Chip(requireContext()).apply {
                text = HealthFilter.displayName(filter)
                isCheckable = true
                tag = filter
            }
            binding.chipGroupHealth.addView(chip)
        }
    }

    private fun setupButtons() {
        binding.btnApply.setOnClickListener {
            dismiss()
        }

        binding.btnClearFilters.setOnClickListener {
            viewModel.clearFilters()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "FilterBottomSheet"
    }
}
