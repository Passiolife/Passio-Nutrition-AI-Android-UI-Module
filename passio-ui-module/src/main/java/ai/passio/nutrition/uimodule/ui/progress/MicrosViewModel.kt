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

class MicrosViewModel : BaseViewModel() {
    private val useCase = DiaryUseCase
    private var currentDate = Date()
    private val _currentDateEvent = SingleLiveEvent<Date>()
    val currentDateEvent: LiveData<Date> get() = _currentDateEvent


    private val _logsLD = SingleLiveEvent<List<FoodRecord>>()
    val logsLD: LiveData<List<FoodRecord>> get() = _logsLD

    init {
        _currentDateEvent.postValue(currentDate)
    }

    fun fetchLogsForCurrentWeek() {
        viewModelScope.launch {
            val records = useCase.getLogsForDay(currentDate)
            _logsLD.postValue(records)
        }
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

}