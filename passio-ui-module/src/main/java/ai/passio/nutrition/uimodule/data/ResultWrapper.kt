package ai.passio.nutrition.uimodule.data

sealed class ResultWrapper<out T> {
    data class Success<out T>(val value: T): ResultWrapper<T>()
    data class Error(val error: String? = null): ResultWrapper<Nothing>()
}