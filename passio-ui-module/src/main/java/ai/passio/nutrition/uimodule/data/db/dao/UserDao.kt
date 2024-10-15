package ai.passio.nutrition.uimodule.data.db.dao

import ai.passio.nutrition.uimodule.data.db.entity.UserEntity
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userEntity: UserEntity)

    @Delete
    suspend fun delete(userEntity: UserEntity)

    @Query("SELECT * FROM user_profile WHERE id = :id")
    suspend fun getUserEntityById(id: Int): UserEntity?
}