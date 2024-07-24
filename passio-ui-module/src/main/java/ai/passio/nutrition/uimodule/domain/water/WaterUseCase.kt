package ai.passio.nutrition.uimodule.domain.water

import ai.passio.nutrition.uimodule.data.Repository
import ai.passio.nutrition.uimodule.ui.model.WaterRecord
import ai.passio.nutrition.uimodule.ui.progress.TimePeriod
import java.util.Date

object WaterUseCase {

    private val repository = Repository.getInstance()

    suspend fun updateRecord(weightRecord: WaterRecord): Boolean {
        return repository.updateWater(weightRecord)
    }

    suspend fun removeRecord(weightRecord: WaterRecord): Boolean {
        return repository.removeWaterRecord(weightRecord)
    }

    suspend fun getRecords(currentDate: Date, timePeriod: TimePeriod): List<WaterRecord> {
        return repository.fetchWaterRecords(currentDate, timePeriod)
    }

    suspend fun getRecords(currentDate: Date): List<WaterRecord> {
        return repository.fetchWaterRecords(currentDate)
    }
}