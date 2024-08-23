package ai.passio.nutrition.uimodule.ui.customfoods

import ai.passio.nutrition.uimodule.domain.customfood.CustomFoodUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.myfood.MyFoodsFragmentDirections
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CustomFoodsViewModel : BaseViewModel() {

    private val useCase = CustomFoodUseCase
    private val _customFoodListEvent = SingleLiveEvent<List<FoodRecord>>()
    val customFoodListEvent: LiveData<List<FoodRecord>> = _customFoodListEvent

    fun getCustomFoods() {
        viewModelScope.launch {
            val customFoods = useCase.fetchCustomFoods()
            _customFoodListEvent.postValue(customFoods)

        }
    }

    fun navigateToFoodCreator() {
        navigate(MyFoodsFragmentDirections.myFoodsToFoodCreator())
    }
    fun navigateToEditFood() {
        navigate(MyFoodsFragmentDirections.myFoodsToEdit())
    }
}