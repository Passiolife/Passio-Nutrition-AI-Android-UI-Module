package ai.passio.nutrition.uimodule.data.db.dao

import ai.passio.nutrition.uimodule.data.db.entity.FavTblName
import ai.passio.nutrition.uimodule.data.db.entity.FavoriteFoodEntity
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodLog: FavoriteFoodEntity)

    @Query("SELECT * FROM $FavTblName WHERE refCode = :refCode")
    suspend fun get(refCode: String): FavoriteFoodEntity?

    @Delete
    suspend fun delete(foodRecord: FavoriteFoodEntity)

    @Query("SELECT * FROM $FavTblName")
    suspend fun getAll(): List<FavoriteFoodEntity>
}