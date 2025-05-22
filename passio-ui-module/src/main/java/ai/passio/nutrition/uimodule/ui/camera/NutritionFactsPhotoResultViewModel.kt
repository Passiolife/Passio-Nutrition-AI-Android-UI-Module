package ai.passio.nutrition.uimodule.ui.camera

import ai.passio.nutrition.uimodule.domain.foodimage.FoodImageUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.DEFAULT_NUTRITION_FACTS_LABEL
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
import ai.passio.nutrition.uimodule.ui.model.ImageFoodResult
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.nutrition.uimodule.ui.util.StringKT.isValid
import ai.passio.nutrition.uimodule.ui.util.generateImageID
import ai.passio.passiosdk.passiofood.Barcode
import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import ai.passio.passiosdk.passiofood.data.model.PassioFoodResultType
import ai.passio.passiosdk.passiofood.data.model.PassioIDEntityType
import ai.passio.passiosdk.passiofood.data.model.PassioNutrients
import ai.passio.passiosdk.passiofood.data.model.PassioServingSize
import ai.passio.passiosdk.passiofood.data.model.PassioServingUnit
import android.graphics.Bitmap
import androidx.lifecycle.LiveData

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NutritionFactsPhotoResultViewModel : BaseViewModel() {
    private val foodImageUseCase = FoodImageUseCase
    private var currentBitmap: Bitmap? = null
    private var scannedBarcode: Barcode? = null

    private val _isFetchingResult = SingleLiveEvent<Boolean>()
    val isFetchingResult: LiveData<Boolean> get() = _isFetchingResult

    private val _resultFoodInfoEvent = SingleLiveEvent<ImageFoodResult?>()
    val resultFoodInfoEvent: LiveData<ImageFoodResult?> get() = _resultFoodInfoEvent

    private val _addIngredientEvent = SingleLiveEvent<List<FoodRecordIngredient>>()
    val addIngredientEvent: LiveData<List<FoodRecordIngredient>> = _addIngredientEvent

    private var isAddIngredient = false


    fun setIsAddIngredient(isAddIngredient: Boolean) {
        this.isAddIngredient = isAddIngredient
    }

    fun getIsAddIngredient(): Boolean {
        return isAddIngredient
    }

    fun setImageBitmap(bitmap: Bitmap, barcode: Barcode?) {
        this.scannedBarcode = barcode
        currentBitmap = bitmap
        viewModelScope.launch(Dispatchers.IO) {
            fetchResult()
        }
    }

    private fun fetchResult() {
        viewModelScope.launch {

            if (currentBitmap != null) {
                _isFetchingResult.postValue(true)
                PassioSDK.instance.recognizeNutritionFactsRemote(currentBitmap!!) { result ->
                    viewModelScope.launch(Dispatchers.IO)
                    {
                        val imageFoodResult = if (result == null) {
                            null
                        } else {
                            val imgResult = ImageFoodResult(
                                record = FoodRecord(result).apply {
                                    if (!name.isValid()) {
                                        name = DEFAULT_NUTRITION_FACTS_LABEL
                                    }
                                    if (!iconId.isValid()) {
                                        val imgId = generateImageID()
                                        foodImageUseCase.updateUserFoodImage(imgId, currentBitmap!!)
                                        iconId = imgId
                                    }
                                },
                                resultType = PassioFoodResultType.NUTRITION_FACTS
                            )
                            if (imgResult.isNutritionFactsDataMissing()) {
                                null
                            } else {
                                imgResult
                            }
                        }
                        _resultFoodInfoEvent.postValue(imageFoodResult)
                        _isFetchingResult.postValue(false)
                    }

                }
            } else {
                _resultFoodInfoEvent.postValue(null)
            }
        }
    }

    fun getCustomFoodByEnterManually() {

        viewModelScope.launch(Dispatchers.IO) {
            val imgId = generateImageID()
            foodImageUseCase.updateUserFoodImage(imgId, currentBitmap!!)


            val foodRecord = FoodRecord().apply {
                entityType = PassioIDEntityType.barcode.value
                barcode = scannedBarcode ?: ""
                iconId = imgId
            }
            val passioNutrients = PassioNutrients(UnitMass())
            foodRecord.servingSizes.add(PassioServingSize())
            foodRecord.servingUnits.add(PassioServingUnit())
            foodRecord.selectedUnit = PassioServingUnit().unitName
            val fi = FoodRecordIngredient(foodRecord, passioNutrients)
            foodRecord.ingredients = mutableListOf(fi)

            _resultFoodInfoEvent.postValue(ImageFoodResult(
                record = foodRecord,
                resultType = PassioFoodResultType.BARCODE
            ))

        }
    }


    fun navigateToDiary() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(NutritionFactsPhotoResultFragmentDirections.nutritionFactsToDiary())
        }

    }

    fun navigateBackToRecipe() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(NutritionFactsPhotoResultFragmentDirections.backToEditRecipe())
        }
    }

    fun navigateBackToScanBarcode() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(NutritionFactsPhotoResultFragmentDirections.backToCamera())
        }
    }


}