package ai.passio.nutrition.uimodule.data.db.entity

import ai.passio.nutrition.uimodule.data.db.typeconverter.FoodLogTypeConverters
import ai.passio.passiosdk.passiofood.data.model.PassioIDEntityType
import ai.passio.passiosdk.passiofood.data.model.PassioServingSize
import ai.passio.passiosdk.passiofood.data.model.PassioServingUnit
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "FoodLog")
data class FoodLogEntity(
    @PrimaryKey val uuid: String,
    var id: String = "",
    var name: String = "",
    var additionalData: String = "",
    var iconId: String = "",
    var foodImagePath: String? = null,
    var passioIDEntityType: String = PassioIDEntityType.item.value,
    var selectedUnit: String = "",
    var selectedQuantity: Double = 0.0,
    var mealLabel: String? = null, // Use String instead of MealLabel enum
    var createdAt: Long? = null,
    var openFoodLicense: String? = null,
    var barcode: String? = null, // Convert Barcode to JSON
    var packagedFoodCode: String? = null,

    @TypeConverters(FoodLogTypeConverters::class)
    var ingredients: MutableList<FoodLogIngredientEntity> = mutableListOf(),

    @TypeConverters(FoodLogTypeConverters::class)
    var servingSizes: MutableList<PassioServingSize> = mutableListOf(),

    @TypeConverters(FoodLogTypeConverters::class)
    var servingUnits: MutableList<PassioServingUnit> = mutableListOf()
)