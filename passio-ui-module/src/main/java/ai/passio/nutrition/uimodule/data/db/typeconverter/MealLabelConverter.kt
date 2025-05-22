package ai.passio.nutrition.uimodule.data.db.typeconverter

import ai.passio.nutrition.uimodule.ui.model.MealLabel
import androidx.room.TypeConverter

class MealLabelConverter {

    @TypeConverter
    fun fromMealLabel(value: String?): MealLabel? {
        return value?.let { MealLabel.valueOf(it) }
    }

    @TypeConverter
    fun toMealLabel(mealLabel: MealLabel?): String? {
        return mealLabel?.name
    }
}
