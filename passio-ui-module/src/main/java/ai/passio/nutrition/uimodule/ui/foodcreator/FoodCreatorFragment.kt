package ai.passio.nutrition.uimodule.ui.foodcreator

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.FragmentFoodCreatorBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.clone
import ai.passio.nutrition.uimodule.ui.profile.GenericSpinnerAdapter
import ai.passio.nutrition.uimodule.ui.util.PhotoPickerListener
import ai.passio.nutrition.uimodule.ui.util.PhotoPickerManager
import ai.passio.nutrition.uimodule.ui.util.StringKT.isGram
import ai.passio.nutrition.uimodule.ui.util.ViewEXT.setupEditable
import ai.passio.nutrition.uimodule.ui.util.loadFoodImage
import ai.passio.nutrition.uimodule.ui.util.saveBitmapToStorage
import ai.passio.nutrition.uimodule.ui.util.toast
import ai.passio.nutrition.uimodule.ui.util.uriToBitmap
import ai.passio.passiosdk.passiofood.data.measurement.Grams
import ai.passio.passiosdk.passiofood.data.measurement.Milliliters
import ai.passio.passiosdk.passiofood.data.model.PassioServingSize
import android.net.Uri
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import coil.load
import coil.transform.CircleCropTransformation

class FoodCreatorFragment : BaseFragment<FoodCreatorViewModel>() {

    private var _binding: FragmentFoodCreatorBinding? = null
    private val binding: FragmentFoodCreatorBinding get() = _binding!!

    private val photoPickerManager = PhotoPickerManager()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodCreatorBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoPickerManager.init(this, photoPickerListener)
        initObserver()
        with(binding)
        {
            toolbar.setup(getString(R.string.food_creator), baseToolbarListener)
            toolbar.hideRightIcon()

            ivThumb.setOnClickListener {
                showAttachmentMenu(ivThumb)
            }
            lblThumb.setOnClickListener {
                showAttachmentMenu(ivThumb)
            }
            barcode.setOnClickListener {
                viewModel.navigateToScanBarcode()
            }
            save.setOnClickListener {
                viewModel.saveCustomFood()
            }
            delete.setOnClickListener {
                viewModel.deleteCustomFood()
            }
            cancel.setOnClickListener {
                viewModel.navigateBack()
            }

            name.setupEditable { txt ->
                viewModel.setProductName(txt)
            }
            brand.setupEditable { txt ->
                viewModel.setBrandName(txt)
            }
            servingSize.setupEditable { txt ->
                viewModel.setServingSize(txt.toDoubleOrNull() ?: 0.0)
            }
            weight.setupEditable { txt ->
                viewModel.setWeightGram(txt.toDoubleOrNull() ?: 0.0)
            }
        }
        setupWeightUnits(Grams.unitName)
        setupUnits(viewModel.unitList.first())
        setupRequiredNutritionFacts()
        setupOtherNutritionFacts()
    }

    private fun setupRequiredNutritionFacts() {
        with(binding) {
            val adapter =
                NutritionFactsItemAdapter(viewModel.requiredNutritionFacts, false) { item ->
                    item.isAdded = false
                }
            rvRequiredNutritionFacts.adapter = adapter
        }
    }

    private val selectNutrient = NutritionFactsItem(
        id = "selectNutrient",
        nutrientName = "Select Nutrient",
        unitSymbol = "",
        value = 0.0,
        isAdded = false
    )

    private fun setupOtherNutritionFacts() {
        with(binding)
        {
            rvOtherNutritionFacts.adapter =
                NutritionFactsItemAdapter(viewModel.otherNutritionFactsAdded, true) { item ->
                    item.isAdded = false
                    setupOtherNutritionFacts()
                }
            val items = mutableListOf(selectNutrient)
            items.addAll(viewModel.otherNutritionFactsNotAdded)
            spinnerNutritionFacts.isVisible = viewModel.otherNutritionFactsNotAdded.isNotEmpty()

            val adapter = GenericSpinnerAdapter(
                context = requireContext(),
                items = items
            ) { item ->
                item.nutrientName
            }
            spinnerNutritionFacts.adapter = adapter
            spinnerNutritionFacts.onItemSelectedListener =
                object : OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (position == 0 || parent == null)
                            return
                        val selectedItem =
                            parent.getItemAtPosition(position) as NutritionFactsItem
//                        viewModel.setServingUnit(selectedItem)
                        selectedItem.isAdded = true
                        setupOtherNutritionFacts()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Another interface callback
                    }
                }

            spinnerNutritionFacts.setSelection(0)
        }
    }

    private fun setupUnits(unit: String) {
        with(binding)
        {
            val items = viewModel.unitList
            if (units.adapter == null || units.adapter.count == 0) {
                val adapter = GenericSpinnerAdapter(
                    context = requireContext(),
                    items = items
                ) { item ->
                    item
                }
                units.adapter = adapter
                units.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (parent == null) return
                        val selectedItem = parent.getItemAtPosition(position) as String
                        viewModel.setServingUnit(selectedItem)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Another interface callback
                    }
                }
            }

            units.setSelection(items.indexOf(unit))
        }
    }

    private fun setupWeightUnits(unit: String) {
        with(binding)
        {
            val items = listOf(Grams.symbol, Milliliters.symbol)
            if (weightUnit.adapter == null || weightUnit.adapter.count == 0) {
                val adapter = GenericSpinnerAdapter(
                    context = requireContext(),
                    items = items
                ) { item ->
                    item
                }
                weightUnit.adapter = adapter
                weightUnit.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {

                        if (parent == null) return
                        val selectedItem = parent.getItemAtPosition(position) as String
                        viewModel.setWeightGramUnit(selectedItem)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }

                }

            }

            val index = items.indexOf(unit)
            weightUnit.setSelection(if (index < 0) 0 else index)
        }
    }

    private fun showPrefilledData(customFood: FoodRecord) {
        with(binding)
        {
            ivThumb.loadFoodImage(customFood)
            name.setText(customFood.name)
            brand.setText(customFood.additionalData)

            viewModel.setBarcode(customFood.barcode ?: "")


            var gramSize: PassioServingSize? = null
            if (customFood.servingSizes.size > 1) {
                gramSize = customFood.servingSizes.find { it.isGram() }
                gramSize?.let {
                    weight.setText(it.quantity.toString())
                    setupWeightUnits(it.unitName)
                }
            }

            val otherSize = customFood.servingSizes.find { it.unitName != gramSize?.unitName }
            otherSize?.let {
                servingSize.setText(it.quantity.toString())
                setupUnits(it.unitName)
                viewModel.setServingUnit(it.unitName)
            }
//            servingSize.setText(customFood.servingWeight().value.toString())
//            setupUnits(customFood.servingWeight().unit.symbol)
//            weight.setText(customFood.servingWeight().gramsValue().toString())
//            setupWeightUnits(customFood.servingWeight().unit.symbol)
            setupRequiredNutritionFacts()
            setupOtherNutritionFacts()
        }

    }

    private val baseToolbarListener = object : BaseToolbar.ToolbarListener {
        override fun onBack() {
            viewModel.navigateBack()
        }

        override fun onRightIconClicked() {
        }

    }

    private fun initObserver() {
        sharedViewModel.photoFoodResultLD.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                saveBitmapToStorage(requireContext(), it[0])?.let { path ->
                    viewModel.setPhotoPath(path)
                }
            }
        }
        sharedViewModel.nutritionFactsPair.observe(viewLifecycleOwner) {
            viewModel.setDataFromNutritionFacts(it)
        }
        sharedViewModel.editCustomFood.observe(viewLifecycleOwner) {
            viewModel.setDataToEdit(it)
        }

        sharedViewModel.editFoodUpdateLog.observe(viewLifecycleOwner) { editRecipe ->
            viewModel.setToUpdateLog(editRecipe.clone())
        }
        sharedViewModel.barcodeScanFoodRecord.observe(viewLifecycleOwner) { barcode ->
            viewModel.setBarcode(barcode)
        }
        viewModel.barcodeEvent.observe(viewLifecycleOwner) { barcode ->
            binding.barcode.text = barcode
        }
        viewModel.photoPathEvent.observe(viewLifecycleOwner) { photoPath ->
            binding.ivThumb.load(photoPath) {
                transformations(CircleCropTransformation())
            }
        }
        viewModel.servingUnitEvent.observe(viewLifecycleOwner) { servingUnit ->
            binding.weightGroup.isVisible =
                !(servingUnit.isGram())
            val index = viewModel.unitList.indexOfLast { it.lowercase() == servingUnit.lowercase() }
            if (binding.units.selectedItemPosition != index) {
                binding.units.setSelection(index)
            }
        }
        viewModel.showMessageEvent.observe(viewLifecycleOwner) { message ->
            requireContext().toast(message)
        }
        viewModel.showLoading.observe(viewLifecycleOwner) {
            binding.loading.isVisible = it
        }
        viewModel.prefillFoodData.observe(viewLifecycleOwner, ::showPrefilledData)
        viewModel.isEditCustomFood.observe(viewLifecycleOwner) { isEditCustomFood ->
            binding.delete.isVisible = isEditCustomFood

        }
    }

    private val photoPickerListener = object : PhotoPickerListener {
        override fun onImagePicked(uris: List<Uri>) {
            if (uris.isNotEmpty()) {
                val bitmap = uriToBitmap(requireContext(), uris[0])
                if (bitmap != null) {
                    saveBitmapToStorage(requireContext(), bitmap)?.let { path ->
                        viewModel.setPhotoPath(path)
                    }
                }
            }
        }
    }

    private fun showAttachmentMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.advisor_menu, popupMenu.menu)
        showMenuIcons(popupMenu)

        // Optional: Handle menu item clicks if needed
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.capture_photo -> {
                    viewModel.navigateToTakePhoto()
                    true
                }

                R.id.select_photo -> {
                    photoPickerManager.pickSingleImage()
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

}