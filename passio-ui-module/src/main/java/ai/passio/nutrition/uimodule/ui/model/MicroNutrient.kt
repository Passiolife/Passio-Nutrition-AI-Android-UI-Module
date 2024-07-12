package ai.passio.nutrition.uimodule.ui.model

import ai.passio.passiosdk.passiofood.data.measurement.Grams
import ai.passio.passiosdk.passiofood.data.measurement.Micrograms
import ai.passio.passiosdk.passiofood.data.measurement.Milligrams

data class MicroNutrient(
    val name: String,
    val value: Double,
    val recommendedValue: Double,
    val unitSymbol: String
) {
    companion object {

        fun nutrientsFromFoodRecords(foodRecords: List<FoodRecord?>?): ArrayList<MicroNutrient> {
            if (foodRecords == null) return arrayListOf()

            return arrayListOf(
                MicroNutrient(
                    name = "Saturated Fat",
                    value = foodRecords.sumOf { it?.nutrients()?.satFat()?.value ?: 0.0 },
                    recommendedValue = 20.0,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.satFat()?.unit?.symbol ?: Grams.symbol
                ),
                MicroNutrient(
                    name = "Trans Fat",
                    value = foodRecords.sumOf { it?.nutrients()?.transFat()?.value ?: 0.0 },
                    recommendedValue = 2.2,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.transFat()?.unit?.symbol ?: Grams.symbol
                ),
                MicroNutrient(
                    name = "Cholesterol",
                    value = foodRecords.sumOf { it?.nutrients()?.cholesterol()?.value ?: 0.0 },
                    recommendedValue = 300.0,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.cholesterol()?.unit?.symbol ?: Milligrams.symbol
                ),
                MicroNutrient(
                    name = "Sodium",
                    value = foodRecords.sumOf { it?.nutrients()?.sodium()?.value ?: 0.0 },
                    recommendedValue = 2300.0,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.sodium()?.unit?.symbol ?: Milligrams.symbol
                ),
                MicroNutrient(
                    name = "Dietary Fiber",
                    value = foodRecords.sumOf { it?.nutrients()?.fibers()?.value ?: 0.0 },
                    recommendedValue = 28.0,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.fibers()?.unit?.symbol ?: Grams.symbol
                ),
                MicroNutrient(
                    name = "Total Sugar",
                    value = foodRecords.sumOf { it?.nutrients()?.sugars()?.value ?: 0.0 },
                    recommendedValue = 50.0,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.sugars()?.unit?.symbol ?: Grams.symbol
                ),
                MicroNutrient(
                    name = "Added Sugar",
                    value = foodRecords.sumOf { it?.nutrients()?.sugarsAdded()?.value ?: 0.0 },
                    recommendedValue = 50.0,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.sugarsAdded()?.unit?.symbol ?: Grams.symbol
                ),
                MicroNutrient(
                    name = "Vitamin D",
                    value = foodRecords.sumOf { it?.nutrients()?.vitaminD()?.value ?: 0.0 },
                    recommendedValue = 20.0,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.vitaminD()?.unit?.symbol ?: Micrograms.symbol
                ),
                MicroNutrient(
                    name = "Calcium",
                    value = foodRecords.sumOf { it?.nutrients()?.calcium()?.value ?: 0.0 },
                    recommendedValue = 1000.0,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.calcium()?.unit?.symbol ?: Milligrams.symbol
                ),
                MicroNutrient(
                    name = "Iron",
                    value = foodRecords.sumOf { it?.nutrients()?.iron()?.value ?: 0.0 },
                    recommendedValue = 18.0,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.iron()?.unit?.symbol ?: Milligrams.symbol
                ),
                MicroNutrient(
                    name = "Potassium",
                    value = foodRecords.sumOf { it?.nutrients()?.potassium()?.value ?: 0.0 },
                    recommendedValue = 4700.0,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.potassium()?.unit?.symbol ?: Milligrams.symbol
                ),
                MicroNutrient(
                    name = "Polyunsaturated Fat",
                    value = foodRecords.sumOf { it?.nutrients()?.polyunsaturatedFat()?.value ?: 0.0 },
                    recommendedValue = 22.0,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.polyunsaturatedFat()?.unit?.symbol ?: Grams.symbol
                ),
                MicroNutrient(
                    name = "Monounsaturated Fat",
                    value = foodRecords.sumOf { it?.nutrients()?.monounsaturatedFat()?.value ?: 0.0 },
                    recommendedValue = 44.0,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.monounsaturatedFat()?.unit?.symbol ?: Grams.symbol
                ),
                MicroNutrient(
                    name = "Magnesium",
                    value = foodRecords.sumOf { it?.nutrients()?.magnesium()?.value ?: 0.0 },
                    recommendedValue = 420.0,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.magnesium()?.unit?.symbol ?: Milligrams.symbol
                ),
                MicroNutrient(
                    name = "Iodine",
                    value = foodRecords.sumOf { it?.nutrients()?.iodine()?.value ?: 0.0 },
                    recommendedValue = 150.0,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.iodine()?.unit?.symbol ?: Micrograms.symbol
                ),
                MicroNutrient(
                    name = "Vitamin B6",
                    value = foodRecords.sumOf { it?.nutrients()?.vitaminB6()?.value ?: 0.0 },
                    recommendedValue = 1.7,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.vitaminB6()?.unit?.symbol ?: Milligrams.symbol
                ),
                MicroNutrient(
                    name = "Vitamin B12",
                    value = foodRecords.sumOf { it?.nutrients()?.vitaminB12()?.value ?: 0.0 },
                    recommendedValue = 2.4,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.vitaminB12()?.unit?.symbol ?: Micrograms.symbol
                ),
                MicroNutrient(
                    name = "Vitamin E",
                    value = foodRecords.sumOf { it?.nutrients()?.vitaminE()?.value ?: 0.0 },
                    recommendedValue = 15.0,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.vitaminE()?.unit?.symbol ?: Milligrams.symbol
                ),
                MicroNutrient(
                    name = "Vitamin A",
                    value = foodRecords.sumOf { it?.nutrients()?.vitaminA() ?: 0.0 },
                    recommendedValue = 3000.0,
                    unitSymbol = "IU"
                ),
                MicroNutrient(
                    name = "Vitamin C",
                    value = foodRecords.sumOf { it?.nutrients()?.vitaminC()?.value ?: 0.0 },
                    recommendedValue = 90.0,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.vitaminC()?.unit?.symbol ?: Milligrams.symbol
                ),
                MicroNutrient(
                    name = "Zinc",
                    value = foodRecords.sumOf { it?.nutrients()?.zinc()?.value ?: 0.0 },
                    recommendedValue = 10.0,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.zinc()?.unit?.symbol ?: Milligrams.symbol
                ),
                MicroNutrient(
                    name = "Selenium",
                    value = foodRecords.sumOf { it?.nutrients()?.selenium()?.value ?: 0.0 },
                    recommendedValue = 55.0,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.selenium()?.unit?.symbol ?: Micrograms.symbol
                ),
                MicroNutrient(
                    name = "Folic Acid",
                    value = foodRecords.sumOf { it?.nutrients()?.folicAcid()?.value ?: 0.0 },
                    recommendedValue = 400.0,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.folicAcid()?.unit?.symbol ?: Micrograms.symbol
                ),
                MicroNutrient(
                    name = "Chromium",
                    value = foodRecords.sumOf { it?.nutrients()?.chromium()?.value ?: 0.0 },
                    recommendedValue = 35.0,
                    unitSymbol = foodRecords.firstOrNull()?.nutrientsSelectedSize()?.chromium()?.unit?.symbol ?: Micrograms.symbol
                )
            )
        }

    }
}


