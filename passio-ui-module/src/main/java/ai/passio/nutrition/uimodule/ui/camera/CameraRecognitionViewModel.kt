package ai.passio.nutrition.uimodule.ui.camera

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.domain.camera.CameraUseCase
import ai.passio.nutrition.uimodule.domain.camera.RecognitionResult
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.passiosdk.core.camera.PassioCameraViewProvider
import ai.passio.passiosdk.passiofood.FoodDetectionConfiguration
import ai.passio.passiosdk.passiofood.PassioID
import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.passiosdk.passiofood.data.model.PassioFoodItem
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CameraRecognitionViewModel : BaseViewModel() {

    private val cameraUseCase = CameraUseCase
    val logFoodEvent = SingleLiveEvent<ResultWrapper<Boolean>>()
    val recognitionResults = MutableLiveData<RecognitionResult>()
    val foodItemResult = SingleLiveEvent<ResultWrapper<PassioFoodItem>>()
    val showLoading = SingleLiveEvent<Boolean>()
    private var scanMode: ScanMode = ScanMode.VISUAL
    val scanModeEvent = MutableLiveData<ScanMode>()

    fun setFoodScanMode(scanMode: ScanMode) {
        this.scanMode = scanMode
        scanModeEvent.postValue(scanMode)
        startOrUpdateDetection()
    }

    fun stopDetection() {
        cameraUseCase.stopFoodDetection()
    }

    private var lastNoScanDetected = 0L
    fun startOrUpdateDetection() {
        viewModelScope.launch {
            recognitionResults.postValue(RecognitionResult.NoRecognition)
            when (scanMode) {
                ScanMode.BARCODE -> {
                    val config = FoodDetectionConfiguration(
                        detectBarcodes = true,
                        detectPackagedFood = false,
                        detectVisual = false
                    )
                    recognitionFlow(config)
                }

                ScanMode.NUTRITION_FACTS -> {
                    recognitionNutritionFactsFlow()
                }

                ScanMode.VISUAL -> { // default ScanMode.VISUAL
                    val config = FoodDetectionConfiguration(
                        detectBarcodes = false,
                        detectPackagedFood = true,
                        detectVisual = true
                    )
                    recognitionFlow(config)
                }
            }

        }
    }

    private suspend fun recognitionFlow(config: FoodDetectionConfiguration) {
        cameraUseCase.recognitionFlow(config).collect { recognitionResult ->
            if (recognitionResult == RecognitionResult.NoRecognition) {
                if (System.currentTimeMillis() - lastNoScanDetected > 5000) {
                    recognitionResults.postValue(RecognitionResult.NoRecognition)
                }
            } else {
                lastNoScanDetected = System.currentTimeMillis()
                recognitionResults.postValue(recognitionResult)
            }
        }
    }

    private suspend fun recognitionNutritionFactsFlow() {
        cameraUseCase.nutritionFactsFlow().collect { recognitionResult ->
            if (recognitionResult == RecognitionResult.NoRecognition) {
                if (System.currentTimeMillis() - lastNoScanDetected > 5000) {
                    recognitionResults.postValue(RecognitionResult.NoRecognition)
                }
            } else {
                lastNoScanDetected = System.currentTimeMillis()
                recognitionResults.postValue(recognitionResult)
            }
        }
    }


    fun startRecognitionSession(cameraViewProvider: PassioCameraViewProvider) {
        PassioSDK.instance.startCamera(cameraViewProvider)
        startOrUpdateDetection()
    }

    fun stopRecognitionSession() {
        PassioSDK.instance.stopCamera()
    }

    fun logFood(foodItem: PassioFoodItem) {
        viewModelScope.launch {
            logFoodEvent.postValue(
                ResultWrapper.Success(
                    cameraUseCase.logFoodRecord(
                        FoodRecord(
                            foodItem
                        )
                    )
                )
            )
        }
    }

    fun logFood(passioID: PassioID) {
        viewModelScope.launch {
            showLoading.postValue(true)
            val foodItem = cameraUseCase.fetchFoodItemForPassioID(passioID)
            if (foodItem != null) {
                logFoodEvent.postValue(
                    ResultWrapper.Success(
                        cameraUseCase.logFoodRecord(
                            FoodRecord(
                                foodItem
                            )
                        )
                    )
                )
            } else {
                logFoodEvent.postValue(ResultWrapper.Error("Could not fetch food item for: $passioID"))
            }
            showLoading.postValue(false)

        }
    }


    fun fetchFoodItemToEdit(passioID: PassioID) {
        viewModelScope.launch {
            showLoading.postValue(true)
            val foodItem = cameraUseCase.fetchFoodItemForPassioID(passioID)
            if (foodItem != null) {
                foodItemResult.postValue(ResultWrapper.Success(foodItem))
            } else {
                foodItemResult.postValue(ResultWrapper.Error("Could not fetch food item for: $passioID"))
            }
            showLoading.postValue(false)
        }
    }

    fun navigateToEdit() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(CameraRecognitionFragmentDirections.cameraToEdit())
        }
    }

    fun navigateToDiary() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(CameraRecognitionFragmentDirections.cameraToDiary())
        }
    }
}