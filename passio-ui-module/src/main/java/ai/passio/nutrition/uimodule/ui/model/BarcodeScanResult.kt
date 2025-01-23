package ai.passio.nutrition.uimodule.ui.model

import ai.passio.nutrition.uimodule.ui.foodcreator.BarcodeResultType
import ai.passio.passiosdk.passiofood.Barcode

internal data class BarcodeScanResult(
    var barcode: Barcode,
    var record: FoodRecord?,
    var resultType: BarcodeResultType
)
