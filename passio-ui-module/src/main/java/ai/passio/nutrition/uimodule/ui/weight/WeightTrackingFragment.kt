package ai.passio.nutrition.uimodule.ui.weight

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.data.ResultWrapper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.databinding.FragmentWeightTrackingBinding
import ai.passio.nutrition.uimodule.ui.activity.UserCache
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.model.WeightRecord
import ai.passio.nutrition.uimodule.ui.progress.TimePeriod
import ai.passio.nutrition.uimodule.ui.progress.WeekMonthPicker
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import ai.passio.nutrition.uimodule.ui.util.StringKT.setDrawableEnd
import ai.passio.nutrition.uimodule.ui.util.getEndOfMonth
import ai.passio.nutrition.uimodule.ui.util.getEndOfWeek
import ai.passio.nutrition.uimodule.ui.util.getMonthName
import ai.passio.nutrition.uimodule.ui.util.getStartOfMonth
import ai.passio.nutrition.uimodule.ui.util.getStartOfWeek
import ai.passio.nutrition.uimodule.ui.util.getWeekDuration
import ai.passio.nutrition.uimodule.ui.util.isPartOfCurrentMonth
import ai.passio.nutrition.uimodule.ui.util.isPartOfCurrentWeek
import ai.passio.nutrition.uimodule.ui.util.toast
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.yanzhenjie.recyclerview.SwipeMenuItem
import org.joda.time.DateTime
import org.joda.time.Days
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

//            weekMonthPicker.setup(TimePeriod.WEEK, timePeriodListener)
            movePrevious.setOnClickListener {
                viewModel.setPrevious()
            }
            moveNext.setOnClickListener {
                viewModel.setNext()
            }
            tvTimeDuration.setOnClickListener {
                wightList.isVisible = !wightList.isVisible
                if (wightList.isVisible) {
                    tvTimeDuration.setDrawableEnd(R.drawable.ic_arrow_up)
                } else {
                    tvTimeDuration.setDrawableEnd(R.drawable.ic_arrow_down)
                }
            }
            setChartSettings()
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
        viewModel.removeRecord.observe(viewLifecycleOwner, ::recordRemoved)
    }

    private fun recordRemoved(resultWrapper: ResultWrapper<Boolean>) {
        when (resultWrapper) {
            is ResultWrapper.Success -> {
                if (resultWrapper.value) {
                    requireContext().toast("Weight record removed!")
                } else {
                    requireContext().toast("Could not remove weight. Please try again.")
                }
            }

            is ResultWrapper.Error -> {
                requireContext().toast(resultWrapper.error)
            }
        }
    }

    private fun updateRecords(result: Pair<List<WeightRecord>, TimePeriod>) {
        with(binding)
        {
            val timePeriod = result.second
            updateTimePeriod(timePeriod)
            val weightRecords = result.first
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

            setUpChart(
                list,
                UserCache.getProfile().getTargetWightInCurrentUnit(),
                timePeriod
            )
        }
    }

    private fun setChartSettings() {
        with(binding)
        {
            lineChart.apply {
                setPinchZoom(false)
                legend.isEnabled = false
                setTouchEnabled(false)
            }
        }
    }

    private fun createDefaultWeightRecords(
        startDate: DateTime,
        endDate: DateTime
    ): MutableList<WeightRecord> {
        val weightRecords = mutableListOf<WeightRecord>()
        val daysBetween = Days.daysBetween(startDate, endDate).days

        for (i in 0..daysBetween) {
            val dateTime = startDate.plusDays(i)
            val temp = WeightRecord()
            temp.weight = 0.0
            temp.dateTime = dateTime.millis
            weightRecords.add(temp)
        }

        return weightRecords
    }

    private fun setUpChart(
        weightRecordsNew: List<WeightRecord>,
        targetWeight: Double,
        timePeriod: TimePeriod
    ) {
        val weightRecords = arrayListOf<WeightRecord>()
        weightRecords.addAll(weightRecordsNew)
        with(binding) {

            val startDate: DateTime
            val endDate: DateTime
            if (timePeriod == TimePeriod.WEEK) {
                startDate = getStartOfWeek(DateTime(viewModel.getCurrentDate().time))
                endDate = getEndOfWeek(DateTime(viewModel.getCurrentDate().time))
            } else {
                startDate = getStartOfMonth(DateTime(viewModel.getCurrentDate().time))
                endDate = getEndOfMonth(DateTime(viewModel.getCurrentDate().time))
            }
            weightRecords.addAll(createDefaultWeightRecords(startDate, endDate))

            val groupedRecords = weightRecords.groupBy {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = it.dateTime
                calendar.get(Calendar.DAY_OF_YEAR)
            }

            val weightEntries = ArrayList<Entry>()
            val weightEntriesWithDots = ArrayList<Entry>()
            val targetEntries = ArrayList<Entry>()
            val calendar = Calendar.getInstance()
            val dateFormat =
                if (timePeriod == TimePeriod.MONTH) {
                    SimpleDateFormat("MMM d", Locale.getDefault())
                } else {
                    SimpleDateFormat("MMM d", Locale.getDefault())
                }

            groupedRecords.toSortedMap().forEach { (dayOfYear, records) ->
                val avgWeight = records.sumOf { it.getWightInCurrentUnit() }.toFloat()
                weightEntries.add(Entry(dayOfYear.toFloat(), avgWeight))
                if (avgWeight != 0f) {
                    weightEntriesWithDots.add(Entry(dayOfYear.toFloat(), avgWeight))
                }
                targetEntries.add(Entry(dayOfYear.toFloat(), targetWeight.toFloat()))
            }


            val weightDataSet = LineDataSet(weightEntries, getString(R.string.weight_txt)).apply {
                color = Color.TRANSPARENT//ContextCompat.getColor(requireContext(), R.color.passio_primary)
                setCircleColor(ContextCompat.getColor(requireContext(), R.color.passio_primary))
                lineWidth = 0f
                circleRadius = 0f
                setDrawCircles(false)
                setDrawCircleHole(false)
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER // Set mode to cubic bezier for smooth curves
            }

            val weightDataSetWithDots =
                LineDataSet(weightEntriesWithDots, getString(R.string.weight_txt)).apply {
                    color = ContextCompat.getColor(requireContext(), R.color.passio_primary)
                    setCircleColor(ContextCompat.getColor(requireContext(), R.color.passio_primary))
                    lineWidth = 2f
                    circleRadius = 4f
                    setDrawCircleHole(false)
                    setDrawValues(false)
                    mode =
                        LineDataSet.Mode.HORIZONTAL_BEZIER // Set mode to cubic bezier for smooth curves
                }

            val targetDataSet =
                LineDataSet(targetEntries, getString(R.string.target_weight)).apply {
                    color = ContextCompat.getColor(requireContext(), R.color.passio_green_800)
                    setDrawCircles(false)
                    lineWidth = 2f
                    enableDashedLine(20f, 14f, 0f)
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                }

            val lineData = if (targetWeight <= 0) {
                LineData(weightDataSet, weightDataSetWithDots)
            } else {
                LineData(weightDataSet, weightDataSetWithDots, targetDataSet)
            }
            lineChart.data = lineData

            val xAxis = lineChart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(false)
            xAxis.setDrawGridLinesBehindData(false)
            xAxis.setDrawLimitLinesBehindData(false)
            if (timePeriod == TimePeriod.MONTH) {
                xAxis.labelCount = 4
//                xAxis.mAxisMaximum = DateTime().dayOfMonth().maximumValue.toFloat() + 1
                xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        calendar.set(Calendar.DAY_OF_YEAR, value.toInt())
                        return dateFormat.format(calendar.time)
                    }
                }
            } else {
                xAxis.labelCount = 7
//                xAxis.mAxisMaximum = 7f
                xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return startDate.plusDays(value.toInt() - 1).dayOfWeek()
                            .getAsText(Locale.getDefault())
                            .substring(0, 2)
                    }
                }

            }


            val yAxisRight = lineChart.axisRight
            yAxisRight.isEnabled = false

            val yAxisLeft = lineChart.axisLeft
            yAxisLeft.granularity = 1f
//            yAxisLeft.axisMinimum = 5f

            lineChart.description.isEnabled = false
            lineChart.legend.form = Legend.LegendForm.SQUARE
            lineChart.invalidate() // Refresh the chart
        }
    }

    private val timePeriodListener = object : WeekMonthPicker.TimePeriodListener {
        override fun onValueChanged(timePeriod: TimePeriod) {
            viewModel.updateTimePeriod(timePeriod)
        }
    }

    private fun updateTimePeriod(timePeriod: TimePeriod) {

        binding.weekMonthPicker.setup(timePeriod, timePeriodListener)
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