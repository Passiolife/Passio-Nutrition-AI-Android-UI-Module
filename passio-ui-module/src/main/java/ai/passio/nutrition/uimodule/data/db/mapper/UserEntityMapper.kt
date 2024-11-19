package ai.passio.nutrition.uimodule.data.db.mapper

import ai.passio.nutrition.uimodule.data.db.entity.UserEntity
import ai.passio.nutrition.uimodule.ui.model.UserProfile

// Convert UserEntity to UserProfile
internal fun UserEntity.toUserProfile(): UserProfile {
    val userEntity = this
    return UserProfile(
        uuid = userEntity.uuid,
        firstName = userEntity.userName,
        age = userEntity.age,
        gender = userEntity.gender,
        height = userEntity.height,
        weight = userEntity.weight,
        targetWeight = userEntity.targetWeight,
        activityLevel = userEntity.activityLevel,
        goalWeightTimeLine = userEntity.calorieDeficit,
        mealPlan = userEntity.passioMealPlan,
        waterTarget = userEntity.waterTarget,
        carbsPercent = userEntity.carbsPer,
        proteinPercent = userEntity.proteinPer,
        fatPercent = userEntity.fatPer,
        caloriesTarget = userEntity.caloriesTarget,
//        measurementUnit = MeasurementUnit(
//            lengthUnit = userEntity.measurementUnit.lengthUnit,
//            weightUnit = userEntity.measurementUnit.weightUnit,
//            waterUnit = userEntity.measurementUnit.waterUnit
//        ),
        userReminder = userEntity.userReminder
    )
}

// Convert UserProfile to UserEntity
internal fun UserProfile.toUserEntity(): UserEntity {
    val userProfile = this
    return UserEntity(
        uuid = uuid,
        userName = userProfile.firstName,
        age = userProfile.age,
        gender = userProfile.gender,
        height = userProfile.height,
        weight = userProfile.weight,
        targetWeight = userProfile.targetWeight,
        activityLevel = userProfile.activityLevel,
        calorieDeficit = userProfile.goalWeightTimeLine,
        passioMealPlan = userProfile.mealPlan,
        waterTarget = userProfile.waterTarget,
        carbsPer = userProfile.carbsPercent,
        proteinPer = userProfile.proteinPercent,
        fatPer = userProfile.fatPercent,
        caloriesTarget = userProfile.caloriesTarget,
//        measurementUnit = MeasurementUnit(
//            lengthUnit = userProfile.lengthUnit,
//            weightUnit = userProfile.weightUnit,
//            waterUnit = userProfile.waterUnit
//        ),
        userReminder = userProfile.userReminder
    )
}