package ai.passio.nutrition.uimodule.data.db.entity

import ai.passio.nutrition.uimodule.data.db.typeconverter.FoodLogTypeConverters
import ai.passio.passiosdk.passiofood.data.model.PassioNutrients
import ai.passio.passiosdk.passiofood.data.model.PassioServingSize
import ai.passio.passiosdk.passiofood.data.model.PassioServingUnit
import androidx.room.Entity
import androidx.room.TypeConverters

/*
@Entity(
    tableName = "FoodLogIngredient",
    foreignKeys = [ForeignKey(
        entity = FoodLogEntity::class,
        parentColumns = arrayOf("uuid"),
        childColumns = arrayOf("foodUUID"),
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("foodUUID")]
)*/

@Entity(tableName = "FoodLogIngredient")
data class FoodLogIngredientEntity(
//    @PrimaryKey(autoGenerate = true) val ingredientId: Long = 0,
//    var foodUUID: String, // Foreign key to reference FoodLogEntity
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
