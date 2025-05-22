package ai.passio.nutrition.uimodule.ui.edit

import ai.passio.nutrition.uimodule.ui.model.FoodRecord

data class EditFoodDataModel(
    val foodRecord: FoodRecord,
    var isUserFood: Boolean = false,
    var isUserRecipe: Boolean = false,
    var isEditFav: Boolean = false
)