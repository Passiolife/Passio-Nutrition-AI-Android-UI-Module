package ai.passio.nutrition.uimodule.data

import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import java.util.*

interface PassioConnector {

    fun initialize()

    suspend fun updateRecord(foodRecord: FoodRecord): Boolean

    suspend fun deleteRecord(foodRecord: FoodRecord): Boolean

    suspend fun fetchDayRecords(day: Date): List<FoodRecord>

    suspend fun fetchWeekRecords(day: Date): List<FoodRecord>

    suspend fun fetchMonthRecords(day: Date): List<FoodRecord>

    suspend fun updateFavorite(foodRecord: FoodRecord)

    suspend fun deleteFavorite(foodRecord: FoodRecord)

    suspend fun fetchFavorites(): List<FoodRecord>

    suspend fun fetchAdherence(): List<Long>

    // TODO user profile
//    fun fetchUserProfile(onResult: (userProfile: UserProfile?) -> Unit)
//
//    fun updateUserProfile(userProfile: UserProfile, onComplete: () -> Unit)
}
