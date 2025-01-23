package ai.passio.nutrition.uimodule.ui.image

import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.editingredient.EditIngredientFragment
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.ImageFoodResult
import ai.passio.nutrition.uimodule.ui.model.copy
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

internal class AdjustServingSizeViewModel : BaseViewModel() {

    private val _editFoodModelLD = MutableLiveData<FoodRecord>()
    val editFoodModelLD: LiveData<FoodRecord> get() = _editFoodModelLD
    private val _internalUpdate =
        SingleLiveEvent<Pair<FoodRecord, EditIngredientFragment.UpdateOrigin>>()
    val internalUpdate: LiveData<Pair<FoodRecord, EditIngredientFragment.UpdateOrigin>> get() = _internalUpdate
    private lateinit var imageFoodResult: ImageFoodResult
    private lateinit var foodRecord: FoodRecord
    private var editIngredientIndex = -1

    fun editFoodRecord(editFoodRecord: Pair<ImageFoodResult, Int>) {
        this.imageFoodResult = editFoodRecord.first
        this.foodRecord = editFoodRecord.first.record.copy()
        this.editIngredientIndex = editFoodRecord.second
        _editFoodModelLD.postValue(this.foodRecord)
    }

    fun updateServingQuantity(value: Double, origin: EditIngredientFragment.UpdateOrigin) {
        foodRecord.setSelectedQuantity(value)
        _internalUpdate.postValue(foodRecord to origin)
    }

    fun updateServingUnit(index: Int, origin: EditIngredientFragment.UpdateOrigin) {
        val unit = foodRecord.servingUnits[index].unitName
        foodRecord.setSelectedUnit(unit)
        _internalUpdate.postValue(foodRecord to origin)
    }

    fun getEditFoodRecordIndex(): Int {
        return editIngredientIndex
    }

    fun getFoodRecord(): FoodRecord {
        return foodRecord
    }
    fun getUpdatedImageFoodResult(): ImageFoodResult {
        imageFoodResult.record = foodRecord
        return imageFoodResult
    }
}