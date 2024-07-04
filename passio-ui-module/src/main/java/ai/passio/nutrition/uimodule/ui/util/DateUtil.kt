package ai.passio.nutrition.uimodule.ui.util

import android.util.Log
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import java.text.SimpleDateFormat
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