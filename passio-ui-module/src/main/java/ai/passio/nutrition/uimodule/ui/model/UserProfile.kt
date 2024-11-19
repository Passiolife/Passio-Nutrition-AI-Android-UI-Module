package ai.passio.nutrition.uimodule.ui.model

import ai.passio.nutrition.uimodule.data.db.entity.USER_UUID
import ai.passio.nutrition.uimodule.ui.activity.UserCache
import ai.passio.nutrition.uimodule.ui.profile.ActivityLevel
import ai.passio.nutrition.uimodule.ui.profile.CalorieDeficit
import ai.passio.nutrition.uimodule.ui.profile.Gender
import ai.passio.nutrition.uimodule.ui.profile.LengthUnit
import ai.passio.nutrition.uimodule.ui.profile.WaterUnit
import ai.passio.nutrition.uimodule.ui.profile.WeightUnit
import ai.passio.nutrition.uimodule.ui.profile.kgToLbs
import ai.passio.nutrition.uimodule.ui.profile.metersToFeetInches
import ai.passio.nutrition.uimodule.ui.profile.metersToMetersCentimeters
import ai.passio.nutrition.uimodule.ui.profile.mlToOz
import kotlin.math.pow

data class UserProfile(
    var firstName: String = "",
    var uuid: String = USER_UUID,
    var age: Int = 0,
    var gender: Gender = Gender.male,
    var height: Double = 0.0, //meter
    var weight: Double = 0.0, //kg
    var targetWeight: Double = 0.0, //kg
    var activityLevel: String = ActivityLevel.notActive.label,
    var goalWeightTimeLine: CalorieDeficit = CalorieDeficit.maintain,
//    var mealPlan: PassioMealPlan? = null,
    var mealPlan: UserMealPlan? = null,
    var waterTarget: Double = 0.0, //ml
    var carbsPercent: Int = 50, //percentage
    var proteinPercent: Int = 25, //percentage
    var fatPercent: Int = 25, //percentage
    var caloriesTarget: Int = 2100,
//    val measurementUnit: MeasurementUnit = MeasurementUnit(),
    var heightUnits: LengthUnit = LengthUnit.imperial,
    var units: WeightUnit = WeightUnit.imperial, //weightUnits
    var waterUnit: WaterUnit = WaterUnit.imperial,
    val userReminder: UserReminder = UserReminder(),


    ) {

    // Calculate BMI value
    fun calculateBMI(): Float {

        if (height <= 0 || weight <= 0) {
            return 0.0f // or handle this case according to your app's logic
        }

        val bmi = (weight / height.pow(2))
        return bmi.toFloat()
    }

    fun getDisplayAge(): String {
        if (age <= 0)
            return ""
        return age.toString()
    }

    fun getDisplayHeight(): String {
        val displayText: String
        if (heightUnits == LengthUnit.imperial) {
            val pair = metersToFeetInches(height)
            displayText = "" + pair.first + "'" + pair.second + "\""
        } else {
            val pair = metersToMetersCentimeters(height)
            displayText = "" + pair.first + "m " + pair.second + "cm"
        }
        return displayText
    }

    fun getDisplayWeight(): String {
        if (weight <= 0)
            return ""
        val displayText: String = if (units == WeightUnit.metric) {
            weight.toString()
        } else {
            kgToLbs(weight).toString()
        }
        return displayText
    }

    fun getTargetWightInCurrentUnit(): Double {
        if (targetWeight <= 0)
            return 0.0
        return if (UserCache.getProfile().units == WeightUnit.metric) {
            targetWeight
        } else {
            kgToLbs(targetWeight)
        }
    }

    fun getDisplayTargetWeight(): String {
        return if (targetWeight <= 0) {
            ""
        } else {
            getTargetWightInCurrentUnit().toString()
        }
    }


    fun getTargetWaterInCurrentUnit(): Double {
        if (waterTarget <= 0)
            return 0.0
        return if (UserCache.getProfile().waterUnit == WaterUnit.metric) {
            waterTarget
        } else {
            mlToOz(waterTarget)
        }
    }

    fun getDisplayTargetWater(): String {
        return if (waterTarget <= 0) {
            ""
        } else {
            getTargetWaterInCurrentUnit().toString()
        }
    }

    fun getCarbsGrams(): Float = (carbsPercent * caloriesTarget) / 400f

    fun getProteinGrams(): Float = (proteinPercent * caloriesTarget) / 400f

    fun getFatGrams(): Float = (fatPercent * caloriesTarget) / 900f
}