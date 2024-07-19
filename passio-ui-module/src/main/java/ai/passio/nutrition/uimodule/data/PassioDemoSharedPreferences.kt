package ai.passio.nutrition.uimodule.data

import ai.passio.nutrition.uimodule.ui.model.WeightRecord
import android.content.SharedPreferences

class PassioDemoSharedPreferences(private val sharedPreferences: SharedPreferences) {

    companion object Key {
        const val PREF_NAME = "PassioDemoPrefs"
        private const val PREF_FOOD_RECORDS = "foodRecords"
        private const val PREF_WEIGHT_RECORDS = "weightRecords"
        private const val PREF_FOOD_FAVORITES = "foodFavorites"
        private const val PREF_PASSIO_USER = "userProfile"
    }

    fun saveRecords(foodRecords: List<String>) {
        sharedPreferences.edit().putStringSet(PREF_FOOD_RECORDS, foodRecords.toSet()).apply()
    }

    fun getRecords(): Set<String> {
        return sharedPreferences.getStringSet(PREF_FOOD_RECORDS, hashSetOf())!!
    }
    fun getWeightRecords(): Set<String> {
        return sharedPreferences.getStringSet(PREF_WEIGHT_RECORDS, hashSetOf())!!
    }
    fun saveWeightRecords(weightRecords: List<String>) {
        sharedPreferences.edit().putStringSet(PREF_WEIGHT_RECORDS, weightRecords.toSet()).apply()
    }

    fun saveFavorites(foodRecords: List<String>) {
        sharedPreferences.edit().putStringSet(PREF_FOOD_FAVORITES, foodRecords.toSet()).apply()
    }

    fun getFavorites(): Set<String> {
        return sharedPreferences.getStringSet(PREF_FOOD_FAVORITES, hashSetOf())!!
    }

    fun saveUserProfile(user: String) {
        sharedPreferences.edit().putString(PREF_PASSIO_USER, user).apply()
    }

    fun getUserProfile(): String {
        return sharedPreferences.getString(PREF_PASSIO_USER, "") ?: ""
    }
}