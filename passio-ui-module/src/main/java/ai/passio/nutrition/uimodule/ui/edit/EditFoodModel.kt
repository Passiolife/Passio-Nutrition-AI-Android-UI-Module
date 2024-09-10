package ai.passio.nutrition.uimodule.ui.edit

import ai.passio.nutrition.uimodule.ui.model.FoodRecord

data class EditFoodModel(
    val foodRecord: FoodRecord?,
    val showIngredients: Boolean,
)