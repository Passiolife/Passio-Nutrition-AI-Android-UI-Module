package ai.passio.nutrition.uimodule.ui.model

import ai.passio.nutrition.uimodule.ui.profile.LengthUnit
import ai.passio.nutrition.uimodule.ui.profile.WaterUnit
import ai.passio.nutrition.uimodule.ui.profile.WeightUnit

data class MeasurementUnit(
    var lengthUnit: LengthUnit = LengthUnit.Imperial,
    var weightUnit: WeightUnit = WeightUnit.Imperial,
    var waterUnit: WaterUnit = WaterUnit.Imperial){

   /* fun getLengthUnit() : LengthUnit{
        return  lengthUnit ?: LengthUnit.Imperial
    }
    fun getWeightUnit() : WeightUnit{
        return  weightUnit ?: WeightUnit.Imperial
    }
    fun getWaterUnit() : WaterUnit{
        return  waterUnit ?: WaterUnit.Imperial
    }*/
}