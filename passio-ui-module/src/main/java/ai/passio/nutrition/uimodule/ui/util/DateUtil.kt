package ai.passio.nutrition.uimodule.ui.util

import ai.passio.nutrition.uimodule.R
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
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

fun showDatePickerDialog(
    context: Context,
    now: DateTime = DateTime.now(),
    onDateSelected: (selectedDateTime: DateTime) -> Unit
) {
//    val now = DateTime.now()
    val year = now.year
    val month = now.monthOfYear - 1 // DatePickerDialog uses 0-based month
    val day = now.dayOfMonth

    val datePickerDialog = DatePickerDialog(
        context, R.style.DatePickerTheme,
        { _, selectedYear, selectedMonth, selectedDay ->
            // Format the selected date
            val selectedDate = DateTime(selectedYear, selectedMonth + 1, selectedDay, 0, 0)
            onDateSelected.invoke(selectedDate)
        },
        year, month, day
    )
    datePickerDialog.show()

    /* val datePicker = MaterialDatePicker.Builder.datePicker()
 //        .setTitleText(context.getString(R.string.select_meal_date))
         .setSelection(now.millis)
         .build()
     datePicker.addOnPositiveButtonClickListener { dateTime ->
         val newDate = DateTime(dateTime)
         onDateSelected.invoke(newDate)
     }
     datePicker.show(requireActivity().supportFragmentManager, "DATE")*/
}

fun showTimePickerDialog(context: Context, onTimeSelected: (selectedDateTime: DateTime) -> Unit) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val timePickerDialog = TimePickerDialog(context, { _, selectedHour, selectedMinute ->
        val selectedDateTime = DateTime.now()
            .withHourOfDay(selectedHour)
            .withMinuteOfHour(selectedMinute)

//        val timeFormatter = DateTimeFormat.forPattern("hh:mm a")
//        val formattedTime = selectedDateTime.toString(timeFormatter)
        onTimeSelected.invoke(selectedDateTime)
    }, hour, minute, false)

    timePickerDialog.show()
}


fun isToday(milliseconds: Long): Boolean {
    val dateTime = DateTime(milliseconds, DateTimeZone.getDefault())
    val today = LocalDate.now()
    val givenDate = dateTime.toLocalDate()
    return givenDate == today
}

//yyyy-MM-dd
fun dateToTimestamp(dateString: String, dateFormat: String): Long {
    // Define the date format
    val formatter = DateTimeFormat.forPattern(dateFormat)

    // Parse the date string to a DateTime object
    val dateTime = DateTime.parse(dateString, formatter)

    // Convert the DateTime object to a timestamp in milliseconds
    return dateTime.millis
}

const val DAY_FORMAT = "EE"
const val DAY_FORMAT_FULL = "EEEE, MMMM dd, yyyy"
fun dateToFormat(localDate: LocalDate, format: String): String {
    // Define the formatter for the desired pattern
    val formatter = DateTimeFormat.forPattern(format).withLocale(Locale.ENGLISH)

    // Format the date to the desired pattern
    val formattedDate = localDate.toString(formatter)
    return formattedDate
}

fun getStartTimestamps(date: DateTime): Long {
    return date.withTimeAtStartOfDay().millis
}

fun getEndTimestamps(date: DateTime): Long {
    return date.withTime(23, 59, 59, 999).millis
}