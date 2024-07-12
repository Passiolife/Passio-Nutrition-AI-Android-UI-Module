package ai.passio.nutrition.uimodule.ui.settings

import ai.passio.nutrition.uimodule.domain.user.UserProfileUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.MeasurementUnit
import ai.passio.nutrition.uimodule.ui.model.UserProfile
import ai.passio.nutrition.uimodule.ui.profile.LengthUnit
import ai.passio.nutrition.uimodule.ui.profile.WaterUnit
import ai.passio.nutrition.uimodule.ui.profile.WeightUnit
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SettingsViewModel : BaseViewModel() {

    private val useCase = UserProfileUseCase
    private lateinit var userProfile: UserProfile
    private val _measurementUnitEvent = SingleLiveEvent<MeasurementUnit>()
    val measurementUnitEvent: LiveData<MeasurementUnit> get() = _measurementUnitEvent

    init {
        getMeasurementUnit()
    }

    fun getMeasurementUnit() {
        viewModelScope.launch {
            userProfile = useCase.getUserProfile()
            _measurementUnitEvent.postValue(userProfile.measurementUnit)
        }
    }

    fun updateLengthUnit(lengthUnit: LengthUnit) {
        viewModelScope.launch {
            with(userProfile) {
                if (measurementUnit.lengthUnit.value != lengthUnit.value) {
                    measurementUnit.lengthUnit = lengthUnit
                    useCase.updateUserProfile(this)
                }
            }
        }
    }

    fun updateWeightUnit(weightUnit: WeightUnit) {
        viewModelScope.launch {
            with(userProfile) {
                if (measurementUnit.weightUnit.value != weightUnit.value) {
                    measurementUnit.weightUnit = weightUnit
                    if (weightUnit == WeightUnit.Metric) {
                        measurementUnit.waterUnit = WaterUnit.Metric
                    } else {
                        measurementUnit.waterUnit = WaterUnit.Imperial
                    }
                    useCase.updateUserProfile(this)
                }
            }
        }
    }


}