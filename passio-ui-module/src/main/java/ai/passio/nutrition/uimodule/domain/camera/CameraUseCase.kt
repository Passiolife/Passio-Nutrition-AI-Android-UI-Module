package ai.passio.nutrition.uimodule.domain.camera

import ai.passio.nutrition.uimodule.data.Repository
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.MealLabel
import ai.passio.passiosdk.passiofood.FoodCandidates
import ai.passio.passiosdk.passiofood.FoodDetectionConfiguration
import ai.passio.passiosdk.passiofood.PassioID
import ai.passio.passiosdk.passiofood.data.model.PassioFoodItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date

object CameraUseCase {

    private val repository = Repository.getInstance()

    fun recognitionFlow(config: FoodDetectionConfiguration): Flow<RecognitionResult> {
        return repository.recognitionResultFlow(config).map { mapRecognitionResult(it) }
    }

    suspend fun fetchFoodItemForPassioID(passioID: PassioID): PassioFoodItem? {
        return repository.fetchFoodItemForPassioID(passioID)
    }


    suspend fun logFoodRecord(record: FoodRecord): Boolean {
        record.create(Date().time)
        val mealLabel = MealLabel.dateToMealLabel(record.createdAtTime()!!)
        record.mealLabel = mealLabel
        return repository.logFoodRecord(record)
    }

    fun stopFoodDetection() {
        repository.stopFoodDetection()
    }

    private suspend fun mapRecognitionResult(candidates: FoodCandidates?): RecognitionResult {
        if (candidates == null) {
            return RecognitionResult.NoRecognition
        }

        val visualCandidate =
            candidates.detectedCandidates?.firstOrNull() //maxByOrNull { it.confidence }
        if (visualCandidate != null) {
            return RecognitionResult.VisualRecognition(visualCandidate)
        }

        val barcodeCandidate =
            candidates.barcodeCandidates?.firstOrNull() //maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
        if (barcodeCandidate != null) {
            val foodItem = repository.fetchFoodItemForProduct(barcodeCandidate.barcode)
                ?: return RecognitionResult.NoProductRecognition
            return RecognitionResult.ProductRecognition(foodItem)
        }

        val packagedCandidate =
            candidates.packagedFoodCandidates?.firstOrNull() //maxByOrNull { it.confidence }
        if (packagedCandidate != null) {
            val foodItem = repository.fetchFoodItemForProduct(packagedCandidate.packagedFoodCode)
                ?: return RecognitionResult.NoProductRecognition
            return RecognitionResult.ProductRecognition(foodItem)
        }

        return RecognitionResult.NoRecognition
    }
}