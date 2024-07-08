package ai.passio.nutrition.uimodule.ui.dashboard

import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import org.joda.time.DateTime
import org.joda.time.LocalDate

class MyDayViewDecorator(
    private val calendarView: MaterialCalendarView,
    private val adherents: MutableList<Long>
) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay?): Boolean {

        //today
        //previous green
        //previous red
        //upcoming gray
        // previos green - blue
        //previous red - blue
        if (day == null)
            return false

        val calendarDay = DateTime(day.year, day.month, day.day, 0, 1).toLocalDate()
        val today = LocalDate.now()

        if (calendarDay.isEqual(today) || day.day == calendarView.currentDate.day || day.day == calendarView.selectedDate?.day) {
            return false
        } else if (calendarDay.isBefore(today)) {
            return true
        }
        return false
    }

    override fun decorate(view: DayViewFacade?) {


        view?.setSelectionDrawable(createCircularDrawable(Color.GREEN, Color.TRANSPARENT))
//        view?.addSpan( ForegroundColorSpan(Color.WHITE));
    }


    fun updateAdherents(adherents: MutableList<Long>) {
        this.adherents.clear()
        this.adherents.addAll(adherents)
    }


}

private fun createCircularDrawable(solidColor: Int, strokeColor: Int): Drawable {
    val shape = GradientDrawable()

    shape.shape = GradientDrawable.OVAL
    shape.setSize(DesignUtils.dp2px(10f), DesignUtils.dp2px(10f))
    shape.setColor(solidColor)
    shape.setStroke(DesignUtils.dp2px(0.5f), strokeColor) // Convert dp to pixels
    return shape
}

class TodayDecorator : DayViewDecorator {
    private val date = CalendarDay.today()

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == date
    }

    override fun decorate(view: DayViewFacade) {
        view.setBackgroundDrawable(ColorDrawable(Color.RED))
    }
}

class SelectedDayDecorator(private val selectedDate: MaterialCalendarView) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == selectedDate.selectedDate
    }

    override fun decorate(view: DayViewFacade) {
        view.setBackgroundDrawable(ColorDrawable(Color.BLUE))
    }
}

fun millisToDay(timestamp: Long): CalendarDay {
    val localDate = DateTime(timestamp).toLocalDate()
    return CalendarDay.from(localDate.year, localDate.monthOfYear, localDate.dayOfMonth)
}

class PastDaysWithEventsDecorator(private val dates: List<CalendarDay>) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {

        return dates.contains(day) && day.isBefore(CalendarDay.today())
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(5f, Color.GREEN))
    }
}

class PastDaysWithoutEventsDecorator(private val dates: Collection<CalendarDay>) :
    DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return !dates.contains(day) && day.isBefore(CalendarDay.today())
    }

    override fun decorate(view: DayViewFacade) {
        view.setBackgroundDrawable(ColorDrawable(Color.RED))
    }
}

class FutureDaysDecorator : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day.isAfter(CalendarDay.today())
    }

    override fun decorate(view: DayViewFacade) {
        view.setBackgroundDrawable(ColorDrawable(Color.GRAY))
    }
}

class DisableDateSelectionDecorator : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return true // Apply this decorator to all days
    }

    override fun decorate(view: DayViewFacade) {
        view.setDaysDisabled(true) // Disable selection for this day
    }
}