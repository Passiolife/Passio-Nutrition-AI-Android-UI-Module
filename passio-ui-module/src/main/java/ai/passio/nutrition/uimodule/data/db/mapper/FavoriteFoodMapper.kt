package ai.passio.nutrition.uimodule.data.db.mapper

import ai.passio.nutrition.uimodule.data.db.entity.FavoriteFoodEntity
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.MealLabel

internal fun List<FavoriteFoodEntity>.toFoodRecords(): List<FoodRecord> {
    val foodLogEntities = this
    return foodLogEntities.map { foodLogEntity -> foodLogEntity.toFoodRecord() }
}

internal fun FoodRecord.toFavoriteEntity(): FavoriteFoodEntity {
    val foodRecord = this
    return FavoriteFoodEntity(
        uuid = foodRecord.uuid,
        id = foodRecord.id,
        name = foodRecord.name,
        additionalData = foodRecord.details,
        iconId = foodRecord.iconId,
//        foodImagePath = foodRecord.foodImagePath,
        passioIDEntityType = foodRecord.entityType,
        selectedUnit = foodRecord.getSelectedUnit(),
        selectedQuantity = foodRecord.getSelectedQuantity(),
        mealLabel = foodRecord.mealLabel?.value, // Convert MealLabel enum to its string value
        createdAt = foodRecord.createdAt,
        openFoodLicense = foodRecord.openFoodLicense,
        barcode = foodRecord.barcode, // Convert barcode to String (handle this conversion properly)
        packagedFoodCode = foodRecord.packagedFoodCode, // Convert packaged food code to String
        refCode = foodRecord.refCode ?: "",

        ingredients = foodRecord.ingredients.map { ingredient ->
            ingredient.toFoodLogIngredientEntity()
        }.toMutableList(), // Map FoodRecordIngredient to FoodLogIngredientEntity

        servingSizes = foodRecord.servingSizes.toMutableList(), // Assuming PassioServingSize can be directly mapped
        servingUnits = foodRecord.servingUnits.toMutableList()  // Assuming PassioServingUnit can be directly mapped
    )
}

internal fun FavoriteFoodEntity.toFoodRecord(): FoodRecord {
    val foodLogEntity = this
    return FoodRecord().apply {
        uuid = foodLogEntity.uuid
        id = foodLogEntity.id
        name = foodLogEntity.name
        details = foodLogEntity.additionalData
        iconId = foodLogEntity.iconId
//        foodImagePath = foodLogEntity.foodImagePath
        entityType = foodLogEntity.passioIDEntityType
        selectedUnit = foodLogEntity.selectedUnit
        selectedQuantity = foodLogEntity.selectedQuantity
        mealLabel = foodLogEntity.mealLabel?.let { label ->
            MealLabel.values().find { it.value == label }
        } // Convert String back to MealLabel enum
        createdAt = foodLogEntity.createdAt
        openFoodLicense = foodLogEntity.openFoodLicense
        barcode = foodLogEntity.barcode
        packagedFoodCode = foodLogEntity.packagedFoodCode
        refCode = foodLogEntity.refCode

        ingredients = foodLogEntity.ingredients.map { ingredientEntity ->
            ingredientEntity.toFoodRecordIngredient()
        }.toMutableList() // Map FoodLogIngredientEntity to FoodRecordIngredient

        servingSizes.clear()
        servingUnits.clear()
        servingSizes.addAll(foodLogEntity.servingSizes)
        servingUnits.addAll(foodLogEntity.servingUnits)
    }
}