package ai.passio.nutrition.uimodule.data

import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.UserProfile
import ai.passio.nutrition.uimodule.ui.model.WaterRecord
import ai.passio.nutrition.uimodule.ui.model.WeightRecord
import java.util.Date

interface PassioConnector {

    fun initialize()

    suspend fun updateRecord(foodRecord: FoodRecord): Boolean

//    suspend fun updateRecords(foodRecords: List<FoodRecord>): Boolean

    suspend fun deleteRecord(foodRecord: FoodRecord): Boolean

    suspend fun fetchDayRecords(day: Date): List<FoodRecord>

    suspend fun fetchDayLogFor(startDate: Date, endDate: Date): List<FoodRecord>

    suspend fun fetchAdherence(): List<Long>

    suspend fun fetchUserProfile(): UserProfile

    suspend fun updateUserProfile(userProfile: UserProfile): Boolean

    suspend fun updateWeightRecord(weightRecord: WeightRecord): Boolean

    suspend fun deleteWeightRecord(weightRecord: WeightRecord): Boolean

    suspend fun fetchWeightRecords(startDate: Date, endDate: Date): List<WeightRecord>

    suspend fun fetchLatestWeightRecord(): WeightRecord?

    suspend fun updateWaterRecord(waterRecord: WaterRecord): Boolean

    suspend fun deleteWaterRecord(waterRecord: WaterRecord): Boolean

    suspend fun fetchWaterRecords(startDate: Date, endDate: Date): List<WaterRecord>

    suspend fun updateUserFood(foodRecord: FoodRecord): Boolean

    suspend fun fetchAllUserFoods(): List<FoodRecord>

    suspend fun fetchAllUserFoodsMatching(searchQuery: String): List<FoodRecord>

    suspend fun fetchUserFood(uuid: String): FoodRecord?

    suspend fun deleteUserFood(foodRecord: FoodRecord): Boolean

    suspend fun fetchUserFoodsForBarcode(barcode: String): FoodRecord?

    suspend fun updateRecipe(foodRecord: FoodRecord): Boolean

    suspend fun fetchRecipe(uuid: String): FoodRecord?

    suspend fun fetchRecipes(): List<FoodRecord>

    suspend fun fetchRecipes(searchQuery: String): List<FoodRecord>

    suspend fun deleteRecipe(foodRecord: FoodRecord): Boolean

    suspend fun updateFavorite(foodRecord: FoodRecord): Boolean

    suspend fun deleteFavorite(foodRecord: FoodRecord): Boolean

    suspend fun fetchFavorites(): List<FoodRecord>

    suspend fun isFavorite(foodRecord: FoodRecord): Boolean

//    suspend fun updateUserFoodImage(id: String, bitmap: Bitmap): Boolean
//    suspend fun fetchUserFoodImage(id: String): Bitmap?
//    suspend fun deleteUserFoodImage(id: String): Boolean
}
