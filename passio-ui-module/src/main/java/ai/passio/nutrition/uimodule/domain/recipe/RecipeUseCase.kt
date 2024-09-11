package ai.passio.nutrition.uimodule.domain.recipe

import ai.passio.nutrition.uimodule.data.Repository
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.MealLabel
import ai.passio.nutrition.uimodule.ui.model.copy
import ai.passio.nutrition.uimodule.ui.model.copyAsCustomFood
import java.util.Date

object RecipeUseCase {

    private val repository = Repository.getInstance()

    suspend fun saveRecipe(foodRecord: FoodRecord): Boolean {
        foodRecord.create(null)
        return repository.saveRecipe(foodRecord)
    }

    suspend fun fetchRecipes(): List<FoodRecord> {
        return repository.fetchRecipes()
    }

    suspend fun fetchRecipes(searchQuery: String): List<FoodRecord> {
        return repository.fetchRecipes(searchQuery)
    }

    suspend fun deleteRecipe(uuid: String): Boolean {
        return repository.deleteRecipe(uuid)
    }

    suspend fun logRecipe(fooRecord: FoodRecord): Boolean {
        val record = fooRecord.copy()
        record.create(Date().time)
        val mealLabel = MealLabel.dateToMealLabel(record.createdAtTime()!!)
        record.mealLabel = mealLabel
        return repository.logFoodRecord(record)
    }

    suspend fun getRecipe(uuid: String): FoodRecord? {
        return repository.fetchRecipe(uuid)
    }

    /*

    suspend fun logRecipe(fooRecord: FoodRecord): Boolean {
        val record = fooRecord.copy()
        record.create(Date().time)
        val mealLabel = MealLabel.dateToMealLabel(record.createdAtTime()!!)
        record.mealLabel = mealLabel
        return repository.logFoodRecord(record)
    }*/

}