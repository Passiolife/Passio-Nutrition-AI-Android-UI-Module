package ai.passio.nutrition.uimodule.ui.progress

import com.github.mikephil.charting.formatter.ValueFormatter
import org.joda.time.DateTime
import java.lang.IllegalStateException

class WeekAxisValueFormatter(private val startTime: DateTime) : ValueFormatter() {

    companion object {
        private const val MONDAY = 1
        private const val TUESDAY = 2
        private const val WEDNESDAY = 3
        private const val THURSDAY = 4
        private const val FRIDAY = 5
        private const val SATURDAY = 6
        private const val SUNDAY = 7
    }

    override fun getFormattedValue(value: Float): String {
//        val startTime = DateTime().minusDays(6)
        return when (startTime.plusDays(value.toInt()).dayOfWeek) {
            MONDAY -> "Mo"
            TUESDAY -> "Tu"
            WEDNESDAY -> "We"
            THURSDAY -> "Th"
            FRIDAY -> "Fr"
            SATURDAY -> "Sa"
            SUNDAY -> "Su"
            else -> throw IllegalStateException("No known value to format: $value")
        }
    }
}