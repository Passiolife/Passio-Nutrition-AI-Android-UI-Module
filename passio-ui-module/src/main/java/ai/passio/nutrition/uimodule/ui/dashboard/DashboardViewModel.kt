package ai.passio.nutrition.uimodule.ui.dashboard

import ai.passio.nutrition.uimodule.domain.diary.DiaryUseCase
import ai.passio.nutrition.uimodule.domain.user.UserProfileUseCase
import ai.passio.nutrition.uimodule.domain.water.WaterUseCase
import ai.passio.nutrition.uimodule.domain.weight.WeightUseCase
import ai.passio.nutrition.uimodule.ui.activity.UserCache
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.UserProfile
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.prolificinteractive.materialcalendarview.CalendarMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.util.Date

class DashboardViewModel : BaseViewModel() {

    private val useCase = DiaryUseCase
    private val useCaseUserProfile = UserProfileUseCase
    private val waterUseCase = WaterUseCase
    private val weightUseCase = WeightUseCase


    private var currentDate = Date()
    private val _currentDateEvent = MutableLiveData<Date>()
    val currentDateEvent: LiveData<Date> get() = _currentDateEvent

    private val _logsLD = SingleLiveEvent<Pair<UserProfile, List<FoodRecord>>>()
    val logsLD: LiveData<Pair<UserProfile, List<FoodRecord>>> get() = _logsLD
    private val _adherents = SingleLiveEvent<List<Long>>()
    val adherents: LiveData<List<Long>> get() = _adherents

    private var calendarModeCurrent = CalendarMode.WEEKS
    private val _calendarMode = MutableLiveData<CalendarMode>()
    val calendarMode: LiveData<CalendarMode> get() = _calendarMode

    private val _waterSummary = SingleLiveEvent<Pair<Double, Double>>()
    val waterSummary: LiveData<Pair<Double, Double>> get() = _waterSummary

    private val _weightSummary = SingleLiveEvent<Pair<Double, Double>>()
    val weightSummary: LiveData<Pair<Double, Double>> get() = _weightSummary

    init {
        _calendarMode.postValue(calendarModeCurrent)
        _currentDateEvent.postValue(currentDate)
    }

    fun toggleCalendarMode() {
        calendarModeCurrent = if (calendarModeCurrent == CalendarMode.WEEKS) {
            CalendarMode.MONTHS
        } else {
            CalendarMode.WEEKS
        }
        _calendarMode.postValue(calendarModeCurrent)
    }

    fun setCurrentDate(date: Date) {
        currentDate = date
        _currentDateEvent.postValue(currentDate)
    }

    fun setNextDay() {
        currentDate = DateTime(currentDate.time).plusDays(1).toDate()
        _currentDateEvent.postValue(currentDate)
    }

    fun setPreviousDay() {
        currentDate = DateTime(currentDate.time).minusDays(1).toDate()
        _currentDateEvent.postValue(currentDate)
    }

    fun fetchLogsForCurrentDay() {
        viewModelScope.launch {
            val userProfile = useCaseUserProfile.getUserProfile()
            val records = useCase.getLogsForDay(currentDate)
            _logsLD.postValue(Pair(userProfile, records))
            fetchWaterSummary()
            fetchWeightSummary()
        }
    }

    fun fetchAdherence() {
        viewModelScope.launch {
            val adherents = useCase.fetchAdherence()
            _adherents.postValue(adherents)
        }
    }

    private fun fetchWaterSummary() {
        viewModelScope.launch {
            val totalWater =
                waterUseCase.getRecords(currentDate).sumOf { it.getWaterInCurrentUnit() }
            val targetWater = UserCache.getProfile().getTargetWaterInCurrentUnit()
            var remainingTarget = targetWater - totalWater
            if (remainingTarget <= 0.0) {
                remainingTarget = 0.0
            }
            _waterSummary.postValue(Pair(totalWater, remainingTarget))
        }
    }
    private fun fetchWeightSummary() {
        viewModelScope.launch {
            val totalWater =
                weightUseCase.getLatest(currentDate)?.getWightInCurrentUnit() ?: 0.0
            val targetWater = UserCache.getProfile().getTargetWightInCurrentUnit()
            var remainingTarget = targetWater - totalWater
            if (remainingTarget <= 0.0) {
                remainingTarget = 0.0
            }
            _weightSummary.postValue(Pair(totalWater, remainingTarget))
        }
    }

    fun navigateToMyProfile() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(DashboardFragmentDirections.dashboardToMyProfile())
        }
    }

    fun navigateToSettings() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(DashboardFragmentDirections.dashboardToSettings())
        }
    }

    fun navigateToProgress() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(DashboardFragmentDirections.dashboardToProgress(currentDate = currentDate.time))
        }
    }

    fun navigateToWeightTracking() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(DashboardFragmentDirections.dashboardToWeightTracking(currentDate = currentDate.time))
        }
    }

    fun navigateToWaterTracking() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(DashboardFragmentDirections.dashboardToWaterTracking(currentDate = currentDate.time))
        }
    }

}