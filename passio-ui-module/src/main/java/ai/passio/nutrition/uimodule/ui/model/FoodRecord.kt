package ai.passio.nutrition.uimodule.ui.model

import ai.passio.nutrition.uimodule.ui.util.StringKT.isValid
import ai.passio.passiosdk.passiofood.Barcode
import ai.passio.passiosdk.passiofood.PackagedFoodCode
import ai.passio.passiosdk.passiofood.data.measurement.Grams
import ai.passio.passiosdk.passiofood.data.measurement.Milliliters
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import ai.passio.passiosdk.passiofood.data.model.PassioFoodAmount
import ai.passio.passiosdk.passiofood.data.model.PassioServingSize
import ai.passio.passiosdk.passiofood.data.model.PassioServingUnit
import ai.passio.passiosdk.passiofood.data.model.PassioFoodItem
import ai.passio.passiosdk.passiofood.data.model.PassioIDEntityType
import ai.passio.passiosdk.passiofood.data.model.PassioNutrients
import android.util.Log
import com.google.gson.GsonBuilder
import java.util.Locale
import java.util.UUID

private const val TIMESTAMP_1970 = 978300000
private const val CUSTOM_FOOD_PREFIX = "custom_food_"
private const val FOOD_RECIPE_PREFIX = "food_recipe_"

open class FoodRecord() {
    var id: String = ""
    var name: String = ""
    var additionalData: String = ""
    var iconId: String = ""
    var foodImagePath: String? = null
    var passioIDEntityType = PassioIDEntityType.item.value

    var ingredients: MutableList<FoodRecordIngredient> = mutableListOf()

    private var selectedUnit: String = ""
    private var selectedQuantity: Double = 0.0
    val servingSizes = mutableListOf<PassioServingSize>()
    val servingUnits = mutableListOf<PassioServingUnit>()

    var mealLabel: MealLabel? = null
    var uuid: String = UUID.randomUUID().toString().uppercase(Locale.ROOT)//null
    var createdAt: Long? = null

    var openFoodLicense: String? = null
    var barcode: Barcode? = null
    var packagedFoodCode: PackagedFoodCode? = null

    companion object {
        const val ZERO_QUANTITY = 0.00001
    }

    //custom food
    constructor(
        productName: String,
        brandName: String,
        barcode: String?,
        servingWeight: Double,
        servingUnit: String,
        weightInGrams: Double,
        weightInGramsUnit: String,
        passioNutrients: PassioNutrients,
        passioIDEntityType: PassioIDEntityType = PassioIDEntityType.item,
        foodImagePath: String? = null
    ) : this() {

//        this.id = "${CUSTOM_FOOD_PREFIX}${UUID.randomUUID().toString().uppercase(Locale.ROOT)}"
        this.uuid = "${CUSTOM_FOOD_PREFIX}${UUID.randomUUID().toString().uppercase(Locale.ROOT)}"
        this.name = productName
        this.additionalData = brandName
        this.barcode = barcode
        this.passioIDEntityType = passioIDEntityType.value
        this.foodImagePath = foodImagePath

        val gramUnit =
            if (weightInGramsUnit.equals(Milliliters.symbol, true)) Milliliters else Grams
        val gramUnitName = if (weightInGramsUnit.equals(
                Milliliters.symbol,
                true
            )
        ) Milliliters.symbol else Grams.unitName
//        iconId = foodItem.iconId

        if (!(servingUnit.equals(Grams.unitName, true) || servingUnit.equals(
                Grams.symbol,
                true
            ) || servingUnit.equals(Milliliters.symbol, true))
        ) {
            servingSizes.add(PassioServingSize(weightInGrams, gramUnitName)) //g or ml
            servingUnits.add(PassioServingUnit(gramUnitName, UnitMass(gramUnit, 1.0)))
        }

        servingSizes.add(PassioServingSize(servingWeight, servingUnit))


        servingUnits.add(
            PassioServingUnit(
                servingUnit,
                UnitMass(Grams, weightInGrams / servingWeight)
            )
        )
        selectedUnit = gramUnitName //foodItem.amount.selectedUnit
        selectedQuantity = weightInGrams //foodItem.amount.selectedQuantity
        ingredients = mutableListOf(FoodRecordIngredient(this, passioNutrients))
    }

    fun editCustomFood(
        productName: String,
        brandName: String,
        barcode: String?,
        servingWeight: Double,
        servingUnit: String,
        weightInGrams: Double,
        weightInGramsUnit: String,
        passioNutrients: PassioNutrients,
        passioIDEntityType: PassioIDEntityType = PassioIDEntityType.item,
        foodImagePath: String? = null
    ): FoodRecord {
        this.name = productName
        this.additionalData = brandName
        this.barcode = barcode
        this.passioIDEntityType = passioIDEntityType.value
        this.foodImagePath = foodImagePath

        val gramUnit =
            if (weightInGramsUnit.equals(Milliliters.symbol, true)) Milliliters else Grams
        val gramUnitName = if (weightInGramsUnit.equals(
                Milliliters.symbol,
                true
            )
        ) Milliliters.symbol else Grams.unitName
//        iconId = foodItem.iconId

        if (!(servingUnit.equals(Grams.unitName, true) || servingUnit.equals(
                Grams.symbol,
                true
            ) || servingUnit.equals(Milliliters.symbol, true))
        ) {
            servingSizes.add(PassioServingSize(weightInGrams, gramUnitName)) //g or ml
            servingUnits.add(PassioServingUnit(gramUnitName, UnitMass(gramUnit, 1.0)))
        }

        servingSizes.add(PassioServingSize(servingWeight, servingUnit))


        servingUnits.add(
            PassioServingUnit(
                servingUnit,
                UnitMass(Grams, weightInGrams / servingWeight)
            )
        )
        selectedUnit = gramUnitName //foodItem.amount.selectedUnit
        selectedQuantity = weightInGrams //foodItem.amount.selectedQuantity
        ingredients = mutableListOf(FoodRecordIngredient(this, passioNutrients))
        return this
    }


    constructor(
        ingredient: FoodRecordIngredient,
        passioIDEntityType: PassioIDEntityType = PassioIDEntityType.item
    ) : this() {
        id = ingredient.id
        name = ingredient.name
        iconId = ingredient.iconId
        this.passioIDEntityType = passioIDEntityType.value
        servingSizes.addAll(ingredient.servingSizes)
        servingUnits.addAll(ingredient.servingUnits)
        selectedUnit = ingredient.selectedUnit
        selectedQuantity = ingredient.selectedQuantity
        ingredients = mutableListOf()
        ingredients.add(ingredient)
        openFoodLicense = ingredient.openFoodLicense
    }

    constructor(
        foodItem: PassioFoodItem,
        passioIDEntityType: PassioIDEntityType = PassioIDEntityType.item
    ) : this() {
        id = foodItem.id
        name = foodItem.name
        additionalData = foodItem.details
        iconId = foodItem.iconId
        this.passioIDEntityType = passioIDEntityType.value
        servingSizes.addAll(foodItem.amount.servingSizes)
        servingUnits.addAll(foodItem.amount.servingUnits)
        selectedUnit = foodItem.amount.selectedUnit
        selectedQuantity = foodItem.amount.selectedQuantity
        openFoodLicense = foodItem.isOpenFood()
        Log.d("foodItem.isOpenFood()", "===foodItem.isOpenFood(): ${foodItem.isOpenFood()}")
        ingredients = foodItem.ingredients.map { FoodRecordIngredient(it) }.toMutableList()
        calculateQuantityForIngredients()
    }

    fun isCustomFood(): Boolean {
//        return id.startsWith(CUSTOM_FOOD_PREFIX)
        return uuid.startsWith(CUSTOM_FOOD_PREFIX)
    }

    fun isRecipe(): Boolean {
        return isUserRecipe() || isPassioRecipe()
    }

    fun isUserRecipe(): Boolean {
        return uuid.startsWith(FOOD_RECIPE_PREFIX)
    }

    fun isPassioRecipe(): Boolean {
        return ingredients.size > 1 && !uuid.startsWith(FOOD_RECIPE_PREFIX)
    }

    fun addIngredient(record: FoodRecord, index: Int? = null) {
        if (ingredients.size == 1) {
            name = "Recipe with ${ingredients.first().name}"
        }
        if (record.ingredients.size == 1) {
            ingredients.add(index ?: ingredients.size, FoodRecordIngredient(record))
//            ingredients.add(index ?: ingredients.size, record.ingredients.first())
        } else {
            ingredients.addAll(index ?: ingredients.size, record.ingredients)
        }
        if (!name.isValid()) {
            name = "Recipe with ${ingredients.firstOrNull()?.name ?: ""}"
        }
        if (!foodImagePath.isValid() && iconId.isValid()) {
            iconId = record.iconId
            passioIDEntityType = record.passioIDEntityType
        }
        setUnitToServing()
    }

    fun addIngredient(record: FoodRecordIngredient, index: Int? = null) {
        ingredients.add(index ?: ingredients.size, record)
        if (!name.isValid()) {
            name = "Recipe with ${ingredients.firstOrNull()?.name ?: ""}"
        }
        if (!foodImagePath.isValid() && iconId.isValid()) {
            iconId = record.iconId
            passioIDEntityType = PassioIDEntityType.item.value
        }
//        ingredients.add(index ?: ingredients.size, record)
        setUnitToServing()
    }

    fun setUnitToServing() {
        if (ingredients.isEmpty())
            return
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
    fun replaceIngredient(newIngredient: FoodRecordIngredient, index: Int): Boolean {
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
        if (ingredients.isNotEmpty()) {
            calculateQuantityForIngredients()
        }
    }

    fun getSelectedUnit(): String = selectedUnit

    fun setSelectedUnit(unit: String): Boolean {
        if (selectedUnit == unit) return true

        if (servingUnits.firstOrNull { it.unitName.equals(unit, true) } == null) return false

        selectedUnit = unit

        selectedQuantity = if (selectedUnit.equals(Grams.unitName, true) || selectedUnit.equals(
                Milliliters.symbol,
                true
            )
        ) {
            100.0
        } else {
            1.0
        }
        if (ingredients.isNotEmpty()) {
            calculateQuantityForIngredients()
        }
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
            it.mealLabel ?: MealLabel.dateToMealLabel(it.createdAtTime() ?: System.currentTimeMillis())
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

fun FoodRecord.copy(): FoodRecord {
    val gson = GsonBuilder().create()
    return gson.fromJson(gson.toJson(this), FoodRecord::class.java)
        .apply {
            id = if (uuid.isValid()) uuid else UUID.randomUUID().toString().uppercase(Locale.ROOT)
            uuid = if (isCustomFood()) {
                "${CUSTOM_FOOD_PREFIX}${UUID.randomUUID().toString().uppercase(Locale.ROOT)}"
            } else if (isUserRecipe()) {
                "${FOOD_RECIPE_PREFIX}${UUID.randomUUID().toString().uppercase(Locale.ROOT)}"
            } else {
                UUID.randomUUID().toString().uppercase(Locale.ROOT)
            }
        }
}

fun FoodRecord.clone(): FoodRecord {
    val gson = GsonBuilder().create()
    return gson.fromJson(gson.toJson(this), FoodRecord::class.java)
}

fun FoodRecord.copyAsCustomFood(): FoodRecord {
    val gson = GsonBuilder().create()
    return gson.fromJson(gson.toJson(this), FoodRecord::class.java)
        .apply {
            uuid = "${CUSTOM_FOOD_PREFIX}${UUID.randomUUID().toString().uppercase(Locale.ROOT)}"
//            id = "${CUSTOM_FOOD_PREFIX}${UUID.randomUUID().toString().uppercase(Locale.ROOT)}"
        }
}

fun FoodRecord.copyAsRecipe(): FoodRecord {
    val gson = GsonBuilder().create()
    return gson.fromJson(gson.toJson(this), FoodRecord::class.java)
        .apply {
            uuid = "${FOOD_RECIPE_PREFIX}${UUID.randomUUID().toString().uppercase(Locale.ROOT)}"
//            if (!id.isValid()) {
//                id = uuid
//            }
        }
}
