package ai.passio.nutrition.uimodule.data

import ai.passio.passiosdk.passiofood.data.measurement.Centigrams
import ai.passio.passiosdk.passiofood.data.measurement.Decigrams
import ai.passio.passiosdk.passiofood.data.measurement.Dekagrams
import ai.passio.passiosdk.passiofood.data.measurement.Grams
import ai.passio.passiosdk.passiofood.data.measurement.Kilograms
import ai.passio.passiosdk.passiofood.data.measurement.Micrograms
import ai.passio.passiosdk.passiofood.data.measurement.Milligrams
import ai.passio.passiosdk.passiofood.data.measurement.Milliliters
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type


class UnitMassSerializer : JsonSerializer<UnitMass>, JsonDeserializer<UnitMass> {

    override fun serialize(src: UnitMass, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()
        with(src) {
            jsonObject.addProperty("unit", this.unit.symbol)
            jsonObject.addProperty("value", this.value)
        }
        return jsonObject
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): UnitMass {
        val jsonObject = json as JsonObject
        val unit = when (val unitString = jsonObject["unit"].asString) {
            "g" -> Grams
            "dag" -> Dekagrams
            "kg" -> Kilograms
            "dg" -> Decigrams
            "cg" -> Centigrams
            "mg" -> Milligrams
            "ug" -> Micrograms
            "ml" -> Milliliters
            else -> throw IllegalArgumentException("No known unit of mass: $unitString")
        }
        return UnitMass(unit, jsonObject["value"].asDouble)
    }

}