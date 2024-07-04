package ai.passio.nutrition.uimodule.ui.progress

import com.github.mikephil.charting.formatter.ValueFormatter
import org.joda.time.DateTime
import java.util.Locale

class WeekAxisValueFormatter(private val startTime: DateTime) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return startTime.plusDays(value.toInt()).dayOfWeek().getAsText(Locale.getDefault())
            .substring(0, 2)
    }
}