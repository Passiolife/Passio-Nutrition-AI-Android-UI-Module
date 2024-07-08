package ai.passio.nutrition.uimodule.ui.dashboard

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import org.joda.time.DateTime

private val styleSpan = StyleSpan(Typeface.BOLD)
private fun createCircularDrawable(solidColor: Int, strokeColor: Int): Drawable {
    val shape = GradientDrawable()

    shape.shape = GradientDrawable.OVAL
    shape.setSize(DesignUtils.dp2px(10f), DesignUtils.dp2px(10f))
    shape.setColor(solidColor)
    shape.setStroke(DesignUtils.dp2px(1.0f), strokeColor) // Convert dp to pixels
    return shape
}


internal fun millisToDay(timestamp: Long): CalendarDay {
    val localDate = DateTime(timestamp).toLocalDate()
    return CalendarDay.from(localDate.year, localDate.monthOfYear, localDate.dayOfMonth)
}

class TodayDecorator(private val context: Context) : DayViewDecorator {
    private val date = CalendarDay.today()

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == date
    }

    override fun decorate(view: DayViewFacade) {
        val colorSpan = ForegroundColorSpan(ContextCompat.getColor(context, R.color.passio_white))
        view.addSpan(colorSpan)
        view.addSpan(styleSpan)
        view.setBackgroundDrawable(
            createCircularDrawable(
                ContextCompat.getColor(context, R.color.passio_primary),
                Color.TRANSPARENT
            )
        )
    }
}

class SelectedDayDecorator(
    private val context: Context,
    private val selectedDate: MaterialCalendarView
) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == selectedDate.selectedDate
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(styleSpan)
        view.setSelectionDrawable(
            createCircularDrawable(
                ContextCompat.getColor(
                    context,
                    R.color.passio_primary
                ), Color.BLUE
            )
        )
    }
}


class PastDaysWithEventsDecorator(
    private val context: Context,
    private val dates: List<CalendarDay>
) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {

        return dates.contains(day) && day.isBefore(CalendarDay.today())
    }

    override fun decorate(view: DayViewFacade) {
        val colorSpan =
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.passio_green_800))
        view.addSpan(colorSpan)
        view.addSpan(styleSpan)
        view.setBackgroundDrawable(
            createCircularDrawable(
                ContextCompat.getColor(context, R.color.passio_green_100),
                Color.TRANSPARENT
            )
        )
//        view.addSpan(DotSpan(5f, Color.GREEN))
    }
}

class PastDaysWithoutEventsDecorator(
    private val context: Context,
    private val dates: Collection<CalendarDay>
) :
    DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return !dates.contains(day) && day.isBefore(CalendarDay.today())
    }

    override fun decorate(view: DayViewFacade) {
        val colorSpan = ForegroundColorSpan(ContextCompat.getColor(context, R.color.passio_red800))
        view.addSpan(colorSpan)
        view.addSpan(styleSpan)
        view.setBackgroundDrawable(
            createCircularDrawable(
                ContextCompat.getColor(
                    context,
                    R.color.passio_red100
                ), Color.TRANSPARENT
            )
        )
    }
}

class FutureDaysDecorator(private val context: Context) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day.isAfter(CalendarDay.today())
    }

    override fun decorate(view: DayViewFacade) {
        val colorSpan = ForegroundColorSpan(ContextCompat.getColor(context, R.color.passio_gray400))
        view.addSpan(colorSpan)
        view.addSpan(styleSpan)
        view.setBackgroundDrawable(
            createCircularDrawable(
                ContextCompat.getColor(context, R.color.passio_indigo50),
                Color.TRANSPARENT
            )
        )
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