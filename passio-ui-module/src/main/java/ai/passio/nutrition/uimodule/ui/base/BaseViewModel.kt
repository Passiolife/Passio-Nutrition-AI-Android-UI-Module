package ai.passio.nutrition.uimodule.ui.base

import ai.passio.nutrition.uimodule.ui.navigation.NavigationCommand
import ai.passio.nutrition.uimodule.ui.util.Event
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.passiosdk.passiofood.PassioMealTime
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import java.util.Calendar

open class BaseViewModel : ViewModel() {

    private val _navigation = SingleLiveEvent<NavigationCommand>()
    val navigation: SingleLiveEvent<NavigationCommand> get() = _navigation

    fun navigate(navDirections: NavDirections) {
        _navigation.postValue(NavigationCommand.ToDirection(navDirections))
    }

    fun navigateBack() {
        _navigation.postValue(NavigationCommand.Back)
    }

    internal fun passioMealTimeNow(): PassioMealTime {
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
}