package ai.passio.nutrition.uimodule.ui.activity

import ai.passio.nutrition.uimodule.data.Repository
import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.domain.user.UserProfileUseCase
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
import ai.passio.nutrition.uimodule.ui.model.UserProfile
import ai.passio.nutrition.uimodule.ui.model.WaterRecord
import ai.passio.nutrition.uimodule.ui.model.WeightRecord
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.passiosdk.passiofood.Barcode
import ai.passio.passiosdk.passiofood.PassioFoodDataInfo
import ai.passio.passiosdk.passiofood.nutritionfacts.PassioNutritionFacts
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

object UserCache {
    private lateinit var userProfile: UserProfile
    fun getProfile(): UserProfile {
        return if (::userProfile.isInitialized) {
            userProfile
        } else {
            UserProfile()
        }
    }

    fun setProfile(userProfile: UserProfile) {
        this.userProfile = userProfile
    }
}

class SharedViewModel : ViewModel() {

    private val _diaryCurrentDate = SingleLiveEvent<Date>()
    val diaryCurrentDate: LiveData<Date> get() = _diaryCurrentDate

    private val _nutritionFactsPair = SingleLiveEvent<Pair<PassioNutritionFacts, String>>()
    val nutritionFactsPair: LiveData<Pair<PassioNutritionFacts, String>> get() = _nutritionFactsPair

    private val _editCustomFood = SingleLiveEvent<FoodRecord>()
    val editCustomFood: LiveData<FoodRecord> get() = _editCustomFood

    private val _editRecipe = SingleLiveEvent<FoodRecord>()
    val editRecipe: LiveData<FoodRecord> get() = _editRecipe
    private val _editRecipeUpdateLog = SingleLiveEvent<FoodRecord>()
    val editRecipeUpdateLog: LiveData<FoodRecord> get() = _editRecipeUpdateLog

    private val _editFoodUpdateLog = SingleLiveEvent<FoodRecord>()
    val editFoodUpdateLog: LiveData<FoodRecord> get() = _editFoodUpdateLog

    private val _barcodeScanFoodRecord = SingleLiveEvent<Barcode>()
    val barcodeScanFoodRecord: LiveData<Barcode> get() = _barcodeScanFoodRecord

    private val _detailsFoodRecordLD = SingleLiveEvent<FoodRecord>()
    val detailsFoodRecordLD: LiveData<FoodRecord> get() = _detailsFoodRecordLD

    private val _editIngredientLD = SingleLiveEvent<Pair<FoodRecordIngredient, Int>>()
    val editIngredientLD: LiveData<Pair<FoodRecordIngredient, Int>> get() = _editIngredientLD

    private val _editIngredientToRecipeLD = SingleLiveEvent<Pair<FoodRecordIngredient?, Int>>()
    val editIngredientToRecipeLD: LiveData<Pair<FoodRecordIngredient?, Int>> get() = _editIngredientToRecipeLD

    private val _addFoodIngredientLD = SingleLiveEvent<FoodRecordIngredient>()
    val addFoodIngredientLD: LiveData<FoodRecordIngredient> get() = _addFoodIngredientLD

    private val _addFoodIngredientsLD = SingleLiveEvent<FoodRecord>()
    val addFoodIngredientsLD: LiveData<FoodRecord> get() = _addFoodIngredientsLD

    private val _addMultipleIngredientsLD = SingleLiveEvent<List<FoodRecordIngredient>>()
    val addMultipleIngredientsLD: LiveData<List<FoodRecordIngredient>> get() = _addMultipleIngredientsLD

    private val _isAddIngredientFromSearchLD = SingleLiveEvent<Boolean>()
    val isAddIngredientFromSearchLD: LiveData<Boolean> get() = _isAddIngredientFromSearchLD

    private val _isAddIngredientFromScanningLD = SingleLiveEvent<Boolean>()
    val isAddIngredientFromScanningLD: LiveData<Boolean> get() = _isAddIngredientFromScanningLD

    private val _isAddIngredientFromVoiceLD = SingleLiveEvent<Boolean>()
    val isAddIngredientFromVoiceLD: LiveData<Boolean> get() = _isAddIngredientFromVoiceLD

    private val _editSearchResultLD = SingleLiveEvent<PassioFoodDataInfo>()
    val editSearchResultLD: LiveData<PassioFoodDataInfo> get() = _editSearchResultLD


    private val _nutritionInfoFoodRecordLD = SingleLiveEvent<FoodRecord>()
    val nutritionInfoFoodRecordLD: LiveData<FoodRecord> get() = _nutritionInfoFoodRecordLD


    private val _addWeightLD = SingleLiveEvent<WeightRecord>()
    val addWeightLD: LiveData<WeightRecord> get() = _addWeightLD

    private val _addWaterLD = SingleLiveEvent<WaterRecord>()
    val addWaterLD: LiveData<WaterRecord> get() = _addWaterLD

    private val _photoFoodResultLD = SingleLiveEvent<List<Bitmap>>()
    val photoFoodResultLD: LiveData<List<Bitmap>> get() = _photoFoodResultLD

    private val userProfileCase = UserProfileUseCase

    private val _userProfileCacheEvent = SingleLiveEvent<ResultWrapper<UserProfile>>()
    val userProfileCacheEvent: LiveData<ResultWrapper<UserProfile>> get() = _userProfileCacheEvent


    init {
//        preCacheUserProfile()
        checkAndMigrateDataFromOldDB()
    }

    private fun checkAndMigrateDataFromOldDB() {
        viewModelScope.launch(Dispatchers.IO) {
            val repository = Repository.getInstance()
            val isDone = repository.migrateDataFromOldSharedPrefsPassioConnector()
            if (isDone) {
                preCacheUserProfile()
            }
        }
    }

    private fun preCacheUserProfile() {
        viewModelScope.launch {
            val userProfile = userProfileCase.getUserProfile()
            UserCache.setProfile(userProfile)
            _userProfileCacheEvent.postValue(ResultWrapper.Success(userProfile))
        }
    }


    fun sendNutritionFactsToFoodCreator(nutritionFacts: Pair<PassioNutritionFacts, String>) {
        _nutritionFactsPair.postValue(nutritionFacts)
    }

    fun editCustomFood(foodRecord: FoodRecord) {
        _editCustomFood.postValue(foodRecord)
    }

    fun editRecipe(foodRecord: FoodRecord) {
        _editRecipe.postValue(foodRecord)
    }

    fun editRecipeUpdateLog(foodRecord: FoodRecord) {
        _editRecipeUpdateLog.postValue(foodRecord)
    }

    fun editFoodUpdateLog(foodRecord: FoodRecord) {
        _editFoodUpdateLog.postValue(foodRecord)
    }


    fun sendBarcodeScanResult(barcode: Barcode) {
        _barcodeScanFoodRecord.postValue(barcode)
    }

    fun passToNutritionInfo(foodRecord: FoodRecord) {
        _nutritionInfoFoodRecordLD.postValue(foodRecord)
    }

    fun passToEdit(searchResult: PassioFoodDataInfo) {
        _editSearchResultLD.postValue(searchResult)
    }

    fun editIngredient(ingredient: FoodRecordIngredient, ingredientIndex: Int) {
        _editIngredientLD.postValue(ingredient to ingredientIndex)
    }

    fun editIngredient(ingredient: FoodRecordIngredient) {
        _editIngredientLD.postValue(ingredient to -1)
    }

    fun detailsFoodRecord(foodRecord: FoodRecord) {
        _detailsFoodRecordLD.postValue(foodRecord)
    }

    //to add ingredient from EditIngredient screen to Recipe screen. send ingredient to recipe screen
    fun addFoodIngredient(foodRecordIngredient: FoodRecordIngredient) {
        _addFoodIngredientLD.postValue(foodRecordIngredient)
    }

    fun updateFoodIngredientToRecipe(ingredient: FoodRecordIngredient?, index: Int) {
        _editIngredientToRecipeLD.postValue(ingredient to index)
    }

    //    send more then one ingredients to recipe screen to add to recipe
    fun addFoodIngredients(foodRecord: FoodRecord) {
        _addFoodIngredientsLD.postValue(foodRecord)
    }

    //    send more then one ingredients to recipe screen to add to recipe
    fun addFoodIngredients(ingredients: List<FoodRecordIngredient>) {
        _addMultipleIngredientsLD.postValue(ingredients)
    }

    fun setIsAddIngredientFromSearch(isAddIngredient: Boolean) {
        _isAddIngredientFromSearchLD.postValue(isAddIngredient)
    }

    fun setIsAddIngredientFromScanning(isAddIngredient: Boolean) {
        _isAddIngredientFromScanningLD.postValue(isAddIngredient)
    }

    fun setIsAddIngredientUsingVoice(isAddIngredient: Boolean) {
        _isAddIngredientFromVoiceLD.postValue(isAddIngredient)
    }

    fun addEditWeight(weightRecord: WeightRecord) {
        _addWeightLD.postValue(weightRecord)
    }

    fun addEditWater(waterRecord: WaterRecord) {
        _addWaterLD.postValue(waterRecord)
    }

    fun addPhotoFoodResult(uris: List<Bitmap>) {
        _photoFoodResultLD.postValue(uris)
    }

    fun setDiaryDate(currentDate: Date) {
        _diaryCurrentDate.postValue(currentDate)
    }

}