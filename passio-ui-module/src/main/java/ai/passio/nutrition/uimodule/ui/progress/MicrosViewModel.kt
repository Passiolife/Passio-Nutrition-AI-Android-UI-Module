package ai.passio.nutrition.uimodule.ui.progress

import ai.passio.nutrition.uimodule.domain.diary.DiaryUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.MicroNutrient
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.util.Date

class MicrosViewModel : BaseViewModel() {
    private val useCase = DiaryUseCase

    private var currentDate = Date()
    private val _currentDateEvent = MutableLiveData<Date>()
    val currentDateEvent: LiveData<Date> get() = _currentDateEvent

    private val nutrientsList = arrayListOf<MicroNutrient>()
    private val _logsLD = SingleLiveEvent<ArrayList<MicroNutrient>>()
    val logsLD: LiveData<ArrayList<MicroNutrient>> get() = _logsLD

    private val _showLoading = SingleLiveEvent<Boolean>()
    val showLoading: LiveData<Boolean> = _showLoading

    private var isExpanded = false

    init {
        _currentDateEvent.postValue(currentDate)
    }

    fun fetchLogsForCurrentDay() {
        viewModelScope.launch {
            _showLoading.postValue(true)
            val records = useCase.getLogsForDay(currentDate)
            val nutrients = MicroNutrient.nutrientsFromFoodRecords(records)
            nutrientsList.clear()
            nutrientsList.addAll(nutrients)
            _logsLD.postValue(filterDataAccordingToExpanded())
            _showLoading.postValue(false)
        }
    }

    private fun filterDataAccordingToExpanded(): ArrayList<MicroNutrient> {
        val tempNutrientsList = arrayListOf<MicroNutrient>()
        if (nutrientsList.size >= 10) {
            if (!isExpanded) {
                tempNutrientsList.addAll(nutrientsList.take(10))
                tempNutrientsList.add(MicroNutrient("Show More", 0.0, 0.0, "showmore"))
            } else {
                tempNutrientsList.addAll(nutrientsList)
                tempNutrientsList.add(MicroNutrient("Show Less", 0.0, 0.0, "showmore"))
            }
        } else {
            tempNutrientsList.addAll(nutrientsList)
        }
        return tempNutrientsList
    }

    fun setShowMore() {
        this.isExpanded = !this.isExpanded
        _logsLD.postValue(filterDataAccordingToExpanded())
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