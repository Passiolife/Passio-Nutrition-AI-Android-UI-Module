package ai.passio.nutrition.uimodule.ui.diary

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.databinding.FragmentDiaryBinding
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.MealLabel
import ai.passio.passiosdk.passiofood.data.measurement.UnitEnergy
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import android.app.DatePickerDialog
import android.text.format.DateFormat
import android.widget.DatePicker
import com.google.gson.GsonBuilder
import java.util.Calendar

/**
 * A simple [Fragment] subclass.
 * Use the [DiaryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DiaryFragment : BaseFragment<DiaryViewModel>(), DiaryCategory.CategoryListener,
    BaseToolbar.ToolbarListener,
    DatePickerDialog.OnDateSetListener {

    private var _binding: FragmentDiaryBinding? = null
    private val binding: FragmentDiaryBinding get() = _binding!!

    private lateinit var dateFormat: java.text.DateFormat
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dateFormat = DateFormat.getLongDateFormat(requireContext())

        viewModel.logsLD.observe(viewLifecycleOwner, ::updateLogs)
        viewModel.fetchLogsForCurrentDay()

        with(binding) {
            toolbar.setup(getString(R.string.my_diary), this@DiaryFragment)
            breakfastCategory.setup(MealLabel.Breakfast, this@DiaryFragment)
            lunchCategory.setup(MealLabel.Lunch, this@DiaryFragment)
            dinnerCategory.setup(MealLabel.Dinner, this@DiaryFragment)
            snackCategory.setup(MealLabel.Snack, this@DiaryFragment)

            toolbarCalendar.text = dateFormat.format(viewModel.getCurrentDate())
            toolbarCalenarBack.setOnClickListener {
                viewModel.setPreviousDay()
            }
            toolbarCalenarForward.setOnClickListener {
                viewModel.setNextDay()
            }
            toolbarCalendar.setOnClickListener {
                calendar.time = viewModel.getCurrentDate()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                DatePickerDialog(
                    requireContext(),
                    this@DiaryFragment,
                    year,
                    month,
                    dayOfMonth
                ).show()
            }
            dailyNutrition.invokeProgressReport(::navigateToProgressReport)
        }
    }

    private fun navigateToProgressReport() {
        viewModel.navigateToProgress()
    }

    private fun updateLogs(records: List<FoodRecord>) {
        val breakfastLogs = records.filter { it.mealLabel == MealLabel.Breakfast }
        val lunchLogs = records.filter { it.mealLabel == MealLabel.Lunch }
        val dinnerLogs = records.filter { it.mealLabel == MealLabel.Dinner }
        val snackLogs = records.filter { it.mealLabel == MealLabel.Snack }

        with(binding) {
            toolbarCalendar.text = dateFormat.format(viewModel.getCurrentDate())

            breakfastCategory.update(breakfastLogs)
            lunchCategory.update(lunchLogs)
            dinnerCategory.update(dinnerLogs)
            snackCategory.update(snackLogs)

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
                2000,
                currentCarbs.toInt(),
                125,
                currentProtein.toInt(),
                100,
                currentFat.toInt(),
                40
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onLogEdit(foodRecord: FoodRecord) {
        val gson = GsonBuilder().create()

        val foodRecordCopy = gson.fromJson(gson.toJson(foodRecord), FoodRecord::class.java)
        sharedViewModel.editFoodRecord(foodRecordCopy)
        viewModel.navigateToEdit()
    }

    override fun onLogDelete(foodRecord: FoodRecord) {
        viewModel.deleteLog(foodRecord)
    }

    override fun onBack() {
        viewModel.navigateBack()
    }

    override fun onRightIconClicked() {

    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        viewModel.setDate(calendar.time)
    }
}