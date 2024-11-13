package ai.passio.nutrition.uimodule.domain.foodimage

import ai.passio.nutrition.uimodule.data.Repository
import android.graphics.Bitmap

object FoodImageUseCase {

    private val repository = Repository.getInstance()

    suspend fun updateUserFoodImage(id: String, bitmap: Bitmap): Boolean {
        return repository.updateUserFoodImage(id, bitmap)
    }

    suspend fun fetchUserFoodImage(id: String): Bitmap? {
        return repository.fetchUserFoodImage(id)
    }

   /* fun fetchUserFoodImage(
        id: String,
        onBitmapFetched: (bitmap: Bitmap?) -> Unit
    ) {
        repository.fetchUserFoodImage(id, onBitmapFetched)
    }*/

    suspend fun deleteUserFoodImage(id: String): Boolean {
        return repository.deleteUserFoodImage(id)
    }

}