package ai.passio.nutrition.uimodule.ui.voice

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.domain.mealplan.MealPlanUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.passiosdk.passiofood.data.model.PassioSpeechRecognitionModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VoiceLoggingViewModel : BaseViewModel() {
    private val mealPlanUseCase = MealPlanUseCase
    private var voiceLoggingState = VoiceLoggingFragment.VoiceLoggingState.START_LISTENING
    private val _voiceLoggingStateEvent = SingleLiveEvent<VoiceLoggingFragment.VoiceLoggingState>()
    val voiceLoggingStateEvent: LiveData<VoiceLoggingFragment.VoiceLoggingState> =
        _voiceLoggingStateEvent
    private var voiceQuery: String = ""
    private val _voiceQueryEvent = MutableLiveData<String>()
    val voiceQueryEvent: LiveData<String> = _voiceQueryEvent

    private val _resultFoodInfo = MutableLiveData<List<PassioSpeechRecognitionModel>>()
    val resultFoodInfo: LiveData<List<PassioSpeechRecognitionModel>> get() = _resultFoodInfo


    private val _showLoading = SingleLiveEvent<Boolean>()
    val showLoading: LiveData<Boolean> get() = _showLoading
    private val _logFoodEvent = SingleLiveEvent<ResultWrapper<Boolean>>()
    val logFoodEvent: LiveData<ResultWrapper<Boolean>> = _logFoodEvent
    private val _addIngredientEvent = SingleLiveEvent<List<FoodRecordIngredient>>()
    val addIngredientEvent: LiveData<List<FoodRecordIngredient>> = _addIngredientEvent


    init {
        updateVoiceLoggingState(VoiceLoggingFragment.VoiceLoggingState.START_LISTENING)
    }

    private var isAddIngredient = false

    fun setIsAddIngredient(isAddIngredient: Boolean) {
        this.isAddIngredient = isAddIngredient
    }

    fun getIsAddIngredient(): Boolean {
        return isAddIngredient
    }

    fun updateVoiceLoggingState(state: VoiceLoggingFragment.VoiceLoggingState) {
        voiceLoggingState = state
        _voiceLoggingStateEvent.postValue(voiceLoggingState)
    }

    fun errorVoiceRecognition() {
        updateVoiceLoggingState(VoiceLoggingFragment.VoiceLoggingState.START_LISTENING)
    }

    fun fetchResult(newVoiceQuery: String) {
        if (newVoiceQuery.trim().isEmpty()) {
            errorVoiceRecognition()
            return
        }
        viewModelScope.launch {
            voiceQuery = newVoiceQuery
            _voiceQueryEvent.postValue(voiceQuery)
            updateVoiceLoggingState(VoiceLoggingFragment.VoiceLoggingState.FETCHING_RESULT)

            PassioSDK.instance.recognizeSpeechRemote(voiceQuery) { result ->
                _resultFoodInfo.postValue(result)
                updateVoiceLoggingState(VoiceLoggingFragment.VoiceLoggingState.RESULT)
            }
        }
    }

    fun logRecords(list: List<PassioSpeechRecognitionModel>) {
        viewModelScope.launch(Dispatchers.IO) {
            _showLoading.postValue(true)
            mealPlanUseCase.getFoodRecordsFromSpeech(list, passioMealTimeNow()).let {
                if (it.isEmpty()) {
                    _logFoodEvent.postValue(ResultWrapper.Error("Could not fetch food items!"))
                } else {
                    if (isAddIngredient) {
                        _addIngredientEvent.postValue(it.map { fr -> FoodRecordIngredient(fr) })
                    } else {
                        _logFoodEvent.postValue(
                            ResultWrapper.Success(
                                mealPlanUseCase.logFoodRecords(
                                    it
                                )
                            )
                        )
                    }
                }
            }
            _showLoading.postValue(false)
        }
    }


    fun navigateToDiary() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(VoiceLoggingFragmentDirections.voiceLoggingToDiary())
        }
    }

    fun navigateToSearch() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(VoiceLoggingFragmentDirections.voiceLoggingToSearch())
        }
    }

    fun navigateBackToRecipe() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(VoiceLoggingFragmentDirections.backToEditRecipe())
        }
    }


}