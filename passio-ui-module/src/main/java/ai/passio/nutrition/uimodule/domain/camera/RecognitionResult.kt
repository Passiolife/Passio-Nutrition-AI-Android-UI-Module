package ai.passio.nutrition.uimodule.domain.camera

import ai.passio.nutrition.uimodule.ui.navigation.NavigationCommand
import ai.passio.passiosdk.passiofood.DetectedCandidate
import ai.passio.passiosdk.passiofood.data.model.PassioFoodItem
import androidx.navigation.NavDirections

sealed class RecognitionResult {
    data class VisualRecognition(val visualCandidate: DetectedCandidate) : RecognitionResult()
    data class ProductRecognition(val foodItem: PassioFoodItem) : RecognitionResult()
    object NoProductRecognition : RecognitionResult()
    object NoRecognition : RecognitionResult()
}