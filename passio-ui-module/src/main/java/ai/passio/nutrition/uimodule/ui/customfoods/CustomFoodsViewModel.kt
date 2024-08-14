package ai.passio.nutrition.uimodule.ui.customfoods

import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.myfood.MyFoodsFragmentDirections

class CustomFoodsViewModel : BaseViewModel() {


    fun navigateToFoodCreator() {
        navigate(MyFoodsFragmentDirections.myFoodsToFoodCreator())
    }
}