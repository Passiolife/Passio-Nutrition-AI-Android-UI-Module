package ai.passio.nutrition.uimodule.ui.image

import ai.passio.nutrition.uimodule.domain.customfood.CustomFoodUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.camera.NutritionFactsPhotoResultFragmentDirections
import ai.passio.nutrition.uimodule.ui.foodcreator.BarcodeResultType
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_CALORIES_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_CARBS_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_FAT_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_PROTEIN_ID
import ai.passio.nutrition.uimodule.ui.model.BarcodeScanResult
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.ImageFoodResult
import ai.passio.nutrition.uimodule.ui.model.clone
import ai.passio.nutrition.uimodule.ui.model.copyAsCustomFood
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.nutrition.uimodule.ui.util.StringKT.isGram
import ai.passio.nutrition.uimodule.ui.util.StringKT.isValid
import ai.passio.passiosdk.passiofood.data.measurement.Grams
import ai.passio.passiosdk.passiofood.data.measurement.KiloCalories
import ai.passio.passiosdk.passiofood.data.measurement.Milliliters
import ai.passio.passiosdk.passiofood.data.measurement.Ounce
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import ai.passio.passiosdk.passiofood.data.model.PassioIDEntityType
import ai.passio.passiosdk.passiofood.data.model.PassioNutrients
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class EditNutritionFactsViewModel : BaseViewModel() {
    private val customFoodUseCase = CustomFoodUseCase
    private val _editFoodModelLD = MutableLiveData<ImageFoodResult>()
    val editFoodModelLD: LiveData<ImageFoodResult> get() = _editFoodModelLD

    private val _saveFoodModelLD = SingleLiveEvent<ImageFoodResult>()
    val saveFoodModelLD: LiveData<ImageFoodResult> get() = _saveFoodModelLD

    private lateinit var imageFoodResult: ImageFoodResult

    private var editIngredientIndex = -1

    private val _servingQuantityEvent = MutableLiveData<Double>()
    val servingQuantityEvent: LiveData<Double> get() = _servingQuantityEvent
    private var servingQuantity: Double = 0.0

    private val _servingUnitEvent = MutableLiveData<String>()
    val servingUnitEvent: LiveData<String> get() = _servingUnitEvent
    private var servingUnit: String = Grams.symbol

    private val _weightGramEvent = MutableLiveData<Double>()
    val weightGramEvent: LiveData<Double> get() = _weightGramEvent
    private var weightGram: Double = 0.0

    private val _weightGramUnitEvent = MutableLiveData<String>()
    val weightGramUnitEvent: LiveData<String> get() = _weightGramUnitEvent
    private var weightGramUnit: String = Grams.symbol
    var isNewToCreate = true
    var isFromBarcodeScan = false

    private val _nutritionFactsValidatorEvent = MutableLiveData<NutritionFactsValidator>()
    val nutritionFactsValidatorEvent: LiveData<NutritionFactsValidator> get() = _nutritionFactsValidatorEvent

//    private val _foodNameEvent = MutableLiveData<String>()
//    val foodNameEvent: LiveData<String> get() = _foodNameEvent
//    private var foodName: String = ""

    val requiredNutritionFacts = mutableListOf<NutritionFactsItemNew>()

    val unitList = mutableListOf(
        "serving",
        "piece",
        "cup",
        Ounce.symbol,
        Grams.symbol,
        Milliliters.symbol,
        "handful",
        "scoop",
        "tbsp",
        "tsp",
        "slice",
        "can",
        "bottle",
        "bar",
        "packet"
    )

    init {
        val refCalories = NutritionFactsItemNew(
            id = REF_CALORIES_ID,
            nutrientName = "Calories",
            unitSymbol = KiloCalories.symbol,
            value = null,
            isAdded = false
        )
        val refFat = NutritionFactsItemNew(
            id = REF_FAT_ID,
            nutrientName = "Fat",
            unitSymbol = Grams.symbol,
            value = null,
            isAdded = false
        )
        val refCarbs = NutritionFactsItemNew(
            id = REF_CARBS_ID,
            nutrientName = "Carbs",
            unitSymbol = Grams.symbol,
            value = null,
            isAdded = false
        )
        val refProtein = NutritionFactsItemNew(
            id = REF_PROTEIN_ID,
            nutrientName = "Protein",
            unitSymbol = Grams.symbol,
            value = null,
            isAdded = false
        )

        requiredNutritionFacts.add(refCalories)
        requiredNutritionFacts.add(refFat)
        requiredNutritionFacts.add(refCarbs)
        requiredNutritionFacts.add(refProtein)
    }

    fun setFromScanningBarcode(isFromBarcodeScan: Boolean) {
        this.isFromBarcodeScan = isFromBarcodeScan
    }

    fun editFoodRecord(editFoodRecord: Pair<ImageFoodResult, Int>) {
        val previous = editFoodRecord.first
        this.imageFoodResult = ImageFoodResult(
            record = previous.record.clone(),
            isCustomFood = previous.isCustomFood,
            isSelected = previous.isSelected,
            resultType = previous.resultType
        )

//        this.imageFoodResult.record = this.imageFoodResult.record.clone()
//        this.foodRecord = this.imageFoodResult.record
        this.editIngredientIndex = editFoodRecord.second
        isNewToCreate = !imageFoodResult.isCustomFood

//        val foodRecord = imageFoodResult.record
        val ratio = 1.0
        val nutritionFacts = imageFoodResult.record.nutrientsSelectedSize()
        setCalories(nutritionFacts.calories()?.value?.div(ratio))
        setCarbs(nutritionFacts.carbs()?.value?.div(ratio))
        setProtein(nutritionFacts.protein()?.value?.div(ratio))
        setFat(nutritionFacts.fat()?.value?.div(ratio))

        servingQuantity = imageFoodResult.record.selectedQuantity
        val delta =
            unitList.find { it.lowercase() == imageFoodResult.record.selectedUnit.lowercase() }
        if (delta == null) {
            unitList.add(imageFoodResult.record.selectedUnit)
        }
        servingUnit = imageFoodResult.record.selectedUnit
        weightGram = imageFoodResult.record.nutrientsSelectedSize().weight.gramsValue()
        weightGramUnit = Grams.symbol

        viewModelScope.launch(Dispatchers.IO)
        {
            delay(400)
            updateNutrientsOnUI()
        }


        _editFoodModelLD.postValue(this.imageFoodResult)
    }

    fun updateNutrientsOnUI() {
        _servingQuantityEvent.postValue(servingQuantity)
        _weightGramEvent.postValue(weightGram)
        _servingUnitEvent.postValue(servingUnit)
        _weightGramUnitEvent.postValue(weightGramUnit)
        validateData()
    }

    fun validateData() {

        val carbs = requiredNutritionFacts.unitMassOf(REF_CARBS_ID)
        val calories = requiredNutritionFacts.unitEnergyOf(REF_CALORIES_ID)
        val proteins = requiredNutritionFacts.unitMassOf(REF_PROTEIN_ID)
        val fat = requiredNutritionFacts.unitMassOf(REF_FAT_ID)

        val isValidName = imageFoodResult.record.name.isValid()

        val isValidCalories = calories != null
        val isValidCarbs = carbs != null
        val isValidProteins = proteins != null
        val isValidFat = fat != null
        val isValidWeight = weightGram > 0.0
        val isValidServing = servingQuantity > 0.0
        val isValidServingUnit = servingUnit.isValid()

        val nutritionFactsValidator = NutritionFactsValidator(
            isValidName = isValidName,
            isValidCalories = isValidCalories,
            isValidCarbs = isValidCarbs,
            isValidProteins = isValidProteins,
            isValidFat = isValidFat,
            isValidWeight = isValidWeight,
            isValidServing = isValidServing,
            isValidServingUnit = isValidServingUnit,
        )
        _nutritionFactsValidatorEvent.postValue(nutritionFactsValidator)

    }

    fun updateServingQuantity(value: Double) {
        if (servingQuantity != value) {
            servingQuantity = value
            validateData()
//            _servingQuantityEvent.postValue(servingQuantity)
        }
//        foodRecord.setSelectedQuantity(value)
//        _internalUpdate.postValue(foodRecord to origin)
    }

    fun updateServingUnit(updatedUnitName: String) {
        if (servingUnit != updatedUnitName) {
            servingUnit = updatedUnitName
            validateData()
        }
//        _servingUnitEvent.postValue(servingUnit)
//        val unit = foodRecord.servingUnits[index].unitName
//        foodRecord.setSelectedUnit(unit)
//        _internalUpdate.postValue(foodRecord to origin)
    }

    fun setWeightInGram(weightInGram: Double) {
        if (weightGram != weightInGram) {
            weightGram = weightInGram
            validateData()
//            _weightGramEvent.postValue(weightGram)
        }

    }

    fun getEditFoodRecordIndex(): Int {
        return editIngredientIndex
    }

//    fun getFoodRecord(): ImageFoodResult {
//        return imageFoodResult
//    }
//    fun getFoodRecord(): FoodRecord {
//        return foodRecord
//    }

    fun setFoodName(name: String) {
//        imageFoodResult.record.name = name
        imageFoodResult.record.name = name
        validateData()
    }

    fun setCalories(calories: Double?) {
        requiredNutritionFacts.find { it.id == REF_CALORIES_ID }.apply {
            this?.value = calories
        }
        validateData()
//        if (foodRecord.nutrientsSelectedSize().calories()?.value != calories) {
//            setNutrient("refCalories", calories)
//        }
    }

    fun setFat(fat: Double?) {
        requiredNutritionFacts.find { it.id == REF_FAT_ID }.apply {
            this?.value = fat
        }
        validateData()
//        if (foodRecord.nutrientsSelectedSize().fat()?.value != fat) {
//            setNutrient("refFat", fat)
//        }
    }

    fun setProtein(protein: Double?) {
        requiredNutritionFacts.find { it.id == REF_PROTEIN_ID }.apply {
            this?.value = protein
        }
        validateData()
//        if (foodRecord.nutrientsSelectedSize().protein()?.value != protein) {
//            setNutrient("refProteins", protein)
//        }
    }

    fun setCarbs(carbs: Double?) {
        requiredNutritionFacts.find { it.id == REF_CARBS_ID }.apply {
            this?.value = carbs
        }
        validateData()
//        if (foodRecord.nutrientsSelectedSize().carbs()?.value != carbs) {
//            setNutrient("refCarbs", carbs)
//        }
    }

    fun updateMissingFromBarcode(barcodeScanResult: BarcodeScanResult) {


        if (barcodeScanResult.resultType == BarcodeResultType.CUSTOM_FOOD_ALREADY_EXIST) {
            imageFoodResult.isCustomFood =
                barcodeScanResult.resultType == BarcodeResultType.CUSTOM_FOOD_ALREADY_EXIST
        }
        else{
            barcodeScanResult.record?.id = imageFoodResult.record.id
            barcodeScanResult.record?.uuid = imageFoodResult.record.uuid
            barcodeScanResult.record?.refCode = imageFoodResult.record.refCode
        }
        barcodeScanResult.record?.let {
            imageFoodResult.record = it
        }
        imageFoodResult.record.barcode = barcodeScanResult.barcode

        editFoodRecord(imageFoodResult to getEditFoodRecordIndex())
    }

    fun updateMissingFromBarcodeOld(barcodeScanResult: BarcodeScanResult) {
//        imageFoodResult.record.barcode = barcode
        val record = barcodeScanResult.record
//        if (barcodeScanResult.barcode.isValid()) {
//            foodRecord.barcode = barcodeScanResult.barcode
//        }
        imageFoodResult.record.barcode = barcodeScanResult.barcode
        if (record != null) {
            if (!imageFoodResult.record.name.isValid() && record.name.isValid()) {
                imageFoodResult.record.name = record.name
            }

            if (record.nutrientsSelectedSize()
                    .calories() != null && requiredNutritionFacts.unitEnergyOf(REF_CALORIES_ID) == null
            ) {
                setCalories(record.nutrientsSelectedSize().calories()!!.value)
            }
            if (record.nutrientsSelectedSize()
                    .carbs() != null && requiredNutritionFacts.unitMassOf(REF_CARBS_ID) == null
            ) {
                setCarbs(record.nutrientsSelectedSize().carbs()!!.value)
            }
            if (record.nutrientsSelectedSize()
                    .protein() != null && requiredNutritionFacts.unitMassOf(REF_PROTEIN_ID) == null
            ) {
                setProtein(record.nutrientsSelectedSize().protein()!!.value)
            }
            if (record.nutrientsSelectedSize()
                    .fat() != null && requiredNutritionFacts.unitMassOf(REF_FAT_ID) == null
            ) {
                setFat(record.nutrientsSelectedSize().fat()!!.value)
            }

//            if (barcodeScanResult.resultType == BarcodeResultType.CUSTOM_FOOD_ALREADY_EXIST) {
//                isNewToCreate = false
//                foodRecord.id = record.id
//                foodRecord.uuid = record.uuid
//                foodRecord.refCode = record.refCode
//                foodRecord.iconId = record.iconId
//                imageFoodResult.isCustomFood = true
//            }

            updateNutrientsOnUI()
            _editFoodModelLD.postValue(imageFoodResult)
        }

    }

    fun saveCustomFood() {
        viewModelScope.launch(Dispatchers.IO)
        {
            var weightGramFinal = weightGram
            if (servingUnit.isGram()) {
                weightGramFinal = servingQuantity
            }

            val oldNutrients =
                PassioNutrients(
                    imageFoodResult.record.nutrientsReference(),
                    UnitMass(Grams, weightGramFinal)
                )
            val passioNutrients = PassioNutrients(
                weight = UnitMass(Grams, weightGramFinal),
                carbs = requiredNutritionFacts.unitMassOf(REF_CARBS_ID),
                calories = requiredNutritionFacts.unitEnergyOf(REF_CALORIES_ID),
                proteins = requiredNutritionFacts.unitMassOf(REF_PROTEIN_ID),
                fat = requiredNutritionFacts.unitMassOf(REF_FAT_ID),
                satFat = oldNutrients.satFat(),
                monounsaturatedFat = oldNutrients.monounsaturatedFat(),
                polyunsaturatedFat = oldNutrients.polyunsaturatedFat(),
                cholesterol = oldNutrients.cholesterol(),
                sodium = oldNutrients.sodium(),
                fibers = oldNutrients.fibers(),
                transFat = oldNutrients.transFat(),
                sugars = oldNutrients.sugars(),
                sugarsAdded = oldNutrients.sugarsAdded(),
                alcohol = oldNutrients.alcohol(),
                iron = oldNutrients.iron(),
                vitaminC = oldNutrients.vitaminC(),
                vitaminD = oldNutrients.vitaminD(),
                vitaminB6 = oldNutrients.vitaminB6(),
                vitaminB12 = oldNutrients.vitaminB12(),
                vitaminB12Added = oldNutrients.vitaminB12Added(),
                vitaminE = oldNutrients.vitaminE(),
                vitaminEAdded = oldNutrients.vitaminEAdded(),
                iodine = oldNutrients.iodine(),
                calcium = oldNutrients.calcium(),
                potassium = oldNutrients.potassium(),
                magnesium = oldNutrients.magnesium(),
                phosphorus = oldNutrients.phosphorus(),
                sugarAlcohol = oldNutrients.sugarAlcohol(),
                vitaminA = oldNutrients.vitaminA(),
                vitaminARAE = oldNutrients.vitaminARAE()
            )

            val customFood = if (imageFoodResult.isCustomFood) {
                imageFoodResult.record.editCustomFood(
                    productName = imageFoodResult.record.name,
                    brandName = imageFoodResult.record.details ?: "",
                    barcode = imageFoodResult.record.barcode,
                    servingWeight = servingQuantity,
                    servingUnit = servingUnit,
                    weightInGrams = weightGramFinal,
                    weightInGramsUnit = weightGramUnit,
                    passioNutrients = passioNutrients,
                    passioIDEntityType = PassioIDEntityType.fromString(imageFoodResult.record.entityType),
                    iconId = imageFoodResult.record.iconId
                )
            } else {
                FoodRecord(
                    productName = imageFoodResult.record.name,
                    brandName = imageFoodResult.record.details ?: "",
                    barcode = imageFoodResult.record.barcode,
                    servingWeight = servingQuantity,
                    servingUnit = servingUnit,
                    weightInGrams = weightGramFinal,
                    weightInGramsUnit = weightGramUnit,
                    passioNutrients = passioNutrients,
                    passioIDEntityType = PassioIDEntityType.fromString(imageFoodResult.record.entityType),
                    iconId = imageFoodResult.record.iconId
                ).copyAsCustomFood()
            }
            customFoodUseCase.saveCustomFood(customFood)
            imageFoodResult.record = customFood
            imageFoodResult.isCustomFood = true
            imageFoodResult.isSelected = true

            if (isFromBarcodeScan) {
                customFoodUseCase.logCustomFood(customFood)
            }

            _saveFoodModelLD.postValue(imageFoodResult)
        }
    }

    fun navigateToScanBarcode() {
        viewModelScope.launch(Dispatchers.Main) {
            if (isFromBarcodeScan) {
                navigate(NutritionFactsPhotoResultFragmentDirections.nutritionFactsToScanBarcode())
            } else {
                navigate(ImageFoodResultFragmentDirections.imageFoodResultToScanBarcode())
            }
        }
    }


}