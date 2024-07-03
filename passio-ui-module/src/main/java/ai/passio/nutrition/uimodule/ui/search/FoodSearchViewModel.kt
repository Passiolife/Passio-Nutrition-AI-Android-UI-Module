package ai.passio.nutrition.uimodule.ui.search

import ai.passio.nutrition.uimodule.domain.search.SearchUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.passiosdk.passiofood.PassioFoodDataInfo
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class FoodSearchViewModel : BaseViewModel() {

    private val useCase = SearchUseCase

    data class SearchResult(
        val query: String,
        val results: List<PassioFoodDataInfo>,
        val suggestions: List<String>
    )

    val searchResults = MutableLiveData<SearchResult>()

    fun fetchSearchResults(query: String) {
        viewModelScope.launch {
            val result = useCase.fetchSearchResults(query)
            searchResults.postValue(SearchResult(query, result.first, result.second))
        }
    }

    fun requestNavigateBack() {
        navigateBack()
    }

    fun navigateToEdit() {
        navigate(FoodSearchFragmentDirections.searchToEdit())
    }
}