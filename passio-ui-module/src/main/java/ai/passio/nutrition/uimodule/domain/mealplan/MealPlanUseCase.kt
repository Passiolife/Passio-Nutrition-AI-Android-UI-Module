package ai.passio.nutrition.uimodule.domain.mealplan

import ai.passio.nutrition.uimodule.data.Repository
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.MealLabel
import ai.passio.passiosdk.passiofood.data.model.PassioMealPlanItem
import java.util.Date

object MealPlanUseCase {

    private val repository = Repository.getInstance()

    suspend fun getFoodRecord(passioMealPlanItem: PassioMealPlanItem): FoodRecord? {
        val passioFoodDataInfo = passioMealPlanItem.meal
        val foodItem = repository.fetchPassioFoodItem(passioFoodDataInfo) ?: return null

        val nutritionPreview = passioFoodDataInfo.nutritionPreview
        val foodRecord = FoodRecord(foodItem)
        foodRecord.mealLabel = MealLabel.stringToMealLabel(passioMealPlanItem.mealTime.mealName)
        if (foodRecord.setSelectedUnit(nutritionPreview.servingUnit)) {
            val quantity = nutritionPreview.servingQuantity
            foodRecord.setSelectedQuantity(quantity)
        } else {
            val weight = nutritionPreview.weightQuantity
            if (foodRecord.setSelectedUnit("gram")) {
                foodRecord.setSelectedQuantity(weight)
            }
        }
        return foodRecord
    }

    suspend fun getFoodRecords(passioMealPlanItems: List<PassioMealPlanItem>): List<FoodRecord> {
        return passioMealPlanItems.mapNotNull { passioMealPlanItem ->
            getFoodRecord(passioMealPlanItem)
        }
    }

    suspend fun logFoodRecord(record: FoodRecord): Boolean {
        record.create(record.createdAtTime() ?: Date().time)
        return repository.logFoodRecord(record)
    }

    suspend fun logFoodRecords(records: List<FoodRecord>): Boolean {
        records.forEach { record ->
            record.create(record.createdAtTime() ?: Date().time)
        }

        return repository.logFoodRecords(records)
    }
}