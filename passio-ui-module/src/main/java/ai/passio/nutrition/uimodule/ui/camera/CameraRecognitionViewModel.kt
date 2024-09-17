package ai.passio.nutrition.uimodule.ui.camera

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.domain.camera.CameraUseCase
import ai.passio.nutrition.uimodule.domain.camera.RecognitionResult
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.passiosdk.core.camera.PassioCameraViewProvider
import ai.passio.passiosdk.passiofood.Barcode
import ai.passio.passiosdk.passiofood.FoodDetectionConfiguration
import ai.passio.passiosdk.passiofood.PackagedFoodCode
import ai.passio.passiosdk.passiofood.PassioID
import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.passiosdk.passiofood.data.model.PassioFoodItem
import androidx.camera.core.CameraSelector
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CameraRecognitionViewModel : BaseViewModel() {

    private val cameraUseCase = CameraUseCase
    val logFoodEvent = SingleLiveEvent<ResultWrapper<Boolean>>()
    val recognitionResults = MutableLiveData<RecognitionResult>()
    val foodItemResult = SingleLiveEvent<ResultWrapper<PassioFoodItem>>()
    val showLoading = SingleLiveEvent<Boolean>()
    private var scanMode: ScanMode = ScanMode.VISUAL
    val scanModeEvent = MutableLiveData<ScanMode>()

    private var cameraZoomLevel: Float = 1f
    private var cameraZoomLevelMin: Float? = null
    private var cameraZoomLevelMax: Float? = null
    val cameraZoomLevelRangeEvent = SingleLiveEvent<Triple<Float, Float?, Float?>>()
    private var isCameraFlashOn = false
    val cameraFlashToggleEvent = SingleLiveEvent<Boolean>()
    val editIngredientEvent = SingleLiveEvent<FoodRecord>()
    val addIngredientEvent = SingleLiveEvent<FoodRecord>()

    private var isAddIngredient = false

    fun setIsAddIngredient(isAddIngredient: Boolean) {
        this.isAddIngredient = isAddIngredient
    }

    fun getIsAddIngredient(): Boolean {
        return isAddIngredient
    }

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
                    setCameraZoomLevel(2f) //default 2f for barcode scanning
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

    fun toggleCameraFlash() {
        isCameraFlashOn = !isCameraFlashOn
        PassioSDK.instance.enableFlashlight(isCameraFlashOn)
        cameraFlashToggleEvent.postValue(isCameraFlashOn)
    }

    fun startRecognitionSession(cameraViewProvider: PassioCameraViewProvider) {

        viewModelScope.launch {
            PassioSDK.instance.startCamera(
                cameraViewProvider,
                displayRotation = 0,
                cameraFacing = CameraSelector.LENS_FACING_BACK,
                tapToFocus = true
            )
            isCameraFlashOn = false
            startOrUpdateDetection()
            cameraFlashToggleEvent.postValue(isCameraFlashOn)
            delay(1000)
            cameraZoomLevelMin = PassioSDK.instance.getMinMaxCameraZoomLevel().first
            cameraZoomLevelMax = PassioSDK.instance.getMinMaxCameraZoomLevel().second

            setCameraZoomLevel(cameraZoomLevel)
        }
    }

    fun setCameraZoomLevel(zoomLevel: Float) {
        if (cameraZoomLevelMin != null && cameraZoomLevelMax != null) {
            cameraZoomLevel = zoomLevel
            PassioSDK.instance.setCameraZoomLevel(zoomLevel)
            cameraZoomLevelRangeEvent.postValue(
                Triple(cameraZoomLevel, cameraZoomLevelMin, cameraZoomLevelMax)
            )
        }
    }

    fun stopRecognitionSession() {
        PassioSDK.instance.stopCamera()
    }

    fun editIngredient(foodRecord: FoodRecord) {
        editIngredientEvent.postValue(foodRecord)
    }

    fun addIngredient(foodRecord: FoodRecord) {
        addIngredientEvent.postValue(foodRecord)
    }

    fun logFoodRecord(foodRecord: FoodRecord) {
        viewModelScope.launch {
            showLoading.postValue(true)
            if (isAddIngredient) {
                addIngredient(foodRecord)
            } else {
                logFoodEvent.postValue(
                    ResultWrapper.Success(
                        cameraUseCase.logFoodRecord(foodRecord)
                    )
                )
            }
            showLoading.postValue(false)
        }
    }

    fun logFood(passioID: PassioID) {
        viewModelScope.launch {
            showLoading.postValue(true)
            val foodItem = cameraUseCase.fetchFoodItemForPassioID(passioID)
            if (foodItem != null) {
                logFoodRecord(FoodRecord(foodItem))
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

    fun navigateBackToEditRecipe() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(CameraRecognitionFragmentDirections.backToEditRecipe())
        }
    }
    fun navigateToEditIngredient() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(CameraRecognitionFragmentDirections.cameraToEditIngredient())
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

    fun navigateToSearch() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(CameraRecognitionFragmentDirections.cameraToSearch())
        }
    }

    fun navigateToFoodCreator() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(CameraRecognitionFragmentDirections.cameraToFoodCreator())
        }
    }
}