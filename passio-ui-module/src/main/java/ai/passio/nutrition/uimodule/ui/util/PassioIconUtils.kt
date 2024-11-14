package ai.passio.nutrition.uimodule.ui.util

import ai.passio.nutrition.uimodule.domain.foodimage.FoodImageUseCase
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.passiosdk.core.icons.IconSize
import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.passiosdk.passiofood.data.model.PassioIDEntityType
import android.widget.ImageView
import coil.load
import coil.transform.CircleCropTransformation
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private val foodImageUseCase = FoodImageUseCase
internal fun ImageView.loadFoodImage(
    foodRecord: FoodRecord
) {
    /*if (foodRecord.foodImagePath.isValid() && File(foodRecord.foodImagePath!!).exists()) {
        this.load(foodRecord.foodImagePath) {
            transformations(CircleCropTransformation())
        }
        return
    }*/
    loadPassioIcon(foodRecord.iconId, PassioIDEntityType.fromString(foodRecord.entityType))
}

@OptIn(DelicateCoroutinesApi::class)
internal fun ImageView.loadPassioIcon(
    iconId: String,
    type: PassioIDEntityType = PassioIDEntityType.item,
    iconSize: IconSize = IconSize.PX90
) {
    this.tag = iconId

    if (isUserImage(iconId)) {
        val mImage = this
        GlobalScope.launch {
            val bitmap = foodImageUseCase.fetchUserFoodImage(iconId)
            mImage.load(bitmap) {
                transformations(CircleCropTransformation())
                size(80)
            }

        }
        /*foodImageUseCase.fetchUserFoodImage(iconId) { bitmap ->
            mImage.load(bitmap) {
                transformations(CircleCropTransformation())
                size(80)
            }
        }*/
        return
    }

    val localImageResult = PassioSDK.instance.lookupIconsFor(context, iconId, iconSize, type)

    if (localImageResult.second != null) {
        this.load(localImageResult.second) {
            transformations(CircleCropTransformation())
        }
//        setImageDrawable(localImageResult.second)
        return
    }

    this.load(localImageResult.first) {
        transformations(CircleCropTransformation())
    }
//    setImageDrawable(localImageResult.first)

    PassioSDK.instance.fetchIconFor(context, iconId, iconSize) { drawable ->
        if (drawable != null && this.tag == iconId) {
            this.load(drawable) {
                transformations(CircleCropTransformation())
            }
//            setImageDrawable(drawable)
        }
    }
}