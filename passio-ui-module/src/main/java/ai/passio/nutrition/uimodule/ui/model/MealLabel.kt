package ai.passio.nutrition.uimodule.ui.model

import java.lang.IllegalStateException
import java.util.*

enum class MealLabel(val value: String) {
    Breakfast("Breakfast"),
    Lunch("Lunch"),
    Dinner("Dinner"),
    Snack("Snack");

    companion object {
        fun stringToMealLabel(text: String): MealLabel {
            return when (text) {
                "Breakfast" -> Breakfast
                "Lunch" -> Lunch
                "Dinner" -> Dinner
                "Snack" -> Snack
                else -> throw IllegalStateException("No known MealLabel: $this")
            }
        }

        fun dateToMealLabel(timestamp: Long): MealLabel {
            val date = Date(timestamp)
            val calendar = Calendar.getInstance()
            calendar.time = date
            val hours = calendar.get(Calendar.HOUR_OF_DAY)
            val minutes = calendar.get(Calendar.MINUTE)
            val timeOfDay = hours * 100 + minutes

            return when (timeOfDay) {
                in 530..1030 -> Breakfast
                in 1130..1400 -> Lunch
                in 1700..2100 -> Dinner
                else -> Snack
            }
        }
    }
}