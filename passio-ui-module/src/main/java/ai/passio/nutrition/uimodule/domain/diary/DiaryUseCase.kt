package ai.passio.nutrition.uimodule.domain.diary

import ai.passio.nutrition.uimodule.data.Repository
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import java.util.Date

object DiaryUseCase {

    private val repository = Repository.getInstance()

    suspend fun getLogsForDay(day: Date): List<FoodRecord> {
        return repository.getLogsForDay(day)
    }

    suspend fun getLogsForWeek(day: Date): List<FoodRecord> {
        return repository.getLogsForWeek(day)
    }

    suspend fun getLogsForMonth(day: Date): List<FoodRecord> {
        return repository.getLogsForMonth(day)
    }
    suspend fun getLogsForLast30Days(): List<FoodRecord> {
        return repository.getLogsForLast30Days()
    }

    suspend fun deleteRecord(foodRecord: FoodRecord): Boolean {
        return repository.deleteFoodRecord(foodRecord.uuid)
    }

    suspend fun fetchAdherence(): List<Long> {
        return repository.fetchAdherence()
    }
}