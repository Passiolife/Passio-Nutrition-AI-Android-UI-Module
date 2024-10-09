package ai.passio.nutrition.uimodule.ui.diary

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.data.passioGson
import ai.passio.nutrition.uimodule.databinding.FragmentDiaryBinding
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.MealLabel
import ai.passio.nutrition.uimodule.ui.model.SuggestedFoods
import ai.passio.nutrition.uimodule.ui.model.UserProfile
import ai.passio.nutrition.uimodule.ui.model.meals
import ai.passio.nutrition.uimodule.ui.util.showDatePickerDialog
import ai.passio.nutrition.uimodule.ui.util.toast
import ai.passio.passiosdk.passiofood.data.measurement.UnitEnergy
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import org.joda.time.DateTime

/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class DiaryFragment : BaseFragment<DiaryViewModel>(), DiaryCategory.CategoryListener,
    BaseToolbar.ToolbarListener {

    private var _binding: FragmentDiaryBinding? = null
    private val binding: FragmentDiaryBinding get() = _binding!!

    private lateinit var dateFormat: java.text.DateFormat

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

        /*var currentDate = Date()
        if (arguments?.containsKey("currentDate") == true) {
            arguments?.getLong("currentDate")?.let {
                currentDate = Date(it)
            }
        }
        viewModel.setDate(currentDate)*/

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
                showDatePickerDialog(
                    requireContext(),
                    DateTime(viewModel.getCurrentDate().time)
                ) { selectedDate ->
                    viewModel.setDate(selectedDate.toDate())
                }
            }
            dailyNutrition.invokeProgressReport(::navigateToProgressReport)
            quickSuggestions.setup(quickSuggestionListener)

            viewModel.fetchLogsForCurrentDay()
        }
    }

    private val quickSuggestionListener = object : QuickSuggestionView.QuickSuggestionListener {
        override fun onLogFood(suggestedFoods: SuggestedFoods) {
            viewModel.logFood(suggestedFoods)
        }

        override fun onEditFood(suggestedFoods: SuggestedFoods) {
            if (suggestedFoods.foodRecord != null) {
                sharedViewModel.detailsFoodRecord(suggestedFoods.foodRecord!!)
                viewModel.navigateToDetails()
            } else if (suggestedFoods.searchResult != null) {
                sharedViewModel.passToEdit(suggestedFoods.searchResult!!)
                viewModel.navigateToDetails()
            }
        }

    }

    private fun initObserver() {
        sharedViewModel.diaryCurrentDate.observe(viewLifecycleOwner) { currentDate ->
            viewModel.setDate(currentDate)
            binding.toolbarCalendar.text = dateFormat.format(viewModel.getCurrentDate())
        }
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
                    requireContext().toast("Food item logged.")
                } else {
                    requireContext().toast("Could not log food item.")
                }
            }

            is ResultWrapper.Error -> {
                requireContext().toast(resultWrapper.error)
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
        val breakfastLogs =
            records.meals(MealLabel.Breakfast) //filter { it.mealLabel == MealLabel.Breakfast }
        val lunchLogs = records.meals(MealLabel.Lunch) //filter { it.mealLabel == MealLabel.Lunch }
        val dinnerLogs =
            records.meals(MealLabel.Dinner) //filter { it.mealLabel == MealLabel.Dinner }
        val snackLogs = records.meals(MealLabel.Snack) //filter { it.mealLabel == MealLabel.Snack }

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
        val gson = passioGson
        val foodRecordCopy = gson.fromJson(gson.toJson(foodRecord), FoodRecord::class.java)
        sharedViewModel.detailsFoodRecord(foodRecordCopy)
        viewModel.navigateToEdit()
    }

    override fun onLogDelete(foodRecord: FoodRecord) {
        viewModel.deleteLog(foodRecord)
    }

    override fun onBack() {
        viewModel.navigateBack()
    }

    override fun onRightIconClicked() {
        showPopupMenu(binding.toolbar.findViewById(R.id.toolbarMenu))
    }
}