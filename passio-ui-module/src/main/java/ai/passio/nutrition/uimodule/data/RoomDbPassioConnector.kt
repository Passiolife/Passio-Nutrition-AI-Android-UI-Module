package ai.passio.nutrition.uimodule.data

import ai.passio.nutrition.uimodule.data.db.PassioDatabase
import ai.passio.nutrition.uimodule.data.db.entity.USER_ID
import ai.passio.nutrition.uimodule.data.db.mapper.toCustomFoodEntity
import ai.passio.nutrition.uimodule.data.db.mapper.toCustomRecipeEntity
import ai.passio.nutrition.uimodule.data.db.mapper.toFoodRecord
import ai.passio.nutrition.uimodule.data.db.mapper.toFoodLogEntities
import ai.passio.nutrition.uimodule.data.db.mapper.toFoodLogEntity
import ai.passio.nutrition.uimodule.data.db.mapper.toFoodRecords
import ai.passio.nutrition.uimodule.data.db.mapper.toUserEntity
import ai.passio.nutrition.uimodule.data.db.mapper.toUserProfile
import ai.passio.nutrition.uimodule.data.db.mapper.toWaterRecord
import ai.passio.nutrition.uimodule.data.db.mapper.toWaterRecordEntity
import ai.passio.nutrition.uimodule.data.db.mapper.toWeightRecord
import ai.passio.nutrition.uimodule.data.db.mapper.toWeightRecordEntity
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.UserProfile
import ai.passio.nutrition.uimodule.ui.model.WaterRecord
import ai.passio.nutrition.uimodule.ui.model.WeightRecord
import ai.passio.nutrition.uimodule.ui.model.getDBTimestamp
import ai.passio.nutrition.uimodule.ui.util.getEndTimestamps
import ai.passio.nutrition.uimodule.ui.util.getStartTimestamps
import android.content.Context
import androidx.room.Room
import org.joda.time.DateTime
import java.util.Calendar
import java.util.Date

class RoomDbPassioConnector(applicationContext: Context) : PassioConnector {

    private val db = Room.databaseBuilder(
        applicationContext,
        PassioDatabase::class.java, PassioDatabase.DATABASE_NAME
    ).build()

    private val foodLogDao = db.foodLogDao()

    override fun initialize() {

    }

    override suspend fun updateRecord(foodRecord: FoodRecord): Boolean {
        foodLogDao.insertFoodLog(foodRecord.toFoodLogEntity())
        return true
    }

    override suspend fun updateRecords(foodRecords: List<FoodRecord>): Boolean {
        foodLogDao.insertFoodLogs(foodRecords.toFoodLogEntities())
        return true
    }

    override suspend fun deleteRecord(uuid: String): Boolean {
        foodLogDao.getFoodRecord(uuid)?.let {
            foodLogDao.deleteFoodRecord(it)
        }
        return true
    }

    override suspend fun fetchDayRecords(day: Date): List<FoodRecord> {
        val startDate = getDBTimestamp(getStartTimestamps(DateTime(day)))
        val endDate = getDBTimestamp(getEndTimestamps(DateTime(day)))
        val result = foodLogDao.getFoodLogsForDate(startDate, endDate).toFoodRecords()
        return result
    }

    override suspend fun fetchLogsRecords(startDate: Date, endDate: Date): List<FoodRecord> {
        val startDateTime = getDBTimestamp(getStartTimestamps(DateTime(startDate)))
        val endDateTime = getDBTimestamp(getEndTimestamps(DateTime(endDate)))
        val result = foodLogDao.getFoodLogsForDate(startDateTime, endDateTime).toFoodRecords()
        return result
    }

    override suspend fun fetchAdherence(): List<Long> {
        val records = foodLogDao.getAllFoodLogs().toFoodRecords()

        val uniqueDates = HashSet<Long>() // HashSet to store unique dates
        // Iterate through each record and add the date component to the HashSet

        fun timestampOnlyDate(timestamp: Long): Long {
            // Convert millis to a date with only date part (ignoring time)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timestamp
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }

        records.forEach { record ->
            record.createdAtTime()?.let { timestamp ->
                val date = timestampOnlyDate(timestamp)
                uniqueDates.add(date)
            }
        }
        val result = uniqueDates.toList()
        return result
    }

    override suspend fun fetchUserProfile(): UserProfile {
        return db.userDao().getUserEntityById(USER_ID)?.toUserProfile() ?: UserProfile()
    }

    override suspend fun updateUserProfile(userProfile: UserProfile): Boolean {
        db.userDao().insert(userProfile.toUserEntity())
        return true
    }

    override suspend fun updateWeightRecord(weightRecord: WeightRecord): Boolean {
        db.weightRecordDao().insertWeightRecord(weightRecord.toWeightRecordEntity())
        return true
    }

    override suspend fun removeWeightRecord(weightRecord: WeightRecord): Boolean {
        db.weightRecordDao().deleteWeightRecord(weightRecord.toWeightRecordEntity())
        return true
    }

    override suspend fun fetchWeightRecords(startDate: Date, endDate: Date): List<WeightRecord> {
        val list = db.weightRecordDao().getWeightRecords(
            getStartTimestamps(DateTime(startDate)), getEndTimestamps(
                DateTime(endDate)
            )
        )
        return list.map { it.toWeightRecord() }
    }

    override suspend fun fetchLatestWeightRecord(): WeightRecord? {
        db.weightRecordDao().getLatestWeightRecord()?.let {
            return it.toWeightRecord()
        }
        return null
    }

    override suspend fun updateWaterRecord(waterRecord: WaterRecord): Boolean {
        db.waterRecordDao().insertWaterRecord(waterRecord.toWaterRecordEntity())
        return true
    }

    override suspend fun removeWaterRecord(waterRecord: WaterRecord): Boolean {
        db.waterRecordDao().deleteWaterRecord(waterRecord.toWaterRecordEntity())
        return true
    }

    override suspend fun fetchWaterRecords(startDate: Date, endDate: Date): List<WaterRecord> {
        val list = db.waterRecordDao().getWaterRecords(
            getStartTimestamps(DateTime(startDate)), getEndTimestamps(DateTime(endDate))
        )
        return list.map { it.toWaterRecord() }
    }

    override suspend fun saveCustomFood(foodRecord: FoodRecord): Boolean {
        db.customFoodDao().insert(foodRecord.toCustomFoodEntity())
        return true
    }

    override suspend fun fetchCustomFoods(): List<FoodRecord> {
        return db.customFoodDao().getAll().toFoodRecords()
    }

    override suspend fun fetchCustomFoods(searchQuery: String): List<FoodRecord> {
        return db.customFoodDao().filterCustomFoods(searchQuery.trim()).toFoodRecords()
    }

    override suspend fun fetchCustomFood(uuid: String): FoodRecord? {
        return db.customFoodDao().get(uuid)?.toFoodRecord()
    }

    override suspend fun deleteCustomFood(uuid: String): Boolean {
        db.customFoodDao().get(uuid)?.let {
            db.customFoodDao().delete(it)
        }
        return true
    }

    override suspend fun getCustomFoodUsingBarcode(barcode: String): FoodRecord? {
        return db.customFoodDao().getByBarcode(barcode)?.toFoodRecord()
    }

    override suspend fun saveRecipe(foodRecord: FoodRecord): Boolean {
        db.customRecipeDao().insert(foodRecord.toCustomRecipeEntity())
        return true
    }

    override suspend fun fetchRecipe(uuid: String): FoodRecord? {
        return db.customRecipeDao().get(uuid)?.toFoodRecord()
    }

    override suspend fun fetchRecipes(): List<FoodRecord> {
        return db.customRecipeDao().getAll().toFoodRecords()
    }

    override suspend fun fetchRecipes(searchQuery: String): List<FoodRecord> {
        return db.customRecipeDao().filterCustomRecipes(searchQuery.trim()).toFoodRecords()
    }

    override suspend fun deleteRecipe(uuid: String): Boolean {
        db.customRecipeDao().get(uuid)?.let {
            db.customRecipeDao().delete(it)
        }
        return true
    }
}