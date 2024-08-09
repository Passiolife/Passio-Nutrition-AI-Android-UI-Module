package ai.passio.nutrition.uimodule.ui.mealplan

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.MealplanCategoryLayoutBinding
import ai.passio.nutrition.uimodule.ui.model.MealLabel
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import ai.passio.passiosdk.passiofood.data.model.PassioMealPlanItem
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible

class MealPlanCategory @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    interface MealPlanCategoryListener {
        fun onLogAdd(foodRecord: PassioMealPlanItem)
        fun onLogAdd(foodRecords: List<PassioMealPlanItem>)
        fun onLogEdit(foodRecord: PassioMealPlanItem)
    }

    private var _binding: MealplanCategoryLayoutBinding? = null
    private val binding: MealplanCategoryLayoutBinding get() = _binding!!
    private val adapter = MealPlanAdapter(::onAddLog, ::onEditLog)
    private lateinit var mealLabel: MealLabel
    private var listener: MealPlanCategoryListener? = null

    init {
        _binding = MealplanCategoryLayoutBinding.inflate(LayoutInflater.from(context), this)
        orientation = VERTICAL
        background = ContextCompat.getDrawable(context, R.drawable.rc_8_white)
        elevation = DesignUtils.dp2pxFloat(4f)

        with(binding) {
            logList.adapter = adapter
            isVisible = adapter.itemCount != 0
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _binding = null
    }

    fun setup(mealLabel: MealLabel, listener: MealPlanCategoryListener) {
        this.mealLabel = mealLabel
        this.listener = listener
        binding.category.text = mealLabel.value
    }

    fun update(records: List<PassioMealPlanItem>) {
        adapter.updateLogs(records)

        isVisible = records.isNotEmpty()

        binding.logEntireMeal.setOnClickListener {
            onAddLog(records)
        }

        postInvalidate()

    }

    private fun onAddLog(foodRecord: PassioMealPlanItem) {
        listener?.onLogAdd(foodRecord)
    }

    private fun onAddLog(foodRecord: List<PassioMealPlanItem>) {
        if (foodRecord.isNotEmpty()) {
            listener?.onLogAdd(foodRecord)
        }
    }

    private fun onEditLog(foodRecord: PassioMealPlanItem) {
        listener?.onLogEdit(foodRecord)
    }
}