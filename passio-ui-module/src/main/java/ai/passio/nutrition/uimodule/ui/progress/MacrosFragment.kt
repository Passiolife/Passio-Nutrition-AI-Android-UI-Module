package ai.passio.nutrition.uimodule.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.FragmentMacrosBinding
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.caloriesSum
import ai.passio.nutrition.uimodule.ui.model.carbsSum
import ai.passio.nutrition.uimodule.ui.model.fatSum
import ai.passio.nutrition.uimodule.ui.model.proteinSum
import ai.passio.nutrition.uimodule.ui.util.getEndOfMonth
import ai.passio.nutrition.uimodule.ui.util.getEndOfWeek
import ai.passio.nutrition.uimodule.ui.util.getMonthName
import ai.passio.nutrition.uimodule.ui.util.getStartOfMonth
import ai.passio.nutrition.uimodule.ui.util.getStartOfWeek
import ai.passio.nutrition.uimodule.ui.util.getWeekDuration
import ai.passio.nutrition.uimodule.ui.util.isPartOfCurrentMonth
import ai.passio.nutrition.uimodule.ui.util.isPartOfCurrentWeek
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import org.joda.time.DateTime
import java.util.Date

class MacrosFragment : BaseFragment<MacrosViewModel>() {

    private var _binding: FragmentMacrosBinding? = null
    private val binding: FragmentMacrosBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMacrosBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragment?.arguments?.let {
            if (it.containsKey("currentDate")) {
                val currentDate = it.getLong("currentDate", 0)
                if (currentDate > 0) {
                    viewModel.setDate(Date(currentDate))
                }
            }
        }

        with(binding)
        {
            setupChart(progressCaloriesBarChart)
            setupChart(progressNutrientsBarChart)
            weekMonthPicker.setup(TimePeriod.WEEK, timePeriodListener)
            movePrevious.setOnClickListener {
                viewModel.setPrevious()
            }
            moveNext.setOnClickListener {
                viewModel.setNext()
            }
        }

        viewModel.logsLD.observe(viewLifecycleOwner, ::updateLogs)
        viewModel.timePeriod.observe(viewLifecycleOwner, ::updateTimePeriod)
        viewModel.showLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loading.isVisible = isLoading
        }

        viewModel.fetchLogsForCurrentWeek()
    }

    private val timePeriodListener = object : WeekMonthPicker.TimePeriodListener {
        override fun onValueChanged(timePeriod: TimePeriod) {
            if (timePeriod == TimePeriod.WEEK) {
                viewModel.fetchLogsForCurrentWeek()
            } else if (timePeriod == TimePeriod.MONTH) {
                viewModel.fetchLogsForCurrentMonth()
            }
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

    }

    private fun updateLogs(result: Pair<TimePeriod, List<FoodRecord>>) {
        val records = result.second
        val timePeriod = result.first
        renderCaloriesBarChart(records, timePeriod)
        renderNutrientBarChart(records, timePeriod)
    }

    private fun setupChart(barChart: BarChart) {
        with(barChart) {
            setDrawValueAboveBar(false)
            setDrawValueAboveBar(false)
            setFitBars(false)
            description.isEnabled = false
            setPinchZoom(false)
            legend.isEnabled = false
            setTouchEnabled(false)

        }
    }

    private fun renderNutrientBarChart(records: List<FoodRecord>, timePeriod: TimePeriod) {
        binding.progressNutrientsBarChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            labelCount = when (timePeriod) {
                TimePeriod.WEEK -> 7
                TimePeriod.MONTH -> 4
            }
            valueFormatter = when (timePeriod) {
                TimePeriod.WEEK -> WeekAxisValueFormatter(getStartOfWeek(DateTime(viewModel.getCurrentDate().time)))
                TimePeriod.MONTH -> MonthAxisValueFormatter(getStartOfMonth(DateTime(viewModel.getCurrentDate().time)))
            }
            axisMaximum = when (timePeriod) {
                TimePeriod.WEEK -> 7f
                TimePeriod.MONTH -> DateTime().dayOfMonth().maximumValue.toFloat() + 1
            }
        }

        binding.progressNutrientsBarChart.axisLeft.apply {
            labelCount = 3
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            spaceTop = 15f
        }

        binding.progressNutrientsBarChart.axisRight.isEnabled = false

        val entries = when (timePeriod) {
            TimePeriod.WEEK -> calculateNutrientEntriesForWeek(records, false)
            TimePeriod.MONTH -> calculateNutrientEntriesForMonth(records, false)
        }

        val barSet = BarDataSet(entries, "Nutrients")
        barSet.setDrawIcons(false)
        barSet.colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.passio_carbs),
            ContextCompat.getColor(requireContext(), R.color.passio_protein),
            ContextCompat.getColor(requireContext(), R.color.passio_fat)
        )

        val dataSets = listOf<IBarDataSet>(barSet)
        val data = BarData(dataSets).apply {
            setValueTextSize(0f)
            barWidth = when (timePeriod) {
                TimePeriod.WEEK -> 0.5f
                TimePeriod.MONTH -> 0.9f
            }

        }

        binding.progressNutrientsBarChart.data = data
        binding.progressNutrientsBarChart.invalidate()
        binding.progressNutrientsBarChart.animateY(600, Easing.EaseInQuad)
    }

    private fun calculateNutrientEntriesForWeek(
        records: List<FoodRecord>,
        calculateCalories: Boolean
    ): List<BarEntry> {
        val today = DateTime(viewModel.getCurrentDate().time)
//        val startOfWeek = getStartOfWeek(today).millis
//        val endOfWeek = getEndOfWeek(today).millis
        val endTime = getEndOfWeek(today)
        val startTime = getStartOfWeek(today)

        val entries = mutableListOf<BarEntry>()
        var time: DateTime = startTime
        while (isBetweenInclusive(startTime, endTime, time)) {
            val day = time.dayOfYear
            val recordsForDay = records.filter {
                val recordTime = DateTime(it.createdAtTime()!!)
                val recordDay = recordTime.dayOfYear
                day == recordDay
            }
            val deltaDay = time.dayOfYear - startTime.dayOfYear
            if (recordsForDay.isEmpty()) {
                if (calculateCalories) {
                    entries.add(BarEntry(deltaDay.toFloat(), 0f))
                } else {
                    entries.add(BarEntry(deltaDay.toFloat(), floatArrayOf(0f, 0f, 0f)))
                }
            } else {
                if (calculateCalories) {
                    val calories = recordsForDay.caloriesSum()
                    entries.add(BarEntry(deltaDay.toFloat(), calories.toFloat()))
                } else {
                    val carbs = recordsForDay.carbsSum()
                    val protein = recordsForDay.proteinSum()
                    val fat = recordsForDay.fatSum()
                    entries.add(
                        BarEntry(
                            deltaDay.toFloat(),
                            floatArrayOf(carbs.toFloat(), protein.toFloat(), fat.toFloat())
                        )
                    )
                }
            }
            time = time.plusDays(1)
        }

        return entries
    }

    private fun calculateNutrientEntriesForMonth(
        records: List<FoodRecord>,
        calculateCalories: Boolean
    ): List<BarEntry> {

        val today = DateTime(viewModel.getCurrentDate().time)
        val endTime = getEndOfMonth(today)
        val startTime = getStartOfMonth(today)

//        val endTime = DateTime()
//        val startTime = endTime.minusDays(30)

        val entries = mutableListOf<BarEntry>()
        var time: DateTime = startTime
        while (isBetweenInclusive(startTime, endTime, time)) {
            val dayOfYear = time.dayOfYear
            val recordsForDay = records.filter {
                val recordTime = DateTime(it.createdAtTime()!!)
                val recordDayOfYears = recordTime.dayOfYear
                recordDayOfYears == dayOfYear
            }
            val deltaDay = time.dayOfYear - startTime.dayOfYear
            if (recordsForDay.isEmpty()) {
                if (calculateCalories) {
                    entries.add(BarEntry(deltaDay.toFloat(), 0f))
                } else {
                    entries.add(BarEntry(deltaDay.toFloat(), floatArrayOf(0f, 0f, 0f)))
                }
            } else {
                if (calculateCalories) {
                    val calories = recordsForDay.caloriesSum()
                    entries.add(BarEntry(deltaDay.toFloat(), calories.toFloat()))
                } else {
                    val carbs = recordsForDay.carbsSum()
                    val protein = recordsForDay.proteinSum()
                    val fat = recordsForDay.fatSum()
                    entries.add(
                        BarEntry(
                            deltaDay.toFloat(),
                            floatArrayOf(carbs.toFloat(), protein.toFloat(), fat.toFloat())
                        )
                    )
                }
            }
            time = time.plusDays(1)
        }

        return entries
    }

    private fun isBetweenInclusive(start: DateTime, end: DateTime, target: DateTime): Boolean {
        return !target.isBefore(start) && !target.isAfter(end)
    }

    private fun renderCaloriesBarChart(records: List<FoodRecord>, timePeriod: TimePeriod) {
        binding.progressCaloriesBarChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            labelCount = when (timePeriod) {
                TimePeriod.WEEK -> 7
                TimePeriod.MONTH -> 4
            }
            valueFormatter = when (timePeriod) {
                TimePeriod.WEEK -> WeekAxisValueFormatter(getStartOfWeek(DateTime(viewModel.getCurrentDate().time)))
                TimePeriod.MONTH -> MonthAxisValueFormatter(getStartOfMonth(DateTime(viewModel.getCurrentDate().time)))
            }
            axisMaximum = when (timePeriod) {
                TimePeriod.WEEK -> 6.5f
                TimePeriod.MONTH -> DateTime().dayOfMonth().maximumValue.toFloat() + 1
            }
        }

        binding.progressCaloriesBarChart.axisLeft.apply {
            labelCount = 3
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            spaceTop = 15f
            axisMinimum = 0f
        }

        binding.progressCaloriesBarChart.axisRight.isEnabled = false

        val entries = when (timePeriod) {
            TimePeriod.WEEK -> calculateNutrientEntriesForWeek(records, true)
            TimePeriod.MONTH -> calculateNutrientEntriesForMonth(records, true)
        }

        val barSet = BarDataSet(entries, "Calories")
        barSet.setDrawIcons(false)
        barSet.setColors(ContextCompat.getColor(requireContext(), R.color.passio_calories))

        val dataSets = listOf<IBarDataSet>(barSet)
        val data = BarData(dataSets).apply {
            setValueTextSize(0f)

            barWidth = when (timePeriod) {
                TimePeriod.WEEK -> 0.5f
                TimePeriod.MONTH -> 0.9f
            }
        }

        binding.progressCaloriesBarChart.data = data
        binding.progressCaloriesBarChart.invalidate()
        binding.progressCaloriesBarChart.animateY(600, Easing.EaseInQuad)
    }


}