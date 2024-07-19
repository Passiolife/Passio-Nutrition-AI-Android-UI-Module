package ai.passio.nutrition.uimodule.ui.weight

import ai.passio.nutrition.uimodule.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.databinding.FragmentWeightTrackingBinding
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.model.WeightRecord
import ai.passio.nutrition.uimodule.ui.progress.TimePeriod
import ai.passio.nutrition.uimodule.ui.progress.WeekMonthPicker
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import ai.passio.nutrition.uimodule.ui.util.getMonthName
import ai.passio.nutrition.uimodule.ui.util.getWeekDuration
import ai.passio.nutrition.uimodule.ui.util.isPartOfCurrentMonth
import ai.passio.nutrition.uimodule.ui.util.isPartOfCurrentWeek
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.yanzhenjie.recyclerview.SwipeMenuItem
import org.joda.time.DateTime
import java.util.Date

class WeightTrackingFragment : BaseFragment<WeightTrackingViewModel>() {

    private var _binding: FragmentWeightTrackingBinding? = null
    private val binding: FragmentWeightTrackingBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeightTrackingBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        with(binding)
        {
            toolbar.setup(getString(R.string.weight_tracking), baseToolbarListener)
            toolbar.setRightIcon(R.drawable.ic_add_food)
            weekMonthPicker.setup(TimePeriod.WEEK, timePeriodListener)
            movePrevious.setOnClickListener {
                viewModel.setPrevious()
            }
            moveNext.setOnClickListener {
                viewModel.setNext()
            }
        }
        arguments?.let {
            if (it.containsKey("currentDate")) {
                val currentDate = it.getLong("currentDate", 0)
                if (currentDate > 0) {
                    viewModel.setPrefilledDate(Date(currentDate))
                }
            }
        }
        viewModel.fetchRecords()

    }

    private fun initObserver() {
        viewModel.weightRecords.observe(viewLifecycleOwner, ::updateRecords)
        viewModel.timePeriod.observe(viewLifecycleOwner, ::updateTimePeriod)
    }

    private fun updateRecords(weightRecords: List<WeightRecord>) {
        with(binding)
        {
            val list = arrayListOf<WeightRecord>()
            list.addAll(weightRecords)
            val adapter = WeightAdapter(list) { weightRecord ->
                sharedViewModel.addEditWeight(weightRecord.copy())
                viewModel.navigateToWeightSave()
            }
            wightList.adapter = null
            viewWeightContainer.isVisible = list.isNotEmpty()
            wightList.setSwipeMenuCreator { leftMenu, rightMenu, position ->
                val editItem = SwipeMenuItem(requireContext()).apply {
                    text = getString(R.string.edit)
                    setTextColor(Color.WHITE)
                    setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.passio_primary
                        )
                    )
                    width = DesignUtils.dp2px(80f)
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                }
                rightMenu.addMenuItem(editItem)
                val deleteItem = SwipeMenuItem(requireContext()).apply {
                    text = getString(R.string.delete)
                    setTextColor(Color.WHITE)
                    setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.passio_red500
                        )
                    )
                    width = DesignUtils.dp2px(80f)
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                }
                rightMenu.addMenuItem(deleteItem)
            }
            wightList.setOnItemMenuClickListener { menuBridge, adapterPosition ->
                menuBridge.closeMenu()
                val weightRecord = adapter.getItem(adapterPosition).copy()
                when (menuBridge.position) {
                    0 -> {
                        sharedViewModel.addEditWeight(weightRecord)
                        viewModel.navigateToWeightSave()
                    }

                    1 -> {
                        viewModel.removeWeightRecord(weightRecord)
                    }
                }
            }

            wightList.adapter = adapter
        }
    }

    private val timePeriodListener = object : WeekMonthPicker.TimePeriodListener {
        override fun onValueChanged(timePeriod: TimePeriod) {
            viewModel.updateTimePeriod(timePeriod)
        }
    }

    private fun updateTimePeriod(timePeriod: TimePeriod) {

        val currentTime = DateTime(viewModel.getCurrentDate().time)
        if (timePeriod == TimePeriod.WEEK) {
            if (isPartOfCurrentWeek(currentTime)) {
                binding.timeTitle.text = requireContext().getString(R.string.this_week)
            } else {
                binding.timeTitle.text = getWeekDuration(currentTime)
            }
        } else if (timePeriod == TimePeriod.MONTH) {
            if (isPartOfCurrentMonth(currentTime)) {
                binding.timeTitle.text = requireContext().getString(R.string.this_month)
            } else {
                binding.timeTitle.text = getMonthName(currentTime)
            }
        }
        binding.tvTimeDuration.text = binding.timeTitle.text

    }

    private val baseToolbarListener = object : BaseToolbar.ToolbarListener {
        override fun onBack() {
            viewModel.navigateBack()
        }

        override fun onRightIconClicked() {
            viewModel.navigateToWeightSave()
        }

    }

}