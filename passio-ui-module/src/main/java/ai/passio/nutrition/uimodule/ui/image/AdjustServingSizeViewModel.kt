package ai.passio.nutrition.uimodule.ui.image

import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.editingredient.EditIngredientFragment
import ai.passio.nutrition.uimodule.ui.model.ImageFoodResult
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class AdjustServingSizeViewModel : BaseViewModel() {

    private val _editFoodModelLD = MutableLiveData<ImageFoodResult>()
    val editFoodModelLD: LiveData<ImageFoodResult> get() = _editFoodModelLD
    private val _internalUpdate =
        SingleLiveEvent<Pair<ImageFoodResult, EditIngredientFragment.UpdateOrigin>>()
    val internalUpdate: LiveData<Pair<ImageFoodResult, EditIngredientFragment.UpdateOrigin>> get() = _internalUpdate
    private lateinit var imageFoodResult: ImageFoodResult
    private var editIngredientIndex = -1

    fun editFoodRecord(editFoodRecord: Pair<ImageFoodResult, Int>) {
        this.imageFoodResult = editFoodRecord.first
        this.editIngredientIndex = editFoodRecord.second
        _editFoodModelLD.postValue(this.imageFoodResult)
    }

    fun updateServingQuantity(value: Double, origin: EditIngredientFragment.UpdateOrigin) {
        imageFoodResult.record.setSelectedQuantity(value)
        _internalUpdate.postValue(imageFoodResult to origin)
    }

    fun updateServingUnit(index: Int, origin: EditIngredientFragment.UpdateOrigin) {
        val unit = imageFoodResult.record.servingUnits[index].unitName
        imageFoodResult.record.setSelectedUnit(unit)
        _internalUpdate.postValue(imageFoodResult to origin)
    }

    fun getEditFoodRecordIndex(): Int {
        return editIngredientIndex
    }

    fun getFoodRecord(): ImageFoodResult {
        return imageFoodResult
    }
}