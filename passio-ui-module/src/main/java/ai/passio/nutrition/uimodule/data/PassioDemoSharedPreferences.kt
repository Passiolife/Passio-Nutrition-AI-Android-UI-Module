package ai.passio.nutrition.uimodule.data

import android.content.SharedPreferences

class PassioDemoSharedPreferences(private val sharedPreferences: SharedPreferences) {

    companion object Key {
        const val PREF_NAME = "PassioDemoPrefs"
        private const val PREF_FOOD_RECORDS = "foodRecords"
        private const val PREF_FOOD_FAVORITES = "foodFavorites"
    }

    fun saveRecords(foodRecords: List<String>) {
        sharedPreferences.edit().putStringSet(PREF_FOOD_RECORDS, foodRecords.toSet()).apply()
    }

    fun getRecords(): Set<String> {
        return sharedPreferences.getStringSet(PREF_FOOD_RECORDS, hashSetOf())!!
    }

    fun saveFavorites(foodRecords: List<String>) {
        sharedPreferences.edit().putStringSet(PREF_FOOD_FAVORITES, foodRecords.toSet()).apply()
    }

    fun getFavorites(): Set<String> {
        return sharedPreferences.getStringSet(PREF_FOOD_FAVORITES, hashSetOf())!!
    }
}