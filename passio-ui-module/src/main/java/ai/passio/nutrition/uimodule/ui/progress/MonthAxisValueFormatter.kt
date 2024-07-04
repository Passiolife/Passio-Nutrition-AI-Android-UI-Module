package ai.passio.nutrition.uimodule.ui.progress

import com.github.mikephil.charting.formatter.ValueFormatter
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.Locale

class MonthAxisValueFormatter(private val startTime: DateTime) : ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        val currentTime = startTime //DateTime().minusDays(30)
        val lastDay = currentTime.plusMonths(1).minusDays(1).dayOfMonth()
        val time: DateTime = if (value.toInt() < lastDay.get()) {
            currentTime.withDayOfMonth(value.toInt() + 1)
        } else{
            currentTime.plusMonths(1).minusDays(1)
        }
//        val currentDay = currentTime.withDayOfMonth(value.toInt() + 1)
//        val time = currentTime.withDayOfMonth(value.toInt() + 1)

        val dateFormat = DateTimeFormat.forPattern("MMM d").withLocale(Locale.getDefault())
        return dateFormat.print(time) //time.dayOfMonth.toString()
    }
}