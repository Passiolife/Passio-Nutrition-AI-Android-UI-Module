package ai.passio.nutrition.uimodule.ui.util

open class Event<out T>(private val content: T) {

    private var eventHandled = false

    fun getContent(): T? {
        return if (eventHandled) null else content
    }

    fun peek(): T = content
}