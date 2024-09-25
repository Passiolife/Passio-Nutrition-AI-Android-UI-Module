package ai.passio.nutrition.uimodule.ui.customfoods

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.domain.customfood.CustomFoodUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.myfood.MyFoodsFragmentDirections
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CustomFoodsViewModel : BaseViewModel() {

    private val useCase = CustomFoodUseCase

    private val _customFoodListEvent = MutableLiveData<List<FoodRecord>>()
    val customFoodListEvent: LiveData<List<FoodRecord>> = _customFoodListEvent

    private val _showLoading = SingleLiveEvent<Boolean>()
    val showLoading: LiveData<Boolean> = _showLoading

    private val _logFoodEvent = SingleLiveEvent<ResultWrapper<Boolean>>()
    val logFoodEvent: LiveData<ResultWrapper<Boolean>> = _logFoodEvent

    fun getCustomFoods() {
        viewModelScope.launch {
            _showLoading.postValue(true)
            val customFoods = useCase.fetchCustomFoods()
            _customFoodListEvent.postValue(customFoods)
            _showLoading.postValue(false)

        }
    }

    fun deleteCustomFood(uuid: String) {
        viewModelScope.launch {
            _showLoading.postValue(true)
            useCase.deleteCustomFood(uuid)
            getCustomFoods()
            _showLoading.postValue(false)
        }
    }

    fun logCustomFood(foodRecord: FoodRecord) {
        viewModelScope.launch {
            _showLoading.postValue(true)
            _logFoodEvent.postValue(ResultWrapper.Success(useCase.logCustomFood(foodRecord)))
            _showLoading.postValue(false)
        }
    }

    fun navigateToFoodCreator() {
        navigate(MyFoodsFragmentDirections.myFoodsToFoodCreator())
    }

    fun navigateToEditFood() {
        navigate(MyFoodsFragmentDirections.myFoodsToEdit())
    }

    fun navigateToDiary() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(MyFoodsFragmentDirections.myFoodsToDiary())
        }
    }
}