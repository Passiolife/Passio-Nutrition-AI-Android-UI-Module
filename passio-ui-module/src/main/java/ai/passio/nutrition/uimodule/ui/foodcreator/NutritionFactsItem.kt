package ai.passio.nutrition.uimodule.ui.foodcreator

import ai.passio.passiosdk.passiofood.data.measurement.Converter
import ai.passio.passiosdk.passiofood.data.measurement.Grams
import ai.passio.passiosdk.passiofood.data.measurement.Unit
import ai.passio.passiosdk.passiofood.data.measurement.UnitEnergy
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import ai.passio.passiosdk.passiofood.data.model.PassioServingUnit

data class NutritionFactsItem(
    val id: String,
    val nutrientName: String,
    var unitSymbol: String,
    var value: Double = 0.0,
    var isAdded: Boolean = false
) {


    fun getUnitMass() = UnitMass(Grams, value)

    internal companion object {
        const val REF_CALORIES_ID = "refCalories"
        const val REF_FAT_ID = "refFat"
        const val REF_CARBS_ID = "refCarbs"
        const val REF_PROTEIN_ID = "refProtein"
        const val REF_SAT_FAT_ID = "refSatFat"
        const val REF_TRANS_FAT_ID = "refTransFat"
        const val REF_CHOLESTEROL_ID = "refCholesterol"
        const val REF_SODIUM_ID = "refSodium"
        const val REF_FIBERS_ID = "refFibers"
        const val REF_SUGARS_ID = "refSugars"
        const val REF_SUGARS_ADDED_ID = "refSugarsAdded"
        const val REF_VITAMIN_D_ID = "refVitaminD"
        const val REF_CALCIUM_ID = "refCalcium"
        const val REF_IRON_ID = "refIron"
        const val REF_POTASSIUM_ID = "refPotassium"
        const val REF_MAGNESIUM_ID = "refMagnesium"

        internal fun List<NutritionFactsItem>.setValue(id: String, value: Double) {
            val item = this.find { it.id == id }
            if (item != null && value > 0.0) {
                item.value = value
                item.isAdded = true
            }
        }

        internal fun List<NutritionFactsItem>.unitMassOf(id: String): UnitMass? {
            val item = this.find { it.id == id }
            if (item != null) {
                return UnitMass(Unit(Converter(), item.unitSymbol), item.value)
            }
            return null
        }

        internal fun List<NutritionFactsItem>.unitEnergyOf(id: String): UnitEnergy? {
            val item = this.find { it.id == id }
            if (item != null) {
                return UnitEnergy(Unit(Converter(), item.unitSymbol), item.value)
            }
            return null
        }

    }

}