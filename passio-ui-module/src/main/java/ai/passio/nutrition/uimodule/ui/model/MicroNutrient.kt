package ai.passio.nutrition.uimodule.ui.model

import ai.passio.nutrition.uimodule.data.UnitMassSerializer
import ai.passio.passiosdk.passiofood.data.measurement.Grams
import ai.passio.passiosdk.passiofood.data.measurement.Unit
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass

data class MicroNutrient(
    val name: String,
    val value: Double,
    val recommendedValue: Double,
    val unitSymbol: String
) {
}

