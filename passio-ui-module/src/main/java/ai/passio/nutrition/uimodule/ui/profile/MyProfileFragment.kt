package ai.passio.nutrition.uimodule.ui.profile

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.databinding.FragmentMyProfileBinding
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.model.MeasurementUnit
import ai.passio.nutrition.uimodule.ui.model.UserProfile
import ai.passio.nutrition.uimodule.ui.profile.DailyNutritionTargetDialog.DailyNutritionTarget
import ai.passio.nutrition.uimodule.ui.settings.HeightPickerDialog
import ai.passio.nutrition.uimodule.ui.util.RoundedSlicesPieChartRenderer
import ai.passio.nutrition.uimodule.ui.util.StringKT.singleDecimal
import ai.passio.nutrition.uimodule.ui.util.toast
import ai.passio.passiosdk.passiofood.data.model.PassioMealPlan
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class MyProfileFragment : BaseFragment<MyProfileViewModel>() {

    private var _binding: FragmentMyProfileBinding? = null
    private val binding: FragmentMyProfileBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObserver()

        with(binding)
        {
            toolbar.setup(getString(R.string.my_profile), baseToolbarListener)
            toolbar.hideRightIcon()

            height.setOnClickListener {
                showHeightPicker()
            }

            save.setOnClickListener {
                viewModel.updateUser()
            }

            dailyNutritionContainer.setOnClickListener {
                viewModel.customizeDailyNutritionTarget()
            }

            setupEditable(name) {
                viewModel.updateUserName(it)
            }
            setupEditable(age) {
                viewModel.updateAge(it)
            }
            setupEditable(weight) {
                viewModel.updateWeight(it)
            }
            setupEditable(targetWeight) {
                viewModel.updateTargetWeight(it)
            }
            setupEditable(waterTarget) {
                viewModel.updateWaterTarget(it)
            }
            setupChart()
        }
    }

    private val baseToolbarListener = object : BaseToolbar.ToolbarListener {
        override fun onBack() {
            viewModel.navigateBack()
        }

        override fun onRightIconClicked() {

        }

    }

    private fun initObserver() {
        viewModel.userProfileEvent.observe(viewLifecycleOwner, ::showUserData)
        viewModel.updateProfileEvent.observe(viewLifecycleOwner, ::showUpdateUserResult)
        viewModel.dailyNutritionTarget.observe(viewLifecycleOwner, ::renderChart)
        viewModel.dailyNutritionTargetCustomize.observe(
            viewLifecycleOwner,
            ::showDailyNutritionTargetPicker
        )
        viewModel.showLoading.observe(viewLifecycleOwner){ isLoading ->
            binding.viewLoader.isVisible = isLoading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showUpdateUserResult(resultWrapper: ResultWrapper<UserProfile>) {
        when (resultWrapper) {
            is ResultWrapper.Success -> {
                requireContext().toast("User Profile Saved!")
//                viewModel.navigateBack()
            }

            is ResultWrapper.Error -> {
                requireContext().toast(resultWrapper.error)
            }
        }

    }

    private fun showUserData(userProfile: UserProfile) {

        with(binding)
        {
            name.setText(userProfile.userName)
            age.setText(userProfile.getDisplayAge())
            height.text = userProfile.getDisplayHeight()
            weight.setText(userProfile.getDisplayWeight())
            weightUnit.text = userProfile.measurementUnit.weightUnit.value
            targetWeight.setText(userProfile.getDisplayTargetWeight())
            targetWeightUnit.text = userProfile.measurementUnit.weightUnit.value
            waterTarget.setText(userProfile.getDisplayTargetWater())
            waterUnit.text = userProfile.measurementUnit.waterUnit.value

            setupGenderView(userProfile.gender)
            setupActivityLevelView(userProfile.activityLevel)
            setupCalorieDeficitView(userProfile.measurementUnit, userProfile.calorieDeficit)
            setupDietView(userProfile.passioMealPlan)

        }
    }

    private fun setupChart() {
        with(binding.macrosChart) {
            renderer = RoundedSlicesPieChartRenderer(this, animator, viewPortHandler)
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 85f
            transparentCircleRadius = 85f
            description.isEnabled = false
            legend.isEnabled = false
            setDrawEntryLabels(false)
//            setDrawSliceText(false)
            setDrawMarkers(false)
            setTouchEnabled(false)
        }
    }

    private fun renderChart(userProfile: UserProfile) {

        binding.height.text = userProfile.getDisplayHeight()


        binding.bmiChart.setCurrentBMI(userProfile.calculateBMI())

        val carbColor = ContextCompat.getColor(requireContext(), R.color.passio_carbs)
        val proteinColor = ContextCompat.getColor(requireContext(), R.color.passio_protein)
        val fatColor = ContextCompat.getColor(requireContext(), R.color.passio_fat)

        val carbPercent = userProfile.carbsPer
        val proteinPercent = userProfile.proteinPer
        val fatPercent = userProfile.fatPer
        val calories = userProfile.caloriesTarget

        with(binding) {

            val carbGrams = "${userProfile.getCarbsGrams().singleDecimal()} g"
            val proteinGrams = "${userProfile.getProteinGrams().singleDecimal()} g"
            val fatGrams = "${userProfile.getFatGrams().singleDecimal()} g"

            val carbString = SpannableString(" $carbGrams ($carbPercent%)")
            carbString.setSpan(
                ForegroundColorSpan(carbColor),
                0,
                carbGrams.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            carbsValue.text = carbString

            val proteinString = SpannableString(" $proteinGrams ($proteinPercent%)")
            proteinString.setSpan(
                ForegroundColorSpan(proteinColor),
                0,
                proteinGrams.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            proteinValue.text = proteinString

            val fatString = SpannableString(" $fatGrams ($fatPercent%)")
            fatString.setSpan(
                ForegroundColorSpan(fatColor),
                0,
                fatGrams.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            fatValue.text = fatString
            caloriesValue.text = calories.toString()

            val entries = mutableListOf<PieEntry>()
            entries.add(PieEntry(carbPercent.toFloat(), "carbs"))
            entries.add(PieEntry(proteinPercent.toFloat(), "protein"))
            entries.add(PieEntry(fatPercent.toFloat(), "fat"))

            val dataSet = PieDataSet(entries, "Macros")
            val colors = listOf(carbColor, proteinColor, fatColor)
            dataSet.colors = colors
            dataSet.valueTextSize = 0f
            dataSet.sliceSpace = 8f

            val data = PieData(dataSet)
            macrosChart.data = data
            macrosChart.invalidate()
        }
    }

    private fun setupEditable(
        editView: AppCompatEditText,
        onValueChanged: (value: String) -> Unit,
    ) {
        editView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                onValueChanged.invoke(s.toString())
            }

            override fun beforeTextChanged(
                s: CharSequence?, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
            }

        })
    }

    private fun setupGenderView(genderValue: Gender) {
        with(binding)
        {
            if (gender.adapter == null || gender.adapter.count == 0) {
                val adapter = GenericSpinnerAdapter(
                    context = requireContext(),
                    items = listOf(Gender.Male, Gender.Female)
                ) { item ->
                    item.value
                }
                gender.adapter = adapter
                gender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View,
                        position: Int,
                        id: Long
                    ) {
                        val selectedItem = parent.getItemAtPosition(position) as Gender
                        viewModel.updateGender(selectedItem)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // Another interface callback
                    }
                }
            }

            gender.setSelection(
                if (genderValue == Gender.Male)
                    0
                else
                    1
            )
        }
    }

    private fun setupActivityLevelView(activityLevelValue: ActivityLevel) {
        with(binding)
        {
            val items = viewModel.activityLevels
            if (activityLevel.adapter == null || activityLevel.adapter.count == 0) {
                val adapter = GenericSpinnerAdapter(
                    context = requireContext(),
                    items = items
                ) { item ->
                    item.label
                }
                activityLevel.adapter = adapter
                activityLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View,
                        position: Int,
                        id: Long
                    ) {
                        val selectedItem = parent.getItemAtPosition(position) as ActivityLevel
                        viewModel.updateActivityLevel(selectedItem)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // Another interface callback
                    }
                }
            }
            activityLevel.setSelection(items.indexOf(activityLevelValue))
        }
    }

    private fun setupCalorieDeficitView(
        measurementUnit: MeasurementUnit,
        calorieDeficitValue: CalorieDeficit
    ) {
        with(binding)
        {
            val items = viewModel.calorieDeficits
            if (calorieDeficit.adapter == null || calorieDeficit.adapter.count == 0) {
                val adapter = GenericSpinnerAdapter(
                    context = requireContext(),
                    items = items
                ) { item ->
                    if (item == CalorieDeficit.Maintain) {
                        item.lblImperial
                    } else if (measurementUnit.weightUnit == WeightUnit.Imperial) {
                        "${item.lblImperial} ${measurementUnit.weightUnit.value} / Week"
                    } else {
                        "${item.lblMetric} ${measurementUnit.weightUnit.value} / Week"
                    }
                }
                calorieDeficit.adapter = adapter
                calorieDeficit.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View,
                            position: Int,
                            id: Long
                        ) {
                            val selectedItem = parent.getItemAtPosition(position) as CalorieDeficit
                            viewModel.updateCalorieDeficit(selectedItem)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {
                            // Another interface callback
                        }
                    }
            }
            calorieDeficit.setSelection(items.indexOf(calorieDeficitValue))
        }
    }

    private fun setupDietView(
        passioMealPlanValue: PassioMealPlan?
    ) {
        with(binding)
        {
            val items = viewModel.passioMealPlans
            if (mealPlan.adapter == null || mealPlan.adapter.count == 0) {
                val adapter = GenericSpinnerAdapter(
                    context = requireContext(),
                    items = items
                ) { item ->
                    item.mealPlanTitle
                }
                mealPlan.adapter = adapter
                mealPlan.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View,
                            position: Int,
                            id: Long
                        ) {
                            val selectedItem = parent.getItemAtPosition(position) as PassioMealPlan
                            viewModel.updateMealPlan(selectedItem)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {
                            // Another interface callback
                        }
                    }
            }
            passioMealPlanValue?.let {
                mealPlan.setSelection(items.indexOf(passioMealPlanValue))
            } ?: mealPlan.setSelection(0)

        }
    }

    private val heightPickerListener = object : HeightPickerDialog.HeightPickerListener {
        override fun onHeightPicked(height: Double) {
            viewModel.updateHeight(height)
        }

    }

    private val dailyNutritionTargetCustomizeListener =
        object : DailyNutritionTargetDialog.DailyNutritionTargetCustomizeListener {
            override fun onCustomized(dailyNutritionTarget: DailyNutritionTarget) {
                viewModel.changeDailyNutritionTarget(dailyNutritionTarget)
            }

        }

    private fun showDailyNutritionTargetPicker(dailyNutritionTarget: DailyNutritionTarget) {
        DailyNutritionTargetDialog(
            dailyNutritionTarget,
            dailyNutritionTargetCustomizeListener
        ).show(
            childFragmentManager,
            "MyProfileFragment"
        )
    }

    private fun showHeightPicker() {
        HeightPickerDialog(heightPickerListener).show(childFragmentManager, "MyProfileFragment")
    }
}