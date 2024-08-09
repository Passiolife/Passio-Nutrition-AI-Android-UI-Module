package ai.passio.nutrition.uimodule.ui.profile

import ai.passio.nutrition.uimodule.databinding.DialogDailyNutritionTargetBinding
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import ai.passio.nutrition.uimodule.ui.util.StringKT.singleDecimal
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.DialogFragment
import com.warkiz.tickseekbar.OnSeekChangeListener
import com.warkiz.tickseekbar.SeekParams
import com.warkiz.tickseekbar.TickSeekBar

class DailyNutritionTargetDialog(
    private val dailyNutritionTarget: DailyNutritionTarget,
    private val listener: DailyNutritionTargetCustomizeListener
) : DialogFragment() {

    data class DailyNutritionTarget(
        var caloriesGoal: Int,
        var carbsPer: Int, //percentage
        var proteinPer: Int, //percentage
        var fatPer: Int //percentage
    ) {

        private fun setInternalPercentage(
            primaryChange: Int,
            secondary: Int,
            tertiary: Int
        ): Array<Int> {
            return if (primaryChange + tertiary > 100) {
                arrayOf(primaryChange, 0, 100 - primaryChange)
            } else {
                arrayOf(primaryChange, 100 - primaryChange - tertiary, tertiary)
            }
        }

        fun setNewCarbsPercentage(newCarbsPercentage: Int) {
            val change = setInternalPercentage(newCarbsPercentage, proteinPer, fatPer)
            carbsPer = change[0]
            proteinPer = change[1]
            fatPer = change[2]
        }

        fun setNewProteinPercentage(newProteinPercentage: Int) {
            val change = setInternalPercentage(newProteinPercentage, fatPer, carbsPer)
            proteinPer = change[0]
            fatPer = change[1]
            carbsPer = change[2]
        }

        fun setNewFatPercentage(newFatPercentage: Int) {
            val change = setInternalPercentage(newFatPercentage, proteinPer, carbsPer)
            fatPer = change[0]
            proteinPer = change[1]
            carbsPer = change[2]
        }

        fun getCarbsGrams(): String =
            "${((carbsPer * caloriesGoal) / 400f).singleDecimal()} g"

        fun getProteinGrams(): String =
            "${((proteinPer * caloriesGoal) / 400f).singleDecimal()} g"

        fun getFatGrams(): String = "${((fatPer * caloriesGoal) / 900f).singleDecimal()} g"
    }

    interface DailyNutritionTargetCustomizeListener {
        fun onCustomized(dailyNutritionTarget: DailyNutritionTarget)
    }


    private var _binding: DialogDailyNutritionTargetBinding? = null
    private val binding: DialogDailyNutritionTargetBinding get() = _binding!!

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            DesignUtils.screenWidth(requireContext()) - DesignUtils.dp2px(20f),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDailyNutritionTargetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.calorieGoal.setText(dailyNutritionTarget.caloriesGoal.toString())
        renderData()

        binding.cancel.setOnClickListener {
            dismiss()
        }

        binding.save.setOnClickListener {
            listener.onCustomized(dailyNutritionTarget)
            dismiss()
        }

        setupSeekbar(binding.carbsSeek) { newPer ->
            dailyNutritionTarget.setNewCarbsPercentage(newPer)
            renderData()
        }
        setupSeekbar(binding.proteinSeek) { newPer ->
            dailyNutritionTarget.setNewProteinPercentage(newPer)
            renderData()
        }
        setupSeekbar(binding.fatSeek) { newPer ->
            dailyNutritionTarget.setNewFatPercentage(newPer)
            renderData()
        }

        setupEditDone(binding.carbsPer) { newPer ->
            dailyNutritionTarget.setNewCarbsPercentage(newPer.toIntOrNull() ?: 0)
            renderData()
        }
        setupEditDone(binding.proteinPer) { newPer ->
            dailyNutritionTarget.setNewProteinPercentage(newPer.toIntOrNull() ?: 0)
            renderData()
        }
        setupEditDone(binding.fatPer) { newPer ->
            dailyNutritionTarget.setNewFatPercentage(newPer.toIntOrNull() ?: 0)
            renderData()
        }
        setupEditable(binding.calorieGoal) { newPer ->
            newPer.toIntOrNull()?.let { newCal ->
                dailyNutritionTarget.caloriesGoal = newCal
                renderData()
            }
        }

    }

    private fun setupEditDone(
        editView: AppCompatEditText,
        onValueChanged: (value: String) -> Unit
    ) {
        editView.setOnEditorActionListener { p0, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Handle the "Done" action here
                p0.clearFocus()
                onValueChanged.invoke(editView.text.toString())
            }
            false
        }
    }

    private fun setupEditable(
        editView: AppCompatEditText,
        onValueChanged: (value: String) -> Unit,
    ) {
        editView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                onValueChanged.invoke(s.toString())
            }

            override fun beforeTextChanged(
                s: CharSequence?, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
            }

        })
    }

    private fun setupSeekbar(seekbar: TickSeekBar, onValueChanged: (value: Int) -> Unit) {
        seekbar.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams?) {
                Log.d(
                    "onProgressChanged",
                    "onSeeking ${seekParams?.fromUser} : ${seekParams?.progressFloat} : ${seekParams?.progress} : ${seekParams?.thumbPosition} : ${seekParams?.tickText}"
                )
                seekParams?.let {
                    if (!it.fromUser) {
                        onValueChanged.invoke(it.progress)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: TickSeekBar?) {
                Log.d("onProgressChanged", "onProgressChanged")
            }

            override fun onStopTrackingTouch(seekBar: TickSeekBar?) {
                Log.d("onProgressChanged", "onProgressChanged")
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun renderData() {
        with(binding)
        {
            fatSeek.setProgress(dailyNutritionTarget.fatPer.toFloat())
            proteinSeek.setProgress(dailyNutritionTarget.proteinPer.toFloat())
            carbsSeek.setProgress(dailyNutritionTarget.carbsPer.toFloat())

            fatPer.setText(dailyNutritionTarget.fatPer.toString())
            proteinPer.setText(dailyNutritionTarget.proteinPer.toString())
            carbsPer.setText(dailyNutritionTarget.carbsPer.toString())

            fatGram.text = dailyNutritionTarget.getFatGrams()
            proteinGram.text = dailyNutritionTarget.getProteinGrams()
            carbsGram.text = dailyNutritionTarget.getCarbsGrams()

        }
    }
}