package ai.passio.nutrition.uimodule.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "WeightRecord")
data class WeightRecordEntity(
    @PrimaryKey
    val uuid: String,
    var weight: Double = 0.0,
    var dateTime: Long = 0
)