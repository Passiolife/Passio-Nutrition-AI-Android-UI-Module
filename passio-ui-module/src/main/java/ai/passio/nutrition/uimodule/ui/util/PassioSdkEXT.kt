package ai.passio.nutrition.uimodule.ui.util

import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.passiosdk.passiofood.data.measurement.Converter
import ai.passio.passiosdk.passiofood.data.measurement.Grams
import ai.passio.passiosdk.passiofood.data.measurement.Unit as PassioUnit


val listOfUnits = mutableListOf<PassioUnit>()
fun PassioSDK.getUnits(): List<PassioUnit> {
    if (listOfUnits.isEmpty()) {
        listOfUnits.add(PassioUnit(Converter(1.0), "Servings"))
        listOfUnits.add(Grams)
    }
    return listOfUnits
}