package ai.passio.nutrition.uimodule.ui.editingredient

import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.edit.EditFoodModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditIngredientViewModel : BaseViewModel() {

    private val _editFoodModelLD = MutableLiveData<EditFoodModel>()
    val editFoodModelLD: LiveData<EditFoodModel> get() = _editFoodModelLD
    private val _internalUpdate =
        SingleLiveEvent<Pair<FoodRecord, EditIngredientFragment.UpdateOrigin>>()
    val internalUpdate: LiveData<Pair<FoodRecord, EditIngredientFragment.UpdateOrigin>> get() = _internalUpdate
    private lateinit var foodRecord: FoodRecord
    private var editIngredientIndex = -1

    fun setFoodRecord(foodRecord: FoodRecord) {
        this.foodRecord = foodRecord
        val model = EditFoodModel(foodRecord, true)
        _editFoodModelLD.postValue(model)
    }

    fun editFoodRecord(editIngredient: Pair<FoodRecordIngredient, Int>) {
        this.foodRecord = FoodRecord(editIngredient.first)
        this.editIngredientIndex = editIngredient.second
        val model = EditFoodModel(foodRecord, true)
        _editFoodModelLD.postValue(model)
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

    fun getEditIngredientIndex(): Int {
        return editIngredientIndex
    }

    fun getEditIngredient(): FoodRecordIngredient {
        return FoodRecordIngredient(foodRecord)
    }

    fun navigateBackToEditRecipe(): FoodRecord {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(EditIngredientFragmentDirections.backToEditRecipe())
        }
        return foodRecord
    }


    fun navigateToNutritionInfo(): FoodRecord {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(EditIngredientFragmentDirections.editIngredientToNutritionInfo())
        }
        return foodRecord

    }


}