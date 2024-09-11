package ai.passio.nutrition.uimodule.ui.mealplan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.databinding.FragmentMealPlanBinding
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.MealLabel
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.toast
import ai.passio.nutrition.uimodule.ui.view.HorizontalSpaceItemDecoration
import ai.passio.passiosdk.passiofood.PassioMealTime
import ai.passio.passiosdk.passiofood.data.model.PassioMealPlanItem
import android.util.Log
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible

class MealPlanFragment : BaseFragment<MealPlanViewModel>() {

    private var _binding: FragmentMealPlanBinding? = null
    private val binding: FragmentMealPlanBinding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMealPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding)
        {
            toolbar.setup(getString(R.string.meal_plan), baseToolbarListener)

            breakfastCategory.setup(MealLabel.Breakfast, mealPlanCategoryListener)
            lunchCategory.setup(MealLabel.Lunch, mealPlanCategoryListener)
            dinnerCategory.setup(MealLabel.Dinner, mealPlanCategoryListener)
            snackCategory.setup(MealLabel.Snack, mealPlanCategoryListener)

            retry.setOnClickListener {
                viewModel.getMealPlans()
            }
        }

        initObserver()
//        viewModel.getMealPlans()

    }

    private val baseToolbarListener = object : BaseToolbar.ToolbarListener {
        override fun onBack() {
            viewModel.navigateBack()
        }

        override fun onRightIconClicked() {
            showPopupMenu(binding.toolbar.findViewById(R.id.toolbarMenu))
        }

    }

    private val mealPlanCategoryListener = object : MealPlanCategory.MealPlanCategoryListener {
        override fun onLogAdd(foodRecord: PassioMealPlanItem) {
            viewModel.logFood(foodRecord)
        }

        override fun onLogAdd(foodRecords: List<PassioMealPlanItem>) {
            viewModel.logFood(foodRecords)
        }

        override fun onLogEdit(foodRecord: PassioMealPlanItem) {
            viewModel.editFood(foodRecord)
        }

    }

    private fun initObserver() {
        viewModel.passioMealPlanItems.observe(viewLifecycleOwner, ::showMealPlans)
        viewModel.logFoodEvent.observe(viewLifecycleOwner, ::foodItemLogged)
        viewModel.editFoodEvent.observe(viewLifecycleOwner, ::editFoodRecord)
        viewModel.showLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.viewLoader.isVisible = isLoading
        }
    }

    private fun editFoodRecord(foodRecord: FoodRecord) {
        sharedViewModel.detailsFoodRecord(foodRecord)
        viewModel.navigateToEdit()
    }

    private fun foodItemLogged(resultWrapper: ResultWrapper<Boolean>) {
        when (resultWrapper) {
            is ResultWrapper.Success -> {
                if (resultWrapper.value) {
                    requireContext().toast("Food item(s) logged.")
                } else {
                    requireContext().toast("Could not log food item(s).")
                }
            }

            is ResultWrapper.Error -> {
                requireContext().toast(resultWrapper.error)
            }
        }
    }


    private fun showMealMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        val menuItems = viewModel.passioMealPlans

        // Dynamically add menu items
        menuItems.forEachIndexed { index, item ->
            popupMenu.menu.add(0, index, index, item.mealPlanTitle.capitalized())
        }

        // Set a click listener for menu item clicks
        popupMenu.setOnMenuItemClickListener { menuItem ->
            viewModel.passioMealPlans.find {
                it.mealPlanTitle.lowercase().equals(
                    menuItem.title.toString().lowercase(),
                    true
                )
            }?.let { mealPlan ->
                viewModel.updateMealPlan(mealPlan)
                binding.tvCurrentMealPlan.text = mealPlan.mealPlanTitle
            }
            true
        }

        // Show the popup menu
        popupMenu.show()
    }


    private fun showMealPlans(mealPlanItems: List<PassioMealPlanItem>) {

        Log.d("showMealPlans", "showMealPlans")
        val breakfast = mealPlanItems.filter { it.mealTime == PassioMealTime.BREAKFAST }
        val lunch = mealPlanItems.filter { it.mealTime == PassioMealTime.LUNCH }
        val dinner = mealPlanItems.filter { it.mealTime == PassioMealTime.DINNER }
        val snack = mealPlanItems.filter { it.mealTime == PassioMealTime.SNACK }

        with(binding)
        {

            menu.setOnClickListener {
                showMealMenu(menu)
            }
            viewModel.selectedMealPlan?.let { mealPlan ->
                tvCurrentMealPlan.text = mealPlan.mealPlanTitle.capitalized()
            }

            if (rvDays.adapter == null || rvDays.adapter?.itemCount == 0) {
                rvDays.addItemDecoration(
                    HorizontalSpaceItemDecoration(
                        DesignUtils.dp2px(16f),
                        DesignUtils.dp2px(8f)
                    )
                )
                rvDays.adapter =
                    DaysAdapter(viewModel.currentDayNumber, (1..14).toList()) { selectedDay ->
                        viewModel.setCurrentDay(selectedDay)
                    }
            }

            retry.isVisible = mealPlanItems.isEmpty()
            breakfastCategory.update(breakfast)
            lunchCategory.update(lunch)
            dinnerCategory.update(dinner)
            snackCategory.update(snack)

        }
    }
}