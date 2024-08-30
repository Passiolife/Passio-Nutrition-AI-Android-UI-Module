package ai.passio.nutrition.uimodule.ui.foodcreator

import ai.passio.nutrition.uimodule.domain.customfood.CustomFoodUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.nutrition.uimodule.ui.util.StringKT.isValid
import ai.passio.passiosdk.core.camera.PassioCameraViewProvider
import ai.passio.passiosdk.passiofood.Barcode
import ai.passio.passiosdk.passiofood.FoodDetectionConfiguration
import ai.passio.passiosdk.passiofood.PassioSDK
import androidx.camera.core.CameraSelector
import androidx.lifecycle.LiveData
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

    private val useCase = CustomFoodUseCase

    private var scanBarcodeStatus: ScanBarcodeStatus = ScanBarcodeStatus.SCANNING
    private val _scanBarcodeStatusEvent = SingleLiveEvent<ScanBarcodeStatus>()
    val scanBarcodeStatusEvent: LiveData<ScanBarcodeStatus> = _scanBarcodeStatusEvent

    //    private var foodRecord: FoodRecord? = null
    private var barcode: Barcode? = null

    var existingSystemItem: FoodRecord? = null
    var existingCustomFood: FoodRecord? = null

    private fun stopDetection() {
        useCase.stopFoodDetection()
    }

    private val config = FoodDetectionConfiguration(
        detectBarcodes = true,
        detectPackagedFood = false,
        detectVisual = false
    )

    private suspend fun recognitionFlow() {
        scanBarcodeStatus = ScanBarcodeStatus.SCANNING
        _scanBarcodeStatusEvent.postValue(scanBarcodeStatus)
        useCase.recognitionBarcode(config).collect { recognitionResult ->
            val barcodeCandidate = recognitionResult?.barcodeCandidates?.firstOrNull()
            if (barcodeCandidate != null && barcodeCandidate.barcode.isValid()) {
                stopDetection()
                barcode = barcodeCandidate.barcode
                validateBarcode()
            }
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
            recognitionFlow()
        }
    }


    fun stopRecognitionSession() {
        PassioSDK.instance.stopCamera()
    }

    private fun validateBarcode() {
        viewModelScope.launch {
            if (!barcode.isValid()) {
                ScanBarcodeStatus.NOT_FOUND
                _scanBarcodeStatusEvent.postValue(scanBarcodeStatus)
                return@launch
            }
            existingCustomFood = useCase.fetchFoodFromCustomFoods(barcode!!)
            if (existingCustomFood != null) {
                scanBarcodeStatus = ScanBarcodeStatus.CUSTOM_FOOD_ALREADY_EXIST
                _scanBarcodeStatusEvent.postValue(scanBarcodeStatus)
                return@launch
            }
            existingSystemItem = useCase.fetchFoodItemForProduct(barcode!!)
            if (existingSystemItem != null) {
                scanBarcodeStatus = ScanBarcodeStatus.BARCODE_IN_SYSTEM
                _scanBarcodeStatusEvent.postValue(scanBarcodeStatus)
                return@launch
            }
            scanBarcodeStatus = ScanBarcodeStatus.NEW_BARCODE
            _scanBarcodeStatusEvent.postValue(scanBarcodeStatus)
        }
    }

    fun geBarcode(): Barcode {
        return barcode ?: ""
    }

    fun navigateToFoodDetails()
    {
        navigate(ScanBarcodeFragmentDirections.scanBarcodeToEdit())
    }

}