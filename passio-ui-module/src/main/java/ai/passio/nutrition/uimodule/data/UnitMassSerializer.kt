package ai.passio.nutrition.uimodule.data

import ai.passio.passiosdk.passiofood.data.measurement.Centigrams
import ai.passio.passiosdk.passiofood.data.measurement.Decigrams
import ai.passio.passiosdk.passiofood.data.measurement.Dekagrams
import ai.passio.passiosdk.passiofood.data.measurement.Grams
import ai.passio.passiosdk.passiofood.data.measurement.Kilograms
import ai.passio.passiosdk.passiofood.data.measurement.Micrograms
import ai.passio.passiosdk.passiofood.data.measurement.Milligrams
import ai.passio.passiosdk.passiofood.data.measurement.Milliliters
import ai.passio.passiosdk.passiofood.data.measurement.Ounce
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
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


// Custom deserializer for Unit
class UnitDeserializer : JsonDeserializer<ai.passio.passiosdk.passiofood.data.measurement.Unit> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ai.passio.passiosdk.passiofood.data.measurement.Unit {
        val jsonObject = json.asJsonObject
        val symbol = jsonObject.get("symbol").asString

        // You can handle the Converter part if needed
        // Example for converter: val converter = context.deserialize(jsonObject.get("converter"), Converter::class.java)

        return ai.passio.passiosdk.passiofood.data.measurement.Unit(symbol = symbol) // Adjust if you need to handle Converter as well
    }
}