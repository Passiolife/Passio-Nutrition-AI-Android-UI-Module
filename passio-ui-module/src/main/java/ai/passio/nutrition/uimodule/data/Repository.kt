package ai.passio.nutrition.uimodule.data

import ai.passio.nutrition.uimodule.ui.activity.UserCache
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.UserProfile
import ai.passio.nutrition.uimodule.ui.model.WaterRecord
import ai.passio.nutrition.uimodule.ui.model.WeightRecord
import ai.passio.nutrition.uimodule.ui.progress.TimePeriod
import ai.passio.nutrition.uimodule.ui.util.StringKT.isValid
import ai.passio.nutrition.uimodule.ui.util.getBefore30Days
import ai.passio.nutrition.uimodule.ui.util.getEndOfMonth
import ai.passio.nutrition.uimodule.ui.util.getEndOfWeek
import ai.passio.nutrition.uimodule.ui.util.getStartOfMonth
import ai.passio.nutrition.uimodule.ui.util.getStartOfWeek
import ai.passio.passiosdk.passiofood.FoodCandidates
import ai.passio.passiosdk.passiofood.FoodDetectionConfiguration
import ai.passio.passiosdk.passiofood.FoodRecognitionListener
import ai.passio.passiosdk.passiofood.NutritionFactsRecognitionListener
import ai.passio.passiosdk.passiofood.PassioFoodDataInfo
import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.passiosdk.passiofood.data.model.PassioFoodItem
import ai.passio.passiosdk.passiofood.nutritionfacts.PassioNutritionFacts
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.joda.time.DateTime
import java.util.Date
import kotlin.coroutines.suspendCoroutine

class Repository private constructor() {

    companion object {
        private lateinit var sharedPrefsPassioConnector: SharedPrefsPassioConnector

        @Volatile
        private var instance: Repository? = null

        fun getInstance(): Repository =
            instance ?: synchronized(this) {
                instance ?: Repository().also { instance = it }
            }

        fun create(context: Context, connector: PassioConnector) {
            sharedPrefsPassioConnector = SharedPrefsPassioConnector(context)
            SharedPrefUtils.init(context)
            getInstance().connector = connector
            connector.initialize()
        }
    }

    private lateinit var connector: PassioConnector


    suspend fun migrateDataFromOldSharedPrefsPassioConnector(): Boolean {

        if (sharedPrefsPassioConnector.isMigrationNeeded()) {
            sharedPrefsPassioConnector.initialize()
            //migrate user profile
            updateUser(sharedPrefsPassioConnector.fetchUserProfile())
            Log.d("DATA MIGRATION", "Done migrating user profile")

            //migrate records
            logFoodRecords(sharedPrefsPassioConnector.getRecords())
            Log.d("DATA MIGRATION", "Done migrating food logs records")

            //migrate custom foods
            sharedPrefsPassioConnector.fetchCustomFoods().forEach {
                saveCustomFood(it)
            }
            Log.d("DATA MIGRATION", "Done migrating custom foods")

            //migrate recipes
            sharedPrefsPassioConnector.fetchRecipes().forEach {
                saveRecipe(it)
            }
            Log.d("DATA MIGRATION", "Done migrating recipes")

            //migrate water records
            sharedPrefsPassioConnector.fetchAllWaterRecords().forEach {
                updateWater(it)
            }
            Log.d("DATA MIGRATION", "Done migrating water records")

            //migrate weight records
            sharedPrefsPassioConnector.fetchAllWeightRecords().forEach {
                updateWeight(it)
            }
            Log.d("DATA MIGRATION", "Done migrating weight records")

            sharedPrefsPassioConnector.markDoneMigration()
            Log.d("DATA MIGRATION", "Done migrating all data")
        }
        return true

    }

    suspend fun fetchPassioFoodItem(
        searchResult: PassioFoodDataInfo,
        weighGrams: Double? = null
    ): PassioFoodItem? = suspendCoroutine { cont ->
        PassioSDK.instance.fetchFoodItemForDataInfo(searchResult, weighGrams) { foodItem ->
            cont.resumeWith(Result.success(foodItem))
        }
    }

    suspend fun fetchSearchResults(
        query: String
    ): Pair<List<PassioFoodDataInfo>, List<String>> = suspendCoroutine { cont ->
        PassioSDK.instance.searchForFood(query) { result, searchOptions ->
            cont.resumeWith(Result.success(result to searchOptions))
        }
    }

    fun stopFoodDetection() {
        PassioSDK.instance.stopFoodDetection()
        PassioSDK.instance.stopNutritionFactsDetection()
    }

    fun nutritionFactsResultFlow(): Flow<Pair<PassioNutritionFacts?, String>> = callbackFlow {
        stopFoodDetection()
        val callback = object : NutritionFactsRecognitionListener {
            override fun onRecognitionResult(nutritionFacts: PassioNutritionFacts?, text: String) {
                trySendBlocking(nutritionFacts to text)
            }
        }
        PassioSDK.instance.startNutritionFactsDetection(callback)

        awaitClose {
            PassioSDK.instance.stopNutritionFactsDetection()
        }
    }

    fun recognitionResultFlow(
        config: FoodDetectionConfiguration
    ): Flow<FoodCandidates?> = callbackFlow {
        stopFoodDetection()
        val callback = object : FoodRecognitionListener {
            override fun onRecognitionResults(candidates: FoodCandidates?, image: Bitmap?) {
                trySendBlocking(candidates)
            }
        }
        PassioSDK.instance.startFoodDetection(callback, config)

        awaitClose {
            PassioSDK.instance.stopFoodDetection()
        }
    }

    suspend fun fetchFoodItemForProduct(productCode: String): PassioFoodItem? =
        suspendCoroutine { cont ->
            PassioSDK.instance.fetchFoodItemForProductCode(productCode) { foodItem ->
                cont.resumeWith(Result.success(foodItem))
            }
        }

    suspend fun fetchFoodItemForPassioID(passioID: String): PassioFoodItem? =
        suspendCoroutine { cont ->
            PassioSDK.instance.fetchFoodItemForPassioID(passioID) { foodItem ->
                cont.resumeWith(Result.success(foodItem))
            }
        }

    suspend fun logFoodRecord(record: FoodRecord): Boolean {
        return connector.updateRecord(record)
    }

    suspend fun logFoodRecords(records: List<FoodRecord>): Boolean {
        return connector.updateRecords(records)
    }

    suspend fun deleteFoodRecord(uuid: String): Boolean {
        return connector.deleteRecord(uuid)
    }

    suspend fun getLogsForDay(day: Date): List<FoodRecord> {
        return connector.fetchDayRecords(day)
    }

    suspend fun getLogsForWeek(day: Date): List<FoodRecord> {
        val today = DateTime(day.time)
        val startOfWeek = getStartOfWeek(today)//.millis
        val endOfWeek = getEndOfWeek(today)//.millis
        return connector.fetchLogsRecords(startOfWeek.toDate(), endOfWeek.toDate())
    }

    suspend fun getLogsForMonth(day: Date): List<FoodRecord> {

        val today = DateTime(day.time)
        val startOfMonth = getStartOfMonth(today)//.millis
        val endOfMonth = getEndOfMonth(today)//.millis

        return connector.fetchLogsRecords(startOfMonth.toDate(), endOfMonth.toDate())
    }


    suspend fun getLogsForLast30Days(): List<FoodRecord> {
        val today = DateTime()
        val before30Days = getBefore30Days(today)
        return connector.fetchLogsRecords(before30Days.toDate(), today.toDate())
    }

    suspend fun fetchAdherence(): List<Long> {
        return connector.fetchAdherence()
    }

    suspend fun updateUser(userProfile: UserProfile): Boolean {
        UserCache.setProfile(userProfile)
        return connector.updateUserProfile(userProfile)
    }

    suspend fun getUser(): UserProfile {
        val userProfile = connector.fetchUserProfile()
        UserCache.setProfile(userProfile)
        return userProfile
    }


    suspend fun updateWeight(weightRecord: WeightRecord): Boolean {
        return connector.updateWeightRecord(weightRecord)
    }

    suspend fun removeWeightRecord(weightRecord: WeightRecord): Boolean {
        return connector.removeWeightRecord(weightRecord)
    }

    suspend fun fetchLatestWeightRecord(): WeightRecord? {
        return connector.fetchLatestWeightRecord()
    }

    suspend fun fetchWeightRecords(currentDate: Date, timePeriod: TimePeriod): List<WeightRecord> {
        val today = DateTime(currentDate.time)
        val startDate: DateTime
        val endDate: DateTime
        if (timePeriod == TimePeriod.MONTH) {
            startDate = getStartOfMonth(today)
            endDate = getEndOfMonth(today)
        } else {
            startDate = getStartOfWeek(today)
            endDate = getEndOfWeek(today)
        }
        return connector.fetchWeightRecords(startDate.toDate(), endDate.toDate())
    }

    suspend fun updateWater(waterRecord: WaterRecord): Boolean {
        return connector.updateWaterRecord(waterRecord)
    }

    suspend fun removeWaterRecord(waterRecord: WaterRecord): Boolean {
        return connector.removeWaterRecord(waterRecord)
    }

    suspend fun fetchWaterRecords(currentDate: Date): List<WaterRecord> {
        val forDate = DateTime(currentDate.time)
        val startDate: DateTime = forDate.withTimeAtStartOfDay()
        val endDate: DateTime = forDate.withTime(23, 59, 59, 999)
        return connector.fetchWaterRecords(startDate.toDate(), endDate.toDate())
    }

    suspend fun fetchWaterRecords(currentDate: Date, timePeriod: TimePeriod): List<WaterRecord> {
        val today = DateTime(currentDate.time)
        val startDate: DateTime
        val endDate: DateTime
        if (timePeriod == TimePeriod.MONTH) {
            startDate = getStartOfMonth(today)
            endDate = getEndOfMonth(today)
        } else {
            startDate = getStartOfWeek(today)
            endDate = getEndOfWeek(today)
        }
        return connector.fetchWaterRecords(startDate.toDate(), endDate.toDate())
    }

    suspend fun saveCustomFood(record: FoodRecord): Boolean {
        return connector.saveCustomFood(record)
    }

    suspend fun fetchCustomFoods(): List<FoodRecord> {
        return connector.fetchCustomFoods()
    }

    suspend fun fetchCustomFoods(searchQuery: String): List<FoodRecord> {
        return if (!searchQuery.isValid()) {
            emptyList()
        } else {
            connector.fetchCustomFoods(searchQuery)
        }
    }

    suspend fun fetchCustomFood(uuid: String): FoodRecord? {
        return connector.fetchCustomFood(uuid)
    }

    suspend fun deleteCustomFood(uuid: String): Boolean {
        return connector.deleteCustomFood(uuid)
    }

    suspend fun getCustomFoodUsingBarcode(barcode: String): FoodRecord? {
        return connector.getCustomFoodUsingBarcode(barcode)
    }


    suspend fun saveRecipe(record: FoodRecord): Boolean {
        return connector.saveRecipe(record)
    }

    suspend fun fetchRecipes(): List<FoodRecord> {
        return connector.fetchRecipes()
    }

    suspend fun fetchRecipes(searchQuery: String): List<FoodRecord> {
        return if (!searchQuery.isValid()) {
            emptyList()
        } else {
            connector.fetchRecipes(searchQuery)
        }
    }

    suspend fun fetchRecipe(id: String): FoodRecord? {
        return connector.fetchRecipe(id)
    }

    suspend fun deleteRecipe(uuid: String): Boolean {
        return connector.deleteRecipe(uuid)
    }

}