package ai.passio.nutrition.uimodule.ui.model

import ai.passio.passiosdk.passiofood.data.measurement.Grams
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import ai.passio.passiosdk.passiofood.data.model.PassioFoodAmount
import ai.passio.passiosdk.passiofood.data.model.PassioServingSize
import ai.passio.passiosdk.passiofood.data.model.PassioServingUnit
import ai.passio.passiosdk.passiofood.data.model.PassioFoodItem
import ai.passio.passiosdk.passiofood.data.model.PassioNutrients
import java.util.Locale
import java.util.UUID

private const val TIMESTAMP_1970 = 978300000

class FoodRecord() {
    var id: String = ""
    var name: String = ""
    var additionalData: String = ""
    var iconId: String = ""

    lateinit var ingredients: MutableList<FoodRecordIngredient>

    private var selectedUnit: String = ""
    private var selectedQuantity: Double = 0.0
    val servingSizes = mutableListOf<PassioServingSize>()
    val servingUnits = mutableListOf<PassioServingUnit>()

    var mealLabel: MealLabel? = null
    val uuid: String = UUID.randomUUID().toString().toUpperCase(Locale.ROOT)//null
    var createdAt: Long? = null

    var openFoodLicense: String? = null

    companion object {
        const val ZERO_QUANTITY = 0.00001
    }

    constructor(ingredient: FoodRecordIngredient) : this() {
        id = ingredient.id
        name = ingredient.name
        iconId = ingredient.iconId
        servingSizes.addAll(ingredient.servingSizes)
        servingUnits.addAll(ingredient.servingUnits)
        selectedUnit = ingredient.selectedUnit
        selectedQuantity = ingredient.selectedQuantity
        ingredients = mutableListOf()
        ingredients.add(ingredient)
        openFoodLicense = ingredient.openFoodLicense
    }

    constructor(foodItem: PassioFoodItem) : this() {
        id = foodItem.id
        name = foodItem.name
        additionalData = foodItem.details
        iconId = foodItem.iconId
        servingSizes.addAll(foodItem.amount.servingSizes)
        servingUnits.addAll(foodItem.amount.servingUnits)
        selectedUnit = foodItem.amount.selectedUnit
        selectedQuantity = foodItem.amount.selectedQuantity
        ingredients = foodItem.ingredients.map { FoodRecordIngredient(it) }.toMutableList()
        calculateQuantityForIngredients()
    }

    fun addIngredient(record: FoodRecord, index: Int? = null) {
        if (ingredients.size == 1) {
            name = "Recipe with ${ingredients.first().name}"
        }
        if (record.ingredients.size == 1) {
            ingredients.add(index ?: ingredients.size, FoodRecordIngredient(record))
        } else {
            ingredients.addAll(index ?: ingredients.size, record.ingredients)
        }
        setUnitToServing()
    }

    private fun setUnitToServing() {
        val weight = ingredientWeight()
        val currentServing = servingUnits.find { it.unitName == PassioFoodAmount.SERVING_UNIT_NAME }
        if (currentServing != null) {
            servingUnits.remove(currentServing)
        }
        val serving = PassioServingUnit(PassioFoodAmount.SERVING_UNIT_NAME, weight)
        servingUnits.add(0, serving)

        servingUnits.removeAll {
            it.unitName !in arrayOf(Grams.unitName, PassioFoodAmount.SERVING_UNIT_NAME)
        }
        servingSizes.removeAll { ss ->
            ss.unitName !in servingUnits.map { it.unitName }
        }

        selectedQuantity = 1.0
        selectedUnit = PassioFoodAmount.SERVING_UNIT_NAME
    }

    fun removeIngredient(index: Int): Boolean {
        if (index >= ingredients.size || ingredients.size == 1) {
            return false
        }
        ingredients.removeAt(index)
        if (ingredients.size == 1) {
            servingUnits.clear()
            servingUnits.addAll(ingredients[0].servingUnits)
            servingSizes.clear()
            servingSizes.addAll(ingredients[0].servingSizes)
            selectedUnit = ingredients[0].selectedUnit
            selectedQuantity = ingredients[0].selectedQuantity
            name = ingredients[0].name
            additionalData = ingredients[0].additionalData
            iconId = ingredients[0].iconId
            calculateQuantity()
        } else {
            setUnitToServing()
        }
        return true
    }

    fun replaceIngredient(newIngredient: FoodRecord, index: Int): Boolean {
        if (index >= ingredients.size) {
            return false
        }

        removeIngredient(index)
        addIngredient(newIngredient, index)
        return true
    }

    private fun calculateQuantity() {
        val weight = ingredientWeight().gramsValue()
        val unitWeight = servingUnits.first { it.unitName == selectedUnit }.weight.gramsValue()
        selectedQuantity = weight / unitWeight
    }

    fun nutrients(): PassioNutrients {
        val currentWeight = ingredientWeight()
        val ingredientNutrients = ingredients.map { ingredient ->
            Pair(ingredient.referenceNutrients, ingredient.servingWeight() / currentWeight)
        }
        return PassioNutrients(ingredientNutrients, currentWeight)
    }

    fun getSelectedQuantity(): Double = selectedQuantity

    fun setSelectedQuantity(quantity: Double) {
        if (selectedQuantity == quantity) return

        selectedQuantity = if (quantity != 0.0) quantity else ZERO_QUANTITY
        calculateQuantityForIngredients()
    }

    fun getSelectedUnit(): String = selectedUnit

    fun setSelectedUnit(unit: String): Boolean {
        if (selectedUnit == unit) return true

        if (servingUnits.firstOrNull { it.unitName == unit } == null) return false

        selectedUnit = unit
        calculateQuantityForIngredients()
        return true
    }

    fun setSelectedUnitKeepWeight(unit: String): Boolean {
        if (selectedUnit == unit) return true

        val servingWeight = servingUnits.firstOrNull { it.unitName == unit }?.weight ?: return false

        selectedUnit = unit
        selectedQuantity = ingredientWeight() / servingWeight
        return true
    }

    fun create(time: Long?) {
        if (time == null) {
            createdAt = null
            return
        }

//        uuid = UUID.randomUUID().toString().toUpperCase(Locale.ROOT)
        createdAt = time / 1000L - TIMESTAMP_1970
    }

    fun createdAtTime(): Long? {
        if (createdAt == null) return null
        return (createdAt!! + TIMESTAMP_1970) * 1000L
    }

    private fun calculateQuantityForIngredients() {
        val ratio = servingWeight().gramsValue() / ingredientWeight().gramsValue()
        ingredients.forEach {
            it.selectedQuantity *= ratio
        }
    }

    fun ingredientWeight(): UnitMass {
        return ingredients.map { it.servingWeight() }.reduce { acc, unitMass -> acc + unitMass }
    }

    fun servingWeight(): UnitMass {
        val selectedUnit =
            servingUnits.firstOrNull { it.unitName == selectedUnit } ?: return UnitMass(Grams, 0.0)
        return selectedUnit.weight * selectedQuantity
    }

    fun nutrientsSelectedSize(): PassioNutrients {
        val currentWeight = servingWeight()
        val ingredientNutrients = ingredients.map { ingredient ->
            Pair(ingredient.referenceNutrients, ingredient.servingWeight() / currentWeight)
        }
        return PassioNutrients(ingredientNutrients, currentWeight)
    }

    fun nutrientsReference(): PassioNutrients {
        val currentWeight = ingredientWeight()
        val ingredientNutrients = ingredients.map { ingredient ->
            Pair(ingredient.referenceNutrients, ingredient.servingWeight() / currentWeight)
        }
        return PassioNutrients(ingredientNutrients, UnitMass(Grams, 100.0))
    }
}

fun List<FoodRecord>.meals(mealLabel: MealLabel): List<FoodRecord> {
    return this.filter {
        val mealLabelTemp =
            it.mealLabel ?: MealLabel.dateToMealLabel(it.createdAt ?: System.currentTimeMillis())
        mealLabelTemp == mealLabel
    }
}

fun List<FoodRecord>.caloriesSum(): Double {
    return map { it.nutrientsSelectedSize().calories()?.value ?: 0.0 }.reduce { acc, d -> acc + d }
}

fun List<FoodRecord>.carbsSum(): Double {
    return map { it.nutrientsSelectedSize().carbs()?.value ?: 0.0 }.reduce { acc, d -> acc + d }
}

fun List<FoodRecord>.proteinSum(): Double {
    return map { it.nutrientsSelectedSize().protein()?.value ?: 0.0 }.reduce { acc, d -> acc + d }
}

fun List<FoodRecord>.fatSum(): Double {
    return map { it.nutrientsSelectedSize().fat()?.value ?: 0.0 }.reduce { acc, d -> acc + d }
}