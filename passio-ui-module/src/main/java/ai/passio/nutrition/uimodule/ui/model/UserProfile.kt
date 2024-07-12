package ai.passio.nutrition.uimodule.ui.model

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
import ai.passio.passiosdk.passiofood.data.model.PassioMealPlan
import kotlin.math.pow

data class UserProfile(
    var userName: String = "",
    var age: Int = 0,
    var gender: Gender = Gender.Male,
    var height: Double = 0.0, //meter
    var weight: Double = 0.0, //kg
    var targetWeight: Double = 0.0, //kg
    var activityLevel: ActivityLevel = ActivityLevel.NotActive,
    var calorieDeficit: CalorieDeficit = CalorieDeficit.Maintain,
    var passioMealPlan: PassioMealPlan? = null,
    var waterTarget: Double = 0.0, //ml
    var carbsPer: Int = 50, //percentage
    var proteinPer: Int = 25, //percentage
    var fatPer: Int = 25, //percentage
    var caloriesTarget: Int = 2100,
    val measurementUnit: MeasurementUnit = MeasurementUnit()
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
        if (measurementUnit.lengthUnit == LengthUnit.Imperial) {
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
        val displayText: String = if (measurementUnit.weightUnit == WeightUnit.Metric) {
            "$weight"
        } else {
            "${kgToLbs(weight)}"
        }
        return displayText
    }

    fun getDisplayTargetWeight(): String {
        if (targetWeight <= 0)
            return ""
        val displayText: String = if (measurementUnit.weightUnit == WeightUnit.Metric) {
            "$targetWeight"
        } else {
            "${kgToLbs(targetWeight)}"
        }
        return displayText
    }

    fun getDisplayTargetWater(): String {
        if (waterTarget <= 0)
            return ""
        val displayText: String = if (measurementUnit.waterUnit == WaterUnit.Metric) {
            "$waterTarget"
        } else {
            "${mlToOz(waterTarget)}"
        }
        return displayText
    }

    fun getCarbsGrams(): Float = (carbsPer * caloriesTarget) / 400f

    fun getProteinGrams(): Float = (proteinPer * caloriesTarget) / 400f

    fun getFatGrams(): Float = (fatPer * caloriesTarget) / 900f
}