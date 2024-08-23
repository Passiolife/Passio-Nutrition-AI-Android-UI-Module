package ai.passio.nutrition.uimodule.ui.foodcreator

import ai.passio.nutrition.uimodule.domain.camera.CameraUseCase
import ai.passio.nutrition.uimodule.domain.camera.RecognitionResult
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.nutrition.uimodule.ui.util.StringKT.isValid
import ai.passio.passiosdk.core.camera.PassioCameraViewProvider
import ai.passio.passiosdk.passiofood.FoodDetectionConfiguration
import ai.passio.passiosdk.passiofood.PassioSDK
import androidx.camera.core.CameraSelector
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

enum class ScanBarcodeStatus {
    SCANNING,
    NEW_BARCODE,
    BARCODE_IN_SYSTEM,
    CUSTOM_FOOD_ALREADY_EXIST,
    NOT_FOUND
}

class ScanBarcodeViewModel : BaseViewModel() {

    private val cameraUseCase = CameraUseCase

    private var scanBarcodeStatus: ScanBarcodeStatus = ScanBarcodeStatus.SCANNING
    private val _scanBarcodeStatusEvent = SingleLiveEvent<ScanBarcodeStatus>()
    val scanBarcodeStatusEvent: LiveData<ScanBarcodeStatus> = _scanBarcodeStatusEvent

    private var foodRecord: FoodRecord? = null

    private fun stopDetection() {
        cameraUseCase.stopFoodDetection()
    }

    private var lastNoScanDetected = System.currentTimeMillis()
    private fun startOrUpdateDetection() {
        viewModelScope.launch {
            val config = FoodDetectionConfiguration(
                detectBarcodes = true,
                detectPackagedFood = false,
                detectVisual = false
            )
            recognitionFlow(config)

        }
    }

    private suspend fun recognitionFlow(config: FoodDetectionConfiguration) {
        scanBarcodeStatus = ScanBarcodeStatus.SCANNING
        _scanBarcodeStatusEvent.postValue(scanBarcodeStatus)
        cameraUseCase.recognitionFlow(config).collect { recognitionResult ->

            if (recognitionResult is RecognitionResult.FoodRecordRecognition && recognitionResult.foodItem.barcode.isValid()) {
                stopDetection()
                foodRecord = recognitionResult.foodItem
                scanBarcodeStatus = ScanBarcodeStatus.NEW_BARCODE
                validateBarcode()
            }

            /*if (recognitionResult == RecognitionResult.NoRecognition) {
                if (System.currentTimeMillis() - lastNoScanDetected > 5000) {
                    scanBarcodeStatus = ScanBarcodeStatus.NOT_FOUND
                    _scanBarcodeStatusEvent.postValue(scanBarcodeStatus)
                }
            } else {
                lastNoScanDetected = System.currentTimeMillis()
                if (recognitionResult is RecognitionResult.FoodRecordRecognition && recognitionResult.foodItem.barcode.isValid()) {
                    stopDetection()
                    foodRecord = recognitionResult.foodItem
                    scanBarcodeStatus = ScanBarcodeStatus.BARCODE_IN_SYSTEM //NEW_BARCODE
                    validateBarcode()
                }
            }*/
        }
    }


    fun startRecognitionSession(cameraViewProvider: PassioCameraViewProvider) {

        viewModelScope.launch {
            PassioSDK.instance.startCamera(
                cameraViewProvider,
                displayRotation = 0,
                cameraFacing = CameraSelector.LENS_FACING_BACK,
                tapToFocus = true
            )
            startOrUpdateDetection()
        }
    }


    fun stopRecognitionSession() {
        PassioSDK.instance.stopCamera()
    }

    private fun validateBarcode() {
        _scanBarcodeStatusEvent.postValue(scanBarcodeStatus)
    }

    fun geBarcode(): String {
        return foodRecord?.barcode ?: ""
    }


    fun getFoodRecord(): FoodRecord? {
        return foodRecord
    }

}