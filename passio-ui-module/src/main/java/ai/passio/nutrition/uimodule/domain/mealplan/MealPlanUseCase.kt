package ai.passio.nutrition.uimodule.domain.mealplan

import ai.passio.nutrition.uimodule.data.Repository
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.ImageFoodResult
import ai.passio.nutrition.uimodule.ui.model.MealLabel
import ai.passio.nutrition.uimodule.ui.model.toMealLabel
import ai.passio.nutrition.uimodule.ui.util.StringKT.isValid
import ai.passio.nutrition.uimodule.ui.util.dateToTimestamp
import ai.passio.passiosdk.passiofood.PassioFoodDataInfo
import ai.passio.passiosdk.passiofood.PassioMealTime
import ai.passio.passiosdk.passiofood.data.model.PassioAdvisorFoodInfo
import ai.passio.passiosdk.passiofood.data.model.PassioFoodResultType
import ai.passio.passiosdk.passiofood.data.model.PassioIDEntityType
import ai.passio.passiosdk.passiofood.data.model.PassioMealPlanItem
import ai.passio.passiosdk.passiofood.data.model.PassioSpeechRecognitionModel
import java.util.Date

object MealPlanUseCase {

    private val repository = Repository.getInstance()

    suspend fun getFoodRecord(
        passioFoodDataInfo: PassioFoodDataInfo,
        passioMealTime: PassioMealTime,
        weighGrams: Double? = null,
        resultType: PassioFoodResultType? = null
    ): FoodRecord? {

//        val foodItem = repository.fetchPassioFoodItem(passioFoodDataInfo, weighGrams) ?: return null
        val foodItem = repository.fetchPassioFoodItem(
            dataInfo = passioFoodDataInfo,
//            servingUnit = Grams.unitName,
//            servingQuantity = weighGrams
            servingUnit = passioFoodDataInfo.nutritionPreview.servingUnit,
            servingQuantity = passioFoodDataInfo.nutritionPreview.servingQuantity
        ) ?: return null

        val nutritionPreview = passioFoodDataInfo.nutritionPreview
        val foodRecord = FoodRecord(foodItem)
        foodRecord.mealLabel = passioMealTime.toMealLabel()
        /*Log.d(
            "nutritionPreview===", "" +
                    "weightQuantity: ${nutritionPreview.weightQuantity}\n" +
                    "weightUnit: ${nutritionPreview.weightUnit}\n" +
                    "servingUnit: ${nutritionPreview.servingUnit}\n" +
                    "servingQuantity: ${nutritionPreview.servingQuantity}\n" +
                    "weighGrams: ${weighGrams}\n" +
                    ""
        )*/
//        if (weighGrams == null || weighGrams == 0.0) {
        if (foodRecord.setSelectedUnit(nutritionPreview.servingUnit)) {
            val quantity = nutritionPreview.servingQuantity
            foodRecord.setSelectedQuantity(quantity)
        } else {
            val weight = nutritionPreview.weightQuantity
//                if (foodRecord.setSelectedUnit(Grams.unitName)) {
            if (foodRecord.setSelectedUnit(nutritionPreview.weightUnit)) {
                foodRecord.setSelectedQuantity(weight)
            }
        }
//        }
        foodRecord.apply {

            if (resultType == PassioFoodResultType.NUTRITION_FACTS && !name.isValid()) {
                name = "Scanned Nutrition Label"
            }

            val entity = if (resultType == PassioFoodResultType.FOOD_ITEM) {
                PassioIDEntityType.item
            } else if (resultType == PassioFoodResultType.NUTRITION_FACTS) {
                PassioIDEntityType.nutritionFacts
            } else if (resultType == PassioFoodResultType.BARCODE) {
                PassioIDEntityType.barcode
            } else {
                PassioIDEntityType.item
            }
            entityType = entity.value
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
                FoodRecord(passioMealPlanItem.packagedFoodItem!!).apply {
                    if (!name.isValid()) {
                        name = "Nutrition Facts Label"
                    }
                    entityType = PassioIDEntityType.packagedFoodCode.value
                }
            } else if (passioMealPlanItem.foodDataInfo != null) {
                getFoodRecord(
                    passioMealPlanItem.foodDataInfo!!,
                    passioMealTime,
                    passioMealPlanItem.weightGrams,
                    resultType = passioMealPlanItem.resultType
                )
            } else {
                null
            }
        }
    }

    suspend fun getFoodRecordsForImages(
        passioMealPlanItems: List<PassioAdvisorFoodInfo>,
        passioMealTime: PassioMealTime
    ): List<ImageFoodResult> {
        return passioMealPlanItems.mapNotNull { passioMealPlanItem ->
            val foodRecord = if (passioMealPlanItem.packagedFoodItem != null) {
                FoodRecord(passioMealPlanItem.packagedFoodItem!!).apply {
                    if (!name.isValid()) {
                        name = "Nutrition Facts Label"
                    }
                    entityType = PassioIDEntityType.packagedFoodCode.value
                }
            } else if (passioMealPlanItem.foodDataInfo != null) {
                getFoodRecord(
                    passioMealPlanItem.foodDataInfo!!,
                    passioMealTime,
                    passioMealPlanItem.weightGrams,
                    resultType = passioMealPlanItem.resultType
                )
            } else {
                null
            }
            if (foodRecord != null) {
                ImageFoodResult(
                    record = foodRecord,
                    resultType = passioMealPlanItem.resultType
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