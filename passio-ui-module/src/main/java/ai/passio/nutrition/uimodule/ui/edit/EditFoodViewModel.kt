package ai.passio.nutrition.uimodule.ui.edit

import ai.passio.nutrition.uimodule.domain.search.EditFoodUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
import ai.passio.nutrition.uimodule.ui.model.MealLabel
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.passiosdk.passiofood.PassioFoodDataInfo
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditFoodViewModel : BaseViewModel() {

    private val useCase = EditFoodUseCase

    private val _editFoodModelLD = MutableLiveData<EditFoodModel>()
    val editFoodModelLD: LiveData<EditFoodModel> get() = _editFoodModelLD
    private val _internalUpdate = SingleLiveEvent<Pair<FoodRecord, EditFoodFragment.UpdateOrigin>>()
    val internalUpdate: LiveData<Pair<FoodRecord, EditFoodFragment.UpdateOrigin>> get() = _internalUpdate
    private var isEditMode = false

    private lateinit var foodRecord: FoodRecord
    private var ingredientIndex = -1

    fun setEditMode(isEditMode: Boolean)
    {
        this.isEditMode = isEditMode
    }

    fun setFoodRecord(foodRecord: FoodRecord) {
        Log.d("logFoodRecord", "tttt=== uuid ${foodRecord.uuid}")
        this.foodRecord = foodRecord
        val model = EditFoodModel(foodRecord, true, true)
        _editFoodModelLD.postValue(model)
    }

    fun getFoodRecord(searchResult: PassioFoodDataInfo) {
        viewModelScope.launch {
            val fr = useCase.getFoodRecord(searchResult)
            val model = EditFoodModel(fr, true, true)
            _editFoodModelLD.postValue(model)
            if (fr != null) {
                foodRecord = fr
            }
        }
    }

    fun getFoodRecordForIngredient(ingredient: FoodRecordIngredient, ingredientIndex: Int) {
        this.ingredientIndex = ingredientIndex
        val ingredientRecord = FoodRecord(ingredient)
        val model = EditFoodModel(ingredientRecord, false, true)
        _editFoodModelLD.postValue(model)
        this.foodRecord = ingredientRecord
    }

    fun updateServingQuantity(value: Double, origin: EditFoodFragment.UpdateOrigin) {
        foodRecord.setSelectedQuantity(value)
        _internalUpdate.postValue(foodRecord to origin)
    }

    fun updateServingUnit(index: Int, origin: EditFoodFragment.UpdateOrigin) {
        val unit = foodRecord.servingUnits[index].unitName
        foodRecord.setSelectedUnit(unit)
        _internalUpdate.postValue(foodRecord to origin)
    }

    fun updateMealLabel(mealLabel: MealLabel) {
        foodRecord.mealLabel = mealLabel
    }

    fun updateCreatedAt(date: Long) {
        foodRecord.create(date)
    }

    fun removeIngredient(index: Int) {
        foodRecord.removeIngredient(index)
        _internalUpdate.postValue(foodRecord to EditFoodFragment.UpdateOrigin.INGREDIENT)
    }

    fun getIngredient(index: Int) = foodRecord.ingredients[index]

    fun logCurrentRecord() {
        viewModelScope.launch {
            if (useCase.logFoodRecord(foodRecord, isEditMode)) {
                navigateToDiary()
            }
        }
    }

    fun navigateToDiary() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(EditFoodFragmentDirections.editToDiary())
        }
    }

    fun navigateToAddIngredient(): FoodRecord {
        // navigate(EditFoodFragmentDirections.editToSearch())
        return foodRecord
    }
}