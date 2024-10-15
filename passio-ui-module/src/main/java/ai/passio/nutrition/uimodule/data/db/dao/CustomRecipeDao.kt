package ai.passio.nutrition.uimodule.data.db.dao

import ai.passio.nutrition.uimodule.data.db.entity.CustomRecipeEntity
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CustomRecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customRecipe: CustomRecipeEntity)

    @Query("SELECT * FROM CustomRecipe WHERE uuid = :uuid")
    suspend fun get(uuid: String): CustomRecipeEntity?

    @Delete
    suspend fun delete(customRecipe: CustomRecipeEntity)

    @Query("SELECT * FROM CustomRecipe")
    suspend fun getAll(): List<CustomRecipeEntity>

    @Query("SELECT * FROM CustomRecipe WHERE name LIKE '%' || :filterQuery || '%' OR additionalData LIKE '%' || :filterQuery || '%'")
    suspend fun filterCustomRecipes(filterQuery: String): List<CustomRecipeEntity>

}