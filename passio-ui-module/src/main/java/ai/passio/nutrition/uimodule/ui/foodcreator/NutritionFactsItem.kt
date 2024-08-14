package ai.passio.nutrition.uimodule.ui.foodcreator

import ai.passio.passiosdk.passiofood.data.measurement.Grams
import ai.passio.passiosdk.passiofood.data.model.PassioServingUnit

data class NutritionFactsItem(
    val unitName: String,
    val passioServingUnit: PassioServingUnit,
    var isAdded: Boolean = false
) {

    companion object{
        internal const val servingSize = "Serving Size"

        fun listOfUnits()
        {

            Grams
        }
    }

}