package ai.passio.nutrition.uimodule.data.db.dao

import ai.passio.nutrition.uimodule.data.db.entity.WeightRecordEntity
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeightRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightRecord(weightRecord: WeightRecordEntity)

    @Delete
    suspend fun deleteWeightRecord(weightRecord: WeightRecordEntity)

    @Query("SELECT * FROM WeightRecord WHERE dateTime >= :startOfDay AND dateTime < :endOfDay")
    suspend fun getWeightRecords(startOfDay: Long, endOfDay: Long): List<WeightRecordEntity>

    @Query("SELECT * FROM WeightRecord ORDER BY dateTime DESC LIMIT 1")
    suspend fun getLatestWeightRecord(): WeightRecordEntity?
}