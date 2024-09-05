package ai.passio.nutrition.uimodule.domain.customfood

import ai.passio.nutrition.uimodule.data.Repository
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.MealLabel
import ai.passio.nutrition.uimodule.ui.model.copy
import ai.passio.nutrition.uimodule.ui.model.newCustomFood
import ai.passio.passiosdk.passiofood.Barcode
import ai.passio.passiosdk.passiofood.FoodCandidates
import ai.passio.passiosdk.passiofood.FoodDetectionConfiguration
import kotlinx.coroutines.flow.Flow
import java.util.Date

object CustomFoodUseCase {

    private val repository = Repository.getInstance()

    suspend fun saveCustomFood(foodRecord: FoodRecord): Boolean {
        val newFood: FoodRecord
        if (!foodRecord.isCustomFood()) {
            newFood = foodRecord.newCustomFood()
            repository.deleteFoodRecord(foodRecord)
            repository.logFoodRecord(newFood)
        } else {
            newFood = foodRecord
        }
        return repository.saveCustomFood(newFood)
    }

    suspend fun fetchCustomFoods(): List<FoodRecord> {
        return repository.fetchCustomFoods()

    }

    suspend fun deleteCustomFood(uuid: String): Boolean {
        return repository.deleteCustomFood(uuid)

    }

    suspend fun logCustomFood(fooRecord: FoodRecord): Boolean {
        val record = fooRecord.copy()
        record.create(Date().time)
        val mealLabel = MealLabel.dateToMealLabel(record.createdAtTime()!!)
        record.mealLabel = mealLabel
        return repository.logFoodRecord(record)
    }

    suspend fun fetchFoodItemForProduct(barcode: Barcode): FoodRecord? {
        return repository.fetchFoodItemForProduct(barcode)?.let {
            return FoodRecord(it)
        }
    }

    suspend fun fetchFoodFromCustomFoods(barcode: Barcode): FoodRecord? {
        return repository.getCustomFoodUsingBarcode(barcode)
    }

    fun recognitionBarcode(config: FoodDetectionConfiguration): Flow<FoodCandidates?> {
        return repository.recognitionResultFlow(config)
    }

    fun stopFoodDetection() {
        repository.stopFoodDetection()
    }

}