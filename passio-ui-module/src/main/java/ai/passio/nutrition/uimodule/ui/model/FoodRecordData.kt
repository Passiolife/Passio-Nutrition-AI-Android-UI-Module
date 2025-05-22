package ai.passio.nutrition.uimodule.ui.model

import com.google.gson.annotations.SerializedName

data class FoodRecordData(
    val uuid: String,
    val createdAt: Double,
    val scannedUnitName: String,
    val passioID: String,
    val name: String,
    val selectedQuantity: Int,
    val openFoodLicense: String,
    val details: String,
    val servingSizes: List<ServingSizeData>,
    val id: String,
    val selectedUnit: String,
    val refCode: String,
    val ingredients: List<IngredientData>
)

data class ServingSizeData(
    val quantity: Int,
    val unitName: String
)

data class IngredientData(
    val details: String?,
    val entityType: String,
    val iconId: String,
    val id: String,
    val name: String,
    val openFoodLicense: String?,
    val refCode: String?,
    val selectedQuantity: Double,
    val selectedUnit: String,
    val servingSizes: List<ServingSizeData>,
    val servingUnits: List<ServingUnitData>,
    val passioID: String,
    val nutrients: PassioNutrientsData
)

data class ServingUnitData(
    val unitName: String,
    val weight: WeightData
)

data class WeightData(
    val value: Double,
    val unit: UnitData
)

data class UnitData(
    val symbol: String,
    val converter: ConverterData
)

data class ConverterData(
    val constant: Double,
    val coefficient: Double
)

data class PassioNutrientsData(
    @SerializedName("_weight") val weight: UnitMassData,
    @SerializedName("_referenceWeight") var referenceWeight: UnitMassData,
    @SerializedName("_alcohol") var alcohol: UnitMassData?,
    @SerializedName("_calcium") var calcium: UnitMassData?,
    @SerializedName("_calories") var calories: UnitMassData?,
    @SerializedName("_carbs") var carbs: UnitMassData?,
    @SerializedName("_cholesterol") var cholesterol: UnitMassData?,
    @SerializedName("_chromium") var chromium: UnitMassData?,
    @SerializedName("_fibers") var fibers: UnitMassData?,
    @SerializedName("_folicAcid") var folicAcid: UnitMassData?,
    @SerializedName("_fat") var fat: UnitMassData?,
    @SerializedName("_iron") var iron: UnitMassData?,
    @SerializedName("_iodine") var iodine: UnitMassData?,
    @SerializedName("_magnesium") var magnesium: UnitMassData?,
    @SerializedName("_monounsaturatedFat") var monounsaturatedFat: UnitMassData?,
    @SerializedName("_phosphorus") var phosphorus: UnitMassData?,
    @SerializedName("_polyunsaturatedFat") var polyunsaturatedFat: UnitMassData?,
    @SerializedName("_potassium") var potassium: UnitMassData?,
    @SerializedName("_proteins") var proteins: UnitMassData?,
    @SerializedName("_satFat") var satFat: UnitMassData?,
    @SerializedName("_selenium") var selenium: UnitMassData?,
    @SerializedName("_sodium") var sodium: UnitMassData?,
    @SerializedName("_sugars") var sugars: UnitMassData?,
    @SerializedName("_sugarsAdded") var sugarsAdded: UnitMassData?,
    @SerializedName("_sugarAlcohol") var sugarAlcohol: UnitMassData?,
    @SerializedName("_transFat") var transFat: UnitMassData?,
    @SerializedName("_vitaminA") var vitaminA: Double?,
    @SerializedName("_vitaminA_RAE") var vitaminA_RAE: UnitMassData?,
    @SerializedName("_vitaminB6") var vitaminB6: UnitMassData?,
    @SerializedName("_vitaminB12") var vitaminB12: UnitMassData?,
    @SerializedName("_vitaminB12Added") var vitaminB12Added: UnitMassData?,
    @SerializedName("_vitaminC") var vitaminC: UnitMassData?,
    @SerializedName("_vitaminD") var vitaminD: UnitMassData?,
    @SerializedName("_vitaminE") var vitaminE: UnitMassData?,
    @SerializedName("_vitaminEAdded") var vitaminEAdded: UnitMassData?,
    @SerializedName("_vitaminKPhylloquinone") var vitaminKPhylloquinone: UnitMassData?,
    @SerializedName("_vitaminKMenaquinone4") var vitaminKMenaquinone4: UnitMassData?,
    @SerializedName("_vitaminKDihydrophylloquinone") var vitaminKDihydrophylloquinone: UnitMassData?,
    @SerializedName("_zinc") var zinc: UnitMassData?
)



data class UnitMassData(
    val value: Double,
    val unit: UnitData
)
