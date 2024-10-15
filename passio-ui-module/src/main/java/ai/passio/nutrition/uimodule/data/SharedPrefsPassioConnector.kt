package ai.passio.nutrition.uimodule.data

import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.UserProfile
import ai.passio.nutrition.uimodule.ui.model.WaterRecord
import ai.passio.nutrition.uimodule.ui.model.WeightRecord
import ai.passio.passiosdk.passiofood.data.measurement.Unit
import ai.passio.passiosdk.passiofood.data.measurement.UnitEnergy
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import android.content.Context
import android.text.format.DateFormat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.joda.time.DateTime
import java.util.Calendar
import java.util.Date

internal val passioGson: Gson by lazy {
    GsonBuilder()
        .registerTypeAdapter(UnitMass::class.java, UnitMassSerializer())
        .registerTypeAdapter(Unit::class.java, UnitDeserializer())
        .registerTypeAdapter(UnitEnergy::class.java, UnitEnergySerializer())
        .create()
}

class SharedPrefsPassioConnector(context: Context) : PassioConnector {

    private val sharedPreferences = PassioDemoSharedPreferences(
        context.getSharedPreferences(PassioDemoSharedPreferences.PREF_NAME, 0)
    )
    private val dateFormat = "yyyyMMdd"
//    private lateinit var records: MutableList<FoodRecord>
    private lateinit var weightRecords: MutableList<WeightRecord>
    private lateinit var waterRecords: MutableList<WaterRecord>
//    private var userProfile: UserProfile = UserProfile()
    private lateinit var customFoods: MutableList<FoodRecord>
    private lateinit var recipes: MutableList<FoodRecord>

    override fun initialize() {
//        records = sharedPreferences.getRecords().map {
//            passioGson.fromJson(it, FoodRecord::class.java) as FoodRecord
//        }.toMutableList()
        customFoods = sharedPreferences.getCustomFoods().map {
            passioGson.fromJson(it, FoodRecord::class.java) as FoodRecord
        }.toMutableList()
        recipes = sharedPreferences.getRecipes().map {
            passioGson.fromJson(it, FoodRecord::class.java) as FoodRecord
        }.toMutableList()

        weightRecords = sharedPreferences.getWeightRecords().map {
            passioGson.fromJson(it, WeightRecord::class.java) as WeightRecord
        }.toMutableList()

        waterRecords = sharedPreferences.getWaterRecords().map {
            passioGson.fromJson(it, WaterRecord::class.java) as WaterRecord
        }.toMutableList()

//        passioGson.fromJson(sharedPreferences.getUserProfile(), UserProfile::class.java)?.let {
//            userProfile = it
//        }

    }

    fun getRecords(): MutableList<FoodRecord>
    {
        val records = sharedPreferences.getRecords().map {
            passioGson.fromJson(it, FoodRecord::class.java) as FoodRecord
        }.toMutableList()
        return records
    }

    suspend fun tempDelay()
    {
//        delay(1000)
    }

    override suspend fun updateRecord(foodRecord: FoodRecord): Boolean {
        tempDelay()
        val records = getRecords()
        val indexToRemove = records.indexOfFirst { it.uuid == foodRecord.uuid }
        if (indexToRemove != -1) {
            records.removeAt(indexToRemove)
            records.add(indexToRemove, foodRecord)
        } else {
            records.add(foodRecord)
        }
//        records.add(foodRecord)

        val json = records.map { passioGson.toJson(it) }
        sharedPreferences.saveRecords(json)
        return true
    }

    override suspend fun updateRecords(foodRecords: List<FoodRecord>): Boolean {
        tempDelay()
        val records = getRecords()
        foodRecords.forEach { foodRecord ->
            val indexToRemove = records.indexOfFirst { it.uuid == foodRecord.uuid }
            if (indexToRemove != -1) {
                records.removeAt(indexToRemove)
                records.add(indexToRemove, foodRecord)
            } else {
                records.add(foodRecord)
            }
        }
        val json = records.map { passioGson.toJson(it) }
        sharedPreferences.saveRecords(json)
        return true
    }

    override suspend fun deleteRecord(uuid: String): Boolean {
        tempDelay()
        val records = getRecords()
        val recordToDelete = records.find { it.uuid == uuid } ?: return false
        records.remove(recordToDelete)
        sharedPreferences.saveRecords(records.map { passioGson.toJson(it) })
        return true
    }

    override suspend fun fetchDayRecords(day: Date): List<FoodRecord> {
        tempDelay()
        val records = getRecords()
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

    override suspend fun fetchLogsRecords(startDate: Date, endDate: Date): List<FoodRecord> {
        tempDelay()
        val records = getRecords()
        val fromDate = DateTime(startDate.time).millis
        val toDate = DateTime(endDate.time).millis
        return records.filter { it.createdAtTime() in fromDate..toDate }
    }

    override suspend fun fetchAdherence(): List<Long> {
        tempDelay()
        val records = getRecords()
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
        tempDelay()
//        this.userProfile = userProfile
        sharedPreferences.saveUserProfile(passioGson.toJson(userProfile))
        return true
    }

    override suspend fun fetchUserProfile(): UserProfile {
        tempDelay()
        val userProfile = passioGson.fromJson(sharedPreferences.getUserProfile(), UserProfile::class.java)?: UserProfile()
        return userProfile
    }

    override suspend fun updateWeightRecord(weightRecord: WeightRecord): Boolean {
        tempDelay()
        val indexToRemove = weightRecords.indexOfFirst { it.uuid == weightRecord.uuid }
        if (indexToRemove != -1) {
            weightRecords.removeAt(indexToRemove)
            weightRecords.add(indexToRemove, weightRecord)
        } else {
            weightRecords.add(weightRecord)
        }
        val json = weightRecords.map { passioGson.toJson(it) }
        sharedPreferences.saveWeightRecords(json)
        return true
    }

    override suspend fun removeWeightRecord(weightRecord: WeightRecord): Boolean {
        tempDelay()
        val indexToRemove = weightRecords.indexOfFirst { it.uuid == weightRecord.uuid }
        if (indexToRemove != -1) {
            weightRecords.removeAt(indexToRemove)
        }
        val json = weightRecords.map { passioGson.toJson(it) }
        sharedPreferences.saveWeightRecords(json)
        return true
    }

    override suspend fun fetchWeightRecords(startDate: Date, endDate: Date): List<WeightRecord> {
        tempDelay()
        val startOfWeek = startDate.time
        val endOfWeek = endDate.time

        return weightRecords.filter { it.dateTime in startOfWeek..endOfWeek }
            .sortedByDescending { it.dateTime }
    }

    override suspend fun fetchLatestWeightRecord(): WeightRecord? {
        tempDelay()
        return weightRecords.maxByOrNull { it.dateTime }
    }

    override suspend fun updateWaterRecord(waterRecord: WaterRecord): Boolean {
        tempDelay()
        val indexToRemove = waterRecords.indexOfFirst { it.uuid == waterRecord.uuid }
        if (indexToRemove != -1) {
            waterRecords.removeAt(indexToRemove)
            waterRecords.add(indexToRemove, waterRecord)
        } else {
            waterRecords.add(waterRecord)
        }
        val json = waterRecords.map { passioGson.toJson(it) }
        sharedPreferences.saveWaterRecords(json)
        return true
    }

    override suspend fun removeWaterRecord(waterRecord: WaterRecord): Boolean {
        tempDelay()
        val indexToRemove = waterRecords.indexOfFirst { it.uuid == waterRecord.uuid }
        if (indexToRemove != -1) {
            waterRecords.removeAt(indexToRemove)
        }
        val json = waterRecords.map { passioGson.toJson(it) }
        sharedPreferences.saveWaterRecords(json)
        return true
    }

    override suspend fun fetchWaterRecords(startDate: Date, endDate: Date): List<WaterRecord> {
        tempDelay()
        val startOfWeek = startDate.time
        val endOfWeek = endDate.time

        return waterRecords.filter { it.dateTime in startOfWeek..endOfWeek }
            .sortedByDescending { it.dateTime }
    }

    override suspend fun saveCustomFood(foodRecord: FoodRecord): Boolean {
        tempDelay()
        val indexToRemove = customFoods.indexOfFirst { it.uuid == foodRecord.uuid }
        if (indexToRemove != -1) {
            customFoods.removeAt(indexToRemove)
            customFoods.add(indexToRemove, foodRecord)
        } else {
            customFoods.add(foodRecord)
        }
        val json = customFoods.map { passioGson.toJson(it) }
        sharedPreferences.saveCustomFoods(json)
        return true
    }

    override suspend fun fetchCustomFoods(): List<FoodRecord> {
        tempDelay()
        return customFoods
    }

    override suspend fun fetchCustomFoods(searchQuery: String): List<FoodRecord> {
        tempDelay()
        return customFoods.filter {
            it.name.trim().replace(" ", "").lowercase()
                .contains(searchQuery.trim().replace(" ", "").lowercase())
        }
    }

    override suspend fun fetchCustomFood(uuid: String): FoodRecord? {
        tempDelay()
        return customFoods.find { it.uuid == uuid }
    }

    override suspend fun deleteCustomFood(uuid: String): Boolean {
        tempDelay()
        val indexToRemove = customFoods.indexOfFirst { it.uuid == uuid }
        if (indexToRemove != -1) {
            customFoods.removeAt(indexToRemove)
        }
        val json = customFoods.map { passioGson.toJson(it) }
        sharedPreferences.saveCustomFoods(json)
        return true
    }

    override suspend fun getCustomFoodUsingBarcode(barcode: String): FoodRecord? {
        tempDelay()
        return customFoods.find { it.barcode == barcode }
    }

    override suspend fun saveRecipe(foodRecord: FoodRecord): Boolean {
        tempDelay()
        val indexToRemove = recipes.indexOfFirst { it.uuid == foodRecord.uuid }
        if (indexToRemove != -1) {
            recipes.removeAt(indexToRemove)
            recipes.add(indexToRemove, foodRecord)
        } else {
            recipes.add(foodRecord)
        }
        val json = recipes.map { passioGson.toJson(it) }
        sharedPreferences.saveRecipes(json)
        return true
    }

    override suspend fun fetchRecipes(searchQuery: String): List<FoodRecord> {
        tempDelay()
        return recipes.filter {
            it.name.trim().replace(" ", "").lowercase()
                .contains(searchQuery.trim().replace(" ", "").lowercase())
        }
    }

    override suspend fun fetchRecipes(): List<FoodRecord> {
        tempDelay()
        return recipes
    }

    override suspend fun fetchRecipe(uuid: String): FoodRecord? {
        tempDelay()
        return recipes.find { it.uuid == uuid }
    }

    override suspend fun deleteRecipe(uuid: String): Boolean {
        tempDelay()
        val indexToRemove = recipes.indexOfFirst { it.uuid == uuid }
        if (indexToRemove != -1) {
            recipes.removeAt(indexToRemove)
        }
        val json = recipes.map { passioGson.toJson(it) }
        sharedPreferences.saveRecipes(json)
        return true
    }
}