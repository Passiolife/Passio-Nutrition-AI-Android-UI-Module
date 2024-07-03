package ai.passio.nutrition.uimodule.ui.util

import ai.passio.passiosdk.core.icons.IconSize
import ai.passio.passiosdk.passiofood.PassioID
import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.passiosdk.passiofood.data.model.PassioIDEntityType
import android.util.Log
import android.widget.ImageView

fun ImageView.loadPassioIcon(
    passioID: PassioID,
    type: PassioIDEntityType = PassioIDEntityType.item,
    iconSize: IconSize = IconSize.PX90
) {
    this.tag = passioID
    val localImageResult = PassioSDK.instance.lookupIconsFor(context, passioID, iconSize, type)

    if (localImageResult.second != null) {
        setImageDrawable(localImageResult.second)
        return
    }

    setImageDrawable(localImageResult.first)

    PassioSDK.instance.fetchIconFor(context, passioID, iconSize) { drawable ->
        if (drawable != null && this.tag == passioID) {
            setImageDrawable(drawable)
        }
    }
}