package ai.passio.nutrition.uimodule.ui.model

import ai.passio.nutrition.uimodule.ui.util.StringKT.isValid
import ai.passio.passiosdk.passiofood.data.model.PassioFoodResultType

data class ImageFoodResult(
    var record: FoodRecord,
    var isSelected: Boolean = false,
    var isCustomFood: Boolean = false,
    val resultType: PassioFoodResultType
) {

    fun isFoodItemDataMissing(): Boolean {
        with(record) {
            return if (resultType == PassioFoodResultType.FOOD_ITEM /*|| entityType == PassioIDEntityType.packagedFoodCode.value*/) {
                (!name.isValid()
                        || nutrients().calories() == null
                        || nutrients().carbs() == null
                        || nutrients().protein() == null
                        || nutrients().fat() == null
                        || selectedQuantity < 0
                        || !selectedUnit.isValid()
                        || nutrients().weight == null)
            } else {
                false
            }
        }
    }
    fun isBarcodeDataMissing(): Boolean {
        with(record) {
            return if (resultType == PassioFoodResultType.BARCODE /*|| entityType == PassioIDEntityType.packagedFoodCode.value*/) {
                (!name.isValid()
                        || nutrients().calories() == null
                        || nutrients().carbs() == null
                        || nutrients().protein() == null
                        || nutrients().fat() == null
                        || selectedQuantity < 0
                        || !selectedUnit.isValid()
                        || nutrients().weight == null)
            } else {
                false
            }
        }
    }

    fun isNutritionFactsDataMissing(): Boolean {
        with(record) {
            return if (resultType == PassioFoodResultType.NUTRITION_FACTS) {
                (nutrients().calories() == null
                        || nutrients().carbs() == null
                        || nutrients().protein() == null
                        || nutrients().fat() == null
                        || selectedQuantity < 0
                        || !selectedUnit.isValid()
                        || nutrients().weight == null)
            } else {
                false
            }
        }

    }
}
