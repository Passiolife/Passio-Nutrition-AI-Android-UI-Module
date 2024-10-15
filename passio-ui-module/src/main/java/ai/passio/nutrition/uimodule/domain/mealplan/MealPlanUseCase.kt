package ai.passio.nutrition.uimodule.domain.mealplan

import ai.passio.nutrition.uimodule.data.Repository
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.MealLabel
import ai.passio.nutrition.uimodule.ui.util.dateToTimestamp
import ai.passio.passiosdk.passiofood.PassioFoodDataInfo
import ai.passio.passiosdk.passiofood.PassioMealTime
import ai.passio.passiosdk.passiofood.data.measurement.Grams
import ai.passio.passiosdk.passiofood.data.model.PassioAdvisorFoodInfo
import ai.passio.passiosdk.passiofood.data.model.PassioMealPlanItem
import ai.passio.passiosdk.passiofood.data.model.PassioSpeechRecognitionModel
import java.util.Date

object MealPlanUseCase {

    private val repository = Repository.getInstance()

    suspend fun getFoodRecord(
        passioFoodDataInfo: PassioFoodDataInfo,
        passioMealTime: PassioMealTime,
        weighGrams: Double? = null
    ): FoodRecord? {
        val foodItem = repository.fetchPassioFoodItem(passioFoodDataInfo, weighGrams) ?: return null

        val nutritionPreview = passioFoodDataInfo.nutritionPreview
        val foodRecord = FoodRecord(foodItem)
        foodRecord.mealLabel = MealLabel.stringToMealLabel(passioMealTime.mealName)
        if (weighGrams == null || weighGrams == 0.0) {
            if (foodRecord.setSelectedUnit(nutritionPreview.servingUnit)) {
                val quantity = nutritionPreview.servingQuantity
                foodRecord.setSelectedQuantity(quantity)
            } else {
                val weight = nutritionPreview.weightQuantity
                if (foodRecord.setSelectedUnit(Grams.unitName)) {
                    foodRecord.setSelectedQuantity(weight)
                }
            }
        }
        return foodRecord
    }

    suspend fun getFoodRecord(passioMealPlanItem: PassioMealPlanItem): FoodRecord? {
        return getFoodRecord(passioMealPlanItem.meal, passioMealPlanItem.mealTime)
    }

    suspend fun getFoodRecords(passioMealPlanItems: List<PassioMealPlanItem>): List<FoodRecord> {
        return passioMealPlanItems.mapNotNull { passioMealPlanItem ->
            getFoodRecord(passioMealPlanItem)
        }
    }

    suspend fun getFoodRecords(
        passioMealPlanItems: List<PassioAdvisorFoodInfo>,
        passioMealTime: PassioMealTime
    ): List<FoodRecord> {
        return passioMealPlanItems.mapNotNull { passioMealPlanItem ->
            if (passioMealPlanItem.packagedFoodItem != null) {
                FoodRecord(passioMealPlanItem.packagedFoodItem!!)
            } else if (passioMealPlanItem.foodDataInfo != null) {
                getFoodRecord(
                    passioMealPlanItem.foodDataInfo!!,
                    passioMealTime,
                    passioMealPlanItem.weightGrams
                )
            } else {
                null
            }
        }
    }

    suspend fun getFoodRecordsFromSpeech(
        passioMealPlanItems: List<PassioSpeechRecognitionModel>,
        passioMealTime: PassioMealTime
    ): List<FoodRecord> {
        return passioMealPlanItems.mapNotNull { passioMealPlanItem ->
            getFoodRecord(
                passioMealPlanItem.advisorInfo.foodDataInfo!!,
                passioMealPlanItem.mealTime ?: passioMealTime,
                passioMealPlanItem.advisorInfo.weightGrams
            )?.apply {
                create(dateToTimestamp(passioMealPlanItem.date, "yyyy-MM-dd"))
            }
        }
    }

    suspend fun logFoodRecord(record: FoodRecord): Boolean {
        record.create(record.createdAtTime() ?: Date().time)
        if (record.mealLabel == null) {
            record.mealLabel = MealLabel.dateToMealLabel(record.createdAtTime()!!)
        }
        return repository.logFoodRecord(record)
    }

    suspend fun logFoodRecords(records: List<FoodRecord>): Boolean {
        records.forEach { record ->
            record.create(record.createdAtTime() ?: Date().time)
            if (record.mealLabel == null) {
                record.mealLabel = MealLabel.dateToMealLabel(record.createdAtTime()!!)
            }
        }

        return repository.logFoodRecords(records)
    }
}