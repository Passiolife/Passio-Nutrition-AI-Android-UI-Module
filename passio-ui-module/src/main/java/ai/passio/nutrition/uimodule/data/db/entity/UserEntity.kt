package ai.passio.nutrition.uimodule.data.db.entity

import ai.passio.nutrition.uimodule.ui.model.MeasurementUnit
import ai.passio.nutrition.uimodule.ui.model.UserReminder
import ai.passio.nutrition.uimodule.ui.profile.ActivityLevel
import ai.passio.nutrition.uimodule.ui.profile.CalorieDeficit
import ai.passio.nutrition.uimodule.ui.profile.Gender
import ai.passio.passiosdk.passiofood.data.model.PassioMealPlan
import androidx.room.Entity
import androidx.room.PrimaryKey

internal const val USER_ID = 1111
@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val id: Int = USER_ID,
    var userName: String = "",
    var age: Int = 0,
    var gender: Gender = Gender.Male,
    var height: Double = 0.0,
    var weight: Double = 0.0,
    var targetWeight: Double = 0.0,
    var activityLevel: ActivityLevel = ActivityLevel.NotActive,
    var calorieDeficit: CalorieDeficit = CalorieDeficit.Maintain,
    var passioMealPlan: PassioMealPlan? = null,
    var waterTarget: Double = 0.0,
    var carbsPer: Int = 50,
    var proteinPer: Int = 25,
    var fatPer: Int = 25,
    var caloriesTarget: Int = 2100,
    val measurementUnit: MeasurementUnit = MeasurementUnit(),
    val userReminder: UserReminder = UserReminder()
)
