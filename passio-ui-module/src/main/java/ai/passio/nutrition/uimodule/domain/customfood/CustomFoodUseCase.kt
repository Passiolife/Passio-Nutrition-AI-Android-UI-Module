package ai.passio.nutrition.uimodule.domain.customfood

import ai.passio.nutrition.uimodule.data.Repository
import ai.passio.nutrition.uimodule.ui.model.FoodRecord

object CustomFoodUseCase {

    private val repository = Repository.getInstance()

    suspend fun saveCustomFood(foodRecord: FoodRecord): Boolean {
        return repository.saveCustomFood(foodRecord)
    }

    suspend fun fetchCustomFoods(): List<FoodRecord> {
        return repository.fetchCustomFoods()

    }
}