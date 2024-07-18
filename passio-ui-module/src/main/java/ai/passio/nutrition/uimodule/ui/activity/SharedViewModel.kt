package ai.passio.nutrition.uimodule.ui.activity

import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.passiosdk.passiofood.PassioFoodDataInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    private val _editFoodRecordLD = SingleLiveEvent<FoodRecord>()
    val editFoodRecordLD: LiveData<FoodRecord> get() = _editFoodRecordLD

    private val _editIngredientLD = SingleLiveEvent<Pair<FoodRecordIngredient, Int>>()
    val editIngredientLD: LiveData<Pair<FoodRecordIngredient, Int>> get() = _editIngredientLD

    private val _addIngredientLD = SingleLiveEvent<FoodRecord>()
    val addIngredientLD: LiveData<FoodRecord> get() = _addIngredientLD

    private val _editSearchResultLD = SingleLiveEvent<PassioFoodDataInfo>()
    val editSearchResultLD: LiveData<PassioFoodDataInfo> get() = _editSearchResultLD


    private val _nutritionInfoFoodRecordLD= SingleLiveEvent<FoodRecord>()
    val nutritionInfoFoodRecordLD: LiveData<FoodRecord> get() = _nutritionInfoFoodRecordLD


    fun passToNutritionInfo(foodRecord: FoodRecord) {
        _nutritionInfoFoodRecordLD.postValue(foodRecord)
    }

    fun passToEdit(searchResult: PassioFoodDataInfo) {
        _editSearchResultLD.postValue(searchResult)
    }

    fun editIngredient(ingredient: FoodRecordIngredient, ingredientIndex: Int) {
        _editIngredientLD.postValue(ingredient to ingredientIndex)
    }

    fun editFoodRecord(foodRecord: FoodRecord) {
        _editFoodRecordLD.postValue(foodRecord)
    }

    fun addIngredient(foodRecord: FoodRecord) {
        _addIngredientLD.postValue(foodRecord)
    }

}