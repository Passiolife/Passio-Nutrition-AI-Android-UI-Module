package ai.passio.nutrition.uimodule.domain.camera

import ai.passio.passiosdk.passiofood.DetectedCandidate
import ai.passio.passiosdk.passiofood.data.model.PassioFoodItem
import ai.passio.passiosdk.passiofood.nutritionfacts.PassioNutritionFacts

sealed class RecognitionResult {
    data class VisualRecognition(val visualCandidate: DetectedCandidate) : RecognitionResult()
    data class ProductRecognition(val foodItem: PassioFoodItem) : RecognitionResult()
    data class NutritionFactRecognition(val nutritionFactsPair: Pair<PassioNutritionFacts?, String>) : RecognitionResult()
    object NoProductRecognition : RecognitionResult()
    object NoRecognition : RecognitionResult()
}