package ai.passio.nutrition.uimodule.ui.repository

import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.passiosdk.passiofood.PassioID
import ai.passio.passiosdk.passiofood.PassioSDK
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.suspendCoroutine

object FoodRecordRepository {

    suspend fun fetchRecordForPassioID(passioID: PassioID): FoodRecord? = suspendCancellableCoroutine { cont ->
        PassioSDK.instance.fetchFoodItemForPassioID(passioID) { foodItem ->
            if (foodItem == null) {
                cont.resumeWith(Result.success(null))
                return@fetchFoodItemForPassioID
            }

            val foodRecord = FoodRecord(foodItem)
            cont.resumeWith(Result.success(foodRecord))
        }
    }

    suspend fun fetchRecordForProductCode(productCode: String): FoodRecord? = suspendCancellableCoroutine { cont ->
        PassioSDK.instance.fetchFoodItemForProductCode(productCode) { foodItem ->
            if (foodItem == null) {
                cont.resumeWith(Result.success(null))
                return@fetchFoodItemForProductCode
            }

            val foodRecord = FoodRecord(foodItem)
            cont.resumeWith(Result.success(foodRecord))
        }
    }
}