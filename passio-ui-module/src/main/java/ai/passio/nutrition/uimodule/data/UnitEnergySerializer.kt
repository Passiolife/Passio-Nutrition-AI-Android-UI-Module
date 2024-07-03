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
import ai.passio.passiosdk.passiofood.data.measurement.UnitEnergy
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class UnitEnergySerializer : JsonSerializer<UnitEnergy>, JsonDeserializer<UnitEnergy> {

    override fun serialize(src: UnitEnergy, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()
        with(src) {
            jsonObject.addProperty("value", this.value)
        }
        return jsonObject
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): UnitEnergy {
        val jsonObject = json as JsonObject
        return UnitEnergy(KiloCalories, jsonObject["value"].asDouble)
    }

}