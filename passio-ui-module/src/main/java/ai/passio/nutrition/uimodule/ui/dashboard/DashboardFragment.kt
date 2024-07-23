package ai.passio.nutrition.uimodule.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.databinding.FragmentDashboardBinding
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.UserProfile
import ai.passio.nutrition.uimodule.ui.util.showDatePickerDialog
import ai.passio.passiosdk.passiofood.data.measurement.UnitEnergy
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.lang.reflect.Field
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
    }

    private fun navigateToProgressReport() {
        viewModel.navigateToProgress()
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
            val userProfile =  data.first
            val records =  data.second
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

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.dashboard_menu, popupMenu.menu)
        showMenuIcons(popupMenu)
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.my_profile -> {
                    viewModel.navigateToMyProfile()
                    true
                }

                R.id.settings -> {
                    viewModel.navigateToSettings()
                    true
                }

                R.id.log_out -> {
                    Toast.makeText(requireContext(), "Logout successfully!", Toast.LENGTH_LONG)
                        .show()
                    requireActivity().finish()
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showMenuIcons(popupMenu: PopupMenu) {
        try {
            val fields: Array<Field> = popupMenu.javaClass.declaredFields
            for (field in fields) {
                if ("mPopup" == field.name) {
                    field.isAccessible = true
                    val menuPopupHelper = field.get(popupMenu)
                    val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                    val setForceIcons =
                        classPopupHelper.getMethod("setForceShowIcon", Boolean::class.java)
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}