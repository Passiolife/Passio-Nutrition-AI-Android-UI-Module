package ai.passio.nutrition.uimodule.ui.favorite

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.domain.favorite.FavoriteUseCase
import ai.passio.nutrition.uimodule.domain.search.EditFoodUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.myfood.MyFoodsFragmentDirections
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoriteViewModel : BaseViewModel() {

    private val useCase = FavoriteUseCase
    private val editFoodUseCase = EditFoodUseCase

    private val _favoriteListEvent = MutableLiveData<List<FoodRecord>>()
    val favoriteListEvent: LiveData<List<FoodRecord>> = _favoriteListEvent

    private val _showLoading = SingleLiveEvent<Boolean>()
    val showLoading: LiveData<Boolean> = _showLoading

    private val _logFoodEvent = SingleLiveEvent<ResultWrapper<Boolean>>()
    val logFoodEvent: LiveData<ResultWrapper<Boolean>> = _logFoodEvent

    fun getFavoriteFoods() {
        viewModelScope.launch {
            _showLoading.postValue(true)
            val customFoods = useCase.fetchFavorites()
            _favoriteListEvent.postValue(customFoods)
            _showLoading.postValue(false)

        }
    }

    fun logFood(foodRecord: FoodRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            _showLoading.postValue(true)
            _logFoodEvent.postValue(
                ResultWrapper.Success(
                    editFoodUseCase.logFoodRecord(
                        foodRecord,
                        false
                    )
                )
            )
            _showLoading.postValue(false)
        }
    }

    fun markAsUnFavorite(foodRecord: FoodRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            _showLoading.postValue(true)
            if (useCase.markUnfavorite(foodRecord)) {
                getFavoriteFoods()
            }
            _showLoading.postValue(false)
        }
    }

    fun navigateToDetails() {
        navigate(FavoriteFragmentDirections.favoriteToEdit())
    }

    fun navigateToDiary() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(MyFoodsFragmentDirections.myFoodsToDiary())
        }
    }
}