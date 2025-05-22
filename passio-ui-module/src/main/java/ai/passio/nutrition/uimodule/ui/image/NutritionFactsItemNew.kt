package ai.passio.nutrition.uimodule.ui.image

import ai.passio.passiosdk.passiofood.data.measurement.Converter
import ai.passio.passiosdk.passiofood.data.measurement.KiloCalories
import ai.passio.passiosdk.passiofood.data.measurement.Unit
import ai.passio.passiosdk.passiofood.data.measurement.UnitEnergy
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass

data class NutritionFactsItemNew(
    val id: String,
    val nutrientName: String,
    var unitSymbol: String,
    var value: Double?,
    var isAdded: Boolean = false
)

internal fun List<NutritionFactsItemNew>.setValue(id: String, value: Double?) {
    val item = this.find { it.id == id }
    if (item != null/* value >= 0.0*/) {

        if (value != null) {
            item.value = value
            item.isAdded = true
        } else {
            item.isAdded = false
        }
    }
}

internal fun List<NutritionFactsItemNew>.unitMassOf(id: String): UnitMass? {
    val item = this.find { it.id == id }
    if (item != null && item.value != null) {
//                return UnitMass(Unit(Converter(), item.unitSymbol), item.value)
        val unit =
            Unit.unitFromString(item.unitSymbol) ?: Unit(Converter(), item.unitSymbol)
        return UnitMass(unit, item.value!!)
    }
    return null
}

internal fun List<NutritionFactsItemNew>.unitEnergyOf(id: String): UnitEnergy? {
    val item = this.find { it.id == id }
    if (item != null && item.value != null) {
//                return UnitEnergy(Unit(Converter(), item.unitSymbol), item.value)
        return UnitEnergy(KiloCalories, item.value!!)
    }
    return null
}

internal data class NutritionFactsValidator(
    val isValidName: Boolean,
    val isValidCalories: Boolean,
    val isValidCarbs: Boolean,
    val isValidProteins: Boolean,
    val isValidFat: Boolean,
    val isValidWeight: Boolean,
    val isValidServing: Boolean,
    val isValidServingUnit: Boolean
) {

    fun isAllDataValid(): Boolean {
        val isAllValid =
            isValidName && isValidCalories && isValidCarbs && isValidProteins && isValidFat && isValidWeight && isValidServing && isValidServingUnit
        return isAllValid
    }
}