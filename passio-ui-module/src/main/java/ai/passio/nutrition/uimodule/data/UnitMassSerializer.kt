package ai.passio.nutrition.uimodule.data

import ai.passio.passiosdk.passiofood.data.measurement.Centigrams
import ai.passio.passiosdk.passiofood.data.measurement.Decigrams
import ai.passio.passiosdk.passiofood.data.measurement.Dekagrams
import ai.passio.passiosdk.passiofood.data.measurement.Grams
import ai.passio.passiosdk.passiofood.data.measurement.KiloCalories
import ai.passio.passiosdk.passiofood.data.measurement.Kilograms
import ai.passio.passiosdk.passiofood.data.measurement.Micrograms
import ai.passio.passiosdk.passiofood.data.measurement.Milligrams
import ai.passio.passiosdk.passiofood.data.measurement.Milliliters
import ai.passio.passiosdk.passiofood.data.measurement.Ounce
import ai.passio.passiosdk.passiofood.data.measurement.UnitEnergy
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type


private val mGson = GsonBuilder().create()

class UnitMassSerializer : JsonSerializer<UnitMass>, JsonDeserializer<UnitMass> {

    override fun serialize(
        src: UnitMass,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {


        /*val jsonObject = JsonObject()
        with(src) {
            jsonObject.addProperty("unit", this.unit.symbol)
            jsonObject.addProperty("value", this.value)
        }*/
        return mGson.fromJson(mGson.toJson(src), JsonObject::class.java) //jsonObject
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): UnitMass {
        val jsonObject = json as JsonObject
        if (jsonObject.has("unit") && !jsonObject.get("unit").isJsonPrimitive && jsonObject.get("unit").isJsonObject) {
            return mGson.fromJson(jsonObject, UnitMass::class.java)
        }
        val unit = when (val unitString = jsonObject["unit"].asString) {
            "g" -> Grams
            "dag" -> Dekagrams
            "kg" -> Kilograms
            "dg" -> Decigrams
            "cg" -> Centigrams
            "mg" -> Milligrams
            "ug" -> Micrograms
            "ml" -> Milliliters
            "oz" -> Ounce
            else -> throw IllegalArgumentException("No known unit of mass: $unitString")
        }
        return UnitMass(unit, jsonObject["value"].asDouble)
        /*val jsonObject = json.asJsonObject

        // Deserialize the 'unit' field manually
        val unitJson = jsonObject.get("unit")
        val unit: ai.passio.passiosdk.passiofood.data.measurement.Unit = context.deserialize(unitJson, ai.passio.passiosdk.passiofood.data.measurement.Unit::class.java)

        // Deserialize the 'value' field
        val value = jsonObject.get("value").asDouble

        return UnitMass(unit, value)*/
    }


}

class UnitEnergySerializer : JsonSerializer<UnitEnergy>, JsonDeserializer<UnitEnergy> {

    override fun serialize(
        src: UnitEnergy,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {


        /*val jsonObject = JsonObject()
        with(src) {
            jsonObject.addProperty("unit", this.unit.symbol)
            jsonObject.addProperty("value", this.value)
        }*/
        return mGson.fromJson(mGson.toJson(src), JsonObject::class.java) //jsonObject
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): UnitEnergy {
        val jsonObject = json as JsonObject
        if (jsonObject.has("unit") && !jsonObject.get("unit").isJsonPrimitive && jsonObject.get("unit").isJsonObject) {
            return mGson.fromJson(jsonObject, UnitEnergy::class.java)
        }
        return UnitEnergy(KiloCalories, jsonObject["value"].asDouble)
    }


}
/*


class UnitDeserializer : JsonDeserializer<ai.passio.passiosdk.passiofood.data.measurement.Unit> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ai.passio.passiosdk.passiofood.data.measurement.Unit {
        val jsonObject = json.asJsonObject
        val symbol = jsonObject.get("symbol").asString

        var converter = ai.passio.passiosdk.passiofood.data.measurement.Converter()
        if (jsonObject.has("converter") && jsonObject.getAsJsonObject("converter")
                .has("coefficient")
        ) {
            converter = ai.passio.passiosdk.passiofood.data.measurement.Converter(
                jsonObject.getAsJsonObject("converter").get("coefficient").asDouble
            )
        }

        return ai.passio.passiosdk.passiofood.data.measurement.Unit(
            symbol = symbol,
            converter = converter
        )
    }
}*/
