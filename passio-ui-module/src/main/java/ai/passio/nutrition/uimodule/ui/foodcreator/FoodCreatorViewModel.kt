package ai.passio.nutrition.uimodule.ui.foodcreator

import ai.passio.nutrition.uimodule.ui.base.BaseViewModel

class FoodCreatorViewModel : BaseViewModel() {

//    val passioServingUnit: PassioServingUnit = PassioServingUnit(quantity = 0.0, unitName = "")
    val nutritionFactsItems = mutableListOf<NutritionFactsItem>()

    private var servingQuantity: Double = 0.0
    private var servingUnit: String = ""
    private var servingWeight: Double = 0.0
    private var servingWeightUnit: String = ""

    fun setServingSize(servingQuantity: Double) {
        this.servingQuantity = servingQuantity
    }

    fun setServingUnit(servingUnit: String) {
        this.servingUnit = servingUnit
    }
    fun setServingWeight(servingWeight: Double) {
        this.servingWeight = servingWeight
    }

    fun setServingWeightUnit(servingWeightUnit: String) {
        this.servingWeightUnit = servingWeightUnit
    }


    fun navigateToTakePhoto() {
        navigate(FoodCreatorFragmentDirections.foodCreatorToTakePhoto())
    }

}