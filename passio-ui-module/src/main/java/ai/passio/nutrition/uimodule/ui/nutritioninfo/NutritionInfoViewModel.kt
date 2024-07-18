package ai.passio.nutrition.uimodule.ui.nutritioninfo

import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.MicroNutrient
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import androidx.lifecycle.LiveData

class NutritionInfoViewModel : BaseViewModel() {

    private val _logsLD = SingleLiveEvent<List<MicroNutrient>>()
    val logsLD: LiveData<List<MicroNutrient>> get() = _logsLD


    private val _foodRecord = SingleLiveEvent<FoodRecord>()
    val foodRecord: LiveData<FoodRecord> get() = _foodRecord

    fun setFoodRecord(foodRecord: FoodRecord) {
        _foodRecord.postValue(foodRecord)
        _logsLD.postValue(MicroNutrient.nutrientsFromFoodRecords(listOf(foodRecord)))
    }

}