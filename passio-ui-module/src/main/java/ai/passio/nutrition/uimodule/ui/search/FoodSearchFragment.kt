package ai.passio.nutrition.uimodule.ui.search

import ai.passio.nutrition.uimodule.databinding.FragmentSearchBinding
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.passiosdk.passiofood.PassioFoodDataInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.launch

class FoodSearchFragment : BaseFragment<FoodSearchViewModel>(), FoodSearchView.PassioSearchListener {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchFoodSearchView.setup(this)

        viewModel.searchResults.observe(viewLifecycleOwner) { searchResult ->
            _binding?.searchFoodSearchView?.updateSearchResult(
                searchResult.query,
                searchResult.results,
                searchResult.suggestions
            )
        }

        sharedViewModel.addIngredientLD.observe(viewLifecycleOwner) { fr ->
            // viewModel.setFoodRecord(fr)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onQueryChange(query: String) {
        viewModel.fetchSearchResults(query)
    }

    override fun onFoodItemSelected(searchItem: PassioFoodDataInfo) {
        sharedViewModel.passToEdit(searchItem)
        viewModel.navigateToEdit()
    }

    override fun onTextCleared() {
        TODO("Not yet implemented")
    }

    override fun onViewDismissed() {
        viewModel.requestNavigateBack()
    }

}