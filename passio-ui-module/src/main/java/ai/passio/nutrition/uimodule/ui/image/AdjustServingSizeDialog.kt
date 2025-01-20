package ai.passio.nutrition.uimodule.ui.image

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.DialogAdjustServingSizeBinding
import ai.passio.nutrition.uimodule.ui.base.BaseDialogFragment
import ai.passio.nutrition.uimodule.ui.editingredient.EditIngredientFragment.UpdateOrigin
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.ImageFoodResult
import ai.passio.nutrition.uimodule.ui.model.getShortInfo
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.StringKT.singleDecimal
import ai.passio.nutrition.uimodule.ui.util.loadFoodImage
import ai.passio.nutrition.uimodule.ui.view.tickseekbar.OnSeekChangeListener
import ai.passio.nutrition.uimodule.ui.view.tickseekbar.SeekParams
import ai.passio.nutrition.uimodule.ui.view.tickseekbar.TickSeekBar
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter

interface OnAdjustServingSizeListener {
    fun onChanged(editedIndex: Int, updatedImageFoodResult: ImageFoodResult)
    fun onEdit(editedIndex: Int, imageFoodResult: ImageFoodResult)
    fun onCancelled(editedIndex: Int)
}

internal class AdjustServingSizeDialog(
    private val imageFoodResult: ImageFoodResult,
    private val editIndex: Int,
    private val onAdjustServingSizeListener: OnAdjustServingSizeListener
) : BaseDialogFragment<AdjustServingSizeViewModel>() {
    private var _binding: DialogAdjustServingSizeBinding? = null
    private val binding: DialogAdjustServingSizeBinding get() = _binding!!
    private lateinit var servingUnitAdapter: ArrayAdapter<String>

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
        _binding = DialogAdjustServingSizeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        viewModel.editFoodRecord(imageFoodResult to editIndex)
        with(binding) {

            cancel.setOnClickListener {
                onAdjustServingSizeListener.onCancelled(viewModel.getEditFoodRecordIndex())
                dismiss()
            }
            edit.setOnClickListener {
                onAdjustServingSizeListener.onEdit(
                    viewModel.getEditFoodRecordIndex(),
                    viewModel.getFoodRecord()
                )
                dismiss()
            }
            done.setOnClickListener {
                onAdjustServingSizeListener.onChanged(
                    viewModel.getEditFoodRecordIndex(),
                    viewModel.getFoodRecord()
                )
                dismiss()
            }

            servingQuantity.setSingleLine()
            servingQuantity.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    try {
                        val quantity = v.text.toString().toDouble()
                        viewModel.updateServingQuantity(quantity, UpdateOrigin.QUANTITY)
                        servingQuantity.clearFocus()
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                        return@setOnEditorActionListener true
                    }
                    return@setOnEditorActionListener false
                }
                true
            }
            servingQuantitySeekBar.onSeekChangeListener = seekChangeListener
        }
        initObservers()

    }

    private fun initObservers() {
        viewModel.editFoodModelLD.observe(viewLifecycleOwner) { editFoodModel ->
            editFoodModel?.let {
                renderFoodRecord(editFoodModel)
            }
        }

        viewModel.internalUpdate.observe(viewLifecycleOwner) { pair ->
            updateFoodRecord(pair.first, pair.second)
        }
    }

    private fun renderFoodRecord(imageFoodResult: ImageFoodResult) {
        setupImmutableProperties(imageFoodResult)
//        renderNutrients(model.foodRecord)
        renderServingSize(imageFoodResult)
    }

    private fun updateFoodRecord(imageFoodResult: ImageFoodResult, origin: UpdateOrigin) {
//        renderNutrients(foodRecord)
        renderServingSize(imageFoodResult, origin)
        if (origin == UpdateOrigin.INGREDIENT) {
            setupImmutableProperties(imageFoodResult)
        }
    }

    private fun setupImmutableProperties(imageFoodResult: ImageFoodResult) {
        if (_binding == null) return

        val foodRecord = imageFoodResult.record
        val units = foodRecord.servingUnits.map { it.unitName }
        val indexOfSelected = units.indexOfFirst { it.lowercase() == foodRecord.getSelectedUnit() }
        servingUnitAdapter =
            ArrayAdapter<String>(requireContext(), R.layout.serving_unit_item, units)
        servingUnitAdapter.setDropDownViewResource(R.layout.serving_unit_item)

        with(binding) {
            foodImage.loadFoodImage(foodRecord)
            foodName.text = foodRecord.name.capitalized()
            infoName.text = foodRecord.getShortInfo()

            servingUnit.adapter = servingUnitAdapter
            servingUnit.onItemSelectedListener = servingUnitListener
            servingUnit.setSelection(indexOfSelected)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun renderServingSize(imageFoodResult: ImageFoodResult, origin: UpdateOrigin? = null) {
        if (_binding == null) return

        val foodRecord = imageFoodResult.record
        with(binding) {
            val weightGrams = foodRecord.servingWeight().gramsValue().singleDecimal()
            servingSizeValue.text = " ($weightGrams g)"

            if (origin != UpdateOrigin.QUANTITY) {
                if (foodRecord.getSelectedQuantity() == FoodRecord.ZERO_QUANTITY) {
                    servingQuantity.setText("0")
                } else {
                    val quantity = foodRecord.getSelectedQuantity().singleDecimal()
                    servingQuantity.setText(quantity)
                }
            }
            if (origin != UpdateOrigin.SEEKBAR) {
                updateSlider(foodRecord.getSelectedQuantity())
            }
        }
    }

    private val servingUnitListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            viewModel.updateServingUnit(position, UpdateOrigin.UNIT)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    private val seekChangeListener = object : OnSeekChangeListener {
        override fun onSeeking(seekParams: SeekParams) {
            if (!seekParams.fromUser) {
                return
            }

            val value = seekParams.progressFloat.toDouble()
            if (value % 0.5 == 0.0) {
                viewModel.updateServingQuantity(
                    seekParams.progressFloat.toDouble(),
                    UpdateOrigin.SEEKBAR
                )
            } else {
                val multiplier = (value / 0.5).toInt()
                val roundedValue = 0.5 * multiplier
                viewModel.updateServingQuantity(roundedValue, UpdateOrigin.SEEKBAR)
            }
        }

        override fun onStartTrackingTouch(seekBar: TickSeekBar?) {}
        override fun onStopTrackingTouch(seekBar: TickSeekBar?) {}
    }

    private fun updateSlider(value: Double) {
        val sliderStep: Float
        val sliderMax: Float
        when {
            value < 5 -> {
                sliderStep = 0.5f
                sliderMax = 4.5f
            }

            value <= 9 -> {
                sliderStep = 1f
                sliderMax = 9f
            }

            value <= 49 -> {
                sliderStep = 1f
                sliderMax = 49f
            }

            value <= 250 -> {
                sliderMax = 250f
                sliderStep = sliderMax / 50
            }

            else -> {
                sliderMax = value.toFloat()
                sliderStep = sliderMax / 50
            }
        }

        var sliderSteps = (sliderMax / sliderStep).toInt() + 1
        if (sliderSteps > 50) {
            sliderSteps = 50
        }

        (binding.servingQuantitySeekBar).apply {
            max = sliderMax
            min = 0f
            tickCount = sliderSteps
            setProgress(value.toFloat())
        }
    }
}