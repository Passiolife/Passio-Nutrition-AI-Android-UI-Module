package ai.passio.nutrition.uimodule.data

import android.content.SharedPreferences

class PassioDemoSharedPreferences(private val sharedPreferences: SharedPreferences) {

    companion object Key {
        const val PREF_NAME = "PassioUIPrefs"
        private const val PREF_FOOD_RECORDS = "foodRecords"
        private const val PREF_CUSTOM_FOODS = "customFoods"
        private const val PREF_RECIPES = "recipes"
        private const val PREF_WEIGHT_RECORDS = "weightRecords"
        private const val PREF_WATER_RECORDS = "waterRecords"
        private const val PREF_FOOD_FAVORITES = "foodFavorites"
        private const val PREF_PASSIO_USER = "userProfile"
    }

    fun saveRecords(foodRecords: List<String>) {
        sharedPreferences.edit().putStringSet(PREF_FOOD_RECORDS, foodRecords.toSet()).apply()
    }

    fun getRecords(): Set<String> {
        return sharedPreferences.getStringSet(PREF_FOOD_RECORDS, hashSetOf())!!
    }

    fun saveCustomFoods(customFoods: List<String>) {
        sharedPreferences.edit().putStringSet(PREF_CUSTOM_FOODS, customFoods.toSet()).apply()
    }

    fun getCustomFoods(): Set<String> {
        return sharedPreferences.getStringSet(PREF_CUSTOM_FOODS, hashSetOf())!!
    }
    fun saveRecipes(recipes: List<String>) {
        sharedPreferences.edit().putStringSet(PREF_RECIPES, recipes.toSet()).apply()
    }

    fun getRecipes(): Set<String> {
        return sharedPreferences.getStringSet(PREF_RECIPES, hashSetOf())!!
    }
    fun getWeightRecords(): Set<String> {
        return sharedPreferences.getStringSet(PREF_WEIGHT_RECORDS, hashSetOf())!!
    }
    fun saveWeightRecords(weightRecords: List<String>) {
        sharedPreferences.edit().putStringSet(PREF_WEIGHT_RECORDS, weightRecords.toSet()).apply()
    }
    fun getWaterRecords(): Set<String> {
        return sharedPreferences.getStringSet(PREF_WATER_RECORDS, hashSetOf())!!
    }
    fun saveWaterRecords(weightRecords: List<String>) {
        sharedPreferences.edit().putStringSet(PREF_WATER_RECORDS, weightRecords.toSet()).apply()
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