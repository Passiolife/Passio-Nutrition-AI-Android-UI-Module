package ai.passio.nutrition.uimodule.ui.water

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.databinding.FragmentWaterTrackingBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.ui.activity.UserCache
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.model.WaterRecord
import ai.passio.nutrition.uimodule.ui.profile.WaterUnit
import ai.passio.nutrition.uimodule.ui.profile.ozToMl
import ai.passio.nutrition.uimodule.ui.progress.TimePeriod
import ai.passio.nutrition.uimodule.ui.progress.WeekMonthPicker
import ai.passio.nutrition.uimodule.ui.util.DAY_FORMAT
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import ai.passio.nutrition.uimodule.ui.util.StringKT.setDrawableEnd
import ai.passio.nutrition.uimodule.ui.util.StringKT.setSpannableBold
import ai.passio.nutrition.uimodule.ui.util.StringKT.singleDecimal
import ai.passio.nutrition.uimodule.ui.util.dateToFormat
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
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.yanzhenjie.recyclerview.SwipeMenuItem
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.LocalDate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WaterTrackingFragment : BaseFragment<WaterTrackingViewModel>() {

    private var _binding: FragmentWaterTrackingBinding? = null
    private val binding: FragmentWaterTrackingBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWaterTrackingBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        with(binding)
        {
            toolbar.setup(getString(R.string.water_tracking), baseToolbarListener)
            toolbar.setRightIcon(R.drawable.ic_add_food)
            lblChart.text = getString(R.string.water_trend)
            weekMonthPicker.setup(TimePeriod.WEEK, timePeriodListener)
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
            setupQuickAdd()
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

    private fun setupQuickAdd() {
        val waterUnit = UserCache.getProfile().measurementUnit.waterUnit
        with(binding)
        {
            val glass: Double
            val bottleSmall: Double
            val bottleLarge: Double

            if (waterUnit == WaterUnit.Imperial) {
                glass = WaterRecord.QUICK_ADD_GLASS
                bottleSmall = WaterRecord.QUICK_ADD_BOTTLE_SMALL
                bottleLarge = WaterRecord.QUICK_ADD_BOTTLE_LARGE
            } else {
                glass = ozToMl(WaterRecord.QUICK_ADD_GLASS)
                bottleSmall = ozToMl(WaterRecord.QUICK_ADD_BOTTLE_SMALL)
                bottleLarge = ozToMl(WaterRecord.QUICK_ADD_BOTTLE_LARGE)
            }

            val glassStr =
                getString(R.string.glass) + " (${glass.singleDecimal()} ${waterUnit.value})"
            val bottleSmallStr =
                getString(R.string.sm_bottle) + " (${bottleSmall.singleDecimal()} ${waterUnit.value})"
            val bottleLargeStr =
                getString(R.string.lg_bottle) + " (${bottleLarge.singleDecimal()} ${waterUnit.value})"
            glassWater.text = glassStr.setSpannableBold(getString(R.string.glass))
            glassBottleSmall.text = bottleSmallStr.setSpannableBold(getString(R.string.sm_bottle))
            glassBottleLarge.text = bottleLargeStr.setSpannableBold(getString(R.string.lg_bottle))

            glassWater.setOnClickListener {
                viewModel.quickAdd(glass)
            }
            glassBottleSmall.setOnClickListener {
                viewModel.quickAdd(bottleSmall)
            }
            glassBottleLarge.setOnClickListener {
                viewModel.quickAdd(bottleLarge)
            }
        }
    }


    private fun initObserver() {
        viewModel.weightRecords.observe(viewLifecycleOwner, ::updateRecords)
        viewModel.timePeriod.observe(viewLifecycleOwner, ::updateTimePeriod)
        viewModel.saveRecord.observe(viewLifecycleOwner, ::recordSaved)
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

    private fun recordSaved(resultWrapper: ResultWrapper<Boolean>) {
        when (resultWrapper) {
            is ResultWrapper.Success -> {
                if (resultWrapper.value) {
                    requireContext().toast("Water record saved!")
                } else {
                    requireContext().toast("Could not record weight. Please try again.")
                }
            }

            is ResultWrapper.Error -> {
                requireContext().toast(resultWrapper.error)
            }
        }
    }

    private fun updateRecords(result: Pair<List<WaterRecord>, TimePeriod>) {
        with(binding)
        {
            val timePeriod = result.second
            updateTimePeriod(timePeriod)
            val weightRecords = result.first
            val list = arrayListOf<WaterRecord>()
            list.addAll(weightRecords)
            val adapter = WaterAdapter(list) { weightRecord ->
                sharedViewModel.addEditWater(weightRecord.copy())
                viewModel.navigateToSave()
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
                val waterRecord = adapter.getItem(adapterPosition).copy()
                when (menuBridge.position) {
                    0 -> {
                        sharedViewModel.addEditWater(waterRecord)
                        viewModel.navigateToSave()
                    }

                    1 -> {
                        viewModel.removeWeightRecord(waterRecord)
                    }
                }
            }

            wightList.adapter = adapter

            setUpChart(
                list,
                UserCache.getProfile().getTargetWaterInCurrentUnit(),
                timePeriod
            )
        }
    }

    private fun setChartSettings() {
        with(binding)
        {
            barChart.apply {

                setDrawValueAboveBar(false)
                setDrawValueAboveBar(false)
                setFitBars(false)
                description.isEnabled = false
                setPinchZoom(false)
                legend.isEnabled = false
                setTouchEnabled(false)

//                setPinchZoom(false)
//                legend.isEnabled = false
//                setTouchEnabled(false)
            }
        }
    }

    private fun createDefaultWaterRecords(
        startDate: DateTime,
        endDate: DateTime
    ): MutableList<WaterRecord> {
        val weightRecords = mutableListOf<WaterRecord>()
        val daysBetween = Days.daysBetween(startDate, endDate).days

        for (i in 0..daysBetween) {
            val dateTime = startDate.plusDays(i)
            val temp = WaterRecord()
            temp.weight = 0.0
            temp.dateTime = dateTime.millis
            weightRecords.add(temp)
        }

        return weightRecords
    }

    private fun setUpChart(
        weightRecordsNew: List<WaterRecord>,
        targetWater: Double,
        timePeriod: TimePeriod
    ) {
        with(binding) {
            val waterRecords = arrayListOf<WaterRecord>()
            waterRecords.addAll(weightRecordsNew)
            val startDate: DateTime
            val endDate: DateTime
            if (timePeriod == TimePeriod.WEEK) {
                startDate = getStartOfWeek(DateTime(viewModel.getCurrentDate().time))
                endDate = getEndOfWeek(DateTime(viewModel.getCurrentDate().time))
            } else {
                startDate = getStartOfMonth(DateTime(viewModel.getCurrentDate().time))
                endDate = getEndOfMonth(DateTime(viewModel.getCurrentDate().time))
            }
            waterRecords.addAll(createDefaultWaterRecords(startDate, endDate))

            val groupedRecords = waterRecords.groupBy {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = it.dateTime
                calendar.get(Calendar.DAY_OF_YEAR)
            }

            val barEntries = ArrayList<BarEntry>()
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

            groupedRecords.forEach { (dayOfYear, records) ->
                val totalWeight = records.sumOf { it.getWaterInCurrentUnit() }.toFloat()
                barEntries.add(BarEntry(dayOfYear.toFloat(), totalWeight))
            }

            barChart.xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelCount = when (timePeriod) {
                    TimePeriod.WEEK -> 7
                    TimePeriod.MONTH -> 4
                }
//                axisMaximum = when (timePeriod) {
//                    TimePeriod.WEEK -> 7f
//                    TimePeriod.MONTH -> startDate.dayOfMonth().maximumValue.toFloat() + 1
//                }
                valueFormatter = when (timePeriod) {
                    TimePeriod.WEEK -> object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            calendar.set(Calendar.DAY_OF_YEAR, value.toInt())
                            return dateToFormat(
                                LocalDate(calendar.timeInMillis),
                                DAY_FORMAT
                            ).substring(0, 2)
                        }
                    }

                    TimePeriod.MONTH -> object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            calendar.set(Calendar.DAY_OF_YEAR, value.toInt())
                            return dateFormat.format(calendar.time)
                        }
                    }
                }
            }

            barChart.axisLeft.apply {
//                labelCount = 3
                setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
//                spaceTop = 15f
            }

            barChart.axisRight.isEnabled = false


            val barDataSet = BarDataSet(barEntries, "").apply {
                color = ContextCompat.getColor(requireContext(), R.color.passio_primary)
                setDrawValues(false)
            }
            val calendarTemp = Calendar.getInstance()
            calendarTemp.timeInMillis =
                waterRecords.firstOrNull()?.dateTime ?: System.currentTimeMillis()

            val barDataSetTarget = BarDataSet(
                listOf(
                    BarEntry(
                        calendarTemp.get(Calendar.DAY_OF_YEAR).toFloat(),
                        targetWater.toFloat()
                    )
                ), ""
            ).apply {
                color = Color.TRANSPARENT
                setDrawValues(false)
            }

            val dataSets = listOf<IBarDataSet>(barDataSet, barDataSetTarget)
            val data = BarData(dataSets).apply {
                setValueTextSize(0f)
                barWidth = when (timePeriod) {
                    TimePeriod.WEEK -> 0.5f
                    TimePeriod.MONTH -> 0.9f
                }

            }
//            if (timePeriod == TimePeriod.WEEK) {
//                barData.barWidth /= 2f
//            }

            barChart.data = data

            /*val xAxis = barChart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f

            if (timePeriod == TimePeriod.MONTH) {
                xAxis.labelCount = 4
                xAxis.mAxisMaximum = DateTime().dayOfMonth().maximumValue.toFloat() + 1
                xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        calendar.set(Calendar.DAY_OF_YEAR, value.toInt())
                        return dateFormat.format(calendar.time)
                    }
                }
            } else {
                xAxis.labelCount = 7
                xAxis.mAxisMaximum = 7f
                xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        calendar.set(Calendar.DAY_OF_YEAR, value.toInt())
                        return dateToFormat(LocalDate(calendar.timeInMillis), DAY_FORMAT)
                    }
                }

            }
            xAxis.setDrawGridLines(false)  // Hide vertical grid lines

            val yAxisRight = barChart.axisRight
            yAxisRight.isEnabled = false

            val yAxisLeft = barChart.axisLeft
            yAxisLeft.granularity = 0.5f*/

            if (targetWater > 0.0) {
                // Add dashed annotation line for target water intake
                val targetLine = LimitLine(targetWater.toFloat(), "").apply {
                    lineWidth = 2f
                    lineColor = ContextCompat.getColor(requireContext(), R.color.passio_green_800)
                    enableDashedLine(20f, 14f, 0f)
                    labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                    textSize = 10f
                }
                barChart.axisLeft.addLimitLine(targetLine)
            }
            barChart.description.isEnabled = false
            barChart.legend.form = Legend.LegendForm.LINE
            barChart.invalidate() // Refresh the chart
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
            viewModel.navigateToSave()
        }

    }

}