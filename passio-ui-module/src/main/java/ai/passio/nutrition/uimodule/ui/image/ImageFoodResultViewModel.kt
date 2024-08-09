package ai.passio.nutrition.uimodule.ui.image

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.domain.mealplan.MealPlanUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.passiosdk.passiofood.data.model.PassioAdvisorFoodInfo
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImageFoodResultViewModel : BaseViewModel() {

    private val mealPlanUseCase = MealPlanUseCase

    private val currentBitmaps = arrayListOf<Bitmap>()

    private val _isProcessing = SingleLiveEvent<Boolean>()
    val isProcessing: LiveData<Boolean> get() = _isProcessing

    private val _showLoading = SingleLiveEvent<Boolean>()
    val showLoading: LiveData<Boolean> get() = _showLoading

    private val resultFoodInfoList = mutableListOf<PassioAdvisorFoodInfo>()
    private val _resultFoodInfo = SingleLiveEvent<List<PassioAdvisorFoodInfo>>()
    val resultFoodInfo: LiveData<List<PassioAdvisorFoodInfo>> get() = _resultFoodInfo

    private val _logFoodEvent = SingleLiveEvent<ResultWrapper<Boolean>>()
    val logFoodEvent: LiveData<ResultWrapper<Boolean>> = _logFoodEvent

    fun setImageBitmaps(bitmaps: List<Bitmap>) {
        viewModelScope.launch(Dispatchers.IO) {
            currentBitmaps.clear()
            currentBitmaps.addAll(bitmaps)
            fetchResult()
        }
    }

    private fun fetchResult() {
        viewModelScope.launch {
            _isProcessing.postValue(true)
            resultFoodInfoList.clear()
            val startTime = System.currentTimeMillis()
            Log.d("food result", "start == ${currentBitmaps.size} ")
            var currentCount = 0
            currentBitmaps.forEach { bitmap ->
                currentCount += 1
                Log.d("food result", "started == ${currentBitmaps.size}/$currentCount ")
                PassioSDK.instance.recognizeImageRemote(bitmap) { result ->
                    Log.d(
                        "food result",
                        "on result == result count: ${result.size} and current count:$currentCount "
                    )
                    result.forEach {
                        Log.d("resultFoodInfoList", Gson().toJson(it))
                    }
                    resultFoodInfoList.addAll(result)
                    if (resultFoodInfoList.isNotEmpty()) {
                        _resultFoodInfo.postValue(resultFoodInfoList)
                    }
                    if (currentCount == currentBitmaps.size) {
                        val endTime = System.currentTimeMillis()
                        Log.d("food result", "duration: ${(endTime - startTime) / 1000f}")
                        Log.d("food result", "done")
                        _isProcessing.postValue(false)
                        if (resultFoodInfoList.isEmpty()) {
                            _resultFoodInfo.postValue(resultFoodInfoList)
                        }
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
                    _logFoodEvent.postValue(ResultWrapper.Success(mealPlanUseCase.logFoodRecords(it)))
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

}