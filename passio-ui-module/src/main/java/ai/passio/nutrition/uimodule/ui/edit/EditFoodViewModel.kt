package ai.passio.nutrition.uimodule.ui.edit

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.domain.customfood.CustomFoodUseCase
import ai.passio.nutrition.uimodule.domain.recipe.RecipeUseCase
import ai.passio.nutrition.uimodule.domain.search.EditFoodUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.MealLabel
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.passiosdk.passiofood.PassioFoodDataInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditFoodViewModel : BaseViewModel() {

    private val useCase = EditFoodUseCase
    private val recipeUseCase = RecipeUseCase
    private val customFoodUseCase = CustomFoodUseCase

    private val _editFoodModelLD = MutableLiveData<EditFoodModel>()
    val editFoodModelLD: LiveData<EditFoodModel> get() = _editFoodModelLD
    private val _internalUpdate = SingleLiveEvent<Pair<FoodRecord, EditFoodFragment.UpdateOrigin>>()
    val internalUpdate: LiveData<Pair<FoodRecord, EditFoodFragment.UpdateOrigin>> get() = _internalUpdate
    private var isEditLogMode = false


    private val _resultLogFood = SingleLiveEvent<ResultWrapper<FoodRecord>>()
    val resultLogFood: LiveData<ResultWrapper<FoodRecord>> get() = _resultLogFood

    private val _recipeInfo =
        SingleLiveEvent<Pair<FoodRecord?, Boolean>>() //custom recipe, is log update
    val recipeInfo: LiveData<Pair<FoodRecord?, Boolean>> get() = _recipeInfo

    private val _customFoodInfo =
        SingleLiveEvent<Pair<FoodRecord?, Boolean>>() //custom food, is log update
    val customFoodInfo: LiveData<Pair<FoodRecord?, Boolean>> get() = _customFoodInfo

    private lateinit var foodRecord: FoodRecord

    fun setEditLogMode(isEditMode: Boolean) {
        this.isEditLogMode = isEditMode
    }

    fun setFoodRecord(foodRecord: FoodRecord) {
        this.foodRecord = foodRecord
        val model = EditFoodModel(foodRecord, true)
        _editFoodModelLD.postValue(model)
    }

    fun getFoodRecord(searchResult: PassioFoodDataInfo) {
        viewModelScope.launch {
            val fr = useCase.getFoodRecord(searchResult)
            val model = EditFoodModel(fr, true)
            _editFoodModelLD.postValue(model)
            if (fr != null) {
                foodRecord = fr
            }
        }
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

    fun logCurrentRecord() {
        viewModelScope.launch {
            if (useCase.logFoodRecord(foodRecord, isEditLogMode)) {
                _resultLogFood.postValue(ResultWrapper.Success(foodRecord))
            } else {
                _resultLogFood.postValue(ResultWrapper.Error("Failed to log food. Please try again"))
            }
        }
    }

    fun editRecipeFromLoggedFood(isUpdateLog: Boolean) {
        viewModelScope.launch {
            val recipe = recipeUseCase.getRecipe(foodRecord.id)
            _recipeInfo.postValue(recipe to isUpdateLog)
        }
    }
    fun editCustomFromLoggedFood(isUpdateLog: Boolean) {
        viewModelScope.launch {
            val customFood = customFoodUseCase.fetchCustomFood(foodRecord.id)
            _customFoodInfo.postValue(customFood to isUpdateLog)
        }
    }

    fun navigateToDiary(createdAtTime: Long?) {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(EditFoodFragmentDirections.editToDiary(currentDate = createdAtTime ?: 0))
        }

    }

    fun navigateToNutritionInfo(): FoodRecord {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(EditFoodFragmentDirections.editToNutritionInfo())
        }
        return foodRecord

    }

    fun navigateToFoodCreator() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(EditFoodFragmentDirections.editToFoodCreator())
        }
    }

    fun navigateToEditRecipe() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(EditFoodFragmentDirections.editToEditRecipe())
        }
    }


    fun isEditLogMode(): Boolean {
        return isEditLogMode
    }

    fun getFoodRecord(): FoodRecord {
        return foodRecord
    }
}