package ai.passio.nutrition.uimodule.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

internal const val TABLE_NAME_WATER_RECORD = "WaterRecords"
@Entity(tableName = TABLE_NAME_WATER_RECORD)
data class WaterRecordEntity(
    @PrimaryKey
    var uuid: String,
    var weight: Double = 0.0,
    var dateTime: Long = 0
)

