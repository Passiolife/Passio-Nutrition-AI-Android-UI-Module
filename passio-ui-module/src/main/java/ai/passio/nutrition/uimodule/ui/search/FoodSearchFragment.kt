package ai.passio.nutrition.uimodule.ui.search

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.databinding.FragmentSearchBinding
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
import ai.passio.nutrition.uimodule.ui.util.toast
import ai.passio.passiosdk.passiofood.PassioFoodDataInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible

class FoodSearchFragment : BaseFragment<FoodSearchViewModel>() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchFoodSearchView.setup(passioSearchListener)

        viewModel.searchResults.observe(viewLifecycleOwner) { searchResult ->
            _binding?.searchFoodSearchView?.updateSearchResult(
                searchResult.query,
                searchResult.results,
                searchResult.suggestions,
                searchResult.myFoods
            )
        }

        sharedViewModel.isAddIngredientFromSearchLD.observe(viewLifecycleOwner) { isAddIngredient ->
            viewModel.setIsAddIngredient(isAddIngredient)
        }
        sharedViewModel.isAddIngredientFromScanningLD.observe(viewLifecycleOwner) { isAddIngredient ->
            viewModel.setIsAddIngredient(isAddIngredient)
        }
        viewModel.showLoading.observe(viewLifecycleOwner) {
            binding.loading.isVisible = it
        }
        viewModel.logFoodEvent.observe(viewLifecycleOwner, ::foodItemLogged)

        viewModel.addIngredientEvent.observe(viewLifecycleOwner, ::addFoodIngredient)
        viewModel.editIngredientEvent.observe(viewLifecycleOwner, ::editFoodIngredient)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun addFoodIngredient(resultWrapper: ResultWrapper<FoodRecord>) {
        when (resultWrapper) {
            is ResultWrapper.Success -> {
//                requireContext().toast("Food item(s) logged.")
                sharedViewModel.addFoodIngredients(resultWrapper.value)
//                sharedViewModel.addFoodIngredient(resultWrapper.value)
//                viewModel.navigateToEditIngredient()
                viewModel.navigateBackToEditRecipe()
            }

            is ResultWrapper.Error -> {
                requireContext().toast(resultWrapper.error)
            }
        }
    }

    private fun editFoodIngredient(resultWrapper: ResultWrapper<FoodRecord>) {
        when (resultWrapper) {
            is ResultWrapper.Success -> {
                val foodRecord = resultWrapper.value
                if (foodRecord.ingredients.size > 1) {
                    sharedViewModel.addFoodIngredients(resultWrapper.value)
                    viewModel.navigateBackToEditRecipe()
                } else {
                    sharedViewModel.editIngredient(FoodRecordIngredient(resultWrapper.value))
                    viewModel.navigateToEditIngredient()
                }
            }

            is ResultWrapper.Error -> {
                requireContext().toast(resultWrapper.error)
            }
        }
    }

    private fun foodItemLogged(resultWrapper: ResultWrapper<Boolean>) {
        when (resultWrapper) {
            is ResultWrapper.Success -> {
                if (resultWrapper.value) {
                    requireContext().toast("Food item(s) logged.")
                    viewModel.navigateToDiary()
                } else {
                    requireContext().toast("Could not log food item(s).")
                }
            }

            is ResultWrapper.Error -> {
                requireContext().toast(resultWrapper.error)
            }
        }
    }

    private val passioSearchListener = object : FoodSearchView.PassioSearchListener {
        override fun onQueryChange(query: String) {
            viewModel.fetchSearchResults(query)
        }

        override fun onFoodItemSelected(searchItem: PassioFoodDataInfo) {
            if (viewModel.getIsAddIngredient()) {
                viewModel.editIngredient(searchItem)
            } else {
                sharedViewModel.passToEdit(searchItem)
                viewModel.navigateToEdit()
            }
        }

        override fun onFoodItemSelected(searchItem: FoodRecord) {
            if (viewModel.getIsAddIngredient()) {
                editFoodIngredient(ResultWrapper.Success(searchItem))
            } else {
                sharedViewModel.detailsFoodRecord(searchItem)
                viewModel.navigateToEdit()
            }
        }

        override fun onFoodItemLog(searchItem: PassioFoodDataInfo) {
            if (viewModel.getIsAddIngredient()) {
                viewModel.addIngredient(searchItem)
            } else {
                viewModel.logFood(searchItem)
            }
        }

        override fun onFoodItemLog(searchItem: FoodRecord) {
            if (viewModel.getIsAddIngredient()) {
                addFoodIngredient(ResultWrapper.Success(searchItem))
            } else {
                viewModel.logFood(searchItem)
            }
        }

        override fun onTextCleared() {
        }

        override fun onViewDismissed() {
            viewModel.requestNavigateBack()
        }

    }

}