package ai.passio.nutrition.uimodule.data.db.mapper

import ai.passio.nutrition.uimodule.data.db.entity.WaterRecordEntity
import ai.passio.nutrition.uimodule.data.db.entity.WeightRecordEntity
import ai.passio.nutrition.uimodule.ui.model.WaterRecord
import ai.passio.nutrition.uimodule.ui.model.WeightRecord


internal fun WaterRecordEntity.toWaterRecord(): WaterRecord {
    val waterRecordEntity = this
    return WaterRecord().apply {
        uuid = waterRecordEntity.uuid
        weight = waterRecordEntity.weight
        dateTime = waterRecordEntity.dateTime
    }
}

internal fun WaterRecord.toWaterRecordEntity(): WaterRecordEntity {
    val waterRecordEntity = this
    return WaterRecordEntity(
        uuid = waterRecordEntity.uuid,
        weight = waterRecordEntity.weight,
        dateTime = waterRecordEntity.dateTime
    )
}

internal fun WeightRecordEntity.toWeightRecord(): WeightRecord {
    val waterRecordEntity = this
    return WeightRecord().apply {
        uuid = waterRecordEntity.uuid
        weight = waterRecordEntity.weight
        dateTime = waterRecordEntity.dateTime
    }
}

internal fun WeightRecord.toWeightRecordEntity(): WeightRecordEntity {
    val waterRecordEntity = this
    return WeightRecordEntity(
        uuid = waterRecordEntity.uuid,
        weight = waterRecordEntity.weight,
        dateTime = waterRecordEntity.dateTime
    )
}