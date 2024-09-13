package ai.passio.nutrition.uimodule.ui.search

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.domain.mealplan.MealPlanUseCase
import ai.passio.nutrition.uimodule.domain.search.SearchUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.passiosdk.passiofood.PassioFoodDataInfo
import ai.passio.passiosdk.passiofood.data.model.PassioMealPlanItem
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FoodSearchViewModel : BaseViewModel() {

    private val useCase = SearchUseCase
    private val mealPlanUseCase = MealPlanUseCase

    private val _logFoodEvent = SingleLiveEvent<ResultWrapper<Boolean>>()
    val logFoodEvent: LiveData<ResultWrapper<Boolean>> = _logFoodEvent

    private val _addIngredientEvent = SingleLiveEvent<ResultWrapper<FoodRecord>>()
    val addIngredientEvent: LiveData<ResultWrapper<FoodRecord>> = _addIngredientEvent
    private val _editIngredientEvent = SingleLiveEvent<ResultWrapper<FoodRecord>>()
    val editIngredientEvent: LiveData<ResultWrapper<FoodRecord>> = _editIngredientEvent
    val showLoading = SingleLiveEvent<Boolean>()

    private var isAddIngredient = false

    fun setIsAddIngredient(isAddIngredient: Boolean) {
        this.isAddIngredient = isAddIngredient
    }

    fun getIsAddIngredient(): Boolean {
        return isAddIngredient
    }

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

    fun addIngredient(passioFoodDataInfo: PassioFoodDataInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            showLoading.postValue(true)
            mealPlanUseCase.getFoodRecord(passioFoodDataInfo, passioMealTimeNow())?.let {
//                _addIngredientEvent.postValue(ResultWrapper.Success(FoodRecordIngredient(it)))
                _addIngredientEvent.postValue(ResultWrapper.Success(it))
            }
                ?: _addIngredientEvent.postValue(ResultWrapper.Error("Could not fetch food item for: ${passioFoodDataInfo.foodName}"))

            showLoading.postValue(false)
        }
    }

    fun editIngredient(passioFoodDataInfo: PassioFoodDataInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            showLoading.postValue(true)
            mealPlanUseCase.getFoodRecord(passioFoodDataInfo, passioMealTimeNow())?.let {
//                _addIngredientEvent.postValue(ResultWrapper.Success(FoodRecordIngredient(it)))
                _editIngredientEvent.postValue(ResultWrapper.Success(it))
            }
                ?: _editIngredientEvent.postValue(ResultWrapper.Error("Could not fetch food item for: ${passioFoodDataInfo.foodName}"))

            showLoading.postValue(false)
        }
    }

    fun logFood(passioFoodDataInfo: PassioFoodDataInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            showLoading.postValue(true)
            mealPlanUseCase.getFoodRecord(passioFoodDataInfo, passioMealTimeNow())?.let {
                _logFoodEvent.postValue(ResultWrapper.Success(mealPlanUseCase.logFoodRecord(it)))
            }
                ?: _logFoodEvent.postValue(ResultWrapper.Error("Could not fetch food item for: ${passioFoodDataInfo.foodName}"))

            showLoading.postValue(false)
        }
    }

    fun requestNavigateBack() {
        navigateBack()
    }

    fun navigateToEditIngredient() {
        navigate(FoodSearchFragmentDirections.searchToEditIngredient())
    }
    fun navigateToEdit() {
        navigate(FoodSearchFragmentDirections.searchToEdit())
    }

    fun navigateToDiary() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(FoodSearchFragmentDirections.searchToDiary())
        }
    }
}