package ai.passio.nutrition.uimodule.ui.base

import ai.passio.nutrition.uimodule.ui.navigation.NavigationCommand
import ai.passio.nutrition.uimodule.ui.util.Event
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections

open class BaseViewModel : ViewModel() {

    private val _navigation = SingleLiveEvent<NavigationCommand>()
    val navigation: SingleLiveEvent<NavigationCommand> get() = _navigation

    fun navigate(navDirections: NavDirections) {
        _navigation.postValue(NavigationCommand.ToDirection(navDirections))
    }

    fun navigateBack() {
        _navigation.postValue(NavigationCommand.Back)
    }
}