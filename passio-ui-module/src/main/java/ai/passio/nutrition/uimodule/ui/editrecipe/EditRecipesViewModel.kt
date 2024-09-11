package ai.passio.nutrition.uimodule.ui.editrecipe

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.domain.recipe.RecipeUseCase
import ai.passio.nutrition.uimodule.domain.search.EditFoodUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.edit.EditFoodFragment
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
import ai.passio.nutrition.uimodule.ui.model.copyAsRecipe
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.nutrition.uimodule.ui.util.StringKT.isValid
import ai.passio.passiosdk.passiofood.data.measurement.Grams
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import ai.passio.passiosdk.passiofood.data.model.PassioIDEntityType
import ai.passio.passiosdk.passiofood.data.model.PassioServingSize
import ai.passio.passiosdk.passiofood.data.model.PassioServingUnit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class EditRecipesViewModel : BaseViewModel() {

    private val useCase = RecipeUseCase
    private val editFoodUseCase = EditFoodUseCase

    private var photoPath: String? = null
    private val _photoPathEvent = MutableLiveData<String>()
    val photoPathEvent: LiveData<String> = _photoPathEvent

    private val _internalUpdate = SingleLiveEvent<Pair<FoodRecord, EditFoodFragment.UpdateOrigin>>()
    val internalUpdate: LiveData<Pair<FoodRecord, EditFoodFragment.UpdateOrigin>> get() = _internalUpdate

    private val _showLoading = SingleLiveEvent<Boolean>()
    val showLoading: LiveData<Boolean> = _showLoading

    private val _saveRecipeEvent = SingleLiveEvent<ResultWrapper<Boolean>>()
    val saveRecipeEvent: LiveData<ResultWrapper<Boolean>> = _saveRecipeEvent

    private val _showMessageEvent = SingleLiveEvent<String>()
    val showMessageEvent: LiveData<String> = _showMessageEvent

    private var foodRecord = FoodRecord()
    private var loggedRecord: FoodRecord? = null
    private var isEditRecipe = false


    private val defaultSizeGram = PassioServingSize(1.0, Grams.unitName) //g or ml
    private val defaultUnitGram = PassioServingUnit(Grams.unitName, UnitMass(Grams, 1.0))

    init {
        foodRecord.passioIDEntityType = PassioIDEntityType.recipe.value
        foodRecord.servingSizes.add(defaultSizeGram)
        foodRecord.servingUnits.add(defaultUnitGram)
        foodRecord.setSelectedUnit(Grams.unitName)
        foodRecord.setSelectedQuantity(100.0)
        _internalUpdate.postValue(foodRecord to EditFoodFragment.UpdateOrigin.INGREDIENT)
    }

    fun setToUpdateLog(loggedRecord: FoodRecord) {
        this.loggedRecord = loggedRecord
    }

    fun showPrefilledData()
    {
        _internalUpdate.postValue(foodRecord to EditFoodFragment.UpdateOrigin.INGREDIENT)
    }

    fun setRecipeToEditOrCreateNew(editRecipe: FoodRecord) {
        viewModelScope.launch {
            foodRecord = editRecipe
            foodRecord.passioIDEntityType = PassioIDEntityType.recipe.value
            if (foodRecord.isUserRecipe() && useCase.getRecipe(foodRecord.uuid) != null) {
                isEditRecipe = true
            }

            foodRecord.setUnitToServing()
            _internalUpdate.postValue(foodRecord to EditFoodFragment.UpdateOrigin.INGREDIENT)
        }
    }

    fun isEditRecipe(): Boolean {
        return isEditRecipe
    }

    fun setPhotoPath(path: String) {
        this.photoPath = path
        foodRecord.foodImagePath = path
        _photoPathEvent.postValue(path)
    }

    fun setRecipeName(recipeName: String) {
        foodRecord.name = recipeName
    }

    fun editIngredient(ingredient: Pair<FoodRecordIngredient?, Int>) {
        val editIngredient = ingredient.first
        val indexToEdit = ingredient.second
        if (editIngredient == null && indexToEdit != -1) {
            this.foodRecord.removeIngredient(indexToEdit)
        } else if (editIngredient != null && indexToEdit != -1) {
            this.foodRecord.replaceIngredient(editIngredient, indexToEdit)
        } else if (editIngredient != null) {
            this.foodRecord.addIngredient(editIngredient)
        }
        _internalUpdate.postValue(foodRecord to EditFoodFragment.UpdateOrigin.INGREDIENT)
    }

    fun addIngredients(foodRecordEdit: FoodRecord) {
        this.foodRecord.addIngredient(foodRecordEdit)
        _internalUpdate.postValue(foodRecord to EditFoodFragment.UpdateOrigin.INGREDIENT)
    }

    fun addIngredient(foodRecordIngredient: FoodRecordIngredient) {
        foodRecord.addIngredient(foodRecordIngredient)
        _internalUpdate.postValue(foodRecord to EditFoodFragment.UpdateOrigin.INGREDIENT)
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


    fun removeIngredient(index: Int) {
        foodRecord.removeIngredient(index)
        _internalUpdate.postValue(foodRecord to EditFoodFragment.UpdateOrigin.INGREDIENT)
    }

    fun getIngredient(index: Int) = foodRecord.ingredients[index]

    fun saveRecipe() {
        viewModelScope.launch {
            if (!foodRecord.name.isValid()) {
                _saveRecipeEvent.postValue(ResultWrapper.Error("Please enter a recipe name."))
            } else if (foodRecord.ingredients.size <= 1) {
                _saveRecipeEvent.postValue(ResultWrapper.Error("Minimum 2 ingredients required to create a recipe."))

            } else {
                _showLoading.postValue(true)
                if (!foodRecord.isUserRecipe()) {
                    foodRecord = foodRecord.copyAsRecipe()
                }
                if (useCase.saveRecipe(foodRecord)) {
                    if (loggedRecord != null) {
                        loggedRecord?.apply {
                            this.name = foodRecord.name
                            this.ingredients = foodRecord.ingredients
                            this.foodImagePath = foodRecord.foodImagePath
                            this.iconId = foodRecord.iconId
                            this.id = foodRecord.uuid
                            this.passioIDEntityType = foodRecord.passioIDEntityType
                            this.servingSizes.clear()
                            this.servingSizes.addAll(foodRecord.servingSizes)
                            this.servingUnits.clear()
                            this.servingUnits.addAll(foodRecord.servingUnits)
                            this.setSelectedQuantity(foodRecord.getSelectedQuantity())
                            this.setSelectedUnit(foodRecord.getSelectedUnit())
                            editFoodUseCase.logFoodRecord(this, true)
                        }
                    }
                    _saveRecipeEvent.postValue(ResultWrapper.Success(true))
                } else {
                    _saveRecipeEvent.postValue(ResultWrapper.Error("Could not save recipe, Please try again."))
                }
                _showLoading.postValue(false)
            }
        }
    }

    fun deleteRecipe() {
        viewModelScope.launch {
            _showLoading.postValue(true)
            if (useCase.deleteRecipe(foodRecord.uuid)) {
                _showMessageEvent.postValue("Recipe deleted!")
            } else {
                _showMessageEvent.postValue("Could not delete recipe, Please try again.")
            }
            navigate(EditRecipeFragmentDirections.editRecipeToMyFoods())
            _showLoading.postValue(false)
        }
    }

    fun navigateToTakePhoto() {
        navigate(EditRecipeFragmentDirections.editRecipeToTakePhoto())

    }

    fun navigateToSearch() {
        navigate(EditRecipeFragmentDirections.editRecipeToSearch())
    }

    fun navigateOnSave() {
        if (loggedRecord != null) //update log upon create or save
        {
            navigate(EditRecipeFragmentDirections.editRecipeToDiary())
        }
        /*else if (isEditRecipe && loggedRecord == null)
        {
            navigateBack()
        }
        else if (isEditRecipe)
        {
            navigateBack()
        }*/
        else {
            navigate(EditRecipeFragmentDirections.editRecipeToMyFoods())
        }
    }

    fun navigateToEditIngredient() {
        navigate(EditRecipeFragmentDirections.editRecipeToEditIngredient())

    }

}