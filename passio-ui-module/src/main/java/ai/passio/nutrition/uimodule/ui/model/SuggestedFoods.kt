package ai.passio.nutrition.uimodule.ui.model

import ai.passio.passiosdk.passiofood.PassioFoodDataInfo

data class SuggestedFoods(
    val name: String,
    val iconId: String,
    var foodRecord: FoodRecord? = null,
    var searchResult: PassioFoodDataInfo? = null
) {
    constructor(foodRecord: FoodRecord) : this(
        name = foodRecord.name,
        iconId = foodRecord.iconId,
        foodRecord = foodRecord
    )

    constructor(searchResult: PassioFoodDataInfo) : this(
        name = searchResult.foodName,
        iconId = searchResult.iconID,
        searchResult = searchResult
    )
}
