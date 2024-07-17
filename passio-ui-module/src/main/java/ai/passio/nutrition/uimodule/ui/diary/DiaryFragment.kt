package ai.passio.nutrition.uimodule.ui.diary

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.databinding.FragmentDiaryBinding
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.MealLabel
import ai.passio.nutrition.uimodule.ui.model.SuggestedFoods
import ai.passio.nutrition.uimodule.ui.model.UserProfile
import ai.passio.passiosdk.passiofood.data.measurement.UnitEnergy
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.gson.GsonBuilder
import java.util.Calendar
import java.util.Date

/**
 * A simple [Fragment] subclass.
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

        initObserver()

        var currentDate = Date()
        if (arguments?.containsKey("currentDate") == true) {
            arguments?.getLong("currentDate")?.let {
                currentDate = Date(it)
            }
        }
        viewModel.setDate(currentDate)

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
            quickSuggestions.setup(quickSuggestionListener)
        }
    }

    private val quickSuggestionListener = object : QuickSuggestionView.QuickSuggestionListener {
        override fun onLogFood(suggestedFoods: SuggestedFoods) {
            viewModel.logFood(suggestedFoods)
        }

        override fun onEditFood(suggestedFoods: SuggestedFoods) {
            if (suggestedFoods.foodRecord != null) {
                sharedViewModel.editFoodRecord(suggestedFoods.foodRecord!!)
                viewModel.navigateToDetails()
            } else if (suggestedFoods.searchResult != null) {
                sharedViewModel.passToEdit(suggestedFoods.searchResult!!)
                viewModel.navigateToDetails()
            }
        }

    }

    private fun initObserver() {
        viewModel.logsLD.observe(viewLifecycleOwner, ::updateLogs)
        viewModel.quickSuggestions.observe(viewLifecycleOwner, ::setupQuickSuggestions)
        viewModel.logFoodEvent.observe(viewLifecycleOwner, ::foodItemLogged)
        viewModel.showLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.viewLoader.isVisible = isLoading
        }
    }

    private fun foodItemLogged(resultWrapper: ResultWrapper<Boolean>) {
        when (resultWrapper) {
            is ResultWrapper.Success -> {
                if (resultWrapper.value) {
                    Toast.makeText(
                        requireContext(),
                        "Food item logged.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Could not log food item.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            is ResultWrapper.Error -> {
                Toast.makeText(
                    requireContext(),
                    resultWrapper.error,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupQuickSuggestions(foodData: List<SuggestedFoods>) {
        binding.quickSuggestions.updateData(foodData)
    }

    private fun navigateToProgressReport() {
        viewModel.navigateToProgress()
    }

    private fun updateLogs(data: Pair<UserProfile, List<FoodRecord>>) {
        val userProfile = data.first
        val records = data.second
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