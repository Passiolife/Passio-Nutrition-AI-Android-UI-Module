package ai.passio.nutrition.uimodule.data.db.mapper

import ai.passio.nutrition.uimodule.data.db.entity.USER_ID
import ai.passio.nutrition.uimodule.data.db.entity.UserEntity
import ai.passio.nutrition.uimodule.ui.model.UserProfile

// Convert UserEntity to UserProfile
internal fun UserEntity.toUserProfile(): UserProfile {
    val userEntity = this
    return UserProfile(
        userName = userEntity.userName,
        age = userEntity.age,
        gender = userEntity.gender,
        height = userEntity.height,
        weight = userEntity.weight,
        targetWeight = userEntity.targetWeight,
        activityLevel = userEntity.activityLevel,
        calorieDeficit = userEntity.calorieDeficit,
        passioMealPlan = userEntity.passioMealPlan,
        waterTarget = userEntity.waterTarget,
        carbsPer = userEntity.carbsPer,
        proteinPer = userEntity.proteinPer,
        fatPer = userEntity.fatPer,
        caloriesTarget = userEntity.caloriesTarget,
        measurementUnit = userEntity.measurementUnit,
        userReminder = userEntity.userReminder
    )
}

// Convert UserProfile to UserEntity
internal fun UserProfile.toUserEntity(): UserEntity {
    val userProfile = this
    return UserEntity(
        id = USER_ID,
        userName = userProfile.userName,
        age = userProfile.age,
        gender = userProfile.gender,
        height = userProfile.height,
        weight = userProfile.weight,
        targetWeight = userProfile.targetWeight,
        activityLevel = userProfile.activityLevel,
        calorieDeficit = userProfile.calorieDeficit,
        passioMealPlan = userProfile.passioMealPlan,
        waterTarget = userProfile.waterTarget,
        carbsPer = userProfile.carbsPer,
        proteinPer = userProfile.proteinPer,
        fatPer = userProfile.fatPer,
        caloriesTarget = userProfile.caloriesTarget,
        measurementUnit = userProfile.measurementUnit,
        userReminder = userProfile.userReminder
    )
}