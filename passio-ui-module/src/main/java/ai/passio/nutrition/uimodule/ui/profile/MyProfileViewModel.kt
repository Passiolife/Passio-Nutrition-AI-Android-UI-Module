package ai.passio.nutrition.uimodule.ui.profile

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.domain.user.UserProfileUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.model.UserProfile
import ai.passio.nutrition.uimodule.ui.profile.DailyNutritionTargetDialog.DailyNutritionTarget
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.passiosdk.passiofood.data.model.PassioMealPlan
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyProfileViewModel : BaseViewModel() {

    private val useCase = UserProfileUseCase
    private var userProfile: UserProfile? = null
    val dailyNutritionTargetCustomize = SingleLiveEvent<DailyNutritionTarget>()
    private val _userProfileEvent = SingleLiveEvent<UserProfile>()
    val userProfileEvent: LiveData<UserProfile> get() = _userProfileEvent

    private val _dailyNutritionTarget = SingleLiveEvent<UserProfile>()
    val dailyNutritionTarget: LiveData<UserProfile> get() = _dailyNutritionTarget

    private val _updateProfileEvent = SingleLiveEvent<ResultWrapper<UserProfile>>()
    val updateProfileEvent: LiveData<ResultWrapper<UserProfile>> get() = _updateProfileEvent


    private val _activityLevels = ActivityLevel.values().map { it }
    val activityLevels: List<ActivityLevel> get() = _activityLevels
    private val _calorieDeficits = CalorieDeficit.values().map { it }
    val calorieDeficits: List<CalorieDeficit> get() = _calorieDeficits
    private val _passioMealPlans = arrayListOf<PassioMealPlan>()
    val passioMealPlans: List<PassioMealPlan> get() = _passioMealPlans
    private val _showLoading = SingleLiveEvent<Boolean>()
    val showLoading: LiveData<Boolean> get() = _showLoading

    init {
        getUser()
    }

    private fun getUser() {
        _showLoading.postValue(true)
        PassioSDK.instance.fetchMealPlans {
            _passioMealPlans.clear()
            _passioMealPlans.addAll(it)
            viewModelScope.launch(Dispatchers.IO) {
                userProfile = useCase.getUserProfile()
                if (userProfile?.passioMealPlan == null) {
                    userProfile?.passioMealPlan =
                        _passioMealPlans.find { mealPlan -> mealPlan.mealPlanLabel == "balanced" }
                            ?: _passioMealPlans.firstOrNull()
                }
                _userProfileEvent.postValue(userProfile!!)
                _showLoading.postValue(false)
            }
        }
    }

    fun updateUser() {
        viewModelScope.launch {
            _showLoading.postValue(true)
            if (userProfile != null && useCase.updateUserProfile(userProfile!!)) {
                _updateProfileEvent.postValue(ResultWrapper.Success(userProfile!!))
                updateNutritionTarget()
            } else {
                _updateProfileEvent.postValue(ResultWrapper.Error("Failed to update profile. Please try again"))
            }
            _showLoading.postValue(false)
        }
    }

    fun changeDailyNutritionTarget(dailyNutritionTarget: DailyNutritionTarget) {

        viewModelScope.launch {
            userProfile?.apply {
                caloriesTarget = dailyNutritionTarget.caloriesGoal
                carbsPer = dailyNutritionTarget.carbsPer
                fatPer = dailyNutritionTarget.fatPer
                proteinPer = dailyNutritionTarget.proteinPer
            }
            updateNutritionTarget()
        }

    }

    fun customizeDailyNutritionTarget() {
        userProfile?.let {
            dailyNutritionTargetCustomize.postValue(
                DailyNutritionTarget(
                    it.caloriesTarget,
                    it.carbsPer,
                    it.proteinPer,
                    it.fatPer
                )
            )
        }
    }

    fun updateUserName(userName: String) {
        userProfile?.userName = userName
    }

    fun updateAge(age: String) {
        userProfile?.age = age.toIntOrNull() ?: 0
        updateNutritionTarget()
    }

    fun updateWeight(weight: String) {
        if (userProfile?.measurementUnit?.weightUnit == WeightUnit.Imperial) {
            userProfile?.weight = lbsToKg(weight.toDoubleOrNull() ?: 0.0)
        } else {
            userProfile?.weight = weight.toDoubleOrNull() ?: 0.0
        }
        updateNutritionTarget()

    }

    fun updateTargetWeight(targetWeight: String) {
        if (userProfile?.measurementUnit?.weightUnit == WeightUnit.Imperial) {
            userProfile?.targetWeight = lbsToKg(targetWeight.toDoubleOrNull() ?: 0.0)
        } else {
            userProfile?.targetWeight = targetWeight.toDoubleOrNull() ?: 0.0
        }
        updateNutritionTarget()
    }

    fun updateWaterTarget(waterTarget: String) {
        if (userProfile?.measurementUnit?.waterUnit == WaterUnit.Imperial) {
            userProfile?.waterTarget = ozToMl(waterTarget.toDoubleOrNull() ?: 0.0)
        } else {
            userProfile?.waterTarget = waterTarget.toDoubleOrNull() ?: 0.0
        }
        updateNutritionTarget()
    }

    fun updateCalorieDeficit(calorieDeficit: CalorieDeficit) {
        Log.d("==update==== updateCalorieDeficit", calorieDeficit.toString())
        userProfile?.calorieDeficit = calorieDeficit
        updateNutritionTarget()
    }

    fun updateMealPlan(passioMealPlan: PassioMealPlan) {
        Log.d("==update==== updateMealPlan", passioMealPlan.toString())
        userProfile?.passioMealPlan = passioMealPlan
        userProfile?.fatPer = passioMealPlan.fatTarget
        userProfile?.proteinPer = passioMealPlan.proteinTarget
        userProfile?.carbsPer = passioMealPlan.carbTarget
        updateNutritionTarget()
    }

    fun updateActivityLevel(activityLevel: ActivityLevel) {
        Log.d("==update==== updateActivityLevel", activityLevel.toString())
        userProfile?.activityLevel = activityLevel
        updateNutritionTarget()
    }

    fun updateGender(gender: Gender) {
        userProfile?.gender = gender
        updateNutritionTarget()
    }

    fun updateHeight(height: Double) {
        userProfile?.height = height
        updateNutritionTarget()
    }

    private fun updateNutritionTarget() {
        if (userProfile != null) {
            userProfile?.caloriesTarget = calculateRecommendedCalorie()
            _dailyNutritionTarget.postValue(userProfile!!)
        }
    }

    private fun calculateRecommendedCalorie(): Int {
        val bmrAndActivityLevel = calculateBMR()
        val bmr = bmrAndActivityLevel.first
        return if (bmr == null) {
            userProfile!!.caloriesTarget
        } else {
            (calculateCaloriesBasedOnActivityLevel(bmr) - userProfile!!.calorieDeficit.calorieValue).toInt()//calorieDeficit.getValue(weightUnit)).toInt()
        }
    }

    private fun calculateCaloriesBasedOnActivityLevel(bmr: Double): Double {
        return (bmr * userProfile!!.activityLevel.valueDiff)
    }

    private fun calculateBMR(): Pair<Double?, ActivityLevel> {

        val activityLevel = userProfile!!.activityLevel
        val age = userProfile!!.age
        val height = userProfile!!.height
        val weight = userProfile!!.weight
        if (age <= 0 || height <= 0 || weight <= 0) {
            return Pair(null, activityLevel)
        }
        val weightInKg = 10 * weight
        val heightInMeter = height //* Conversion.CENTIMETER_TO_METER.value
        val bmr = if (userProfile!!.gender == Gender.Male) {
            weightInKg + (6.25 * heightInMeter) - (5 * age) + 5
        } else {
            weightInKg + (6.25 * heightInMeter) - (5 * age) - 161
        }
        return Pair(bmr, activityLevel)
    }


}