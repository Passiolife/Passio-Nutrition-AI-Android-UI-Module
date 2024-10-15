package ai.passio.nutrition.uimodule.data.db.typeconverter

import ai.passio.nutrition.uimodule.data.db.entity.FoodLogIngredientEntity
import ai.passio.passiosdk.passiofood.data.model.PassioNutrients
import ai.passio.passiosdk.passiofood.data.model.PassioServingSize
import ai.passio.passiosdk.passiofood.data.model.PassioServingUnit
import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class FoodLogTypeConverters {

    private val gson = GsonBuilder().create()

    @TypeConverter
    fun fromPassioServingSizeList(value: MutableList<PassioServingSize>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toPassioServingSizeList(value: String?): MutableList<PassioServingSize>? {
        return gson.fromJson(value, Array<PassioServingSize>::class.java)?.toMutableList()
    }

    @TypeConverter
    fun fromPassioServingUnitList(value: MutableList<PassioServingUnit>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toPassioServingUnitList(value: String?): MutableList<PassioServingUnit>? {
        return gson.fromJson(value, Array<PassioServingUnit>::class.java)?.toMutableList()
    }

    @TypeConverter
    fun fromPassioNutrients(value: PassioNutrients?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toPassioNutrients(value: String?): PassioNutrients? {
        return gson.fromJson(value, PassioNutrients::class.java)
    }

    @TypeConverter
    fun fromIngredientList(ingredients: List<FoodLogIngredientEntity>?): String? {
        return gson.toJson(ingredients)
    }

    // Convert JSON string to List<FoodLogIngredientEntity>
    @TypeConverter
    fun toIngredientList(ingredientsJson: String?): List<FoodLogIngredientEntity>? {
        val listType = object : TypeToken<List<FoodLogIngredientEntity>>() {}.type
        return gson.fromJson(ingredientsJson, listType)
    }
}