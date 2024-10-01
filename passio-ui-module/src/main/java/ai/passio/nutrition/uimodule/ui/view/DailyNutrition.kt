package ai.passio.nutrition.uimodule.ui.view

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.DailyNutritionLayoutBinding
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class DailyNutrition @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var _binding: DailyNutritionLayoutBinding? = null
    private val binding: DailyNutritionLayoutBinding get() = _binding!!

    private var caloriesColor: Int = ContextCompat.getColor(context, R.color.passio_calories)
    private var carbsColor: Int = ContextCompat.getColor(context, R.color.passio_carbs)
    private var proteinColor: Int = ContextCompat.getColor(context, R.color.passio_protein)
    private var fatColor: Int = ContextCompat.getColor(context, R.color.passio_fat)
    private var caloriesOverColor: Int =
        ContextCompat.getColor(context, R.color.passio_calories_over)
    private var carbsOverColor: Int = ContextCompat.getColor(context, R.color.passio_carbs_over)
    private var proteinOverColor: Int = ContextCompat.getColor(context, R.color.passio_protein_over)
    private var fatOverColor: Int = ContextCompat.getColor(context, R.color.passio_fat_over)
    private var noColor: Int = ContextCompat.getColor(context, R.color.passio_gray200)
    private var valueOverColor: Int = ContextCompat.getColor(context, R.color.passio_red600)
    private var blackColor: Int = ContextCompat.getColor(context, R.color.passio_black)

    init {
        _binding = DailyNutritionLayoutBinding.inflate(LayoutInflater.from(context), this)
        background = ContextCompat.getDrawable(context, R.drawable.rc_8_white)
        elevation = DesignUtils.dp2pxFloat(4f)
        setPadding(
            DesignUtils.dp2px(8f),
            DesignUtils.dp2px(16f),
            DesignUtils.dp2px(8f),
            DesignUtils.dp2px(16f)
        )

        with(binding) {
            setupChart(caloriesChart)
            setupChart(carbsChart)
            setupChart(proteinChart)
            setupChart(fatChart)
        }

        setup(0, 0, 0, 0, 0, 0, 0, 0)
    }

    fun setLoading(isLoading: Boolean) {
        binding.progressDailyNutrition.isVisible = isLoading
    }


    fun invokeProgressReport(onclick: () -> Unit) {
        binding.progressReport.setOnClickListener {
            onclick.invoke()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _binding = null
    }

    fun setup(
        caloriesCurrent: Int,
        caloriesTarget: Int,
        carbsCurrent: Int,
        carbsTarget: Int,
        proteinCurrent: Int,
        proteinTarget: Int,
        fatCurrent: Int,
        fatTarget: Int,
    ) {
        if (_binding == null) return

        renderChart(
            binding.caloriesChart,
            binding.caloriesCurrent,
            binding.caloriesTarget,
            caloriesCurrent,
            caloriesTarget,
            caloriesColor,
            caloriesOverColor
        )
        renderChart(
            binding.carbsChart,
            binding.carbsCurrent,
            binding.carbsTarget,
            carbsCurrent,
            carbsTarget,
            carbsColor,
            carbsOverColor
        )
        renderChart(
            binding.proteinChart,
            binding.proteinCurrent,
            binding.proteinTarget,
            proteinCurrent,
            proteinTarget,
            proteinColor,
            proteinOverColor
        )
        renderChart(
            binding.fatChart,
            binding.fatCurrent,
            binding.fatTarget,
            fatCurrent,
            fatTarget,
            fatColor,
            fatOverColor
        )
    }

    private fun setupChart(pieChart: PieChart) {
        with(pieChart) {
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 85f
            transparentCircleRadius = 85f
            description.isEnabled = false
            legend.isEnabled = false
            setDrawEntryLabels(false)
            setDrawSliceText(false)
            setDrawMarkers(false)
            setTouchEnabled(false)
        }
    }

    private fun renderChart(
        chart: PieChart,
        currentTextView: TextView,
        targetTextView: TextView,
        currentValue: Int,
        targetValue: Int,
        valueColor: Int,
        overValueColor: Int
    ) {
        currentTextView.text = currentValue.toString()
        targetTextView.text = targetValue.toString()

        val entries = mutableListOf<PieEntry>()
        if (currentValue <= targetValue) {
            entries.add(PieEntry(currentValue.toFloat(), "value"))
            entries.add(PieEntry(targetValue.toFloat() - currentValue, "noValue"))

            val dataSet = PieDataSet(entries, "Chart")
            val colors = listOf(valueColor, noColor)
            dataSet.colors = colors
            dataSet.valueTextSize = 0f

            val data = PieData(dataSet)
            chart.data = data
            chart.invalidate()

            currentTextView.setTextColor(blackColor)
        } else {
            var ratio = (currentValue - targetValue).toFloat() / targetValue
            if (ratio > 1f) {
                ratio = 1f
            }
            entries.add(PieEntry(ratio, "value"))
            entries.add(PieEntry(1f - ratio, "noValue"))

            val dataSet = PieDataSet(entries, "chart")
            val colors = listOf(overValueColor, valueColor)
            dataSet.colors = colors
            dataSet.valueTextSize = 0f

            val data = PieData(dataSet)
            chart.data = data
            chart.invalidate()

            currentTextView.setTextColor(valueOverColor)
        }
    }
}