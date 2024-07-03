package ai.passio.nutrition.uimodule.data

import android.content.Context
import android.content.SharedPreferences

internal class SharedPrefUtils(private val application: Context) {

    private val prefName = "PassioSharedPreferences"
    private var sharedPreferences: SharedPreferences =
        application.getSharedPreferences(prefName, Context.MODE_PRIVATE)

    companion object {
        private lateinit var sharedPrefUtils: SharedPrefUtils
        fun init(context: Context): SharedPrefUtils {
            if (!::sharedPrefUtils.isInitialized) {
                sharedPrefUtils = SharedPrefUtils(context)
            }
            return sharedPrefUtils
        }


        fun <T> get(key: String, clazz: Class<T>): T = sharedPrefUtils.get(key, clazz)
        fun <T> put(key: String, data: T) = sharedPrefUtils.put(key, data)
        fun contains(key: String) = sharedPrefUtils.contains(key)


    }

    private fun contains(key: String): Boolean = sharedPreferences.contains(key)

    @Suppress("UNCHECKED_CAST")
    private fun <T> get(key: String, clazz: Class<T>): T =
        when (clazz) {
            String::class.java -> sharedPreferences.getString(key, "")
            Boolean::class.java -> sharedPreferences.getBoolean(key, false)
            Float::class.java -> sharedPreferences.getFloat(key, -1f)
            Double::class.java -> sharedPreferences.getFloat(key, -1f)
            Int::class.java -> sharedPreferences.getInt(key, -1)
            Long::class.java -> sharedPreferences.getLong(key, -1L)
            else -> null
        } as T

    private fun <T> put(key: String, data: T) {
        val editor = sharedPreferences.edit()
        when (data) {
            is String -> editor.putString(key, data)
            is Boolean -> editor.putBoolean(key, data)
            is Float -> editor.putFloat(key, data)
            is Double -> editor.putFloat(key, data.toFloat())
            is Int -> editor.putInt(key, data)
            is Long -> editor.putLong(key, data)
        }
        editor.apply()
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}