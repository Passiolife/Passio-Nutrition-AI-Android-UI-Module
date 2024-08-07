package ai.passio.nutrition.uimodule.ui.advisor

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.domain.mealplan.MealPlanUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.PassioAdvisorData
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.passiosdk.passiofood.NutritionAdvisor
import ai.passio.passiosdk.passiofood.data.model.PassioAdvisorFoodInfo
import ai.passio.passiosdk.passiofood.data.model.PassioAdvisorResponse
import ai.passio.passiosdk.passiofood.data.model.PassioResult
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdvisorViewModel : BaseViewModel() {

    private var initConversationStatus = false
    private val _initConversation = SingleLiveEvent<PassioResult<Any>>()
    val initConversation: LiveData<PassioResult<Any>> = _initConversation
    private val passioAdvisorData = mutableListOf<PassioAdvisorData>()
    private val _passioAdvisorEvent = MutableLiveData<List<PassioAdvisorData>>()
    val passioAdvisorEvent: LiveData<List<PassioAdvisorData>> = _passioAdvisorEvent
    private val _errorEvent = SingleLiveEvent<String>()
    val errorEvent: LiveData<String> = _errorEvent


    private val mealPlanUseCase = MealPlanUseCase
    private val _logFoodEvent = SingleLiveEvent<ResultWrapper<Boolean>>()
    val logFoodEvent: LiveData<ResultWrapper<Boolean>> = _logFoodEvent
    private val _showLoading = SingleLiveEvent<Boolean>()
    val showLoading: LiveData<Boolean> get() = _showLoading


    fun initConversation() {
        if (!initConversationStatus) {
            NutritionAdvisor.instance.initConversation { passioResult ->
                initConversationStatus = passioResult is PassioResult.Success
                _initConversation.postValue(passioResult)
                sendInstruction()
            }
        } else {
            _initConversation.postValue(PassioResult.Success(Any()))
            sendInstruction()
        }

    }

    private fun sendInstruction() {
        if (passioAdvisorData.isEmpty()) {
            passioAdvisorData.add(PassioAdvisorData.createInstruction())
            _passioAdvisorEvent.postValue(passioAdvisorData)
        }
    }

    fun sendTextMessage(text: String) {
        if (text.trim().isEmpty()) return
        passioAdvisorData.add(PassioAdvisorData.createSender(text))
        addQueryProcessing()
        NutritionAdvisor.instance.sendMessage(text) { passioResult ->
            when (passioResult) {
                is PassioResult.Success -> {
                    passioAdvisorData.add(PassioAdvisorData.createTextResponse(passioResult.value))
                }

                is PassioResult.Error -> {
                    _errorEvent.postValue(passioResult.message)
                }

                else -> {
                    _errorEvent.postValue("Unknown error!")
                }
            }
            removeQueryProcessing()
        }
    }

    private fun addQueryProcessing() {
        passioAdvisorData.add(PassioAdvisorData.createProcessing())
        _passioAdvisorEvent.postValue(passioAdvisorData)
    }

    private fun removeQueryProcessing() {
        passioAdvisorData.removeIf { it.dataItemType == PassioAdvisorData.TYPE_PROCESSING }
        _passioAdvisorEvent.postValue(passioAdvisorData)
    }

    fun findFood(passioAdvisorDataItem: PassioAdvisorData) {
        if (passioAdvisorDataItem.passioAdvisorResponse == null)
            return
        addQueryProcessing()
        NutritionAdvisor.instance.fetchIngredients(passioAdvisorDataItem.passioAdvisorResponse!!) { passioResult ->
            when (passioResult) {
                is PassioResult.Success -> {
                    passioAdvisorData.add(
                        PassioAdvisorData.createIngredientsResponse(
                            passioResult.value,
                            PassioAdvisorData.TYPE_SENDER_TEXT
                        )
                    )
                }

                is PassioResult.Error -> {
                    _errorEvent.postValue(passioResult.message)
                }

                else -> {
                    _errorEvent.postValue("Unknown error!")
                }
            }
            removeQueryProcessing()
        }
    }

    fun sendImages(currentBitmaps: List<Bitmap>) {
        viewModelScope.launch {
            passioAdvisorData.add(PassioAdvisorData.createSender(currentBitmaps))
            addQueryProcessing()
            var currentCount = 0
            var error = "Unknown error!"
            val listAdvisorResponse = mutableListOf<PassioAdvisorResponse>()
            currentBitmaps.forEach { bitmap ->

                NutritionAdvisor.instance.sendImage(bitmap) { passioResult ->
                    currentCount += 1
                    when (passioResult) {
                        is PassioResult.Success -> {
                            listAdvisorResponse.add(passioResult.value)

                        }

                        is PassioResult.Error -> {
                            error = passioResult.message
                        }

                    }

                    if (currentCount == currentBitmaps.size) {
                        if (listAdvisorResponse.isNotEmpty()) {
                            val passioAdvisorDataItem = listAdvisorResponse[0]
                            val passioAdvisorFoodInfos =
                                mutableListOf<PassioAdvisorFoodInfo>()
                            listAdvisorResponse.forEach {
                                if (!it.extractedIngredients.isNullOrEmpty()) {
                                    passioAdvisorFoodInfos.addAll(it.extractedIngredients!!)
                                }
                            }
                            val temp = PassioAdvisorResponse(
                                threadId = passioAdvisorDataItem.threadId,
                                messageId = passioAdvisorDataItem.messageId,
                                markupContent = passioAdvisorDataItem.markupContent,
                                rawContent = passioAdvisorDataItem.rawContent,
                                tools = passioAdvisorDataItem.tools,
                                extractedIngredients = passioAdvisorFoodInfos
                            )
                            passioAdvisorData.add(
                                PassioAdvisorData.createIngredientsResponse(
                                    temp, PassioAdvisorData.TYPE_SENDER_IMAGES
                                )
                            )
                        } else {
                            _errorEvent.postValue(error)
                        }
                        removeQueryProcessing()
                    }
                }
            }

        }

    }

    fun logRecords(passioAdvisorDataItem: PassioAdvisorData) {
        viewModelScope.launch(Dispatchers.IO) {
            _showLoading.postValue(true)
            val list =
                passioAdvisorDataItem.passioAdvisorResponse?.extractedIngredients?.filterIndexed { index, _ -> index in passioAdvisorDataItem.selectedFoodIndexes }
                    ?: emptyList()
            if (list.isEmpty()) {
                _logFoodEvent.postValue(ResultWrapper.Error("Could not found food items!"))
                _showLoading.postValue(false)
                return@launch
            }
            mealPlanUseCase.getFoodRecords(list, passioMealTimeNow()).let {
                if (it.isEmpty()) {
                    _logFoodEvent.postValue(ResultWrapper.Error("Could not fetch food items!"))
                } else {
                    passioAdvisorDataItem.isLogged = true
                    _logFoodEvent.postValue(ResultWrapper.Success(mealPlanUseCase.logFoodRecords(it)))
                }
            }
            _showLoading.postValue(false)
        }
    }


    fun navigateToDiary() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(AdvisorFragmentDirections.advisorToDiary())
        }
    }


    fun navigateToSearch() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(AdvisorFragmentDirections.advisorToSearch())
        }
    }

    fun navigateToTakePhoto() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(AdvisorFragmentDirections.advisorToTakePhoto(isPicker = true))
        }

    }


}