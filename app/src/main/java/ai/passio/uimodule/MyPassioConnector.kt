package ai.passio.uimodule

import ai.passio.nutrition.uimodule.PassioNutrientsExclusionStrategy
import ai.passio.nutrition.uimodule.data.PassioConnector
import ai.passio.nutrition.uimodule.data.UnitEnergySerializer
import ai.passio.nutrition.uimodule.data.UnitMassSerializer
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.UserProfile
import ai.passio.nutrition.uimodule.ui.model.WaterRecord
import ai.passio.nutrition.uimodule.ui.model.WeightRecord
import ai.passio.passiosdk.passiofood.data.measurement.UnitEnergy
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import android.content.Context
import android.graphics.Bitmap
import android.text.format.DateFormat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.Date

internal val myGson: Gson by lazy {
    GsonBuilder()
        .registerTypeAdapter(UnitMass::class.java, UnitMassSerializer())
//        .registerTypeAdapter(Unit::class.java, UnitDeserializer())
        .registerTypeAdapter(UnitEnergy::class.java, UnitEnergySerializer())
        .setExclusionStrategies(PassioNutrientsExclusionStrategy())
        .create()
}

internal class MyPassioConnector(context: Context) : PassioConnector {

    private val sharedPreferences = DemoSharedPreferences(
        context.getSharedPreferences(DemoSharedPreferences.PREF_NAME, 0)
    )
    private val dateFormat = "yyyyMMdd"
    private lateinit var weightRecords: MutableList<WeightRecord>
    private lateinit var waterRecords: MutableList<WaterRecord>
    private lateinit var customFoods: MutableList<FoodRecord>
    private lateinit var recipes: MutableList<FoodRecord>
    private lateinit var favorites: MutableList<FoodRecord>

    override fun initialize() {
        customFoods = sharedPreferences.getCustomFoods().map {
            myGson.fromJson(it, FoodRecord::class.java) as FoodRecord
        }.toMutableList()
        recipes = sharedPreferences.getRecipes().map {
            myGson.fromJson(it, FoodRecord::class.java) as FoodRecord
        }.toMutableList()

        weightRecords = sharedPreferences.getWeightRecords().map {
            myGson.fromJson(it, WeightRecord::class.java) as WeightRecord
        }.toMutableList()

        waterRecords = sharedPreferences.getWaterRecords().map {
            myGson.fromJson(it, WaterRecord::class.java) as WaterRecord
        }.toMutableList()
        favorites = sharedPreferences.getFavorites().map {
            myGson.fromJson(it, FoodRecord::class.java) as FoodRecord
        }.toMutableList()

    }

    private fun getRecords(): MutableList<FoodRecord> {
        val records = sharedPreferences.getRecords().map {
            myGson.fromJson(it, FoodRecord::class.java) as FoodRecord
        }.toMutableList()
        return records
    }

    override suspend fun updateRecord(foodRecord: FoodRecord): Boolean {
        val records = getRecords()
        val indexToRemove = records.indexOfFirst { it.uuid == foodRecord.uuid }
        if (indexToRemove != -1) {
            records.removeAt(indexToRemove)
            records.add(indexToRemove, foodRecord)
        } else {
            records.add(foodRecord)
        }
//        records.add(foodRecord)

        val json = records.map { myGson.toJson(it) }
        sharedPreferences.saveRecords(json)
        return true
    }

    /*override suspend fun updateRecords(foodRecords: List<FoodRecord>): Boolean {
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
        val json = records.map { myGson.toJson(it) }
        sharedPreferences.saveRecords(json)
        return true
    }*/

    override suspend fun deleteRecord(foodRecord: FoodRecord): Boolean {
        val records = getRecords()
        val recordToDelete = records.find { it.uuid == foodRecord.uuid } ?: return false
        records.remove(recordToDelete)
        sharedPreferences.saveRecords(records.map { myGson.toJson(it) })
        return true
    }

    override suspend fun fetchDayRecords(day: Date): List<FoodRecord> {
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

    override suspend fun fetchDayLogFor(startDate: Date, endDate: Date): List<FoodRecord> {
        val records = getRecords()

        val fromDate = startDate.time
        val toDate = endDate.time
        return records.filter { it.createdAtTime() in fromDate..toDate }
    }

    override suspend fun updateUserProfile(userProfile: UserProfile): Boolean {
//        this.userProfile = userProfile
        sharedPreferences.saveUserProfile(myGson.toJson(userProfile))
        return true
    }

    override suspend fun fetchUserProfile(): UserProfile {
        val userProfile =
            myGson.fromJson(sharedPreferences.getUserProfile(), UserProfile::class.java)
                ?: UserProfile()
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
        val json = weightRecords.map { myGson.toJson(it) }
        sharedPreferences.saveWeightRecords(json)
        return true
    }

    override suspend fun deleteWeightRecord(weightRecord: WeightRecord): Boolean {
        val indexToRemove = weightRecords.indexOfFirst { it.uuid == weightRecord.uuid }
        if (indexToRemove != -1) {
            weightRecords.removeAt(indexToRemove)
        }
        val json = weightRecords.map { myGson.toJson(it) }
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
        val json = waterRecords.map { myGson.toJson(it) }
        sharedPreferences.saveWaterRecords(json)
        return true
    }

    override suspend fun deleteWaterRecord(waterRecord: WaterRecord): Boolean {
        val indexToRemove = waterRecords.indexOfFirst { it.uuid == waterRecord.uuid }
        if (indexToRemove != -1) {
            waterRecords.removeAt(indexToRemove)
        }
        val json = waterRecords.map { myGson.toJson(it) }
        sharedPreferences.saveWaterRecords(json)
        return true
    }

    override suspend fun fetchWaterRecords(startDate: Date, endDate: Date): List<WaterRecord> {
        val startOfWeek = startDate.time
        val endOfWeek = endDate.time

        return waterRecords.filter { it.dateTime in startOfWeek..endOfWeek }
            .sortedByDescending { it.dateTime }
    }

    override suspend fun updateUserFood(foodRecord: FoodRecord): Boolean {
        val indexToRemove = customFoods.indexOfFirst { it.uuid == foodRecord.uuid }
        if (indexToRemove != -1) {
            customFoods.removeAt(indexToRemove)
            customFoods.add(indexToRemove, foodRecord)
        } else {
            customFoods.add(foodRecord)
        }
        val json = customFoods.map { myGson.toJson(it) }
        sharedPreferences.saveCustomFoods(json)
        return true
    }

    override suspend fun fetchAllUserFoods(): List<FoodRecord> {
        return customFoods
    }

    override suspend fun fetchAllUserFoodsMatching(searchQuery: String): List<FoodRecord> {
        return customFoods.filter {
            it.name.trim().replace(" ", "").lowercase()
                .contains(searchQuery.trim().replace(" ", "").lowercase())
        }
    }

    override suspend fun fetchUserFood(uuid: String): FoodRecord? {
        return customFoods.find { it.uuid == uuid }
    }

    override suspend fun deleteUserFood(foodRecord: FoodRecord): Boolean {
        val indexToRemove = customFoods.indexOfFirst { it.uuid == foodRecord.uuid }
        if (indexToRemove != -1) {
            customFoods.removeAt(indexToRemove)
        }
        val json = customFoods.map { myGson.toJson(it) }
        sharedPreferences.saveCustomFoods(json)
        return true
    }

    override suspend fun fetchUserFoodsForBarcode(barcode: String): FoodRecord? {
        return customFoods.find { it.barcode == barcode }
    }

    override suspend fun updateRecipe(foodRecord: FoodRecord): Boolean {
        val indexToRemove = recipes.indexOfFirst { it.uuid == foodRecord.uuid }
        if (indexToRemove != -1) {
            recipes.removeAt(indexToRemove)
            recipes.add(indexToRemove, foodRecord)
        } else {
            recipes.add(foodRecord)
        }
        val json = recipes.map { myGson.toJson(it) }
        sharedPreferences.saveRecipes(json)
        return true
    }

    override suspend fun fetchRecipes(searchQuery: String): List<FoodRecord> {
        return recipes.filter {
            it.name.trim().replace(" ", "").lowercase()
                .contains(searchQuery.trim().replace(" ", "").lowercase())
        }
    }

    override suspend fun fetchRecipes(): List<FoodRecord> {
        return recipes
    }

    override suspend fun fetchRecipe(uuid: String): FoodRecord? {
        return recipes.find { it.uuid == uuid }
    }

    override suspend fun deleteRecipe(foodRecord: FoodRecord): Boolean {
        val indexToRemove = recipes.indexOfFirst { it.uuid == foodRecord.uuid }
        if (indexToRemove != -1) {
            recipes.removeAt(indexToRemove)
        }
        val json = recipes.map { myGson.toJson(it) }
        sharedPreferences.saveRecipes(json)
        return true
    }

    override suspend fun updateFavorite(foodRecord: FoodRecord): Boolean {
        favorites.find { it.refCode == foodRecord.refCode }?.let {
            favorites.remove(it)
        }
        favorites.add(foodRecord)
        val json = favorites.map { myGson.toJson(it) }
        sharedPreferences.saveFavorites(json)
        return true
    }

    override suspend fun deleteFavorite(foodRecord: FoodRecord): Boolean {
        favorites.find { it.refCode == foodRecord.refCode }?.let {
            favorites.remove(it)
        }
        val json = favorites.map { myGson.toJson(it) }
        sharedPreferences.saveFavorites(json)
        return true
    }

    override suspend fun fetchFavorites(): List<FoodRecord> {
        return favorites
    }

    override suspend fun isFavorite(foodRecord: FoodRecord): Boolean {
        return favorites.find { it.refCode == foodRecord.refCode } != null
    }

    override suspend fun updateUserFoodImage(iconId: String, bitmap: Bitmap): Boolean {
        return true
    }
    override suspend fun fetchUserFoodImage(iconId: String): Bitmap? {
        return null
    }

    override suspend fun deleteUserFoodImage(iconId: String): Boolean {
        return true
    }

}