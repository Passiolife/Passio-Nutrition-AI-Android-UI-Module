package ai.passio.nutrition.uimodule.ui.progress

import com.github.mikephil.charting.formatter.ValueFormatter
import org.joda.time.DateTime

class MonthAxisValueFormatter(private val startTime: DateTime) : ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        val currentTime = startTime //DateTime().minusDays(30)
//        currentTime.plusMonths(1).minusDays(1)
        val time = currentTime.plusDays(value.toInt())
        return time.dayOfMonth.toString()
    }
}