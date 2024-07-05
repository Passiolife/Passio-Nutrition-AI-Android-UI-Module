package ai.passio.nutrition.uimodule.ui.progress

import ai.passio.nutrition.uimodule.domain.diary.DiaryUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.util.Date

class MacrosViewModel : BaseViewModel() {

    private val useCase = DiaryUseCase

    private val _logsLD = SingleLiveEvent<Pair<MacrosFragment.TimePeriod, List<FoodRecord>>>()
    val logsLD: LiveData<Pair<MacrosFragment.TimePeriod, List<FoodRecord>>> get() = _logsLD

    private var currentDate = Date()

    private val _timePeriod = SingleLiveEvent<MacrosFragment.TimePeriod>()
    val timePeriod: LiveData<MacrosFragment.TimePeriod> get() = _timePeriod

    fun fetchLogsForCurrentWeek() {
        viewModelScope.launch {
            _timePeriod.postValue(MacrosFragment.TimePeriod.WEEK)
            val records = useCase.getLogsForWeek(currentDate)
            _logsLD.postValue(Pair(MacrosFragment.TimePeriod.WEEK, records))
        }
    }

    fun fetchLogsForCurrentMonth() {
        viewModelScope.launch {
            _timePeriod.postValue(MacrosFragment.TimePeriod.MONTH)
            val records = useCase.getLogsForMonth(currentDate)
            _logsLD.postValue(Pair(MacrosFragment.TimePeriod.MONTH, records))
        }
    }

    fun getCurrentDate() = currentDate

    fun setPrevious() {
        if (_timePeriod.value == MacrosFragment.TimePeriod.WEEK) {
            val dateTime = DateTime(currentDate)
            currentDate = dateTime.minusDays(7).toDate()
            fetchLogsForCurrentWeek()
        } else if (_timePeriod.value == MacrosFragment.TimePeriod.MONTH) {
            val dateTime = DateTime(currentDate)
            currentDate = dateTime.minusMonths(1).toDate()
            fetchLogsForCurrentMonth()
        }
    }

    fun setNext() {
        if (_timePeriod.value == MacrosFragment.TimePeriod.WEEK) {
            val dateTime = DateTime(currentDate)
            currentDate = dateTime.plusDays(7).toDate()
            fetchLogsForCurrentWeek()
        } else if (_timePeriod.value == MacrosFragment.TimePeriod.MONTH) {
            val dateTime = DateTime(currentDate)
            currentDate = dateTime.plusMonths(1).toDate()
            fetchLogsForCurrentMonth()
        }
    }

    fun setDate(date: Date) {
        currentDate = date
        fetchLogsForCurrentWeek()
    }

    fun deleteLog(foodRecord: FoodRecord) {
        viewModelScope.launch {
            useCase.deleteRecord(foodRecord)
            fetchLogsForCurrentWeek()
        }
    }

}