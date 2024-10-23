package ai.passio.nutrition.uimodule.data.db.dao

import ai.passio.nutrition.uimodule.data.db.entity.TABLE_NAME_WATER_RECORD
import ai.passio.nutrition.uimodule.data.db.entity.WaterRecordEntity
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WaterRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterRecord(waterRecord: WaterRecordEntity)

    @Delete
    suspend fun deleteWaterRecord(waterRecord: WaterRecordEntity)

    @Query("SELECT * FROM $TABLE_NAME_WATER_RECORD WHERE dateTime >= :startOfDay AND dateTime < :endOfDay")
    suspend fun getWaterRecords(startOfDay: Long, endOfDay: Long): List<WaterRecordEntity>

}