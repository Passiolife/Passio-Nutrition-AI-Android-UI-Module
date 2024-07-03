package ai.passio.nutrition.uimodule.ui.navigation

import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
import ai.passio.passiosdk.passiofood.PassioFoodDataInfo

interface Router {

    fun navigateUp()

    fun navigateToEdit(searchResult: PassioFoodDataInfo)

    fun editIngredient(ingredient: FoodRecordIngredient)

    fun navigateToTop3()

    fun navigateToImageLocal()

    fun navigateToBarcode()

    fun navigateToOCR()

    fun navigateToNutritionFacts()

    fun navigateToVoice()

    fun navigateToImageRemote()

    fun navigateToAdvisor()
}