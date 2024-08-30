package ai.passio.nutrition.uimodule.domain.camera

import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.passiosdk.passiofood.DetectedCandidate
import ai.passio.passiosdk.passiofood.nutritionfacts.PassioNutritionFacts

sealed class RecognitionResult {
    data class VisualRecognition(val visualCandidate: DetectedCandidate) : RecognitionResult()
    data class FoodRecordRecognition(val foodItem: FoodRecord) : RecognitionResult()
    data class NutritionFactRecognition(val nutritionFactsPair: Pair<PassioNutritionFacts?, String>) : RecognitionResult()
    object NoProductRecognition : RecognitionResult()
    object NoRecognition : RecognitionResult()
}