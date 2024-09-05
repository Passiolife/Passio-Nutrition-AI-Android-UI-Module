package ai.passio.nutrition.uimodule.ui.model

import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import ai.passio.passiosdk.passiofood.data.model.PassioServingSize
import ai.passio.passiosdk.passiofood.data.model.PassioServingUnit
import ai.passio.passiosdk.passiofood.data.model.PassioIngredient
import ai.passio.passiosdk.passiofood.data.model.PassioNutrients

class FoodRecordIngredient {

    var id: String = ""
    var name: String = ""
    var additionalData: String = ""
    var iconId: String = ""

    var selectedUnit: String = ""
    var selectedQuantity: Double = 0.0
    var servingSizes: List<PassioServingSize>
    var servingUnits: List<PassioServingUnit>
    internal var referenceNutrients: PassioNutrients

    var openFoodLicense: String? = null

    //custom food
    constructor(foodRecord: FoodRecord, passioNutrients: PassioNutrients) {
        id = foodRecord.id
        name = foodRecord.name
        additionalData = foodRecord.additionalData
        iconId = foodRecord.iconId

        selectedUnit = foodRecord.getSelectedUnit()
        selectedQuantity = foodRecord.getSelectedQuantity()
        servingSizes = foodRecord.servingSizes
        servingUnits = foodRecord.servingUnits

        referenceNutrients = passioNutrients
        openFoodLicense = foodRecord.openFoodLicense
    }
    constructor(foodRecord: FoodRecord) {
        id = foodRecord.id
        name = foodRecord.name
        additionalData = foodRecord.additionalData
        iconId = foodRecord.iconId

        selectedUnit = foodRecord.getSelectedUnit()
        selectedQuantity = foodRecord.getSelectedQuantity()
        servingSizes = foodRecord.servingSizes
        servingUnits = foodRecord.servingUnits

        referenceNutrients = foodRecord.nutrients()
        openFoodLicense = foodRecord.openFoodLicense
    }

    constructor(ingredient: PassioIngredient) {
        id = ingredient.id
        name = ingredient.name
        iconId = ingredient.iconId
        servingSizes = ingredient.amount.servingSizes
        servingUnits = ingredient.amount.servingUnits
        selectedUnit = ingredient.amount.selectedUnit
        selectedQuantity = ingredient.amount.selectedQuantity
        referenceNutrients = ingredient.referenceNutrients
        openFoodLicense =
            ingredient.metadata.foodOrigins?.firstOrNull { it.source == "openfood" }?.licenseCopy
    }

    fun servingWeight(): UnitMass {
        val unitWeight = servingUnits.first { it.unitName == selectedUnit }.weight
        return unitWeight * selectedQuantity
    }

    fun nutrientsSelectedSize(): PassioNutrients {
        return PassioNutrients(referenceNutrients, servingWeight())
    }

    fun nutrientsReference(): PassioNutrients = referenceNutrients
}