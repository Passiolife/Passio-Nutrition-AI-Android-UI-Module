package ai.passio.nutrition.uimodule.ui.image

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.databinding.FragmentImageFoodResultBinding
import ai.passio.nutrition.uimodule.ui.activity.UserCache
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
import ai.passio.nutrition.uimodule.ui.model.ImageFoodResult
import ai.passio.nutrition.uimodule.ui.profile.GenericSpinnerAdapter
import ai.passio.nutrition.uimodule.ui.util.CustomFoodCreatedInfoDialog
import ai.passio.nutrition.uimodule.ui.util.DAY_FORMAT_FULL_WITH_TIME
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.ViewEXT.disable
import ai.passio.nutrition.uimodule.ui.util.ViewEXT.enable
import ai.passio.nutrition.uimodule.ui.util.dateToFormat
import ai.passio.nutrition.uimodule.ui.util.isToday
import ai.passio.nutrition.uimodule.ui.util.showDateTimePickerDialog
import ai.passio.nutrition.uimodule.ui.util.toast
import ai.passio.passiosdk.passiofood.PassioMealTime
import ai.passio.passiosdk.passiofood.data.measurement.UnitEnergy
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import android.annotation.SuppressLint
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.view.isVisible
import org.joda.time.DateTime

internal class ImageFoodResultFragment : BaseFragment<ImageFoodResultViewModel>() {

    private var _binding: FragmentImageFoodResultBinding? = null
    private val binding: FragmentImageFoodResultBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageFoodResultBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        with(binding)
        {
            toolbar.setup(getString(R.string.your_result), baseToolbarListener)
            toolbar.hideRightIcon()
            dailyNutrition.hideTitleAndProgressReportButton()
            enableLogButton(false)
            rvResult.adapter = FoodImageResultAdapter(object : OnFoodImageSelectChange {
                override fun onItemSelectChange(selectedCount: Int) {
//                    enableLogButton(selectedCount != 0)
//                    updateNutritionChart()
                    viewModel.updateItemSelection()
                }

                override fun onTapped(editIndex: Int, imageFoodResult: ImageFoodResult) {
                    if (imageFoodResult.isBarcodeDataMissing() || imageFoodResult.isNutritionFactsDataMissing()) {
                        showEditNutritionFactsDialog(editIndex, imageFoodResult)
                    } else {
                        showAdjustServingSizeDialog(editIndex, imageFoodResult)
                    }
                }

            })
            updateNutritionChart(emptyList())
            cancelFetch.setOnClickListener {
                viewModel.navigateBack()
            }
            dateValue.setOnClickListener {
                showDateTimePickerDialog(
                    requireContext(),
                    now = viewModel.getDateTime(),
                    onDateSelected = { selectedDateTime ->
                        viewModel.setDateTime(selectedDateTime)
                    })
            }
            /*createRecipe.setOnClickListener {
                viewModel.createRecipe()
            }*/
            tryAgain.setOnClickListener {
                viewModel.navigateBack()
            }
            reselect.setOnClickListener {
                viewModel.navigateBack()
            }

            search.setOnClickListener {
                sharedViewModel.setIsAddIngredientFromSearch(viewModel.getIsAddIngredient())
                viewModel.navigateToSearch()
            }
            log.setOnClickListener {
//                viewModel.logRecords((rvResult.adapter as FoodImageResultAdapter).getSelectedItems())
                viewModel.logRecords()
            }
            viewDiary.setOnClickListener {
                sharedViewModel.setDiaryDate(viewModel.getDateTime().toDate())
                viewModel.navigateToDiary()
            }
            addMore.setOnClickListener {
                viewModel.navigateToTakeOrSelectPhoto()
            }
        }

    }


    private val onAdjustServingSizeListener = object : OnAdjustServingSizeListener {
        override fun onChanged(editedIndex: Int, updatedImageFoodResult: ImageFoodResult) {
            viewModel.updateFoodRecord(editedIndex, updatedImageFoodResult)
        }

        override fun onCancelled(editedIndex: Int) {

        }

        override fun onEdit(editedIndex: Int, updatedImageFoodResult: ImageFoodResult) {
            showEditNutritionFactsDialog(editedIndex, updatedImageFoodResult)
        }
    }

    private fun showCustomFoodCreatedInfo() {
        CustomFoodCreatedInfoDialog(requireContext()).show()
    }

    private val onEditNutritionFactsListener = object : OnEditNutritionFactsListener {
        override fun onChanged(
            editedIndex: Int,
            updatedImageFoodResult: ImageFoodResult,
            isNewCreated: Boolean
        ) {
            if (isNewCreated) {
                showCustomFoodCreatedInfo()
            }
            viewModel.updateFoodRecord(editedIndex, updatedImageFoodResult)
        }

        override fun onCancelled(
            editedIndex: Int,
            imageFoodResult: ImageFoodResult
        ) {
            if (!imageFoodResult.isBarcodeDataMissing() && !imageFoodResult.isNutritionFactsDataMissing()) {
                showAdjustServingSizeDialog(editedIndex, imageFoodResult)
            }

        }

    }

    private fun showAdjustServingSizeDialog(indexToEdit: Int, imageFoodResult: ImageFoodResult) {
        AdjustServingSizeDialog(
            editIndex = indexToEdit,
            imageFoodRecordResult = imageFoodResult,
            onAdjustServingSizeListener = onAdjustServingSizeListener
        ).show(
            childFragmentManager,
            "AdjustServingSizeDialog"
        )
    }

    private fun showEditNutritionFactsDialog(indexToEdit: Int, imageFoodResult: ImageFoodResult) {
        EditNutritionFactsDialog(
            editIndex = indexToEdit,
            imageFoodRecordResult = imageFoodResult,
            onEditNutritionFactsListener = onEditNutritionFactsListener
        ).show(
            childFragmentManager,
            "EditNutritionFactsDialog"
        )
    }


    private val baseToolbarListener = object : BaseToolbar.ToolbarListener {
        override fun onBack() {
            viewModel.navigateBack()
        }

        override fun onRightIconClicked() {

        }

    }

    private fun enableLogButton(isEnable: Boolean) {
        with(binding)
        {
            if (isEnable) {
                log.enable()
            } else {
                log.disable()
            }
        }
    }

    private fun hideAll() {
        with(binding)
        {
            viewLoading.isVisible = false
            resultView.isVisible = false
            noResultFound.isVisible = false
            viewAddedToDiary.isVisible = false
        }
    }

    private fun initObserver() {
        setIngredientMode(viewModel.getIsAddIngredient())
        sharedViewModel.isAddIngredientFromVoiceLD.observe(viewLifecycleOwner) { isAddIngredient ->
            viewModel.setIsAddIngredient(isAddIngredient)
            setIngredientMode(isAddIngredient)

        }
        sharedViewModel.photoFoodResultLD.observe(viewLifecycleOwner) {
            viewModel.setImageBitmaps(it)
        }

        viewModel.isFetchingResult.observe(viewLifecycleOwner) {
            if (it) {
                hideAll()
                binding.viewLoading.isVisible = true
                binding.progress.startProgress()
            } else {
                binding.progress.stopProgress()
                binding.viewLoading.isVisible = false
            }
        }
        viewModel.showLoading.observe(viewLifecycleOwner) {
            binding.loading.isVisible = it
        }
        viewModel.resultFoodInfoEvent.observe(viewLifecycleOwner, ::showResult)
        viewModel.logFoodEvent.observe(viewLifecycleOwner, ::foodItemLogged)
        viewModel.createRecipeEvent.observe(viewLifecycleOwner, ::createNewRecipe)
        viewModel.addIngredientEvent.observe(viewLifecycleOwner, ::addIngredients)
        viewModel.currentMealTimeEvent.observe(viewLifecycleOwner, ::setupMeals)
        viewModel.selectedDateTimeEvent.observe(viewLifecycleOwner, ::setDateTime)
    }


    private fun setDateTime(selectedDateTime: DateTime) {
        with(binding)
        {
            if (isToday(selectedDateTime.millis)) {
                dateValue.text = requireContext().getString(R.string.today)
            } else {
                dateValue.text = dateToFormat(
                    selectedDateTime,
                    DAY_FORMAT_FULL_WITH_TIME
                )
            }
        }
    }

    private fun setupMeals(currentMealTime: PassioMealTime) {
        with(binding)
        {
            val items = listOf(
                PassioMealTime.BREAKFAST,
                PassioMealTime.LUNCH,
                PassioMealTime.SNACK,
                PassioMealTime.DINNER
            )
            if (mealTimeSpinner.adapter == null || mealTimeSpinner.adapter.count == 0) {
                val adapter = GenericSpinnerAdapter(
                    context = requireContext(),
                    items = items
                ) { item ->
                    item.mealName.capitalized()
                }
                mealTimeSpinner.adapter = adapter
                mealTimeSpinner.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (parent == null) return
                        val selectedItem = parent.getItemAtPosition(position) as PassioMealTime
                        viewModel.setMealTime(selectedItem)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Another interface callback
                    }
                }
            }

            mealTimeSpinner.setSelection(items.indexOf(currentMealTime))
        }
    }


    private fun addIngredients(foodRecords: List<FoodRecordIngredient>) {
        sharedViewModel.addFoodIngredients(foodRecords)
        viewModel.navigateBackToRecipe()
    }

    private fun setIngredientMode(isOn: Boolean) {
        if (isOn) {
            binding.log.text = getString(R.string.add_ingredient)
//            binding.createRecipe.isVisible = false
        }
    }

    private fun createNewRecipe(foodRecord: FoodRecord) {
        sharedViewModel.editRecipe(foodRecord)
        viewModel.navigateToRecipe()
    }

    @SuppressLint("SetTextI18n")
    private fun foodItemLogged(resultWrapper: ResultWrapper<Triple<Boolean, Int, Int>>) {
        when (resultWrapper) {
            is ResultWrapper.Success -> {
                val isLogged = resultWrapper.value.first
                val totalLoggedCount = resultWrapper.value.second
                val totalCustomFoodSaved = resultWrapper.value.third
                if (isLogged) {
                    with(binding) {
                        viewAddedToDiary.isVisible = true
                        tvCustomFoodCount.isVisible = totalCustomFoodSaved != 0
                        tvCustomFoodCount.text =
                            "$totalCustomFoodSaved ${resources.getString(R.string.custom_food_created)}"
                        tvLoggedCount.text =
                            "$totalLoggedCount ${resources.getString(R.string.item_added_to_diary)}"
                    }

                } else {
                    requireContext().toast("Could not log food item(s).")
                }
            }

            is ResultWrapper.Error -> {
                requireContext().toast(resultWrapper.error)
            }
        }
    }

    private fun updateNutritionChart(records: List<FoodRecord>) {
        with(binding)
        {
//            val adapter = rvResult.adapter as FoodImageResultAdapter
//            val records = adapter.getSelectedItems().map { it.record }
            val currentCalories = records.map { it.nutrients().calories() }
                .fold(UnitEnergy()) { acc, unitEnergy -> acc + unitEnergy }.kcalValue()
            val currentCarbs = records.map { it.nutrients().carbs() }
                .fold(UnitMass()) { acc, unitMass -> acc + unitMass }.gramsValue()
            val currentProtein = records.map { it.nutrients().protein() }
                .fold(UnitMass()) { acc, unitMass -> acc + unitMass }.gramsValue()
            val currentFat = records.map { it.nutrients().fat() }
                .fold(UnitMass()) { acc, unitMass -> acc + unitMass }.gramsValue()

            val userProfile = UserCache.getProfile()
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

    private fun showResult(result: List<ImageFoodResult>) {
        with(binding) {
            hideAll()
            noResultFound.isVisible = result.isEmpty()
            resultView.isVisible = result.isNotEmpty()


            val adapter = rvResult.adapter as FoodImageResultAdapter
//            val selectionList = mutableListOf<Int>()
//            for (i in result.indices) {
//                if (!result[i].isBarcodeDataMissing() && !result[i].isNutritionFactsDataMissing()) {
//                    selectionList.add(i)
//                }
//            }

            adapter.addData(result)
//            adapter.addData(result, selectionList)

            val selectedRecords = result.filter { it.isSelected }.map { it.record }
            enableLogButton(selectedRecords.isNotEmpty())
            updateNutritionChart(selectedRecords)
        }
    }


}