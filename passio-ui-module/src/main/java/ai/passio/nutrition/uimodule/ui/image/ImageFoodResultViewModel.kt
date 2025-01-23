package ai.passio.nutrition.uimodule.ui.image

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.domain.customfood.CustomFoodUseCase
import ai.passio.nutrition.uimodule.domain.mealplan.MealPlanUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
import ai.passio.nutrition.uimodule.ui.model.ImageFoodResult
import ai.passio.nutrition.uimodule.ui.model.clone
import ai.passio.nutrition.uimodule.ui.model.copy
import ai.passio.nutrition.uimodule.ui.model.copyAsCustomFood
import ai.passio.nutrition.uimodule.ui.model.toMealLabel
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.passiosdk.passiofood.PassioMealTime
import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.passiosdk.passiofood.data.model.PassioAdvisorFoodInfo
import ai.passio.passiosdk.passiofood.data.model.PassioFoodResultType
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.DateTime

class ImageFoodResultViewModel : BaseViewModel() {

    private val mealPlanUseCase = MealPlanUseCase
    private val customFoodUseCase = CustomFoodUseCase

    private val currentBitmaps = arrayListOf<Bitmap>()

    private val _isFetchingResult = SingleLiveEvent<Boolean>()
    val isFetchingResult: LiveData<Boolean> get() = _isFetchingResult

    private val _showLoading = SingleLiveEvent<Boolean>()
    val showLoading: LiveData<Boolean> get() = _showLoading

    private val resultFoodInfoList = mutableListOf<ImageFoodResult>()
    private val _resultFoodInfoEvent = MutableLiveData<List<ImageFoodResult>>()
    val resultFoodInfoEvent: LiveData<List<ImageFoodResult>> get() = _resultFoodInfoEvent

    private val _createRecipeEvent = SingleLiveEvent<FoodRecord>()
    val createRecipeEvent: LiveData<FoodRecord> get() = _createRecipeEvent

    private val _logFoodEvent = SingleLiveEvent<ResultWrapper<Triple<Boolean, Int, Int>>>()
    val logFoodEvent: LiveData<ResultWrapper<Triple<Boolean, Int, Int>>> = _logFoodEvent

    private val _addIngredientEvent = SingleLiveEvent<List<FoodRecordIngredient>>()
    val addIngredientEvent: LiveData<List<FoodRecordIngredient>> = _addIngredientEvent

    private var isAddIngredient = false

    private var currentMealTime = passioMealTimeNow()
    private val _currentMealTimeEvent = MutableLiveData<PassioMealTime>()
    val currentMealTimeEvent: LiveData<PassioMealTime> get() = _currentMealTimeEvent

    private lateinit var selectedDateTime: DateTime
    private val _selectedDateTimeEvent = MutableLiveData<DateTime>()
    val selectedDateTimeEvent: LiveData<DateTime> get() = _selectedDateTimeEvent

    init {
        setMealTime(currentMealTime)
        setDateTime(DateTime.now())
    }

    fun setMealTime(mealTime: PassioMealTime) {
        currentMealTime = mealTime
        _currentMealTimeEvent.postValue(currentMealTime)
    }

    fun setDateTime(dateTime: DateTime) {
        selectedDateTime = dateTime
        _selectedDateTimeEvent.postValue(selectedDateTime)
    }

    fun getDateTime(): DateTime {
        return selectedDateTime
    }

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

            val resultList = mutableListOf<PassioAdvisorFoodInfo>()
            var currentCount = 0
            currentBitmaps.forEach { bitmap ->
//                currentCount += 1
                PassioSDK.instance.recognizeImageRemote(bitmap) { result ->
                    currentCount += 1
                    resultList.addAll(result)
                    //enable comment to get continues result for each images
                    /*if (resultFoodInfoList.isNotEmpty()) {
                        _resultFoodInfo.postValue(resultFoodInfoList)
                    }*/
                    if (currentCount == currentBitmaps.size) {


                        fetchFoodRecords(resultList)
//                        _resultFoodInfo.postValue(resultFoodInfoList)
                        /*if (resultFoodInfoList.isEmpty()) {
                            _resultFoodInfo.postValue(resultFoodInfoList)
                        }*/
                    }
                }
            }
        }
    }

    private fun fetchFoodRecords(list: List<PassioAdvisorFoodInfo>) {
        viewModelScope.launch(Dispatchers.IO) {
            resultFoodInfoList.clear()
            resultFoodInfoList.addAll(
                mealPlanUseCase.getFoodRecordsForImages(
                    list,
                    currentMealTime
                )
            )
            resultFoodInfoList.forEach {
                it.isSelected = !(it.isBarcodeDataMissing() || it.isNutritionFactsDataMissing())
            }
            _isFetchingResult.postValue(false)
            _resultFoodInfoEvent.postValue(resultFoodInfoList)
        }
    }

    fun updateItemSelection() {
        _resultFoodInfoEvent.postValue(resultFoodInfoList)
    }

    //    private val customFoodUseCase = CustomFoodUseCase
//    fun logRecords(list1: List<ImageFoodResult>) {
    fun logRecords() {
        viewModelScope.launch(Dispatchers.IO) {
            _showLoading.postValue(true)
            val list = resultFoodInfoList.filter { it.isSelected }
            if (list.isEmpty()) {
                _logFoodEvent.postValue(ResultWrapper.Error("Could not fetch food items!"))
            } else {


                list.forEach {
                    it.record.create(selectedDateTime.millis)
                    it.record.mealLabel = currentMealTime.toMealLabel()
                }

                if (isAddIngredient) {
                    _addIngredientEvent.postValue(list.map { fr -> FoodRecordIngredient(fr.record.copy()) })
                } else {


                    val totalLoggedItems = list.size
                    var totalCustomFoodSaved = 0 //list.count { it.isCustomFood }

                    list.forEach { imageRecord ->
                        if (!imageRecord.isCustomFood && (imageRecord.resultType == PassioFoodResultType.NUTRITION_FACTS || imageRecord.resultType == PassioFoodResultType.BARCODE)) {
                            val customFood = imageRecord.record.copyAsCustomFood()
                            customFoodUseCase.saveCustomFood(customFood)
                            imageRecord.record = customFood
                            imageRecord.isCustomFood = true
                            totalCustomFoodSaved = totalCustomFoodSaved + 1
                        }
                    }
                    val isLogged = mealPlanUseCase.logFoodRecords(list.map { it.record.copy() })
                    _logFoodEvent.postValue(
                        ResultWrapper.Success(
                            Triple(
                                isLogged,
                                totalLoggedItems,
                                totalCustomFoodSaved
                            )
                        )
                    )
                }
            }
            _showLoading.postValue(false)
        }
    }

    //    fun createRecipe(list1: List<ImageFoodResult>) {
    fun createRecipe() {
        viewModelScope.launch(Dispatchers.IO) {
            _showLoading.postValue(true)
            val list = resultFoodInfoList.filter { it.isSelected }
            if (list.isEmpty()) {
                _logFoodEvent.postValue(ResultWrapper.Error("Could not fetch food items!"))
            } else {
                list.forEach {
                    it.record.create(selectedDateTime.millis)
                    it.record.mealLabel = currentMealTime.toMealLabel()
                }
                val recipeRecord = list.first().record.clone()
                recipeRecord.ingredients.clear()
                recipeRecord.addIngredients(list.map { FoodRecordIngredient(it.record) })
                _createRecipeEvent.postValue(recipeRecord)
            }
            _showLoading.postValue(false)
        }
    }

    val editedRecordsIndex = mutableListOf<Int>()
    fun updateFoodRecord(indexToEdit: Int, updatedImageFoodResult: ImageFoodResult) {
        if (!editedRecordsIndex.contains(indexToEdit)) {
            editedRecordsIndex.add(indexToEdit)
        }
        resultFoodInfoList[indexToEdit] = updatedImageFoodResult
        _resultFoodInfoEvent.postValue(resultFoodInfoList)
    }

    fun navigateToDiary() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(ImageFoodResultFragmentDirections.imageFoodResultToDiary())
        }

    }

    fun navigateToTakeOrSelectPhoto() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(ImageFoodResultFragmentDirections.imageFoodResultToTakeSelectPhoto())
        }
    }

    fun navigateToSearch() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(ImageFoodResultFragmentDirections.imageFoodResultToSearch())
        }
    }

    fun navigateToRecipe() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(ImageFoodResultFragmentDirections.imageFoodToEditRecipe())
        }
    }

    fun navigateBackToRecipe() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(ImageFoodResultFragmentDirections.backToEditRecipe())
        }
    }


}