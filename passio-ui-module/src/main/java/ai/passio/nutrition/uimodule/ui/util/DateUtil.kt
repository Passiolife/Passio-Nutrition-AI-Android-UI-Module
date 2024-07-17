package ai.passio.nutrition.uimodule.ui.util

import android.app.DatePickerDialog
import android.content.Context
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


fun getStartOfWeek(date: DateTime): DateTime {
    return date.withDayOfWeek(DateTimeConstants.MONDAY).withTimeAtStartOfDay()
}

fun getEndOfWeek(date: DateTime): DateTime {
    return date.withDayOfWeek(DateTimeConstants.MONDAY).withTimeAtStartOfDay().plusDays(6)
        .withTime(23, 59, 59, 999)
}

fun getStartOfMonth(date: DateTime): DateTime {
    return date.withDayOfMonth(1).withTimeAtStartOfDay()
}

fun getEndOfMonth(date: DateTime): DateTime {
    return date.plusMonths(1).withDayOfMonth(1).minusDays(1).withTime(23, 59, 59, 999)
}
fun getBefore30Days(date: DateTime): DateTime {
    return date.minusDays(30).withTimeAtStartOfDay()
}

fun isPartOfCurrentWeek(date: DateTime): Boolean {
    val startOfWeek = getStartOfWeek(DateTime.now())
    val endOfWeek = getEndOfWeek(DateTime.now())
    return date.isAfter(startOfWeek) && date.isBefore(endOfWeek.plusMillis(1))
}

fun isPartOfCurrentMonth(date: DateTime): Boolean {
    val startOfMonth = getStartOfMonth(DateTime.now())
    val endOfMonth = getEndOfMonth(DateTime.now())
    return date.isAfter(startOfMonth) && date.isBefore(endOfMonth.plusMillis(1))
}

fun getWeekDuration(date: DateTime): String {
    val startOfWeek = getStartOfWeek(date)
    val endOfWeek = getEndOfWeek(date)

    val dateFormat = SimpleDateFormat("MM/dd/yy", Locale.getDefault())

    val startOfWeekStr = dateFormat.format(startOfWeek.toDate())
    val endOfWeekStr = dateFormat.format(endOfWeek.toDate())

    return "$startOfWeekStr - $endOfWeekStr"
}

fun getMonthName(date: DateTime): String {
    val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return dateFormat.format(date.toDate())
}

fun showDatePickerDialog(context: Context, onDateSelected: (selectedDateTime: DateTime) -> Unit) {
    val now = DateTime.now()
    val year = now.year
    val month = now.monthOfYear - 1 // DatePickerDialog uses 0-based month
    val day = now.dayOfMonth

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            // Format the selected date
            val selectedDate = DateTime(selectedYear, selectedMonth + 1, selectedDay, 0, 0)
            onDateSelected.invoke(selectedDate)
        },
        year, month, day
    )
    datePickerDialog.show()
}

fun timestampToDate(timestamp: Long): Long {
    // Convert millis to a date with only date part (ignoring time)
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

fun isToday(milliseconds: Long): Boolean {
    val dateTime = DateTime(milliseconds, DateTimeZone.getDefault())
    val today = LocalDate.now()
    val givenDate = dateTime.toLocalDate()
    return givenDate == today
}