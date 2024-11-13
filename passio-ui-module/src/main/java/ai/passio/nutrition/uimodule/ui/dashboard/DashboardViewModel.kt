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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.util.Calendar
import java.util.Date

class DashboardViewModel : BaseViewModel() {

    private val useCase = DiaryUseCase
    private val useCaseUserProfile = UserProfileUseCase
    private val waterUseCase = WaterUseCase
    private val weightUseCase = WeightUseCase


    private var currentDate = Date()
    private var adherenceDate = Date()
    private val _currentDateEvent = MutableLiveData<Date>()
    val currentDateEvent: LiveData<Date> get() = _currentDateEvent

    private val _logsLD = MutableLiveData<Pair<UserProfile, List<FoodRecord>>>()
    val logsLD: LiveData<Pair<UserProfile, List<FoodRecord>>> get() = _logsLD
    private val _isLogsLoading = SingleLiveEvent<Boolean>()
    val isLogsLoading: LiveData<Boolean> get() = _isLogsLoading

    private val _adherents = MutableLiveData<List<Long>>()
    val adherents: LiveData<List<Long>> get() = _adherents
    private val _isAdherentsLoading = SingleLiveEvent<Boolean>()
    val isAdherentsLoading: LiveData<Boolean> get() = _isAdherentsLoading

    private var calendarModeCurrent = CalendarMode.WEEKS
    private val _calendarMode = MutableLiveData<CalendarMode>()
    val calendarMode: LiveData<CalendarMode> get() = _calendarMode

    private val _waterSummary = MutableLiveData<Pair<Double, Double>>()
    val waterSummary: LiveData<Pair<Double, Double>> get() = _waterSummary
    private val _isWaterLoading = SingleLiveEvent<Boolean>()
    val isWaterLoading: LiveData<Boolean> get() = _isWaterLoading

    private val _weightSummary = MutableLiveData<Pair<Double, Double>>()
    val weightSummary: LiveData<Pair<Double, Double>> get() = _weightSummary
    private val _isWeightLoading = SingleLiveEvent<Boolean>()
    val isWeightLoading: LiveData<Boolean> get() = _isWeightLoading



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
            _isLogsLoading.postValue(true)
            val userProfile = useCaseUserProfile.getUserProfile()
            val records = useCase.getLogsForDay(currentDate)
            _logsLD.postValue(Pair(userProfile, records))
            _isLogsLoading.postValue(false)
        }
        fetchWaterSummary()
        fetchWeightSummary()
    }


    private var adherenceScope : Job?= null
    fun fetchAdherence() {
        adherenceScope?.cancel()
        adherenceScope = viewModelScope.launch {
            val records = useCase.getLogsForMonth(adherenceDate)
            val uniqueDates = HashSet<Long>() // HashSet to store unique dates
            // Iterate through each record and add the date component to the HashSet
            fun timestampOnlyDate(timestamp: Long): Long {
                // Convert millis to a date with only date part (ignoring time)
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = timestamp
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                return calendar.timeInMillis
            }

            records.forEach { record ->
                record.createdAtTime()?.let { timestamp ->
                    val date = timestampOnlyDate(timestamp)
                    uniqueDates.add(date)
                }
            }
            val result = uniqueDates.toList()
            _adherents.postValue(result)
            _isAdherentsLoading.postValue(false)
        }
    }

    /*fun fetchAdherence() {
        viewModelScope.launch {
            _isAdherentsLoading.postValue(true)
            useCase.getLogsForMonth(adherenceDate)
            val adherents = useCase.fetchAdherence()
            _adherents.postValue(adherents)
            _isAdherentsLoading.postValue(false)
        }
    }*/

    private fun areDatesSame(date1: Date, date2: Date): Boolean {
        val calendar1 = Calendar.getInstance()
        val calendar2 = Calendar.getInstance()

        calendar1.time = date1
        calendar2.time = date2

        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH) &&
                calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH)
    }
    fun setAdherenceDate(date: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val calendarTemp = Calendar.getInstance()
        calendarTemp.time = adherenceDate

        if (!areDatesSame(calendar.time, calendarTemp.time)) {
            adherenceDate = calendar.time
            fetchAdherence()
        }
    }

    private fun fetchWaterSummary() {
        viewModelScope.launch {
            _isWaterLoading.postValue(true)
            val totalWater =
                waterUseCase.getRecords(currentDate).sumOf { it.getWaterInCurrentUnit() }
            val targetWater = UserCache.getProfile().getTargetWaterInCurrentUnit()
            var remainingTarget = targetWater - totalWater
            if (remainingTarget <= 0.0) {
                remainingTarget = 0.0
            }
            _waterSummary.postValue(Pair(totalWater, remainingTarget))
            _isWaterLoading.postValue(false)
        }
    }

    private fun fetchWeightSummary() {
        viewModelScope.launch {
            _isWeightLoading.postValue(true)
            val totalWater =
                weightUseCase.getLatest()?.getWightInCurrentUnit() ?: 0.0
            val targetWater = UserCache.getProfile().getTargetWightInCurrentUnit()
            var remainingTarget = targetWater - totalWater
            if (remainingTarget <= 0.0) {
                remainingTarget = 0.0
            }
            _weightSummary.postValue(Pair(totalWater, remainingTarget))
            _isWeightLoading.postValue(false)
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
    fun navigateToDiary() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(DashboardFragmentDirections.dashboardToDiary())
        }
    }

}