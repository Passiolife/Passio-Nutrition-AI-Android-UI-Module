package ai.passio.nutrition.uimodule.ui.model

import ai.passio.passiosdk.passiofood.data.model.PassioMealPlan

data class UserMealPlan(
    var mealPlanLabel: String,
    var mealPlanTitle: String,
    var macroTargets: MacroTargets,
)

data class MacroTargets(
    var protein: Int,
    var carbs: Int,
    var fat: Int,
)

internal fun PassioMealPlan.toUserMealPlan(): UserMealPlan {
    return UserMealPlan(
        mealPlanLabel = this.mealPlanLabel,
        mealPlanTitle = this.mealPlanTitle,
        macroTargets = MacroTargets(
            protein = this.proteinTarget,
            carbs = this.carbTarget,
            fat = this.fatTarget,
        )
    )
}

internal fun UserMealPlan.toPassioMealPlan(): PassioMealPlan {
    return PassioMealPlan(
        mealPlanLabel = this.mealPlanLabel,
        mealPlanTitle = this.mealPlanTitle,
        proteinTarget = this.macroTargets.protein,
        carbTarget = this.macroTargets.carbs,
        fatTarget = this.macroTargets.fat
    )

}
