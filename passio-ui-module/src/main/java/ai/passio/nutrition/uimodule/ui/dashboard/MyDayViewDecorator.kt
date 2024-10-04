package ai.passio.nutrition.uimodule.ui.dashboard

import ai.passio.nutrition.uimodule.R
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
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
    // Create the innermost shape with blue solid background
    val backgroundShape = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(solidColor) // Set solid color to blue
    }

    // Create the first border (red) with 1px width
    val borderShape = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(strokeColor) // Transparent inside
        setStroke(1, Color.TRANSPARENT) // Red border with 1px width
    }

    // Create the second border (transparent padding area) with 3px width
    val paddingShape = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(Color.TRANSPARENT) // Fully transparent padding shape
        setStroke(3, Color.TRANSPARENT) // Green border with 3px width
    }

    // Layer the paddingShape, borderShape, and backgroundShape
    val layers = arrayOf(paddingShape, borderShape, backgroundShape)
    val layerDrawable = LayerDrawable(layers)

    // Set padding between the layers to ensure borders appear correctly
    layerDrawable.setLayerInset(1, 1, 1, 1, 1) // Padding for the borderShape layer (1px inset)
    layerDrawable.setLayerInset(2, 4, 4, 4, 4) // Padding for the backgroundShape (3px inset from the paddingShape)

    return layerDrawable
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