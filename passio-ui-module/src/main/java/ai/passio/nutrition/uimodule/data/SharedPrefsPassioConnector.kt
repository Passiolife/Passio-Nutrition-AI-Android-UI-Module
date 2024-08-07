package ai.passio.nutrition.uimodule.data

import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.UserProfile
import ai.passio.nutrition.uimodule.ui.model.WaterRecord
import ai.passio.nutrition.uimodule.ui.model.WeightRecord
import ai.passio.passiosdk.passiofood.data.measurement.UnitEnergy
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import android.content.Context
import android.text.format.DateFormat
import com.google.gson.GsonBuilder
import org.joda.time.DateTime
import java.util.Calendar
import java.util.Date

class SharedPrefsPassioConnector(context: Context) : PassioConnector {

    private val gson = GsonBuilder()
        .registerTypeAdapter(UnitMass::class.java, UnitMassSerializer())
        .registerTypeAdapter(UnitEnergy::class.java, UnitEnergySerializer())
        .create()
    private val sharedPreferences = PassioDemoSharedPreferences(
        context.getSharedPreferences(PassioDemoSharedPreferences.PREF_NAME, 0)
    )
    private val dateFormat = "yyyyMMdd"
    private lateinit var records: MutableList<FoodRecord>
    private lateinit var favorites: MutableList<FoodRecord>
    private lateinit var weightRecords: MutableList<WeightRecord>
    private lateinit var waterRecords: MutableList<WaterRecord>
    private var userProfile: UserProfile = UserProfile()

    override fun initialize() {
        records = sharedPreferences.getRecords().map {
            gson.fromJson(it, FoodRecord::class.java) as FoodRecord
        }.toMutableList()

        weightRecords = sharedPreferences.getWeightRecords().map {
            gson.fromJson(it, WeightRecord::class.java) as WeightRecord
        }.toMutableList()

        waterRecords = sharedPreferences.getWaterRecords().map {
            gson.fromJson(it, WaterRecord::class.java) as WaterRecord
        }.toMutableList()

        favorites = sharedPreferences.getFavorites().map {
            gson.fromJson(it, FoodRecord::class.java) as FoodRecord
        }.toMutableList()

        gson.fromJson(sharedPreferences.getUserProfile(), UserProfile::class.java)?.let {
            userProfile = it
        }

    }

    override suspend fun updateRecord(foodRecord: FoodRecord): Boolean {
        /* var indexToRemove = -1

         run breaking@ {
             records.forEachIndexed { index, fr ->
                 if (fr.uuid == foodRecord.uuid) {
                     indexToRemove = index
                     return@breaking
                 }
             }
         }*/
//        records.removeIf { it.uuid == foodRecord.uuid }
        val indexToRemove = records.indexOfFirst { it.uuid == foodRecord.uuid }
        if (indexToRemove != -1) {
            records.removeAt(indexToRemove)
            records.add(indexToRemove, foodRecord)
        } else {
            records.add(foodRecord)
        }
//        records.add(foodRecord)

        val json = records.map { gson.toJson(it) }
        sharedPreferences.saveRecords(json)
        return true
    }

    override suspend fun updateRecords(foodRecords: List<FoodRecord>): Boolean {
        foodRecords.forEach { foodRecord ->
            val indexToRemove = records.indexOfFirst { it.uuid == foodRecord.uuid }
            if (indexToRemove != -1) {
                records.removeAt(indexToRemove)
                records.add(indexToRemove, foodRecord)
            } else {
                records.add(foodRecord)
            }
        }
        val json = records.map { gson.toJson(it) }
        sharedPreferences.saveRecords(json)
        return true
    }

    override suspend fun deleteRecord(foodRecord: FoodRecord): Boolean {
        val recordToDelete = records.find { it.uuid == foodRecord.uuid } ?: return false
        records.remove(recordToDelete)
        sharedPreferences.saveRecords(records.map { gson.toJson(it) })
        return true
    }

    override suspend fun fetchDayRecords(day: Date): List<FoodRecord> {
        val dayString = DateFormat.format(dateFormat, day)
        val dayRecords = records.filter { foodRecord ->
            if (foodRecord.createdAtTime() == null) {
                false
            } else {
                val recordDay = DateFormat.format(dateFormat, foodRecord.createdAtTime()!!)
                dayString == recordDay
            }
        }
        return dayRecords
    }


    /* override suspend fun getLogsForLast30Days(): List<FoodRecord> {
         val today = DateTime()
         val todayDay = today.millis
         val before30Days = getBefore30Days(today).millis

         return records.filter { it.createdAtTime() in before30Days..todayDay }
     }*/

    override suspend fun fetchLogsRecords(startDate: Date, endDate: Date): List<FoodRecord> {
        val fromDate = DateTime(startDate.time).millis
        val toDate = DateTime(endDate.time).millis
        return records.filter { it.createdAtTime() in fromDate..toDate }
    }


    /*override suspend fun fetchMonthRecords(day: Date): List<FoodRecord> {
        val today = DateTime(day.time)
        val startOfMonth = getStartOfMonth(today).millis
        val endOfMonth = getEndOfMonth(today).millis

        return records.filter { it.createdAtTime() in startOfMonth..endOfMonth }
    }*/

    /* override suspend fun fetchWeekRecords(day: Date): List<FoodRecord> {

         val today = DateTime(day.time)
         val startOfWeek = getStartOfWeek(today).millis
         val endOfWeek = getEndOfWeek(today).millis

         return records.filter { it.createdAtTime() in startOfWeek..endOfWeek }
     }*/

    override suspend fun updateFavorite(foodRecord: FoodRecord) {
        val currentFavorite = favorites.find { it.uuid == foodRecord.uuid }
        if (currentFavorite != null) {
            favorites.remove(currentFavorite)
        }
        favorites.add(foodRecord)
        sharedPreferences.saveFavorites(favorites.map { gson.toJson(it) })
    }

    override suspend fun deleteFavorite(foodRecord: FoodRecord) {
        val favoriteToDelete = favorites.find { it.uuid == foodRecord.uuid } ?: return
        favorites.remove(favoriteToDelete)
        sharedPreferences.saveFavorites(favorites.map { gson.toJson(it) })
    }

    override suspend fun fetchFavorites(): List<FoodRecord> = favorites

    override suspend fun fetchAdherence(): List<Long> {
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
        return uniqueDates.toList()
    }

    override suspend fun updateUserProfile(userProfile: UserProfile): Boolean {
        this.userProfile = userProfile
        sharedPreferences.saveUserProfile(gson.toJson(userProfile))
        return true
    }

    override suspend fun fetchUserProfile(): UserProfile {
        return userProfile
    }

    override suspend fun updateWeightRecord(weightRecord: WeightRecord): Boolean {
        val indexToRemove = weightRecords.indexOfFirst { it.uuid == weightRecord.uuid }
        if (indexToRemove != -1) {
            weightRecords.removeAt(indexToRemove)
            weightRecords.add(indexToRemove, weightRecord)
        } else {
            weightRecords.add(weightRecord)
        }
        val json = weightRecords.map { gson.toJson(it) }
        sharedPreferences.saveWeightRecords(json)
        return true
    }

    override suspend fun removeWeightRecord(weightRecord: WeightRecord): Boolean {
        val indexToRemove = weightRecords.indexOfFirst { it.uuid == weightRecord.uuid }
        if (indexToRemove != -1) {
            weightRecords.removeAt(indexToRemove)
        }
        val json = weightRecords.map { gson.toJson(it) }
        sharedPreferences.saveWeightRecords(json)
        return true
    }

    override suspend fun fetchWeightRecords(startDate: Date, endDate: Date): List<WeightRecord> {
        val startOfWeek = startDate.time
        val endOfWeek = endDate.time

        return weightRecords.filter { it.dateTime in startOfWeek..endOfWeek }
            .sortedByDescending { it.dateTime }
    }

    override suspend fun fetchLatestWeightRecord(): WeightRecord? {
        return weightRecords.maxByOrNull { it.dateTime }
    }

    override suspend fun updateWaterRecord(waterRecord: WaterRecord): Boolean {
        val indexToRemove = waterRecords.indexOfFirst { it.uuid == waterRecord.uuid }
        if (indexToRemove != -1) {
            waterRecords.removeAt(indexToRemove)
            waterRecords.add(indexToRemove, waterRecord)
        } else {
            waterRecords.add(waterRecord)
        }
        val json = waterRecords.map { gson.toJson(it) }
        sharedPreferences.saveWaterRecords(json)
        return true
    }

    override suspend fun removeWaterRecord(waterRecord: WaterRecord): Boolean {
        val indexToRemove = waterRecords.indexOfFirst { it.uuid == waterRecord.uuid }
        if (indexToRemove != -1) {
            waterRecords.removeAt(indexToRemove)
        }
        val json = waterRecords.map { gson.toJson(it) }
        sharedPreferences.saveWaterRecords(json)
        return true
    }

    override suspend fun fetchWaterRecords(startDate: Date, endDate: Date): List<WaterRecord> {
        val startOfWeek = startDate.time
        val endOfWeek = endDate.time

        return waterRecords.filter { it.dateTime in startOfWeek..endOfWeek }
            .sortedByDescending { it.dateTime }
    }
}