package ai.passio.nutrition.uimodule.ui.edit

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.MealTimeLayoutBinding
import ai.passio.nutrition.uimodule.ui.model.MealLabel
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding

class MealTimePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    interface MealTimeListener {
        fun onValueChanged(mealLabel: MealLabel)
    }

    private var _binding: MealTimeLayoutBinding? = null
    private val binding: MealTimeLayoutBinding get() = _binding!!
    private var listener: MealTimeListener? = null

    private val backgroundSelected = ContextCompat.getColor(context, R.color.passio_primary)
    private val backgroundDeselected = ContextCompat.getColor(context, R.color.passio_white)
    private val textColorSelected = ContextCompat.getColor(context, R.color.passio_white)
    private val textColorDeselected = ContextCompat.getColor(context, R.color.passio_gray500)
    private val leftBackgroundSelected = ContextCompat.getDrawable(context, R.drawable.rc_8_primary_left)
    private val leftBackgroundDeselected = ContextCompat.getDrawable(context, R.drawable.rc_8_white_left)
    private val rightBackgroundSelected = ContextCompat.getDrawable(context, R.drawable.rc_8_primary_right)
    private val rightBackgroundDeselected = ContextCompat.getDrawable(context, R.drawable.rc_8_white_right)

    init {
        _binding = MealTimeLayoutBinding.inflate(LayoutInflater.from(context), this)
        background = ContextCompat.getDrawable(context, R.drawable.rc_8_border_gray300)
        setPadding(DesignUtils.dp2px(1f))
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _binding = null
    }

    fun setup(selected: MealLabel, listener: MealTimeListener) {
        if (_binding == null) return
        this.listener = listener
        renderState(selected)

        with(binding) {
            breakfast.setOnClickListener {
                renderState(MealLabel.Breakfast)
                listener.onValueChanged(MealLabel.Breakfast)
            }
            lunch.setOnClickListener {
                renderState(MealLabel.Lunch)
                listener.onValueChanged(MealLabel.Lunch)
            }
            dinner.setOnClickListener {
                renderState(MealLabel.Dinner)
                listener.onValueChanged(MealLabel.Dinner)
            }
            snack.setOnClickListener {
                renderState(MealLabel.Snack)
                listener.onValueChanged(MealLabel.Snack)
            }
        }
    }

    private fun renderState(selected: MealLabel) {
        with(binding) {
            when (selected) {
                MealLabel.Breakfast -> {
                    breakfast.background = leftBackgroundSelected
                    breakfast.setTextColor(textColorSelected)
                    lunch.setBackgroundColor(backgroundDeselected)
                    lunch.setTextColor(textColorDeselected)
                    dinner.setBackgroundColor(backgroundDeselected)
                    dinner.setTextColor(textColorDeselected)
                    snack.background = rightBackgroundDeselected
                    snack.setTextColor(textColorDeselected)
                }

                MealLabel.Lunch -> {
                    breakfast.background = leftBackgroundDeselected
                    breakfast.setTextColor(textColorDeselected)
                    lunch.setBackgroundColor(backgroundSelected)
                    lunch.setTextColor(textColorSelected)
                    dinner.setBackgroundColor(backgroundDeselected)
                    dinner.setTextColor(textColorDeselected)
                    snack.background = rightBackgroundDeselected
                    snack.setTextColor(textColorDeselected)
                }

                MealLabel.Dinner -> {
                    breakfast.background = leftBackgroundDeselected
                    breakfast.setTextColor(textColorDeselected)
                    lunch.setBackgroundColor(backgroundDeselected)
                    lunch.setTextColor(textColorDeselected)
                    dinner.setBackgroundColor(backgroundSelected)
                    dinner.setTextColor(textColorSelected)
                    snack.background = rightBackgroundDeselected
                    snack.setTextColor(textColorDeselected)
                }

                MealLabel.Snack -> {
                    breakfast.background = rightBackgroundDeselected
                    breakfast.setTextColor(textColorDeselected)
                    lunch.setBackgroundColor(backgroundDeselected)
                    lunch.setTextColor(textColorDeselected)
                    dinner.setBackgroundColor(backgroundDeselected)
                    dinner.setTextColor(textColorDeselected)
                    snack.background = rightBackgroundSelected
                    snack.setTextColor(textColorSelected)
                }
            }
        }
    }
}