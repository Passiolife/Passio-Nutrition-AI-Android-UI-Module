package ai.passio.nutrition.uimodule.data

import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.util.getEndOfMonth
import ai.passio.nutrition.uimodule.ui.util.getEndOfWeek
import ai.passio.nutrition.uimodule.ui.util.getStartOfMonth
import ai.passio.nutrition.uimodule.ui.util.getStartOfWeek
import ai.passio.passiosdk.passiofood.data.measurement.UnitEnergy
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import com.google.gson.GsonBuilder
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
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

    override fun initialize() {
        records = sharedPreferences.getRecords().map {
            gson.fromJson(it, FoodRecord::class.java) as FoodRecord
        }.toMutableList()

        favorites = sharedPreferences.getFavorites().map {
            gson.fromJson(it, FoodRecord::class.java) as FoodRecord
        }.toMutableList()
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


    override suspend fun fetchMonthRecords(day: Date): List<FoodRecord> {
        val today = DateTime(day.time)
        val startOfWeek = getStartOfMonth(today).millis
        val endOfWeek = getEndOfMonth(today).millis

        return records.filter { it.createdAtTime() in startOfWeek..endOfWeek }
    }

    override suspend fun fetchWeekRecords(day: Date): List<FoodRecord> {

        val today = DateTime(day.time)
        val startOfWeek = getStartOfWeek(today).millis
        val endOfWeek = getEndOfWeek(today).millis

        return records.filter { it.createdAtTime() in startOfWeek..endOfWeek }
    }

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
}