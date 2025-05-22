package ai.passio.nutrition.uimodule.data.db.typeconverter

import ai.passio.nutrition.uimodule.data.passioGson
import ai.passio.nutrition.uimodule.ui.model.UserMealPlan
import ai.passio.nutrition.uimodule.ui.model.UserReminder
import ai.passio.nutrition.uimodule.ui.profile.ActivityLevel
import ai.passio.nutrition.uimodule.ui.profile.CalorieDeficit
import ai.passio.nutrition.uimodule.ui.profile.Gender
import ai.passio.nutrition.uimodule.ui.profile.LengthUnit
import ai.passio.nutrition.uimodule.ui.profile.WaterUnit
import ai.passio.nutrition.uimodule.ui.profile.WeightUnit
import ai.passio.passiosdk.passiofood.data.model.PassioMealPlan
import androidx.room.TypeConverter

class UserProfileTypeConverters {
    private val gson = passioGson

    // Gender converter
    @TypeConverter
    fun fromGender(gender: Gender): String {
        return gender.name
    }

    @TypeConverter
    fun toGender(gender: String): Gender {
        return Gender.valueOf(gender)
    }
    // LengthUnit converter
    @TypeConverter
    fun fromLengthUnit(lengthUnit: LengthUnit): String {
        return lengthUnit.name
    }

    @TypeConverter
    fun toLengthUnit(lengthUnit: String): LengthUnit {
        return LengthUnit.valueOf(lengthUnit)
    }
    // WeightUnit converter
    @TypeConverter
    fun fromWeightUnit(lengthUnit: WeightUnit): String {
        return lengthUnit.name
    }

    @TypeConverter
    fun toWeightUnit(lengthUnit: String): WeightUnit {
        return WeightUnit.valueOf(lengthUnit)
    }
    // WaterUnit converter
    @TypeConverter
    fun fromWaterUnit(lengthUnit: WaterUnit): String {
        return lengthUnit.name
    }

    @TypeConverter
    fun toWaterUnit(lengthUnit: String): WaterUnit {
        return WaterUnit.valueOf(lengthUnit)
    }

    // ActivityLevel converter
    @TypeConverter
    fun fromActivityLevel(activityLevel: ActivityLevel): String {
        return activityLevel.name
    }

    @TypeConverter
    fun toActivityLevel(activityLevel: String): ActivityLevel {
        return ActivityLevel.valueOf(activityLevel)
    }

    // CalorieDeficit converter
    @TypeConverter
    fun fromCalorieDeficit(calorieDeficit: CalorieDeficit): String {
        return calorieDeficit.name
    }

    @TypeConverter
    fun toCalorieDeficit(calorieDeficit: String): CalorieDeficit {
        return CalorieDeficit.valueOf(calorieDeficit)
    }

    // PassioMealPlan converter
    @TypeConverter
    fun fromPassioMealPlan(passioMealPlan: PassioMealPlan?): String? {
        return gson.toJson(passioMealPlan)
    }

    @TypeConverter
    fun toPassioMealPlan(data: String?): PassioMealPlan? {
        return gson.fromJson(data, PassioMealPlan::class.java)
    }

    // UserMealPlan converter
    @TypeConverter
    fun fromUserMealPlan(passioMealPlan: UserMealPlan?): String? {
        return gson.toJson(passioMealPlan)
    }

    @TypeConverter
    fun toUserMealPlan(data: String?): UserMealPlan? {
        return gson.fromJson(data, UserMealPlan::class.java)
    }

    // MeasurementUnit converter
    /*@TypeConverter
    fun fromMeasurementUnit(measurementUnit: MeasurementUnit): String {
        return gson.toJson(measurementUnit)
    }

    @TypeConverter
    fun toMeasurementUnit(data: String): MeasurementUnit {
        return gson.fromJson(data, MeasurementUnit::class.java)
    }*/

    // UserReminder converter
    @TypeConverter
    fun fromUserReminder(userReminder: UserReminder): String {
        return gson.toJson(userReminder)
    }

    @TypeConverter
    fun toUserReminder(data: String): UserReminder {
        return gson.fromJson(data, UserReminder::class.java)
    }
}