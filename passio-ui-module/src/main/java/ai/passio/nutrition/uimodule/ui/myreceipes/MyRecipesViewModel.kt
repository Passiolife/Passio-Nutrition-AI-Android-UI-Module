package ai.passio.nutrition.uimodule.ui.myreceipes

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.domain.recipe.RecipeUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.myfood.MyFoodsFragmentDirections
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyRecipesViewModel : BaseViewModel() {

    private val useCase = RecipeUseCase

    private val _recipeListEvent = MutableLiveData<List<FoodRecord>>()
    val recipeListEvent: LiveData<List<FoodRecord>> = _recipeListEvent

    private val _showLoading = SingleLiveEvent<Boolean>()
    val showLoading: LiveData<Boolean> = _showLoading

    private val _logRecipeEvent = SingleLiveEvent<ResultWrapper<Boolean>>()
    val logRecipeEvent: LiveData<ResultWrapper<Boolean>> = _logRecipeEvent

    fun getRecipes() {
        viewModelScope.launch {
            _showLoading.postValue(true)
            val customFoods = useCase.fetchRecipes()
            _recipeListEvent.postValue(customFoods)
            _showLoading.postValue(false)

        }
    }

    fun deleteRecipe(uuid: String) {
        viewModelScope.launch {
            _showLoading.postValue(true)
            useCase.deleteRecipe(uuid)
            getRecipes()
            _showLoading.postValue(false)
        }
    }

    fun logRecipe(foodRecord: FoodRecord) {
        viewModelScope.launch {
            _showLoading.postValue(true)
            _logRecipeEvent.postValue(ResultWrapper.Success(useCase.logRecipe(foodRecord)))
            _showLoading.postValue(false)
        }
    }

    fun navigateToDiary() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(MyFoodsFragmentDirections.myFoodsToDiary())
        }
    }

    fun navigateToEditRecipe() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(MyFoodsFragmentDirections.myFoodsToEditRecipe())
        }
    }
    fun navigateToDetails() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(MyFoodsFragmentDirections.myFoodsToEdit())
        }
    }

}