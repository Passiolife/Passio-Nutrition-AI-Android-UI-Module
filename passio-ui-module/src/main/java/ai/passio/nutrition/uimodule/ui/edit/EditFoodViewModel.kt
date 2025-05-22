package ai.passio.nutrition.uimodule.ui.edit

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.domain.customfood.CustomFoodUseCase
import ai.passio.nutrition.uimodule.domain.favorite.FavoriteUseCase
import ai.passio.nutrition.uimodule.domain.mealplan.MealPlanUseCase
import ai.passio.nutrition.uimodule.domain.recipe.RecipeUseCase
import ai.passio.nutrition.uimodule.domain.search.EditFoodUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
import ai.passio.nutrition.uimodule.ui.model.MealLabel
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.nutrition.uimodule.ui.util.StringKT.isValid
import ai.passio.passiosdk.passiofood.PassioFoodDataInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class EditFoodViewModel : BaseViewModel() {

    private val favoriteUseCase = FavoriteUseCase
    private val useCase = EditFoodUseCase
    private val mealPlanUseCase = MealPlanUseCase
    private val recipeUseCase = RecipeUseCase
    private val customFoodUseCase = CustomFoodUseCase

    private var customFood: FoodRecord? = null
    private var customRecipe: FoodRecord? = null
    private lateinit var editFoodDataModel: EditFoodDataModel
    private val _editFoodModelLD = MutableLiveData<EditFoodDataModel>()
    val editFoodModelLD: LiveData<EditFoodDataModel> get() = _editFoodModelLD
    private val _internalUpdate = SingleLiveEvent<Pair<FoodRecord, EditFoodFragment.UpdateOrigin>>()
    val internalUpdate: LiveData<Pair<FoodRecord, EditFoodFragment.UpdateOrigin>> get() = _internalUpdate
    private var isEditLogMode = false


    private val _resultLogFood = SingleLiveEvent<ResultWrapper<FoodRecord>>()
    val resultLogFood: LiveData<ResultWrapper<FoodRecord>> get() = _resultLogFood

    private val _deleteLogFood = SingleLiveEvent<Boolean>()
    val deleteLogFood: LiveData<Boolean> get() = _deleteLogFood

    private val _recipeInfo =
        SingleLiveEvent<Pair<FoodRecord?, Boolean>>() //custom recipe, is log update
    val recipeInfo: LiveData<Pair<FoodRecord?, Boolean>> get() = _recipeInfo

    private val _customFoodInfo =
        SingleLiveEvent<Pair<FoodRecord?, Boolean>>() //custom food, is log update
    val customFoodInfo: LiveData<Pair<FoodRecord?, Boolean>> get() = _customFoodInfo

    private val _showLoading = SingleLiveEvent<Boolean>()
    val showLoading: LiveData<Boolean> = _showLoading


    private var isFav = false
    private val _isFavEvent = MutableLiveData<Boolean>()
    val isFavEvent: LiveData<Boolean> = _isFavEvent
    private val _isEditFavEvent = MutableLiveData<Boolean>()
    val isEditFavEvent: LiveData<Boolean> = _isEditFavEvent
//    private var isEditFav = false

//    private lateinit var foodRecord: FoodRecord


    fun setEditLogMode(isEditMode: Boolean) {
        this.isEditLogMode = isEditMode
    }

    /* fun setFoodRecord(foodRecord: FoodRecord, isEditFav: Boolean = false) {
         this.isEditFav = isEditFav
         _isEditFavEvent.postValue(this.isEditFav)
 //        this.foodRecord = foodRecord
         checkFavStatus()
         val model = EditFoodModel(foodRecord, true)
         _editFoodModelLD.postValue(model)
     }*/
    fun setFoodRecord(editFoodDataModelTemp: EditFoodDataModel) {
        viewModelScope.launch(Dispatchers.IO) {
            _showLoading.postValue(true)
            editFoodDataModel = editFoodDataModelTemp
            _isEditFavEvent.postValue(editFoodDataModel.isEditFav)

            if (editFoodDataModel.foodRecord.refCode.isValid()) {
                customFood = customFoodUseCase.fetchCustomFood(editFoodDataModel.foodRecord.refCode)
                customRecipe = recipeUseCase.getRecipe(editFoodDataModel.foodRecord.refCode)
            }
            editFoodDataModel.isUserFood = customFood != null
            editFoodDataModel.isUserRecipe = customRecipe != null


//        this.isEditFav = isEditFav
//        _isEditFavEvent.postValue(this.isEditFav)
//        this.foodRecord = foodRecord
            checkFavStatus()
//        val model = EditFoodModel(foodRecord, true)
            _editFoodModelLD.postValue(editFoodDataModel)
            _showLoading.postValue(false)
        }
    }

    fun getFoodRecord(searchResult: PassioFoodDataInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            _showLoading.postValue(true)
            val fr = mealPlanUseCase.getFoodRecord(searchResult, passioMealTimeNow())
            if (fr != null) {
                editFoodDataModel = EditFoodDataModel(
                    foodRecord = fr,
                    isUserFood = customFood != null,
                    isUserRecipe = customRecipe != null,
                    isEditFav = false
                )
                _editFoodModelLD.postValue(editFoodDataModel)
                _showLoading.postValue(false)
                checkFavStatus()
            }
        }
    }

    fun updateServingQuantity(value: Double, origin: EditFoodFragment.UpdateOrigin) {
        editFoodDataModel.foodRecord.setSelectedQuantity(value)
        _internalUpdate.postValue(editFoodDataModel.foodRecord to origin)
    }

    fun updateServingUnit(index: Int, origin: EditFoodFragment.UpdateOrigin) {
        val unit = editFoodDataModel.foodRecord.servingUnits[index].unitName
        editFoodDataModel.foodRecord.setSelectedUnit(unit)
        _internalUpdate.postValue(editFoodDataModel.foodRecord to origin)
    }

    fun updateMealLabel(mealLabel: MealLabel) {
        editFoodDataModel.foodRecord.mealLabel = mealLabel
    }

    fun updateCreatedAt(date: Long) {
        editFoodDataModel.foodRecord.create(date)
    }

    fun deleteCurrentRecord() {
        viewModelScope.launch(Dispatchers.IO) {
            _showLoading.postValue(true)
            _deleteLogFood.postValue(useCase.deleteRecord(editFoodDataModel.foodRecord))
            _showLoading.postValue(false)
        }
    }

    fun logCurrentRecord() {
        viewModelScope.launch(Dispatchers.IO) {
            _showLoading.postValue(true)
            if (useCase.logFoodRecord(editFoodDataModel.foodRecord, isEditLogMode)) {
                _resultLogFood.postValue(ResultWrapper.Success(editFoodDataModel.foodRecord))
            } else {
                _resultLogFood.postValue(ResultWrapper.Error("Failed to log food. Please try again"))
            }
            _showLoading.postValue(false)
        }
    }

    fun editRecipeFromLoggedFood(isUpdateLog: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _showLoading.postValue(true)
//            val recipe = recipeUseCase.getRecipe(foodRecord.id)
            _recipeInfo.postValue(customRecipe to isUpdateLog)
            _showLoading.postValue(false)
        }
    }

    fun editCustomFromLoggedFood(isUpdateLog: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _showLoading.postValue(true)
//            val customFood = customFoodUseCase.fetchCustomFood(foodRecord.id)
            _customFoodInfo.postValue(customFood to isUpdateLog)
            _showLoading.postValue(false)
        }
    }

    fun navigateToDiary() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(EditFoodFragmentDirections.editToDiary())
        }

    }

    fun navigateToNutritionInfo(): FoodRecord {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(EditFoodFragmentDirections.editToNutritionInfo())
        }
        return editFoodDataModel.foodRecord

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

    fun navigateToEditIngredient() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(EditFoodFragmentDirections.editToEditIngredient())
        }

    }


    fun isEditLogMode(): Boolean {
        return isEditLogMode
    }

    fun getFoodRecord(): FoodRecord {
        return editFoodDataModel.foodRecord
    }

    fun getIngredient(index: Int) = editFoodDataModel.foodRecord.ingredients[index]

    fun editIngredient(ingredient: Pair<FoodRecordIngredient?, Int>) {
        val editIngredient = ingredient.first
        val indexToEdit = ingredient.second
        if (editIngredient == null && indexToEdit != -1) {
            editFoodDataModel.foodRecord.removeIngredient(indexToEdit)
        } else if (editIngredient != null && indexToEdit != -1) {
            editFoodDataModel.foodRecord.replaceIngredient(editIngredient, indexToEdit)
        } else if (editIngredient != null) {
            editFoodDataModel.foodRecord.addIngredient(editIngredient)
        }
        _internalUpdate.postValue(editFoodDataModel.foodRecord to EditFoodFragment.UpdateOrigin.INGREDIENT)
    }

    private fun checkFavStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            isFav = favoriteUseCase.isFavorite(editFoodDataModel.foodRecord)
            _isFavEvent.postValue(isFav)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = if (!isFav) {
                favoriteUseCase.markFavorite(editFoodDataModel.foodRecord)
            } else {
                favoriteUseCase.markUnfavorite(editFoodDataModel.foodRecord)
            }
            if (result) {
                isFav = !isFav
                _isFavEvent.postValue(isFav)
            }
        }
    }

    fun saveFavorite() {
        viewModelScope.launch(Dispatchers.IO) {
            _showLoading.postValue(true)
            val result = favoriteUseCase.markFavorite(editFoodDataModel.foodRecord)
            if (result) {
                isFav = !isFav
                _isFavEvent.postValue(isFav)
                navigateBack()
            }
            _showLoading.postValue(false)
        }
    }

    fun isCustomFood(): Boolean {
        if (::editFoodDataModel.isInitialized) {
            return editFoodDataModel.isUserFood
        }
        return false
    }

    fun isUserRecipe(): Boolean {
        if (::editFoodDataModel.isInitialized) {
            return editFoodDataModel.isUserRecipe
        }
        return false
    }


}