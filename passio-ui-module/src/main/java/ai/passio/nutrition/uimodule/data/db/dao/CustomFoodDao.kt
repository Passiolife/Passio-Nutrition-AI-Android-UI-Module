package ai.passio.nutrition.uimodule.data.db.dao

import ai.passio.nutrition.uimodule.data.db.entity.CustomFoodEntity
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CustomFoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customFood: CustomFoodEntity)

    @Query("SELECT * FROM CustomFood WHERE uuid = :uuid")
    suspend fun get(uuid: String): CustomFoodEntity?

    @Query("SELECT * FROM CustomFood WHERE barcode = :barcode")
    suspend fun getByBarcode(barcode: String): CustomFoodEntity?

    @Delete
    suspend fun delete(customFood: CustomFoodEntity)

    @Query("SELECT * FROM CustomFood")
    suspend fun getAll(): List<CustomFoodEntity>

    @Query("SELECT * FROM CustomFood WHERE name LIKE '%' || :filterQuery || '%' OR additionalData LIKE '%' || :filterQuery || '%'")
    suspend fun filterCustomFoods(filterQuery: String): List<CustomFoodEntity>

}