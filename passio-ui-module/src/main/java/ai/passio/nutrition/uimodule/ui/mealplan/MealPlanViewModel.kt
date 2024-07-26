package ai.passio.nutrition.uimodule.ui.mealplan

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.domain.mealplan.MealPlanUseCase
import ai.passio.nutrition.uimodule.ui.activity.UserCache
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.passiosdk.passiofood.data.model.PassioMealPlan
import ai.passio.passiosdk.passiofood.data.model.PassioMealPlanItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MealPlanViewModel : BaseViewModel() {

    private val mealPlanUseCase = MealPlanUseCase

    val logFoodEvent = SingleLiveEvent<ResultWrapper<Boolean>>()
    val showLoading = SingleLiveEvent<Boolean>()
    val editFoodEvent = SingleLiveEvent<FoodRecord>()

    var selectedMealPlan: PassioMealPlan? = null
    private val _passioMealPlans = arrayListOf<PassioMealPlan>()
    val passioMealPlans: List<PassioMealPlan> get() = _passioMealPlans

    private val _passioMealPlanItems = MutableLiveData<List<PassioMealPlanItem>>()
    val passioMealPlanItems: LiveData<List<PassioMealPlanItem>> get() = _passioMealPlanItems

    var currentDayNumber = 1

    init {
        getMealPlans()
    }

    fun setCurrentDay(day: Int) {
        viewModelScope.launch {
            currentDayNumber = day
            getMealPlans()
        }
    }

    fun updateMealPlan(selectedMealPlan: PassioMealPlan) {
        this.selectedMealPlan = selectedMealPlan
        viewModelScope.launch {
            getMealPlans()
        }
    }


    fun getMealPlans() {
        showLoading.postValue(true)
        if (_passioMealPlans.isEmpty()) {
            PassioSDK.instance.fetchMealPlans {
                _passioMealPlans.clear()
                _passioMealPlans.addAll(it)
                getMealPlanItems()
            }
        } else {
            getMealPlanItems()
        }
    }

    private fun getMealPlanItems() {
        viewModelScope.launch {
            if (selectedMealPlan == null) {
                selectedMealPlan = (UserCache.getProfile().passioMealPlan
                    ?: _passioMealPlans.find { mealPlan -> mealPlan.mealPlanLabel == "balanced" }
                    ?: _passioMealPlans.firstOrNull()
                        )
            }

            if (selectedMealPlan != null) {
                PassioSDK.instance.fetchMealPlanForDay(
                    mealPlanLabel = selectedMealPlan!!.mealPlanLabel,
                    day = currentDayNumber
                ) { items ->
                    _passioMealPlanItems.postValue(items)
                    showLoading.postValue(false)
                }
            } else {
                showLoading.postValue(false)
            }
        }

    }

    fun logFood(passioMealPlanItem: PassioMealPlanItem) {
        viewModelScope.launch(Dispatchers.IO) {
            showLoading.postValue(true)
            mealPlanUseCase.getFoodRecord(passioMealPlanItem)?.let {
                logFoodEvent.postValue(ResultWrapper.Success(mealPlanUseCase.logFoodRecord(it)))
            }
                ?: logFoodEvent.postValue(ResultWrapper.Error("Could not fetch food item for: ${passioMealPlanItem.meal.foodName}"))

            showLoading.postValue(false)
        }
    }

    fun logFood(passioMealPlanItems: List<PassioMealPlanItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            showLoading.postValue(true)
            mealPlanUseCase.getFoodRecords(passioMealPlanItems).let {
                if (it.isEmpty()) {
                    logFoodEvent.postValue(ResultWrapper.Error("Could not fetch food items"))
                } else {
                    logFoodEvent.postValue(ResultWrapper.Success(mealPlanUseCase.logFoodRecords(it)))
                }
            }
            showLoading.postValue(false)
        }
    }

    fun editFood(passioMealPlanItem: PassioMealPlanItem) {
        viewModelScope.launch(Dispatchers.IO) {
            showLoading.postValue(true)
            mealPlanUseCase.getFoodRecord(passioMealPlanItem)?.let {
                editFoodEvent.postValue(it)
            }
                ?: logFoodEvent.postValue(ResultWrapper.Error("Could not fetch food item for: ${passioMealPlanItem.meal.foodName}, Can't edit try again."))

            showLoading.postValue(false)
        }
    }


    fun navigateToEdit() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(MealPlanFragmentDirections.mealplanToEdit())
        }
    }

}