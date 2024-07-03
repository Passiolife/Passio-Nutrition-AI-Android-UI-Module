package ai.passio.nutrition.uimodule.ui.diary

import ai.passio.nutrition.uimodule.domain.diary.DiaryUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.MealLabel
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

class DiaryViewModel : BaseViewModel() {

    private val useCase = DiaryUseCase

    private val _logsLD = SingleLiveEvent<List<FoodRecord>>()
    val logsLD: LiveData<List<FoodRecord>> get() = _logsLD

    private var currentDate = Date()

    fun fetchLogsForCurrentDay() {
        viewModelScope.launch {
            val records = useCase.getLogsForDay(currentDate)
            _logsLD.postValue(records)
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

    fun navigateToEdit() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(DiaryFragmentDirections.diaryToEdit(isEdit = true))
        }
    }

    fun navigateToProgress() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(DiaryFragmentDirections.diaryToProgress())
        }
    }
}