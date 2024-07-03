package ai.passio.nutrition.uimodule.ui.util

object StringKT {

    fun String.capitalized(): String {
        return this.replaceFirstChar {
            if (it.isLowerCase())
                it.titlecase()
            else it.toString()
        }
    }

}