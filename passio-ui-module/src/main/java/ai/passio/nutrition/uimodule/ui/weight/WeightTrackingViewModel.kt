package ai.passio.nutrition.uimodule.ui.weight

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.domain.weight.WeightUseCase
import ai.passio.nutrition.uimodule.ui.activity.UserCache
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.MeasurementUnit
import ai.passio.nutrition.uimodule.ui.model.WeightRecord
import ai.passio.nutrition.uimodule.ui.profile.WeightUnit
import ai.passio.nutrition.uimodule.ui.profile.lbsToKg
import ai.passio.nutrition.uimodule.ui.progress.TimePeriod
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.util.Date

class WeightTrackingViewModel : BaseViewModel() {
    private val useCase = WeightUseCase
    private var weightRecordCurrent: WeightRecord? = null

    private val _weightRecordCurrentEvent = SingleLiveEvent<WeightRecord>()
    val weightRecordCurrentEvent: LiveData<WeightRecord> = _weightRecordCurrentEvent
    private val measurementUnit: MeasurementUnit get() = UserCache.getProfile().measurementUnit

    private val _saveRecord = SingleLiveEvent<ResultWrapper<Boolean>>()
    val saveRecord: LiveData<ResultWrapper<Boolean>> = _saveRecord
    private val _removeRecord = SingleLiveEvent<ResultWrapper<Boolean>>()
    val removeRecord: LiveData<ResultWrapper<Boolean>> = _removeRecord

    private val _weightRecords = SingleLiveEvent<Pair<List<WeightRecord>, TimePeriod>>()
    val weightRecords: LiveData<Pair<List<WeightRecord>, TimePeriod>> = _weightRecords

    private var currentTimePeriod = TimePeriod.WEEK
    private val _timePeriod = SingleLiveEvent<TimePeriod>()
    val timePeriod: LiveData<TimePeriod> get() = _timePeriod
    private var currentDate = Date()

    fun initRecord(weightRecordEdit: WeightRecord?) {
        viewModelScope.launch {
            weightRecordCurrent = weightRecordEdit ?: WeightRecord.create()
            _weightRecordCurrentEvent.postValue(weightRecordCurrent!!)
        }
    }

    fun setDay(date: DateTime) {
        weightRecordCurrent?.let {
            var newDateTime = DateTime(it.dateTime)
            newDateTime = newDateTime.withDayOfMonth(date.dayOfMonth)
            newDateTime = newDateTime.withMonthOfYear(date.monthOfYear)
            newDateTime = newDateTime.withYear(date.year)
            it.dateTime = newDateTime.millis
            _weightRecordCurrentEvent.postValue(it)
        }
    }

    fun setTime(time: DateTime) {
        weightRecordCurrent?.let {
            var newDateTime = DateTime(it.dateTime)
            newDateTime = newDateTime.withHourOfDay(time.hourOfDay)
            newDateTime = newDateTime.withMinuteOfHour(time.minuteOfHour)
            it.dateTime = newDateTime.millis
            _weightRecordCurrentEvent.postValue(it)
        }
    }

    fun updateWeight(weight: String) {
        weightRecordCurrent?.apply {
            if (measurementUnit.weightUnit == WeightUnit.Imperial) {
                weightRecordCurrent?.weight = lbsToKg(weight.toDoubleOrNull() ?: 0.0)
            } else {
                weightRecordCurrent?.weight = weight.toDoubleOrNull() ?: 0.0
            }
        }
    }

    fun updateWeightRecord() {
        viewModelScope.launch {
            weightRecordCurrent?.let {
                _weightRecordCurrentEvent.postValue(it)
                if (it.dateTime <= 0) {
                    _saveRecord.postValue(ResultWrapper.Error("Please select valid date and time."))
                } else if (it.weight <= 0) {
                    _saveRecord.postValue(ResultWrapper.Error("Please enter valid weight."))
                } else {
                    _saveRecord.postValue(ResultWrapper.Success(useCase.updateWeightRecord(it)))
                }
            }
        }
    }
    fun removeWeightRecord(weightRecordRemove: WeightRecord) {
        viewModelScope.launch {
            weightRecordRemove.let {
                _removeRecord.postValue(ResultWrapper.Success(useCase.removeWeightRecord(it)))
            }
            fetchRecords()
        }
    }

    fun updateTimePeriod(timePeriod: TimePeriod) {
        currentTimePeriod = timePeriod
        fetchRecords()
    }

    fun fetchRecords() {
        viewModelScope.launch {
            _timePeriod.postValue(currentTimePeriod)
            val records = useCase.getWeightRecords(currentDate, currentTimePeriod)
            _weightRecords.postValue(Pair(records, currentTimePeriod))
        }
    }

    fun getCurrentDate() = currentDate

    fun setPrevious() {
        if (currentTimePeriod == TimePeriod.WEEK) {
            val dateTime = DateTime(currentDate)
            currentDate = dateTime.minusDays(7).toDate()
        } else if (currentTimePeriod == TimePeriod.MONTH) {
            val dateTime = DateTime(currentDate)
            currentDate = dateTime.minusMonths(1).toDate()
        }
        fetchRecords()
    }

    fun setNext() {
        if (currentTimePeriod == TimePeriod.WEEK) {
            val dateTime = DateTime(currentDate)
            currentDate = dateTime.plusDays(7).toDate()
        } else if (currentTimePeriod == TimePeriod.MONTH) {
            val dateTime = DateTime(currentDate)
            currentDate = dateTime.plusMonths(1).toDate()
        }
        fetchRecords()
    }

    private var isDatePrefilled = false
    fun setPrefilledDate(date: Date) {
        if (isDatePrefilled) {
            currentDate = date
            isDatePrefilled = true
        }
    }

    fun navigateToWeightSave() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(WeightTrackingFragmentDirections.weightTrackingToSaveWeight())
        }
    }

}