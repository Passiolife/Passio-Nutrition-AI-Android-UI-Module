package ai.passio.nutrition.uimodule.data.db.dao

import ai.passio.nutrition.uimodule.data.db.entity.FoodLogEntity
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FoodLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodRecord(foodRecord: FoodLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodRecords(foodRecords: List<FoodLogEntity>)

    @Query("SELECT * FROM foodlog WHERE uuid = :uuid")
    suspend fun getFoodRecord(uuid: String): FoodLogEntity?

    @Delete
    suspend fun deleteFoodRecord(foodRecord: FoodLogEntity)

    @Query("SELECT * FROM FoodLog WHERE createdAt >= :startOfDay AND createdAt < :endOfDay")
    suspend fun getFoodLogsForDate(startOfDay: Long, endOfDay: Long): List<FoodLogEntity>

    @Query("SELECT * FROM FoodLog")
    suspend fun getAllFoodLogs(): List<FoodLogEntity>
}