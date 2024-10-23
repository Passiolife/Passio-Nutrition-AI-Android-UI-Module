package ai.passio.nutrition.uimodule.data.db

import ai.passio.nutrition.uimodule.data.db.dao.CustomFoodDao
import ai.passio.nutrition.uimodule.data.db.dao.CustomRecipeDao
import ai.passio.nutrition.uimodule.data.db.dao.FoodLogDao
import ai.passio.nutrition.uimodule.data.db.dao.UserDao
import ai.passio.nutrition.uimodule.data.db.dao.WaterRecordDao
import ai.passio.nutrition.uimodule.data.db.dao.WeightRecordDao
import ai.passio.nutrition.uimodule.data.db.entity.CustomFoodEntity
import ai.passio.nutrition.uimodule.data.db.entity.CustomRecipeEntity
import ai.passio.nutrition.uimodule.data.db.entity.FoodLogEntity
import ai.passio.nutrition.uimodule.data.db.entity.UserEntity
import ai.passio.nutrition.uimodule.data.db.entity.WaterRecordEntity
import ai.passio.nutrition.uimodule.data.db.entity.WeightRecordEntity
import ai.passio.nutrition.uimodule.data.db.typeconverter.FoodLogTypeConverters
import ai.passio.nutrition.uimodule.data.db.typeconverter.MealLabelConverter
import ai.passio.nutrition.uimodule.data.db.typeconverter.UserProfileTypeConverters
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [FoodLogEntity::class/*, FoodLogIngredientEntity::class*/, WaterRecordEntity::class, WeightRecordEntity::class, UserEntity::class, CustomFoodEntity::class, CustomRecipeEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    FoodLogTypeConverters::class,
    MealLabelConverter::class,
    UserProfileTypeConverters::class,
)
abstract class PassioDatabase : RoomDatabase() {
    internal companion object {
        const val DATABASE_NAME = "passio_ui_module_db"
    }

    abstract fun foodLogDao(): FoodLogDao
    abstract fun waterRecordDao(): WaterRecordDao
    abstract fun weightRecordDao(): WeightRecordDao
    abstract fun userDao(): UserDao
    abstract fun customFoodDao(): CustomFoodDao
    abstract fun customRecipeDao(): CustomRecipeDao
}