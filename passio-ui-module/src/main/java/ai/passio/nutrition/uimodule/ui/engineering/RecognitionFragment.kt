package ai.passio.nutrition.uimodule.ui.engineering

import ai.passio.nutrition.uimodule.databinding.FragmentRecognitionBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.passiosdk.passiofood.FoodCandidates
import ai.passio.passiosdk.passiofood.FoodDetectionConfiguration
import ai.passio.passiosdk.passiofood.FoodRecognitionListener
import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.passiosdk.passiofood.fragment.PassioCameraFragment
import ai.passio.passiosdk.passiofood.nutritionfacts.PassioNutritionFacts
import android.graphics.Bitmap
import android.widget.Toast
import androidx.camera.view.PreviewView
import java.lang.IllegalStateException

class RecognitionFragment(
    private val mode: Int
) : PassioCameraFragment(true), FoodRecognitionListener {

    private var _binding: FragmentRecognitionBinding? = null
    private val binding get() = _binding!!

    override fun getPreviewView(): PreviewView = binding.previewView

    override fun onCameraPermissionDenied() {
        Toast.makeText(
            requireContext(),
            "The app requires Camera Permission to run recognition",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onCameraReady() {
        val detectionConfig = when (mode) {
            0 -> FoodDetectionConfiguration(detectVisual = true)
            1 -> FoodDetectionConfiguration(detectVisual = false, detectBarcodes = true)
            2 -> FoodDetectionConfiguration(detectVisual = false, detectPackagedFood = true)
            else -> throw IllegalStateException("No known recognition mode: $mode")
        }
        PassioSDK.instance.startFoodDetection(this, detectionConfig)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecognitionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (mode == 3) {
            binding.nutritionFactsLayout.visibility = View.VISIBLE
            binding.visualLayout.visibility = View.GONE
        } else {
            binding.nutritionFactsLayout.visibility = View.GONE
            binding.visualLayout.visibility = View.VISIBLE
        }
    }

    override fun onStop() {
        super.onStop()
        PassioSDK.instance.stopFoodDetection()
    }

    override fun onRecognitionResults(candidates: FoodCandidates?, image: Bitmap?) {
        with(binding) {
            if (mode != 3) {
                result1.text = ""
                result2.text = ""
                result3.text = ""

                candidates?.detectedCandidates?.forEachIndexed { index, detectedCandidate ->
                    when (index) {
                        0 -> result1.text = detectedCandidate.foodName.capitalize() + " (${detectedCandidate.confidence})"
                        1 -> result2.text = detectedCandidate.foodName.capitalize() + " (${detectedCandidate.confidence})"
                        2 -> result3.text = detectedCandidate.foodName.capitalize() + " (${detectedCandidate.confidence})"
                    }
                }

                candidates?.barcodeCandidates?.forEachIndexed { index, barcodeCandidate ->
                    PassioSDK.instance.fetchFoodItemForProductCode(barcodeCandidate.barcode) { fi ->
                        if (fi == null) return@fetchFoodItemForProductCode

                        when (index) {
                            0 -> result1.text = fi.name.capitalize()
                            1 -> result2.text = fi.name.capitalize()
                            2 -> result3.text = fi.name.capitalize()
                        }
                    }
                }

                candidates?.packagedFoodCandidates?.forEachIndexed { index, packageFoodCandidate ->
                    PassioSDK.instance.fetchFoodItemForProductCode(packageFoodCandidate.packagedFoodCode) { fi ->
                        if (fi == null) return@fetchFoodItemForProductCode

                        when (index) {
                            0 -> result1.text = fi.name.capitalize()
                            1 -> result2.text = fi.name.capitalize()
                            2 -> result3.text = fi.name.capitalize()
                        }
                    }
                }
            } else {
//                servingValue.text = "?"
//                caloriesValue.text = "?"
//                carbsValue.text = "?"
//                proteinValue.text = "?"
//                fatValue.text = "?"
//
//                if (nutritionFacts == null) return@with
//
//                if (nutritionFacts.servingSizeQuantity != null && nutritionFacts.servingSizeUnitName != null) {
//                    servingValue.text = "${nutritionFacts.servingSizeQuantity} ${nutritionFacts.servingSizeUnitName}"
//                }
//                caloriesValue.text = nutritionFacts.calories?.toString() ?: "?"
//                carbsValue.text = nutritionFacts.carbs?.toString() ?: "?"
//                proteinValue.text = nutritionFacts.protein?.toString() ?: "?"
//                fatValue.text = nutritionFacts.fat?.toString() ?: "?"
            }
        }
    }
}