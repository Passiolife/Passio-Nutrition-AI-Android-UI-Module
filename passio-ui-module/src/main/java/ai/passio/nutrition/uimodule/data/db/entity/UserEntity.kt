package ai.passio.nutrition.uimodule.data.db.entity

import ai.passio.nutrition.uimodule.ui.model.UserMealPlan
import ai.passio.nutrition.uimodule.ui.model.UserReminder
import ai.passio.nutrition.uimodule.ui.profile.ActivityLevel
import ai.passio.nutrition.uimodule.ui.profile.CalorieDeficit
import ai.passio.nutrition.uimodule.ui.profile.Gender
import ai.passio.nutrition.uimodule.ui.profile.LengthUnit
import ai.passio.nutrition.uimodule.ui.profile.WaterUnit
import ai.passio.nutrition.uimodule.ui.profile.WeightUnit
import androidx.room.Entity
import androidx.room.PrimaryKey

internal const val USER_UUID = "1111"
@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val uuid: String = USER_UUID,
    var userName: String = "",
    var age: Int = 0,
    var gender: Gender = Gender.male,
    var height: Double = 0.0,
    var weight: Double = 0.0,
    var targetWeight: Double = 0.0,
    var activityLevel: String = ActivityLevel.notActive.label,
    var calorieDeficit: CalorieDeficit = CalorieDeficit.maintain,
    var passioMealPlan: UserMealPlan? = null,
    var waterTarget: Double = 0.0,
    var carbsPer: Int = 50,
    var proteinPer: Int = 25,
    var fatPer: Int = 25,
    var caloriesTarget: Int = 2100,
    var lengthUnit: LengthUnit = LengthUnit.imperial,
    var weightUnit: WeightUnit = WeightUnit.imperial,
    var waterUnit: WaterUnit = WaterUnit.imperial,
    val userReminder: UserReminder = UserReminder()
)
