package ai.passio.nutrition.uimodule.ui.model.mapper

import ai.passio.nutrition.uimodule.PassioNutrientsExclusionStrategy
import ai.passio.nutrition.uimodule.data.passioGson
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

private val iOSGson = GsonBuilder()
    .registerTypeAdapter(UnitMass::class.java, UnitMassSerializer())
    .registerTypeAdapter(FoodRecordIngredient::class.java, IngredientSerializer())
    .registerTypeAdapter(FoodRecordIngredient::class.java, IngredientDeserializer())
    .setExclusionStrategies(PassioNutrientsExclusionStrategy())
    .create()

// Mapping from Android names to iOS names
private val androidToIosMapNutrients = mapOf(
    "refAlcohol" to "_alcohol",
    "refCalcium" to "_calcium",
    "refCalories" to "_calories",
    "refCarbs" to "_carbs",
    "refCholesterol" to "_cholesterol",
    "refChromium" to "_chromium",
    "refFibers" to "_fibers",
    "refFolicAcid" to "_folicAcid",
    "refFat" to "_fat",
    "refIron" to "_iron",
    "refIodine" to "_iodine",
    "refMagnesium" to "_magnesium",
    "refMonounsaturatedFat" to "_monounsaturatedFat",
    "refPhosphorus" to "_phosphorus",
    "refPolyunsaturatedFat" to "_polyunsaturatedFat",
    "refPotassium" to "_potassium",
    "refProteins" to "_proteins",
    "refSatFat" to "_satFat",
    "refSelenium" to "_selenium",
    "refSodium" to "_sodium",
    "refSugars" to "_sugars",
    "refSugarsAdded" to "_sugarsAdded",
    "refSugarAlcohol" to "_sugarAlcohol",
    "refTransFat" to "_transFat",
    "refVitaminA" to "_vitaminA",
    "refVitaminARAE" to "_vitaminA_RAE",
    "refVitaminB6" to "_vitaminB6",
    "refVitaminB12" to "_vitaminB12",
    "refVitaminB12Added" to "_vitaminB12Added",
    "refVitaminC" to "_vitaminC",
    "refVitaminD" to "_vitaminD",
    "refVitaminE" to "_vitaminE",
    "refVitaminEAdded" to "_vitaminEAdded",
    "refVitaminKPhylloquinone" to "_vitaminKPhylloquinone",
    "refVitaminKMenaquinone4" to "_vitaminKMenaquinone4",
    "refVitaminKDihydrophylloquinone" to "_vitaminKDihydrophylloquinone",
    "refZinc" to "_zinc"
)

// reverse map for iOS to Android
private val iosToAndroidMapNutrients =
    androidToIosMapNutrients.entries.associate { (key, value) -> value to key }


fun FoodRecord.toiOSJson(): String {
    return iOSGson.toJson(this)
}

fun String.fromIOSJson(): FoodRecord? {
    return iOSGson.fromJson(foodRecordDeserializer(this), FoodRecord::class.java)
}

private fun foodRecordDeserializer(json: String): JsonObject {
    val jsonObject = passioGson.fromJson(json, JsonObject::class.java)
    val createdAtDouble = jsonObject.get("createdAt").asDouble
    jsonObject.remove("createdAt")
    jsonObject.addProperty("createdAt", createdAtDouble.toLong())
    return jsonObject
}

private class IngredientSerializer : JsonSerializer<FoodRecordIngredient> {
    override fun serialize(
        src: FoodRecordIngredient,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {

        val foodRecordIngredientObject =
            passioGson.fromJson(passioGson.toJson(src), JsonObject::class.java)

        val referenceNutrients = foodRecordIngredientObject.getAsJsonObject("referenceNutrients")
        val nutrientsObject = JsonObject()
        referenceNutrients.entrySet().forEach() { (key, value) ->

            val newKey = if (androidToIosMapNutrients.containsKey(key)) {
                androidToIosMapNutrients[key]
            } else {
                key
            }
            nutrientsObject.add(newKey, value)
            // Modify the key as per requirements
            /*if (key == "refVitaminARAE") {
                nutrientsObject.add("_vitaminA_RAE", value)
            } else {
                val newKey =
                    key.replaceRange(3, 4, key.substring(3, 4).lowercase()).replaceFirst("ref", "_")
                nutrientsObject.add(newKey, value)
            }*/

        }
        foodRecordIngredientObject.addProperty("passioID", src.id)
        foodRecordIngredientObject.remove("referenceNutrients")
        foodRecordIngredientObject.add("nutrients", nutrientsObject)
        return foodRecordIngredientObject
    }
}

private class IngredientDeserializer : JsonDeserializer<FoodRecordIngredient> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): FoodRecordIngredient {
        val jsonObject = json.asJsonObject

        // Extract the "nutrients" object
        val nutrientsObject = jsonObject.getAsJsonObject("nutrients")
        val referenceNutrients = JsonObject()

        nutrientsObject.entrySet().forEach { (key, value) ->

            val newKey = if (iosToAndroidMapNutrients.containsKey(key)) {
                iosToAndroidMapNutrients[key]
            } else {
                key
            }
            referenceNutrients.add(newKey, value)
            /* val originalKey = when (key) {
                 "_vitaminA_RAE" -> "refVitaminARAE"
                 else -> key.replaceFirst("_", "ref")
                     .replaceRange(3, 4, key.substring(3, 4).uppercase())
             }
             referenceNutrients.add(originalKey, value)*/
        }

        // Remove the "nutrients" object and add back "referenceNutrients"
        jsonObject.remove("nutrients")
        jsonObject.add("referenceNutrients", referenceNutrients)
        jsonObject.addProperty("id", jsonObject.get("passioID").asString)

        // Use Gson to deserialize the modified JsonObject into FoodRecordIngredient
        return passioGson.fromJson(jsonObject, FoodRecordIngredient::class.java)
    }
}


private class UnitMassSerializer :
    JsonSerializer<UnitMass> {

    override fun serialize(
        unitMass: UnitMass,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement {
        val unit = unitMass.unit
        val converter = unit.converter

        // Create the nested converter object
        val converterJson = JsonObject().apply {
            addProperty("coefficient", converter.coefficient)
            addProperty("constant", 0)
        }

        // Create the unit object with converter and symbol
        val unitJson = JsonObject().apply {
            add("converter", converterJson)
            addProperty("symbol", unit.symbol)
        }

        // Create the unitMass object with unit and value
        val unitMassJson = JsonObject().apply {
            add("unit", unitJson)
            addProperty("value", unitMass.value)
        }
        return unitMassJson
    }
}
