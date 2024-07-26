package ai.passio.nutrition.uimodule.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.databinding.FragmentDashboardBinding
import ai.passio.nutrition.uimodule.ui.activity.UserCache
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.UserProfile
import ai.passio.nutrition.uimodule.ui.util.StringKT.setSpannableBold
import ai.passio.nutrition.uimodule.ui.util.StringKT.singleDecimal
import ai.passio.nutrition.uimodule.ui.util.showDatePickerDialog
import ai.passio.passiosdk.passiofood.data.measurement.UnitEnergy
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import android.annotation.SuppressLint
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.Date
import java.util.Locale

class DashboardFragment : BaseFragment<DashboardViewModel>() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding: FragmentDashboardBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        with(binding) {

            toolbar.setup(getString(R.string.welcome), baseToolbarListener)

            timeTitle.setOnClickListener {
                showDatePickerDialog(requireContext()) { selectedDate ->
                    viewModel.setCurrentDate(selectedDate.toDate())
                }
            }
            moveNext.setOnClickListener {
                viewModel.setNextDay()
            }
            movePrevious.setOnClickListener {
                viewModel.setPreviousDay()
            }
            toggleWeekMonth.setOnClickListener {
                viewModel.toggleCalendarMode()
            }
            weightContainer.setOnClickListener {
                viewModel.navigateToWeightTracking()
            }
            waterContainer.setOnClickListener {
                viewModel.navigateToWaterTracking()
            }


            setupCalendarView()

            dailyNutrition.invokeProgressReport(::navigateToProgressReport)
        }
    }

    private fun setupCalendarView() {
        with(binding) {
            // Change the month name text color
            val titleTextView =
                calendarView.findViewById<TextView>(R.id.month_name)//getChildAt(0) as TextView
            titleTextView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.passio_gray900
                )
            )
//            calendarView.currentDate = CalendarDay.today()
            val aa = DateTime.now()//.plusDays(2)
            calendarView.selectedDate = CalendarDay.from(aa.year, aa.monthOfYear, aa.dayOfMonth)

            viewModel.fetchAdherence()
        }
    }

    private val baseToolbarListener = object : BaseToolbar.ToolbarListener {
        override fun onBack() {
            viewModel.navigateBack()
        }

        override fun onRightIconClicked() {
            showPopupMenu(binding.toolbar.findViewById(R.id.toolbarMenu))
        }

    }

    private fun initObserver() {
        viewModel.currentDateEvent.observe(viewLifecycleOwner, ::updateDate)
        viewModel.logsLD.observe(viewLifecycleOwner, ::updateLogs)
        viewModel.calendarMode.observe(viewLifecycleOwner, ::updateCalenderMode)
        viewModel.adherents.observe(viewLifecycleOwner, ::updateAdherence)
        viewModel.waterSummary.observe(viewLifecycleOwner, ::showWaterSummary)
        viewModel.weightSummary.observe(viewLifecycleOwner, ::showWeightSummary)
    }

    private fun navigateToProgressReport() {
        viewModel.navigateToProgress()
    }

    @SuppressLint("SetTextI18n")
    private fun showWaterSummary(summary: Pair<Double, Double>) {
        with(binding)
        {
            val unitVal = UserCache.getProfile().measurementUnit.waterUnit.value.lowercase()
            val totalVal = summary.first.singleDecimal()
            val remainingVal = summary.second.singleDecimal() + " $unitVal"
            waterValue.text = totalVal
            waterUnit.text = unitVal
            waterTarget.text = (remainingVal + " " + getString(R.string.remain_to_daily_goal)).setSpannableBold(remainingVal)
        }
    }
    @SuppressLint("SetTextI18n")
    private fun showWeightSummary(summary: Pair<Double, Double>) {
        with(binding)
        {
            val unitVal = UserCache.getProfile().measurementUnit.weightUnit.value.lowercase()
            val totalVal = summary.first.singleDecimal()
            val remainingVal = summary.second.singleDecimal() + " $unitVal"
            weightValue.text = totalVal
            weightUnit.text = unitVal
            weightTarget.text = (remainingVal + " " + getString(R.string.remain_to_daily_goal)).setSpannableBold(remainingVal)
        }
    }

    private fun updateCalenderMode(calendarMode: CalendarMode) {
        with(binding)
        {
            if (calendarView.calendarMode != calendarMode) {
                calendarView.state().edit().setCalendarDisplayMode(calendarMode).commit()
            }

            if (calendarMode == CalendarMode.WEEKS) {
                labelAdherence.text = requireContext().getString(R.string.weekly_adherence)
                toggleWeekMonth.rotation = 0f
            } else {
                labelAdherence.text = requireContext().getString(R.string.monthly_adherence)
                toggleWeekMonth.rotation = 180f
            }
        }
    }

    private fun updateDate(currentDate: Date) {

        val selectedDate = DateTime(currentDate.time)
        val formattedDate = if (selectedDate.toLocalDate() == DateTime.now().toLocalDate()) {
            requireContext().getString(R.string.today)
        } else {
            val dateFormat =
                DateTimeFormat.forPattern("MMMM dd, yyyy").withLocale(Locale.getDefault())
            dateFormat.print(selectedDate)
        }

        with(binding) {
            timeTitle.text = formattedDate

            val calendarDay = CalendarDay.from(
                selectedDate.year,
                selectedDate.monthOfYear,
                selectedDate.dayOfMonth
            )
            calendarView.currentDate = calendarDay
            calendarView.selectedDate = calendarDay
            calendarView.invalidateDecorators()
            viewModel.fetchLogsForCurrentDay()

        }
    }

    private fun updateAdherence(records: List<Long>) {

        with(binding)
        {
            val days = records.map { millisToDay(it) }
            calendarView.removeDecorators()
            calendarView.addDecorators(
                TodayDecorator(requireContext()),
                PastDaysWithEventsDecorator(requireContext(), days),
                PastDaysWithoutEventsDecorator(requireContext(), days),
                FutureDaysDecorator(requireContext()),
                SelectedDayDecorator(requireContext(), calendarView),
                DisableDateSelectionDecorator()
            )
        }

    }

    private fun updateLogs(data: Pair<UserProfile, List<FoodRecord>>) {

        with(binding) {
            val userProfile = data.first
            val records = data.second
            val currentCalories = records.map { it.nutrients().calories() }
                .fold(UnitEnergy()) { acc, unitEnergy -> acc + unitEnergy }.kcalValue()
            val currentCarbs = records.map { it.nutrients().carbs() }
                .fold(UnitMass()) { acc, unitMass -> acc + unitMass }.gramsValue()
            val currentProtein = records.map { it.nutrients().protein() }
                .fold(UnitMass()) { acc, unitMass -> acc + unitMass }.gramsValue()
            val currentFat = records.map { it.nutrients().fat() }
                .fold(UnitMass()) { acc, unitMass -> acc + unitMass }.gramsValue()

            dailyNutrition.setup(
                currentCalories.toInt(),
                userProfile.caloriesTarget,
                currentCarbs.toInt(),
                userProfile.getCarbsGrams().toInt(),
                currentProtein.toInt(),
                userProfile.getProteinGrams().toInt(),
                currentFat.toInt(),
                userProfile.getFatGrams().toInt()
            )
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}