package ai.passio.nutrition.uimodule.ui.image

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.domain.mealplan.MealPlanUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.passiosdk.passiofood.data.model.PassioAdvisorFoodInfo
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImageFoodResultViewModel : BaseViewModel() {

    private val mealPlanUseCase = MealPlanUseCase

    private val currentBitmaps = arrayListOf<Bitmap>()

    private val _isFetchingResult = SingleLiveEvent<Boolean>()
    val isFetchingResult: LiveData<Boolean> get() = _isFetchingResult

    private val _showLoading = SingleLiveEvent<Boolean>()
    val showLoading: LiveData<Boolean> get() = _showLoading

    private val resultFoodInfoList = mutableListOf<PassioAdvisorFoodInfo>()
    private val _resultFoodInfo = MutableLiveData<List<PassioAdvisorFoodInfo>>()
    val resultFoodInfo: LiveData<List<PassioAdvisorFoodInfo>> get() = _resultFoodInfo

    private val _logFoodEvent = SingleLiveEvent<ResultWrapper<Boolean>>()
    val logFoodEvent: LiveData<ResultWrapper<Boolean>> = _logFoodEvent

    private val _addIngredientEvent = SingleLiveEvent<List<FoodRecordIngredient>>()
    val addIngredientEvent: LiveData<List<FoodRecordIngredient>> = _addIngredientEvent

    private var isAddIngredient = false

    fun setIsAddIngredient(isAddIngredient: Boolean) {
        this.isAddIngredient = isAddIngredient
    }

    fun getIsAddIngredient(): Boolean {
        return isAddIngredient
    }

    fun setImageBitmaps(bitmaps: List<Bitmap>) {
        viewModelScope.launch(Dispatchers.IO) {
            currentBitmaps.clear()
            currentBitmaps.addAll(bitmaps)
            fetchResult()
        }
    }

    private fun fetchResult() {
        viewModelScope.launch {
            _isFetchingResult.postValue(true)
            resultFoodInfoList.clear()
            var currentCount = 0
            currentBitmaps.forEach { bitmap ->
//                currentCount += 1
                PassioSDK.instance.recognizeImageRemote(bitmap) { result ->
                    currentCount += 1
                    resultFoodInfoList.addAll(result)
                    //enable comment to get continues result for each images
                    /*if (resultFoodInfoList.isNotEmpty()) {
                        _resultFoodInfo.postValue(resultFoodInfoList)
                    }*/
                    if (currentCount == currentBitmaps.size) {
                        _isFetchingResult.postValue(false)
                        _resultFoodInfo.postValue(resultFoodInfoList)
                        /*if (resultFoodInfoList.isEmpty()) {
                            _resultFoodInfo.postValue(resultFoodInfoList)
                        }*/
                    }
                }
            }
        }
    }

    fun logRecords(list: List<PassioAdvisorFoodInfo>) {
        viewModelScope.launch(Dispatchers.IO) {
            _showLoading.postValue(true)
            mealPlanUseCase.getFoodRecords(list, passioMealTimeNow()).let {
                if (it.isEmpty()) {
                    _logFoodEvent.postValue(ResultWrapper.Error("Could not fetch food items!"))
                } else {
                    if (isAddIngredient) {
                        _addIngredientEvent.postValue(it.map { fr -> FoodRecordIngredient(fr) })
                    } else {
                        _logFoodEvent.postValue(
                            ResultWrapper.Success(
                                mealPlanUseCase.logFoodRecords(
                                    it
                                )
                            )
                        )
                    }
                }
            }
            _showLoading.postValue(false)
        }
    }

    fun navigateToDiary() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(ImageFoodResultFragmentDirections.imageFoodResultToDiary())
        }

    }

    fun navigateToSearch() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(ImageFoodResultFragmentDirections.imageFoodResultToSearch())
        }
    }

    fun navigateBackToRecipe() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(ImageFoodResultFragmentDirections.backToEditRecipe())
        }
    }

}