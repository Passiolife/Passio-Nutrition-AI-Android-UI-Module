package ai.passio.nutrition.uimodule.ui.water

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.domain.water.WaterUseCase
import ai.passio.nutrition.uimodule.ui.activity.UserCache
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.MeasurementUnit
import ai.passio.nutrition.uimodule.ui.model.WaterRecord
import ai.passio.nutrition.uimodule.ui.profile.WaterUnit
import ai.passio.nutrition.uimodule.ui.profile.ozToMl
import ai.passio.nutrition.uimodule.ui.progress.TimePeriod
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.util.Date

class WaterTrackingViewModel : BaseViewModel() {
    private val useCase = WaterUseCase
    private var weightRecordCurrent: WaterRecord? = null

    private val _weightRecordCurrentEvent = SingleLiveEvent<WaterRecord>()
    val weightRecordCurrentEvent: LiveData<WaterRecord> = _weightRecordCurrentEvent
    private val measurementUnit: MeasurementUnit get() = UserCache.getProfile().measurementUnit

    private val _saveRecord = SingleLiveEvent<ResultWrapper<Boolean>>()
    val saveRecord: LiveData<ResultWrapper<Boolean>> = _saveRecord
    private val _removeRecord = SingleLiveEvent<ResultWrapper<Boolean>>()
    val removeRecord: LiveData<ResultWrapper<Boolean>> = _removeRecord

    private val _weightRecords = SingleLiveEvent<Pair<List<WaterRecord>, TimePeriod>>()
    val weightRecords: LiveData<Pair<List<WaterRecord>, TimePeriod>> = _weightRecords

    private var currentTimePeriod = TimePeriod.WEEK
    private val _timePeriod = SingleLiveEvent<TimePeriod>()
    val timePeriod: LiveData<TimePeriod> get() = _timePeriod
    private var currentDate = Date()

    fun initRecord(weightRecordEdit: WaterRecord?) {
        viewModelScope.launch {
            weightRecordCurrent = weightRecordEdit ?: WaterRecord.create()
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
            if (measurementUnit.waterUnit == WaterUnit.Imperial) {
                weightRecordCurrent?.weight = ozToMl(weight.toDoubleOrNull() ?: 0.0)
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
                    _saveRecord.postValue(ResultWrapper.Error("Please enter valid water consumed value."))
                } else {
                    _saveRecord.postValue(ResultWrapper.Success(useCase.updateRecord(it)))
                }
            }
        }
    }

    fun quickAdd(quickWeight: Double) {
        viewModelScope.launch {
            val quickRecord = WaterRecord.create()
            quickRecord.apply {
                if (measurementUnit.waterUnit == WaterUnit.Imperial) {
                    quickRecord.weight = ozToMl(quickWeight)
                } else {
                    quickRecord.weight = quickWeight
                }
            }
            _saveRecord.postValue(ResultWrapper.Success(useCase.updateRecord(quickRecord)))
            fetchRecords()
        }
    }

    fun removeWeightRecord(weightRecordRemove: WaterRecord) {
        viewModelScope.launch {
            weightRecordRemove.let {
                _removeRecord.postValue(ResultWrapper.Success(useCase.removeRecord(it)))
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
            val records = useCase.getRecords(currentDate, currentTimePeriod)
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

    fun navigateToSave() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(WaterTrackingFragmentDirections.waterTrackingToSaveWater())
        }
    }

}