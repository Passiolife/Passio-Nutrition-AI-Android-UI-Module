package ai.passio.nutrition.uimodule.ui.util

import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.util.StringKT.isValid
import ai.passio.passiosdk.core.icons.IconSize
import ai.passio.passiosdk.passiofood.PassioID
import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.passiosdk.passiofood.data.model.PassioIDEntityType
import android.widget.ImageView
import coil.load
import coil.transform.CircleCropTransformation
import java.io.File

internal fun ImageView.loadFoodImage(
    foodRecord: FoodRecord
) {
    if (/*foodRecord.isCustomFood() && */foodRecord.foodImagePath.isValid() && File(foodRecord.foodImagePath!!).exists()) {
        this.load(foodRecord.foodImagePath) {
            transformations(CircleCropTransformation())
        }
        return
    }
    loadPassioIcon(foodRecord.iconId, PassioIDEntityType.fromString(foodRecord.passioIDEntityType))
}

internal fun ImageView.loadPassioIcon(
    passioID: PassioID,
    type: PassioIDEntityType = PassioIDEntityType.item,
    iconSize: IconSize = IconSize.PX90
) {
    this.tag = passioID
    val localImageResult = PassioSDK.instance.lookupIconsFor(context, passioID, iconSize, type)

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

    PassioSDK.instance.fetchIconFor(context, passioID, iconSize) { drawable ->
        if (drawable != null && this.tag == passioID) {
            this.load(drawable) {
                transformations(CircleCropTransformation())
            }
//            setImageDrawable(drawable)
        }
    }
}