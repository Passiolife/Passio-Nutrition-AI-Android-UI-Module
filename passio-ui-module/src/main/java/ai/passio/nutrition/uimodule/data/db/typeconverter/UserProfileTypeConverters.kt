package ai.passio.nutrition.uimodule.data.db.typeconverter

import ai.passio.nutrition.uimodule.data.passioGson
import ai.passio.nutrition.uimodule.ui.model.MeasurementUnit
import ai.passio.nutrition.uimodule.ui.model.UserReminder
import ai.passio.nutrition.uimodule.ui.profile.ActivityLevel
import ai.passio.nutrition.uimodule.ui.profile.CalorieDeficit
import ai.passio.nutrition.uimodule.ui.profile.Gender
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

    // MeasurementUnit converter
    @TypeConverter
    fun fromMeasurementUnit(measurementUnit: MeasurementUnit): String {
        return gson.toJson(measurementUnit)
    }

    @TypeConverter
    fun toMeasurementUnit(data: String): MeasurementUnit {
        return gson.fromJson(data, MeasurementUnit::class.java)
    }

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