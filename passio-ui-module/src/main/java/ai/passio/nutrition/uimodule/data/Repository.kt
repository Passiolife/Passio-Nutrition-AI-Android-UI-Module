package ai.passio.nutrition.uimodule.data

import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.passiosdk.passiofood.FoodCandidates
import ai.passio.passiosdk.passiofood.FoodDetectionConfiguration
import ai.passio.passiosdk.passiofood.FoodRecognitionListener
import ai.passio.passiosdk.passiofood.PassioFoodDataInfo
import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.passiosdk.passiofood.data.model.PassioFoodItem
import ai.passio.passiosdk.passiofood.nutritionfacts.PassioNutritionFacts
import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Date
import kotlin.coroutines.suspendCoroutine

class Repository private constructor() {

    companion object {

        @Volatile
        private var instance: Repository? = null

        fun getInstance(): Repository =
            instance ?: synchronized(this) {
                instance ?: Repository().also { instance = it }
            }

        fun create(context: Context) {
            getInstance().connector = SharedPrefsPassioConnector(context).apply {
                initialize()
            }
        }
    }

    private lateinit var connector: PassioConnector

    suspend fun fetchPassioFoodItem(
        searchResult: PassioFoodDataInfo
    ): PassioFoodItem? = suspendCoroutine { cont ->
        PassioSDK.instance.fetchFoodItemForDataInfo(searchResult) { foodItem ->
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
    }

    fun recognitionResultFlow(
        config: FoodDetectionConfiguration
    ): Flow<FoodCandidates?> = callbackFlow {
        PassioSDK.instance.stopFoodDetection()
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

    suspend fun deleteFoodRecord(record: FoodRecord): Boolean {
        return connector.deleteRecord(record)
    }

    suspend fun getLogsForDay(day: Date): List<FoodRecord> {
        return connector.fetchDayRecords(day)
    }

    suspend fun getLogsForWeek(day: Date): List<FoodRecord> {
        return connector.fetchWeekRecords(day)
    }
    suspend fun getLogsForMonth(day: Date): List<FoodRecord> {
        return connector.fetchMonthRecords(day)
    }

}