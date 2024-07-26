package ai.passio.nutrition.uimodule.ui.image

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.domain.mealplan.MealPlanUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.edit.EditFoodFragmentDirections
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.passiosdk.passiofood.PassioMealTime
import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.passiosdk.passiofood.data.model.PassioAdvisorFoodInfo
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

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
        _isProcessing.postValue(true)
        resultFoodInfoList.clear()
        val startTime = System.currentTimeMillis()
        Log.d("food result", "start == ${currentBitmaps.size} ")
        var currentCount = 0
        viewModelScope.launch(Dispatchers.Main) {
            currentBitmaps.forEach { bitmap ->

                Log.d("food result", "started == ${currentBitmaps.size}/$currentCount ")
                PassioSDK.instance.recognizeImageRemote(bitmap) { result ->
                    currentCount += 1
                    Log.d(
                        "food result",
                        "on result == result count: ${result.size} and current count:$currentCount "
                    )
                    resultFoodInfoList.addAll(result)
                    if (resultFoodInfoList.isNotEmpty()) {
                        _resultFoodInfo.postValue(resultFoodInfoList)
                    }
                    if (currentCount == currentBitmaps.size) {
                        val endTime = System.currentTimeMillis()
                        Log.d("food result", "duration: ${(endTime - startTime) / 1000f}")
                        Log.d("food result", "done == count $currentCount")
                        _isProcessing.postValue(false)
                        if (resultFoodInfoList.isEmpty()) {
                            _resultFoodInfo.postValue(resultFoodInfoList)
                        }
                    }
                }
            }
        }
    }

    private fun fetchResult1() {
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

    private fun passioMealTimeNow(): PassioMealTime {
        val calendar = Calendar.getInstance()
        val hours = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        val timeOfDay = hours * 100 + minutes

        return when (timeOfDay) {
            in 530..1030 -> PassioMealTime.BREAKFAST
            in 1130..1400 -> PassioMealTime.LUNCH
            in 1700..2100 -> PassioMealTime.DINNER
            else -> PassioMealTime.SNACK
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