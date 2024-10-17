package ai.passio.nutrition.uimodule.data.db.mapper

import ai.passio.nutrition.uimodule.data.db.entity.CustomRecipeEntity
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.MealLabel

internal fun List<CustomRecipeEntity>.toFoodRecords(): List<FoodRecord> {
    val foodLogEntities = this
    return foodLogEntities.map { foodLogEntity -> foodLogEntity.toFoodRecord() }
}

internal fun FoodRecord.toCustomRecipeEntity(): CustomRecipeEntity {
    val foodRecord = this
    return CustomRecipeEntity(
        uuid = foodRecord.uuid,
        id = foodRecord.id,
        name = foodRecord.name,
        additionalData = foodRecord.additionalData,
        iconId = foodRecord.iconId,
        foodImagePath = foodRecord.foodImagePath,
        passioIDEntityType = foodRecord.passioIDEntityType,
        selectedUnit = foodRecord.getSelectedUnit(),
        selectedQuantity = foodRecord.getSelectedQuantity(),
        mealLabel = foodRecord.mealLabel?.value, // Convert MealLabel enum to its string value
        createdAt = foodRecord.createdAt,
        openFoodLicense = foodRecord.openFoodLicense,
        barcode = foodRecord.barcode, // Convert barcode to String (handle this conversion properly)
        packagedFoodCode = foodRecord.packagedFoodCode, // Convert packaged food code to String

        ingredients = foodRecord.ingredients.map { ingredient ->
            ingredient.toFoodLogIngredientEntity()
        }.toMutableList(), // Map FoodRecordIngredient to FoodLogIngredientEntity

        servingSizes = foodRecord.servingSizes.toMutableList(),
        servingUnits = foodRecord.servingUnits.toMutableList()
    )
}

internal fun CustomRecipeEntity.toFoodRecord(): FoodRecord {
    val foodLogEntity = this
    return FoodRecord().apply {
        uuid = foodLogEntity.uuid
        id = foodLogEntity.id
        name = foodLogEntity.name
        additionalData = foodLogEntity.additionalData
        iconId = foodLogEntity.iconId
        foodImagePath = foodLogEntity.foodImagePath
        passioIDEntityType = foodLogEntity.passioIDEntityType
        selectedUnit = foodLogEntity.selectedUnit
        selectedQuantity = foodLogEntity.selectedQuantity
        mealLabel = foodLogEntity.mealLabel?.let { label ->
            MealLabel.values().find { it.value == label }
        } // Convert String back to MealLabel enum
        createdAt = foodLogEntity.createdAt
        openFoodLicense = foodLogEntity.openFoodLicense
        barcode = foodLogEntity.barcode
        packagedFoodCode = foodLogEntity.packagedFoodCode

        ingredients = foodLogEntity.ingredients.map { ingredientEntity ->
            ingredientEntity.toFoodRecordIngredient()
        }.toMutableList() // Map FoodLogIngredientEntity to FoodRecordIngredient

        servingSizes.clear()
        servingUnits.clear()
        servingSizes.addAll(foodLogEntity.servingSizes)
        servingUnits.addAll(foodLogEntity.servingUnits)
    }
}