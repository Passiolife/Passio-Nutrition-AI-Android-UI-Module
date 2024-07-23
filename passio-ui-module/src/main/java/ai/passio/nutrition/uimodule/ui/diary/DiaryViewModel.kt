package ai.passio.nutrition.uimodule.ui.diary

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.domain.diary.DiaryUseCase
import ai.passio.nutrition.uimodule.domain.mealplan.MealPlanUseCase
import ai.passio.nutrition.uimodule.domain.user.UserProfileUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.SuggestedFoods
import ai.passio.nutrition.uimodule.ui.model.UserProfile
import ai.passio.nutrition.uimodule.ui.model.copy
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.isToday
import ai.passio.passiosdk.passiofood.PassioFoodDataInfo
import ai.passio.passiosdk.passiofood.PassioMealTime
import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.passiosdk.passiofood.data.model.PassioMealPlanItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class DiaryViewModel : BaseViewModel() {

    private val useCase = DiaryUseCase
    private val useCaseUserProfile = UserProfileUseCase
    private val mealPlanUseCase = MealPlanUseCase

    private val _logsLD = SingleLiveEvent<Pair<UserProfile, List<FoodRecord>>>()
    val logsLD: LiveData<Pair<UserProfile, List<FoodRecord>>> get() = _logsLD

    private var currentDate = Date()

    private var currentMealTime: PassioMealTime = passioMealTimeNow()
    private val quickSuggestionsPassio: ArrayList<PassioFoodDataInfo> = arrayListOf()
    private val _quickSuggestions = SingleLiveEvent<List<SuggestedFoods>>()
    val quickSuggestions: LiveData<List<SuggestedFoods>> get() = _quickSuggestions

    val logFoodEvent = SingleLiveEvent<ResultWrapper<Boolean>>()
    val showLoading = SingleLiveEvent<Boolean>()

    init {

    }


    private fun fetchLogsForCurrentDay() {
        viewModelScope.launch {
            val userProfile = useCaseUserProfile.getUserProfile()
            val records = useCase.getLogsForDay(currentDate)
            _logsLD.postValue(Pair(userProfile, records))
            getQuickSuggestions()
        }
    }

    fun getCurrentDate() = currentDate

    fun setPreviousDay() {
        val cal: Calendar = Calendar.getInstance()
        cal.time = currentDate
        cal.add(Calendar.DAY_OF_MONTH, -1)
        currentDate = cal.time
        fetchLogsForCurrentDay()
    }

    fun setNextDay() {
        val cal: Calendar = Calendar.getInstance()
        cal.time = currentDate
        cal.add(Calendar.DAY_OF_MONTH, 1)
        currentDate = cal.time
        fetchLogsForCurrentDay()
    }

    fun setDate(date: Date) {
        currentDate = date
        fetchLogsForCurrentDay()
    }

    fun deleteLog(foodRecord: FoodRecord) {
        viewModelScope.launch {
            useCase.deleteRecord(foodRecord)
            fetchLogsForCurrentDay()
        }
    }

    fun logFood(suggestedFoods: SuggestedFoods) {
        viewModelScope.launch(Dispatchers.IO) {

            showLoading.postValue(true)
            val foodRecord: FoodRecord?
            if (suggestedFoods.foodRecord != null) {
                foodRecord = suggestedFoods.foodRecord
            } else if (suggestedFoods.searchResult != null) {
                val passioMealPlanItem =
                    PassioMealPlanItem(
                        1,
                        "Day 1",
                        passioMealTimeNow(),
                        suggestedFoods.searchResult!!
                    )
                foodRecord = mealPlanUseCase.getFoodRecord(passioMealPlanItem)
            } else {
                foodRecord = null
            }
            if (foodRecord != null) {
                logFoodEvent.postValue(
                    ResultWrapper.Success(
                        mealPlanUseCase.logFoodRecord(
                            foodRecord
                        )
                    )
                )
                fetchLogsForCurrentDay()
                getQuickSuggestions()
            } else {
                logFoodEvent.postValue(ResultWrapper.Error("Could not fetch food item for: ${suggestedFoods.name.capitalized()}"))
            }
            showLoading.postValue(false)
        }
    }

    fun navigateToEdit() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(DiaryFragmentDirections.diaryToEdit(isEdit = true))
        }
    }

    fun navigateToDetails() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(DiaryFragmentDirections.diaryToEdit(isEdit = false))
        }
    }

    fun navigateToProgress() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(DiaryFragmentDirections.diaryToProgress(currentDate = currentDate.time))
        }
    }

    private fun getQuickSuggestions() {
        viewModelScope.launch {
            if (currentMealTime == passioMealTimeNow() && quickSuggestionsPassio.isNotEmpty()) {
                improveQuickSuggestions()
            } else {
                currentMealTime = passioMealTimeNow()
                PassioSDK.instance.fetchSuggestions(currentMealTime) { foodDataInfo ->
                    viewModelScope.launch {
                        quickSuggestionsPassio.clear()
                        quickSuggestionsPassio.addAll(foodDataInfo)
                        improveQuickSuggestions()
                    }
                }
            }
        }
    }

    private suspend fun improveQuickSuggestions() {
        viewModelScope.launch {
            getQuickAdds {
                _quickSuggestions.postValue(it)
            }
        }
    }

    private fun passioMealTimeNow(): PassioMealTime {
        val calendar = Calendar.getInstance()
        val hours = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        val timeOfDay = hours * 100 + minutes

        return when (timeOfDay) {
            in 530..1030 -> PassioMealTime.BREAKFAST
            in 1130..1400 -> PassioMealTime.LUNCH
            in 1700..2100 -> PassioMealTime.DINNER
            else -> PassioMealTime.SNACK
        }
    }


    private suspend fun getQuickAdds(completion: (List<SuggestedFoods>) -> Unit) {

        viewModelScope.launch {

            fun fetchSDKSuggestions(
                todayRecords: List<String>,
                userSuggestedFoods: List<SuggestedFoods>,
                completion: (List<SuggestedFoods>) -> Unit
            ) {
                viewModelScope.launch {
                    val sdkSuggestedFoods = quickSuggestionsPassio.map { SuggestedFoods(it) }
                    val finalSdkSuggestedFoods = (userSuggestedFoods + sdkSuggestedFoods)
                        .distinctBy { it.name.lowercase() }
                        .take(30)
                    val finalFoodRecords =
                        finalSdkSuggestedFoods.filter { !todayRecords.contains(it.name.lowercase()) }
                    completion.invoke(finalFoodRecords)
                }
            }

            val maxSuggestedCount = 30

            useCase.getLogsForLast30Days().let { dayLogs ->
                val filterFoodRecords = dayLogs
                    .filter { it.mealLabel!!.value.equals(currentMealTime.mealName, true) }
                val todayRecords = filterFoodRecords.filter { isToday(it.createdAtTime() ?: 0) }
                    .map { it.name.lowercase() }
                val finalFoodRecords =
                    filterFoodRecords.filter { !todayRecords.contains(it.name.lowercase()) }

                if (finalFoodRecords.isNotEmpty()) {
                    val lowerCasedFoodRecords = finalFoodRecords.map {
                        it.copy().apply {
                            name = name.lowercase()
                            createdAt = null
                        }
                    }

                    val foodNamesCount =
                        lowerCasedFoodRecords.groupingBy { it.name }.eachCount()
                    val sortedFoodRecords = lowerCasedFoodRecords.distinctBy { it.name }
                        .sortedByDescending { foodNamesCount[it.name] ?: 0 }

                    val userSuggestedFoods = sortedFoodRecords.map {
                        SuggestedFoods(it)
                    }

                    if (userSuggestedFoods.size > maxSuggestedCount) {
                        completion.invoke(userSuggestedFoods.take(maxSuggestedCount))
                    } else {
                        fetchSDKSuggestions(todayRecords, userSuggestedFoods, completion)
                    }
                } else {
                    fetchSDKSuggestions(todayRecords, emptyList(), completion)
                }
            }
        }
    }


}