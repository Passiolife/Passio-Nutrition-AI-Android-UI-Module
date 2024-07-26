package ai.passio.nutrition.uimodule.ui.edit

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.databinding.FragmentEditFoodBinding
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.MealLabel
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import ai.passio.nutrition.uimodule.ui.util.RoundedSlicesPieChartRenderer
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.loadPassioIcon
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.datepicker.MaterialDatePicker
import com.warkiz.tickseekbar.OnSeekChangeListener
import com.warkiz.tickseekbar.SeekParams
import com.warkiz.tickseekbar.TickSeekBar
import com.yanzhenjie.recyclerview.SwipeMenuItem
import java.text.DecimalFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import kotlin.math.roundToInt

class EditFoodFragment : BaseFragment<EditFoodViewModel>() {

    private var _binding: FragmentEditFoodBinding? = null
    private val binding: FragmentEditFoodBinding get() = _binding!!

    private var carbColor: Int = -1
    private var proteinColor: Int = -1
    private var fatColor: Int = -1
    private val decimalFormat = DecimalFormat("0.#")

    // private val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
    private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
    private lateinit var servingUnitAdapter: ArrayAdapter<String>
    private val ingredientAdapter = IngredientAdapter(::onIngredientSelected)

    enum class UpdateOrigin {
        QUANTITY,
        UNIT,
        SEEKBAR,
        INGREDIENT,
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditFoodBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getBoolean("isEdit", false)?.let {
            viewModel.setEditMode(it)
            if (it) {
                binding.log.text = requireContext().getString(R.string.save)
            }
        }

        carbColor = ContextCompat.getColor(requireContext(), R.color.passio_carbs)
        proteinColor = ContextCompat.getColor(requireContext(), R.color.passio_protein)
        fatColor = ContextCompat.getColor(requireContext(), R.color.passio_fat)
        setupToolbar()
        setupChart()

        with(binding) {
            servingQuantity.setSingleLine()
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
                    }

                    1 -> {
                        viewModel.removeIngredient(adapterPosition)
                    }
                }
            }

            ingredientList.adapter = ingredientAdapter

            cancel.setOnClickListener {
                viewModel.navigateBack()
            }

            log.setOnClickListener {
                viewModel.logCurrentRecord()
            }

            addIngredientLabel.setOnClickListener {
                val fr = viewModel.navigateToAddIngredient()
                sharedViewModel.addIngredient(fr)
            }

            openFoodFacts.setOnClickListener {
                OpenFoodFactsDialog(openFoodFactsListener).show(childFragmentManager, "EditFood")
            }

            moreDetails.setOnClickListener {
                val foodRecord = viewModel.navigateToNutritionInfo()
                sharedViewModel.passToNutritionInfo(foodRecord)

            }
        }

        sharedViewModel.editFoodRecordLD.observe(viewLifecycleOwner) { foodRecord ->
            viewModel.setFoodRecord(foodRecord)
        }

        sharedViewModel.editSearchResultLD.observe(viewLifecycleOwner) { searchResult ->
            viewModel.getFoodRecord(searchResult)
        }

        sharedViewModel.editIngredientLD.observe(viewLifecycleOwner) { result ->
            viewModel.getFoodRecordForIngredient(result.first, result.second)
        }

        viewModel.editFoodModelLD.observe(viewLifecycleOwner) { editFoodModel ->
            if (editFoodModel.foodRecord == null) {
                renderError()
            } else {
                binding.openFoodFacts.isVisible = !editFoodModel.foodRecord.openFoodLicense.isNullOrEmpty()
                renderFoodRecord(editFoodModel)
            }
        }

        viewModel.internalUpdate.observe(viewLifecycleOwner) { pair ->
            updateFoodRecord(pair.first, pair.second)
        }

        viewModel.resultLogFood.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ResultWrapper.Error -> {
                    Toast.makeText(requireContext(), result.error, Toast.LENGTH_SHORT).show()
                }
                is ResultWrapper.Success -> {
                    viewModel.navigateToDiary(result.value.createdAtTime())
                }
            }
        }
    }

    private val openFoodFactsListener = object : OpenFoodFactsDialog.OpenFoodFactsListener{
        override fun onOpenFoodFactsClicked() {

        }

        override fun onOpenDatabaseLicenseClicked() {

        }

    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setup(getString(R.string.edit), toolbarListener)
            setRightIcon(R.drawable.icon_switch)
        }
    }

    private val toolbarListener = object : BaseToolbar.ToolbarListener {
        override fun onBack() {
            viewModel.navigateBack()
        }

        override fun onRightIconClicked() {

        }

    }

    private val servingUnitListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            viewModel.updateServingUnit(position, UpdateOrigin.UNIT)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    private fun setupChart() {
        with(binding.macrosChart) {
            renderer = RoundedSlicesPieChartRenderer(this, animator, viewPortHandler)
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

    private fun renderError() {
        Toast.makeText(requireContext(), "Could not fetch nutrition data", Toast.LENGTH_LONG).show()
    }

    private fun setupImmutableProperties(foodRecord: FoodRecord) {
        if (_binding == null) return

        val units = foodRecord.servingUnits.map { it.unitName.capitalized() }
        val indexOfSelected = units.indexOfFirst { it.lowercase() == foodRecord.getSelectedUnit() }
        servingUnitAdapter =
            ArrayAdapter<String>(requireContext(), R.layout.serving_unit_item, units)
        servingUnitAdapter.setDropDownViewResource(R.layout.serving_unit_item)

        with(binding) {
            foodImage.loadPassioIcon(foodRecord.iconId)
            foodName.text = foodRecord.name.capitalized()
            infoName.text = foodRecord.additionalData.capitalized()

            servingUnit.adapter = servingUnitAdapter
            servingUnit.onItemSelectedListener = servingUnitListener
            servingUnit.setSelection(indexOfSelected)
        }
    }

    private fun renderFoodRecord(model: EditFoodModel) {
        setupImmutableProperties(model.foodRecord!!)
        renderNutrients(model.foodRecord)
        renderServingSize(model.foodRecord)
        if (model.showIngredients) {
            renderMealTimeAndDate(model.foodRecord)
            renderIngredients(model.foodRecord)
        } else {
            hideSecondaryViews()
        }
    }

    private fun hideSecondaryViews() {
        if (_binding == null) return
        with(binding) {
            mealTimeLayout.visibility = View.GONE
            dateLayout.visibility = View.GONE
            addIngredientLayout.visibility = View.GONE
        }
    }

    private fun updateFoodRecord(foodRecord: FoodRecord, origin: UpdateOrigin) {
        renderNutrients(foodRecord)
        renderServingSize(foodRecord, origin)
        if (origin == UpdateOrigin.INGREDIENT) {
            setupImmutableProperties(foodRecord)
            renderIngredients(foodRecord)
        }
    }

    private fun renderNutrients(foodRecord: FoodRecord) {
        if (_binding == null) return

        with(binding) {
            val nutrients = foodRecord.nutrientsSelectedSize()
            val calories = nutrients.calories()?.value ?: 0.0
            val carbs = nutrients.carbs()?.value ?: 0.0
            val protein = nutrients.protein()?.value ?: 0.0
            val fat = nutrients.fat()?.value ?: 0.0

            val sum = 4 * carbs + 4 * protein + 9 * fat
            var carbPercent = if (sum != 0.0) {
                (((4 * carbs) / sum) * 100).roundToInt()
            } else {
                0
            }
            val proteinPercent = if (sum != 0.0) {
                (((4 * protein) / sum) * 100).roundToInt()
            } else {
                0
            }
            val fatPercent = if (sum != 0.0) {
                (((9 * fat) / sum) * 100).roundToInt()
            } else {
                0
            }
            if (carbPercent + proteinPercent + fatPercent == 99) {
                carbPercent += 1
            } else if (carbPercent + proteinPercent + fatPercent == 101) {
                carbPercent -= 1
            }

            val carbGrams = "${decimalFormat.format(carbs)} g"
            val proteinGrams = "${decimalFormat.format(protein)} g"
            val fatGrams = "${decimalFormat.format(fat)} g"

            val carbString = SpannableString(" $carbGrams ($carbPercent%)")
            carbString.setSpan(
                ForegroundColorSpan(carbColor),
                0,
                carbGrams.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            carbsValue.text = carbString

            val proteinString = SpannableString(" $proteinGrams ($proteinPercent%)")
            proteinString.setSpan(
                ForegroundColorSpan(proteinColor),
                0,
                proteinGrams.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            proteinValue.text = proteinString

            val fatString = SpannableString(" $fatGrams ($fatPercent%)")
            fatString.setSpan(
                ForegroundColorSpan(fatColor),
                0,
                fatGrams.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            fatValue.text = fatString

            renderChart(carbPercent, proteinPercent, fatPercent)

            caloriesValue.text = calories.roundToInt().toString()
        }
    }

    private fun renderServingSize(foodRecord: FoodRecord, origin: UpdateOrigin? = null) {
        if (_binding == null) return

        with(binding) {
            val weightGrams = decimalFormat.format(foodRecord.servingWeight().gramsValue())
            servingSizeValue.text = " $weightGrams g"

            if (origin != UpdateOrigin.QUANTITY) {
                if (foodRecord.getSelectedQuantity() == FoodRecord.ZERO_QUANTITY) {
                    servingQuantity.setText("0")
                } else {
                    val quantity = decimalFormat.format(foodRecord.getSelectedQuantity())
                    servingQuantity.setText(quantity)
                }
            }
            if (origin != UpdateOrigin.SEEKBAR) {
                updateSlider(foodRecord.getSelectedQuantity())
            }
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

    private fun renderChart(carbPercent: Int, proteinPercent: Int, fatPercent: Int) {
        val entries = mutableListOf<PieEntry>()
        entries.add(PieEntry(carbPercent.toFloat(), "carbs"))
        entries.add(PieEntry(proteinPercent.toFloat(), "protein"))
        entries.add(PieEntry(fatPercent.toFloat(), "fat"))

        val dataSet = PieDataSet(entries, "Macros")
        val colors = listOf(carbColor, proteinColor, fatColor)
        dataSet.colors = colors
        dataSet.valueTextSize = 0f
        dataSet.sliceSpace = 8f

        val data = PieData(dataSet)
        binding.macrosChart.data = data
        binding.macrosChart.invalidate()
    }

    private fun renderMealTimeAndDate(foodRecord: FoodRecord) {
        if (_binding == null) return
        val mealLabel =
            foodRecord.mealLabel ?: MealLabel.dateToMealLabel(System.currentTimeMillis())
        viewModel.updateMealLabel(mealLabel)
        binding.mealTimePicker.setup(mealLabel, object : MealTimePicker.MealTimeListener {
            override fun onValueChanged(mealLabel: MealLabel) {
                viewModel.updateMealLabel(mealLabel)
            }
        })
        val creationDate = foodRecord.createdAtTime() ?: System.currentTimeMillis()
        val date = Date(creationDate)
        //        binding.date.text = dateFormat.format(date)
        val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        binding.date.text = localDate.format(dateFormatter)
        binding.date.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_meal_date))
                .setSelection(creationDate)
                .build()
            datePicker.addOnPositiveButtonClickListener { dateTime ->
                val newDate = Date(dateTime)
                val newLocalDate = newDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                binding.date.text = newLocalDate.format(dateFormatter)
                viewModel.updateCreatedAt(dateTime)
            }
            datePicker.show(requireActivity().supportFragmentManager, "DATE")
        }
    }

    private fun renderIngredients(foodRecord: FoodRecord) {
        if (foodRecord.ingredients.size == 1) {
            ingredientAdapter.updateIngredients(emptyList())
            return
        }

        ingredientAdapter.updateIngredients(foodRecord.ingredients)
    }

    private fun onIngredientSelected(index: Int) {
        val ingredient = viewModel.getIngredient(index)
        sharedViewModel.editIngredient(ingredient, index)
    }
}