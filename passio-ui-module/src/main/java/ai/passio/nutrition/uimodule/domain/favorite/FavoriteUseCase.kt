package ai.passio.nutrition.uimodule.domain.favorite

import ai.passio.nutrition.uimodule.data.Repository
import ai.passio.nutrition.uimodule.ui.model.FoodRecord

object FavoriteUseCase {

    private val repository = Repository.getInstance()

    suspend fun markFavorite(foodRecord: FoodRecord): Boolean {
        foodRecord.create(null)
        foodRecord.mealLabel = null
        return repository.markFavorite(foodRecord)
    }
    suspend fun markUnfavorite(foodRecord: FoodRecord): Boolean {
        foodRecord.create(null)
        foodRecord.mealLabel = null
        return repository.markUnfavorite(foodRecord)
    }

    suspend fun fetchFavorites(): List<FoodRecord> {
        return repository.getFavorites()

    }

    suspend fun isFavorite(foodRecord: FoodRecord): Boolean
    {
        return repository.isFavorite(foodRecord)
    }

}