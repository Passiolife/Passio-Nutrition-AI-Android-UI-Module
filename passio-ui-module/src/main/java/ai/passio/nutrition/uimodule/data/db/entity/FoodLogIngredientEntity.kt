package ai.passio.nutrition.uimodule.data.db.entity

import ai.passio.nutrition.uimodule.data.db.typeconverter.FoodLogTypeConverters
import ai.passio.passiosdk.passiofood.data.model.PassioNutrients
import ai.passio.passiosdk.passiofood.data.model.PassioServingSize
import ai.passio.passiosdk.passiofood.data.model.PassioServingUnit
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "FoodLogIngredient")
data class FoodLogIngredientEntity(
    @PrimaryKey(autoGenerate = true) val ingredientId: Long = 0, // Auto-generate primary key
    var uid: String = "",
    var id: String = "",
    var name: String = "",
    var additionalData: String = "",
    var iconId: String = "",
    var selectedUnit: String = "",
    var selectedQuantity: Double = 0.0,
    @TypeConverters(FoodLogTypeConverters::class)
    var servingSizes: List<PassioServingSize> = listOf(),
    @TypeConverters(FoodLogTypeConverters::class)
    var servingUnits: List<PassioServingUnit> = listOf(),
    @TypeConverters(FoodLogTypeConverters::class)
    var referenceNutrients: PassioNutrients
)
