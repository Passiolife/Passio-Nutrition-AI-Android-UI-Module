package ai.passio.nutrition.uimodule.domain.user

import ai.passio.nutrition.uimodule.data.Repository
import ai.passio.nutrition.uimodule.ui.model.UserProfile

object UserProfileUseCase {

    private val repository = Repository.getInstance()

    suspend fun updateUserProfile(userProfile: UserProfile): Boolean {
        return repository.updateUser(userProfile)
    }

    suspend fun getUserProfile(): UserProfile {
        return repository.getUser()
    }

}