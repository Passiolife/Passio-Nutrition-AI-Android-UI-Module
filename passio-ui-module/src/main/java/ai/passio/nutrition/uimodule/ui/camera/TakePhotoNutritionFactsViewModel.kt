package ai.passio.nutrition.uimodule.ui.camera

import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.passiosdk.passiofood.Barcode
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TakePhotoNutritionFactsViewModel : BaseViewModel() {
    private var barcode: Barcode? = null

    private val _addIngredientEvent = SingleLiveEvent<List<FoodRecordIngredient>>()
    val addIngredientEvent: LiveData<List<FoodRecordIngredient>> = _addIngredientEvent

    private var isAddIngredient = false


    fun setIsAddIngredient(isAddIngredient: Boolean) {
        this.isAddIngredient = isAddIngredient
    }

    fun getIsAddIngredient(): Boolean {
        return isAddIngredient
    }

    fun setBarcode(barcode: Barcode?) {
        this.barcode = barcode
    }

    fun getBarcode(): Barcode? {
        return barcode
    }

    fun navigateToBarcodeImageFoodResult() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(TakePhotoNutritionFactsFragmentDirections.takePhotoToBarcodeImageFoodResult())
        }

    }


}