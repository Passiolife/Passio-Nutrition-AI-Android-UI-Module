package ai.passio.nutrition.uimodule.ui.progress

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.TimePeriodLayoutBinding
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding

enum class TimePeriod(val text: String) {
    WEEK("Week"),
    MONTH("Month");
}

class WeekMonthPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {



    interface TimePeriodListener {
        fun onValueChanged(timePeriod: TimePeriod)
    }

    private val binding: TimePeriodLayoutBinding =
        TimePeriodLayoutBinding.inflate(LayoutInflater.from(context), this)
    private var listener: TimePeriodListener? = null

    private val textColorSelected = ContextCompat.getColor(context, R.color.passio_white)
    private val textColorDeselected = ContextCompat.getColor(context, R.color.passio_gray500)
    private val leftBackgroundSelected =
        ContextCompat.getDrawable(context, R.drawable.rc_8_primary_left)
    private val rightBackgroundSelected =
        ContextCompat.getDrawable(context, R.drawable.rc_8_primary_right)
    private val rightBackgroundDeselected =
        ContextCompat.getDrawable(context, R.drawable.rc_8_white_right)
    private val leftBackgroundDeselected =
        ContextCompat.getDrawable(context, R.drawable.rc_8_white_left)

    init {
//        _binding = TimePeriodLayoutBinding.inflate(LayoutInflater.from(context), this)
        background = ContextCompat.getDrawable(context, R.drawable.rc_8_border_gray300)
        setPadding(DesignUtils.dp2px(1f))
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
//        _binding = null
    }

    fun setup(selected: TimePeriod, listener: TimePeriodListener) {
//        if (_binding == null) return
        this.listener = listener
        renderState(selected)

        with(binding) {
            week.setOnClickListener {
                renderState(TimePeriod.WEEK)
                listener.onValueChanged(TimePeriod.WEEK)
            }
            month.setOnClickListener {
                renderState(TimePeriod.MONTH)
                listener.onValueChanged(TimePeriod.MONTH)
            }
        }
    }

    private fun renderState(timePeriod: TimePeriod) {
        with(binding) {
            when (timePeriod) {
                TimePeriod.WEEK -> {
                    week.background = leftBackgroundSelected
                    week.setTextColor(textColorSelected)
                    month.background = rightBackgroundDeselected
                    month.setTextColor(textColorDeselected)
                }

                TimePeriod.MONTH -> {
                    week.background = leftBackgroundDeselected
                    week.setTextColor(textColorDeselected)
                    month.background = rightBackgroundSelected
                    month.setTextColor(textColorSelected)
                }
            }
        }
    }
}