package ai.passio.nutrition.uimodule.ui.image

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.DialogEditNutritionFactsBinding
import ai.passio.nutrition.uimodule.ui.base.BaseDialogFragment
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_CALORIES_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_CARBS_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_FAT_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_PROTEIN_ID
import ai.passio.nutrition.uimodule.ui.model.ImageFoodResult
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.StringKT.singleDecimal
import ai.passio.nutrition.uimodule.ui.util.ViewEXT.setupEditable
import ai.passio.nutrition.uimodule.ui.util.loadFoodImage
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatEditText

interface OnEditNutritionFactsListener {
    fun onChanged(editedIndex: Int, updatedImageFoodResult: ImageFoodResult)
    fun onCancelled(editedIndex: Int, imageFoodResult: ImageFoodResult)
}

internal class EditNutritionFactsDialog(
    private val imageFoodResult: ImageFoodResult,
    private val editIndex: Int,
    private val onEditNutritionFactsListener: OnEditNutritionFactsListener
) : BaseDialogFragment<EditNutritionFactsViewModel>() {
    private var _binding: DialogEditNutritionFactsBinding? = null
    private val binding: DialogEditNutritionFactsBinding get() = _binding!!
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
        _binding = DialogEditNutritionFactsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        if (editIndex != viewModel.getEditFoodRecordIndex()) {
            viewModel.editFoodRecord(imageFoodResult to editIndex)
        }
        viewModel.updateNutrientsOnUI()

        sharedViewModel.barcodeScanResult.observe(viewLifecycleOwner) {
            viewModel.updateMissingFromBarcode(it.first, it.second)
        }

        with(binding) {

            cancel.setOnClickListener {
                dismiss()
                onEditNutritionFactsListener.onCancelled(
                    editIndex,
                    imageFoodResult
                )
            }

            save.setOnClickListener {

                viewModel.saveCustomFood()
            }

            barcode.setOnClickListener {
                sharedViewModel.scanBarcode(isCheckExisting = false)
                viewModel.navigateToScanBarcode()
            }
            serving.setupEditable { txt ->
                viewModel.updateServingQuantity(txt.toDoubleOrNull() ?: 0.0)
            }
            weight.setupEditable { txt ->
                viewModel.setWeightInGram(txt.toDoubleOrNull() ?: 0.0)
            }

            foodName.setupEditable { txt ->
                viewModel.setFoodName(txt)
            }
            calories.setupEditable { txt ->
                viewModel.setCalories(txt.toDoubleOrNull())
            }
            fat.setupEditable { txt ->
                viewModel.setFat(txt.toDoubleOrNull())
            }
            protein.setupEditable { txt ->
                viewModel.setProtein(txt.toDoubleOrNull())
            }
            carbs.setupEditable { txt ->
                viewModel.setCarbs(txt.toDoubleOrNull())
            }
        }
        initObservers()

    }

    private fun initObservers() {
        viewModel.editFoodModelLD.observe(viewLifecycleOwner) { editFoodModel ->
            editFoodModel?.let {
                renderFoodRecord(editFoodModel)
            }
        }

        viewModel.saveFoodModelLD.observe(viewLifecycleOwner) { editFoodModel ->
            onEditNutritionFactsListener.onChanged(
                viewModel.getEditFoodRecordIndex(),
                editFoodModel
            )
            dismiss()
        }

        viewModel.servingQuantityEvent.observe(viewLifecycleOwner) { value ->
            setNutrientValue(binding.serving, value)
        }

        viewModel.servingUnitEvent.observe(viewLifecycleOwner) { value ->
            renderServingSize(value, viewModel.getFoodRecord())
        }

        viewModel.weightGramEvent.observe(viewLifecycleOwner) { value ->
            setNutrientValue(binding.weight, value)
        }
        viewModel.weightGramUnitEvent.observe(viewLifecycleOwner) { value ->

        }


//        viewModel.internalUpdate.observe(viewLifecycleOwner) { pair ->
//            updateFoodRecord(pair.first, pair.second)
//        }
    }

    private fun renderFoodRecord(model: ImageFoodResult) {
        renderNutrients(model)
        setupImmutableProperties(model)
//        renderServingSize(model)
    }

    private fun renderNutrients(model: ImageFoodResult) {
        val requiredNutritionFacts = viewModel.requiredNutritionFacts
        with(binding) {
            setNutrientValue(
                calories,
                requiredNutritionFacts.find { it.id == REF_CALORIES_ID }?.value
            )
            setNutrientValue(carbs, requiredNutritionFacts.find { it.id == REF_CARBS_ID }?.value)
            setNutrientValue(
                protein,
                requiredNutritionFacts.find { it.id == REF_PROTEIN_ID }?.value
            )
            setNutrientValue(fat, requiredNutritionFacts.find { it.id == REF_FAT_ID }?.value)
        }
    }

//    private fun updateFoodRecord(foodRecord: FoodRecord, origin: UpdateOrigin) {
////        renderNutrients(foodRecord)
//        renderServingSize(foodRecord, origin)
//        if (origin == UpdateOrigin.INGREDIENT) {
//            setupImmutableProperties(foodRecord)
//        }
//    }

    private fun setupImmutableProperties(imageFoodResult: ImageFoodResult) {
        if (_binding == null) return

        val foodRecord = imageFoodResult.record
        with(binding) {
            foodImage.loadFoodImage(foodRecord)
            foodName.setText(foodRecord.name.capitalized())
            barcode.text = foodRecord.barcode ?: ""
        }
    }

    private fun renderServingSize(selectedUnit: String, imageFoodResult: ImageFoodResult) {
        if (_binding == null) return

        val units = viewModel.unitList
        val indexOfSelected = units.indexOfFirst { it.lowercase() == selectedUnit.lowercase() }
        servingUnitAdapter =
            ArrayAdapter<String>(requireContext(), R.layout.serving_unit_item, units)
        servingUnitAdapter.setDropDownViewResource(R.layout.serving_unit_item)

        with(binding) {
            servingUnit.adapter = servingUnitAdapter
            servingUnit.onItemSelectedListener = servingUnitListener
            servingUnit.setSelection(indexOfSelected)
        }
    }

    private fun setNutrientValue(editText: AppCompatEditText, value: Double?) {
        value?.let {
            val updatedVal = it.singleDecimal()
            if (editText.text.toString() != updatedVal) {
                editText.setText(updatedVal)
            }
        } ?: {
            editText.setText("")
        }
    }

    private val servingUnitListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            viewModel.updateServingUnit(viewModel.unitList[position])
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

}