package ai.passio.nutrition.uimodule.ui.editrecipe

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.databinding.FragmentEditRecipeBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.edit.EditFoodFragment.UpdateOrigin
import ai.passio.nutrition.uimodule.ui.edit.IngredientAdapter
import ai.passio.nutrition.uimodule.ui.menu.AddFoodOption
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
import ai.passio.nutrition.uimodule.ui.model.clone
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import ai.passio.nutrition.uimodule.ui.util.PhotoPickerListener
import ai.passio.nutrition.uimodule.ui.util.PhotoPickerManager
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.StringKT.isValid
import ai.passio.nutrition.uimodule.ui.util.StringKT.singleDecimal
import ai.passio.nutrition.uimodule.ui.util.ViewEXT.setupEditable
import ai.passio.nutrition.uimodule.ui.util.loadFoodImage
import ai.passio.nutrition.uimodule.ui.util.loadPassioIcon
import ai.passio.nutrition.uimodule.ui.util.saveBitmapToStorage
import ai.passio.nutrition.uimodule.ui.util.toast
import ai.passio.nutrition.uimodule.ui.util.uriToBitmap
import ai.passio.passiosdk.passiofood.data.model.PassioIDEntityType
import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import coil.load
import coil.transform.CircleCropTransformation
import com.warkiz.tickseekbar.OnSeekChangeListener
import com.warkiz.tickseekbar.SeekParams
import com.warkiz.tickseekbar.TickSeekBar
import com.yanzhenjie.recyclerview.SwipeMenuItem

class EditRecipeFragment : BaseFragment<EditRecipesViewModel>() {

    private var _binding: FragmentEditRecipeBinding? = null
    private val binding: FragmentEditRecipeBinding get() = _binding!!

    private val photoPickerManager = PhotoPickerManager()
    private val ingredientAdapter = IngredientAdapter(::onIngredientSelected)
    private lateinit var servingUnitAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditRecipeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoPickerManager.init(this, photoPickerListener)
        initObserver()
        with(binding)
        {
            ivThumb.loadPassioIcon("", PassioIDEntityType.recipe)
            toolbar.setup(getString(R.string.edit_recipe), baseToolbarListener)
            toolbar.hideRightIcon()
            ivThumb.setOnClickListener {
                showAttachmentMenu(ivThumb)
            }
            lblThumb.setOnClickListener {
                showAttachmentMenu(ivThumb)
            }

            save.setOnClickListener {
                viewModel.saveRecipe()
            }
            delete.setOnClickListener {
                viewModel.deleteRecipe()
            }
            cancel.setOnClickListener {
                viewModel.navigateBack()
            }

            name.setupEditable { txt ->
                viewModel.setRecipeName(txt)
            }
            addIngredientLabel.setOnClickListener {
                PickIngredientMenuDialog(onPickIngredientOption).show(
                    childFragmentManager,
                    "PickIngredientMenuDialog"
                )
            }

            servingQuantity.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    try {
                        val quantity = v.text.toString().toDouble()
                        viewModel.updateServingQuantity(quantity, UpdateOrigin.QUANTITY)
                        servingQuantity.clearFocus()
                    } catch (e: NumberFormatException) {
                        return@setOnEditorActionListener true
                    }
                    return@setOnEditorActionListener false
                }
                true
            }
            servingQuantitySeekBar.onSeekChangeListener = seekChangeListener

            setupIngredients()

            viewModel.showPrefilledData()
        }

    }

    private val onPickIngredientOption = object : OnPickIngredientOption {
        override fun onPickIngredient(addFoodOption: AddFoodOption) {

            /*when (id) {
            0 -> viewModel.navigate(AddFoodFragmentDirections.addFoodToCamera())
            1 -> viewModel.navigate(AddFoodFragmentDirections.addFoodToSearch())
            2 -> viewModel.navigate(AddFoodFragmentDirections.addFoodToPhoto())
            3 -> viewModel.navigate(AddFoodFragmentDirections.addFoodToAdvisor())
            4 -> viewModel.navigate(AddFoodFragmentDirections.addFoodToVoiceLogging())
            6 -> viewModel.navigate(AddFoodFragmentDirections.addFoodToMyFoods())
        }*/

            when (addFoodOption.id) {
                //food scanner
                0 -> {
                    sharedViewModel.setIsAddIngredientFromSearch(isAddIngredient = true)
                    viewModel.navigateToCameraScanning()
                }
                //food search
                1 -> {
                    sharedViewModel.setIsAddIngredientFromSearch(isAddIngredient = true)
                    viewModel.navigateToSearch()
                }
                //voice logging
                4 -> {
                    sharedViewModel.setIsAddIngredientUsingVoice(isAddIngredient = true)
                    viewModel.navigateToVoice()
                }
            }

        }

    }

    private fun onIngredientSelected(adapterPosition: Int) {
        val ingredient = viewModel.getIngredient(adapterPosition)
        sharedViewModel.editIngredient(ingredient, adapterPosition)
        viewModel.navigateToEditIngredient()
    }

    private fun setupIngredients() {
        with(binding)
        {
            ingredientList.adapter = null
            ingredientList.setSwipeMenuCreator { leftMenu, rightMenu, position ->
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
            ingredientList.setOnItemMenuClickListener { menuBridge, adapterPosition ->
                menuBridge.closeMenu()
                when (menuBridge.position) {
                    0 -> {
                        val ingredient = viewModel.getIngredient(adapterPosition)
                        sharedViewModel.editIngredient(ingredient, adapterPosition)
                        viewModel.navigateToEditIngredient()
                    }

                    1 -> {
                        viewModel.removeIngredient(adapterPosition)
                    }
                }
            }
            ingredientList.adapter = ingredientAdapter
        }
    }

    private fun initObserver() {
        viewModel.showLoading.observe(viewLifecycleOwner) {
            binding.loading.isVisible = it
        }
        sharedViewModel.photoFoodResultLD.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                saveBitmapToStorage(requireContext(), it[0])?.let { path ->
                    viewModel.setPhotoPath(path)
                }
            }
        }
        sharedViewModel.editRecipe.observe(viewLifecycleOwner) { editRecipe ->
            viewModel.setRecipeToEditOrCreateNew(editRecipe.clone())
        }
        sharedViewModel.editRecipeUpdateLog.observe(viewLifecycleOwner) { editRecipe ->
            viewModel.setToUpdateLog(editRecipe.clone())
        }
        sharedViewModel.addFoodIngredientLD.observe(viewLifecycleOwner, ::addFoodIngredient)
        sharedViewModel.addFoodIngredientsLD.observe(viewLifecycleOwner, ::addFoodIngredients)
        sharedViewModel.addMultipleIngredientsLD.observe(viewLifecycleOwner, ::addFoodIngredients)
        sharedViewModel.editIngredientToRecipeLD.observe(
            viewLifecycleOwner,
            ::editDeleteFoodIngredients
        )
        viewModel.photoPathEvent.observe(viewLifecycleOwner) { photoPath ->
            if (photoPath.isValid()) {
                binding.ivThumb.load(photoPath) {
                    transformations(CircleCropTransformation())
                }
            } else {
                binding.ivThumb.loadPassioIcon("", PassioIDEntityType.recipe)
            }
        }
        viewModel.internalUpdate.observe(viewLifecycleOwner) { pair ->
            updateFoodRecord(pair.first, pair.second)
        }
        viewModel.saveRecipeEvent.observe(viewLifecycleOwner, ::recipeSaved)
        viewModel.showMessageEvent.observe(viewLifecycleOwner, ::showMessage)
    }


    private fun showMessage(message: String) {
        requireContext().toast(message)
    }

    private fun recipeSaved(resultWrapper: ResultWrapper<Boolean>) {
        when (resultWrapper) {
            is ResultWrapper.Success -> {
                if (resultWrapper.value) {
                    requireContext().toast("Recipe saved successfully.")
                    viewModel.navigateOnSave()
                } else {
                    requireContext().toast("Could not save recipe, please try again.")
                }
            }

            is ResultWrapper.Error -> {
                requireContext().toast(resultWrapper.error)
            }
        }
    }

    private fun updateFoodRecord(foodRecord: FoodRecord, origin: UpdateOrigin) {
//        renderNutrients(foodRecord)
        binding.delete.isVisible = viewModel.isEditRecipe()
        renderServingSize(foodRecord, origin)
        if (origin == UpdateOrigin.INGREDIENT) {
            setupImmutableProperties(foodRecord)
//            renderIngredients(foodRecord)
        }
        renderIngredients(foodRecord)
    }

    private fun renderIngredients(foodRecord: FoodRecord) {
        /*if (foodRecord.ingredients.size == 1) {
            ingredientAdapter.updateIngredients(emptyList())
            return
        }*/

        ingredientAdapter.updateIngredients(foodRecord.ingredients)
    }

    @SuppressLint("SetTextI18n")
    private fun renderServingSize(foodRecord: FoodRecord, origin: UpdateOrigin? = null) {
        if (_binding == null) return

        with(binding) {
            val weightGrams = foodRecord.servingWeight().gramsValue().singleDecimal()
            servingSizeValue.text = " $weightGrams g"

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

    private fun setupImmutableProperties(foodRecord: FoodRecord) {
        if (_binding == null) return

        val units = foodRecord.servingUnits.map { it.unitName.capitalized() }
        val indexOfSelected = units.indexOfFirst { it.lowercase() == foodRecord.getSelectedUnit() }
        servingUnitAdapter =
            ArrayAdapter<String>(requireContext(), R.layout.serving_unit_item, units)
        servingUnitAdapter.setDropDownViewResource(R.layout.serving_unit_item)

        with(binding) {
            ivThumb.loadFoodImage(foodRecord)
            name.setText(foodRecord.name.capitalized())
//            if (!foodRecord.name.equals(foodRecord.additionalData, true)) {
//                infoName.text = foodRecord.additionalData.capitalized()
//            }

            servingUnit.adapter = servingUnitAdapter
            servingUnit.onItemSelectedListener = servingUnitListener
            servingUnit.setSelection(indexOfSelected)
        }
    }

    private val servingUnitListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            viewModel.updateServingUnit(position, UpdateOrigin.UNIT)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
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

    private fun addFoodIngredient(foodRecordIngredient: FoodRecordIngredient) {
        viewModel.addIngredient(foodRecordIngredient)
    }

    private fun addFoodIngredients(foodRecord: FoodRecord) {
        viewModel.addIngredients(foodRecord)
    }
    private fun addFoodIngredients(ingredients: List<FoodRecordIngredient>) {
        viewModel.addIngredients(ingredients)
    }

    private fun editDeleteFoodIngredients(ingredient: Pair<FoodRecordIngredient?, Int>) {
        viewModel.editIngredient(ingredient)
    }

    private val baseToolbarListener = object : BaseToolbar.ToolbarListener {
        override fun onBack() {
            viewModel.navigateBack()
        }

        override fun onRightIconClicked() {
        }

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