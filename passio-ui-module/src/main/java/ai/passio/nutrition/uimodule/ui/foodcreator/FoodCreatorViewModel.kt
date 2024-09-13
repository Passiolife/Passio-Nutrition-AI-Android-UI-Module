package ai.passio.nutrition.uimodule.ui.foodcreator

import ai.passio.nutrition.uimodule.domain.customfood.CustomFoodUseCase
import ai.passio.nutrition.uimodule.domain.search.EditFoodUseCase
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_CALCIUM_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_CALORIES_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_CARBS_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_CHOLESTEROL_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_FAT_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_FIBERS_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_IRON_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_MAGNESIUM_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_POTASSIUM_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_PROTEIN_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_SAT_FAT_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_SODIUM_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_SUGARS_ADDED_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_SUGARS_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_TRANS_FAT_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_VITAMIN_D_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.setValue
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.unitEnergyOf
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.unitMassOf
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.copyAsCustomFood
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.nutrition.uimodule.ui.util.StringKT.isValid
import ai.passio.passiosdk.passiofood.data.measurement.Grams
import ai.passio.passiosdk.passiofood.data.measurement.KiloCalories
import ai.passio.passiosdk.passiofood.data.measurement.Micrograms
import ai.passio.passiosdk.passiofood.data.measurement.Milligrams
import ai.passio.passiosdk.passiofood.data.measurement.Milliliters
import ai.passio.passiosdk.passiofood.data.measurement.Ounce
import ai.passio.passiosdk.passiofood.data.measurement.UnitMass
import ai.passio.passiosdk.passiofood.data.model.PassioIDEntityType
import ai.passio.passiosdk.passiofood.data.model.PassioNutrients
import ai.passio.passiosdk.passiofood.nutritionfacts.PassioNutritionFacts
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FoodCreatorViewModel : BaseViewModel() {

    private val useCase = CustomFoodUseCase
    private val editFoodUseCase = EditFoodUseCase
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
    private var passioIDEntityType: PassioIDEntityType = PassioIDEntityType.item
    private var productName: String = ""
    private var brandName: String = ""
    private var servingQuantity: Double = 0.0
    private var servingUnit: String = unitList.first()
    private var weightGram: Double = 0.0
    private var weightGramUnit: String = Grams.symbol //grams or milliliters
    private var barcode: String = ""
    val barcodeEvent = MutableLiveData<String>()
    val servingUnitEvent = MutableLiveData<String>()
    private val _showMessageEvent = SingleLiveEvent<String>()
    val showMessageEvent: LiveData<String> = _showMessageEvent
    private val _showLoading = SingleLiveEvent<Boolean>()
    val showLoading: LiveData<Boolean> get() = _showLoading


    val requiredNutritionFacts = mutableListOf<NutritionFactsItem>()
    private val otherNutritionFacts = mutableListOf<NutritionFactsItem>()
    val otherNutritionFactsAdded get() = otherNutritionFacts.filter { it.isAdded }
    val otherNutritionFactsNotAdded get() = otherNutritionFacts.filter { !it.isAdded }

    private var customFoodRecord: FoodRecord? = null
    private val _isEditCustomFood = SingleLiveEvent<Boolean>()
    val isEditCustomFood: LiveData<Boolean> = _isEditCustomFood
    private val _prefillFoodData = SingleLiveEvent<FoodRecord>()
    val prefillFoodData: LiveData<FoodRecord> = _prefillFoodData


    private var photoPath: String? = null
    private val _photoPathEvent = MutableLiveData<String>()
    val photoPathEvent: LiveData<String> = _photoPathEvent

    private var loggedRecord: FoodRecord? = null

    init {

        val refCalories = NutritionFactsItem(
            id = REF_CALORIES_ID,
            nutrientName = "Calories",
            unitSymbol = KiloCalories.symbol,
            value = 0.0,
            isAdded = false
        )
        val refFat = NutritionFactsItem(
            id = REF_FAT_ID,
            nutrientName = "Fat",
            unitSymbol = Grams.symbol,
            value = 0.0,
            isAdded = false
        )
        val refCarbs = NutritionFactsItem(
            id = REF_CARBS_ID,
            nutrientName = "Carbs",
            unitSymbol = Grams.symbol,
            value = 0.0,
            isAdded = false
        )
        val refProtein = NutritionFactsItem(
            id = REF_PROTEIN_ID,
            nutrientName = "Protein",
            unitSymbol = Grams.symbol,
            value = 0.0,
            isAdded = false
        )

        requiredNutritionFacts.add(refCalories)
        requiredNutritionFacts.add(refFat)
        requiredNutritionFacts.add(refCarbs)
        requiredNutritionFacts.add(refProtein)

        val refSatFat = NutritionFactsItem(
            id = REF_SAT_FAT_ID,
            nutrientName = "Saturated Fat",
            unitSymbol = Grams.symbol,
            value = 0.0,
            isAdded = false
        )
        otherNutritionFacts.add(refSatFat)

        val refTransFat = NutritionFactsItem(
            id = REF_TRANS_FAT_ID,
            nutrientName = "Trans Fat",
            unitSymbol = Grams.symbol,
            value = 0.0,
            isAdded = false
        )
        otherNutritionFacts.add(refTransFat)

        val refCholesterol = NutritionFactsItem(
            id = REF_CHOLESTEROL_ID,
            nutrientName = "Cholesterol",
            unitSymbol = Milligrams.symbol,
            value = 0.0,
            isAdded = false
        )
        otherNutritionFacts.add(refCholesterol)

        val refSodium = NutritionFactsItem(
            id = REF_SODIUM_ID,
            nutrientName = "Sodium",
            unitSymbol = Milligrams.symbol,
            value = 0.0,
            isAdded = false
        )
        otherNutritionFacts.add(refSodium)

        val refFibers = NutritionFactsItem(
            id = REF_FIBERS_ID,
            nutrientName = "Dietary Fiber",
            unitSymbol = Grams.symbol,
            value = 0.0,
            isAdded = false
        )
        otherNutritionFacts.add(refFibers)

        val refSugars = NutritionFactsItem(
            id = REF_SUGARS_ID,
            nutrientName = "Total Sugars",
            unitSymbol = Grams.symbol,
            value = 0.0,
            isAdded = false
        )
        otherNutritionFacts.add(refSugars)

        val refSugarsAdded = NutritionFactsItem(
            id = REF_SUGARS_ADDED_ID,
            nutrientName = "Added Sugar",
            unitSymbol = Grams.symbol,
            value = 0.0,
            isAdded = false
        )
        otherNutritionFacts.add(refSugarsAdded)

        val refVitaminD = NutritionFactsItem(
            id = REF_VITAMIN_D_ID,
            nutrientName = "Vitamin D",
            unitSymbol = Micrograms.symbol,
            value = 0.0,
            isAdded = false
        )
        otherNutritionFacts.add(refVitaminD)

        val refCalcium = NutritionFactsItem(
            id = REF_CALCIUM_ID,
            nutrientName = "Calcium",
            unitSymbol = Milligrams.symbol,
            value = 0.0,
            isAdded = false
        )
        otherNutritionFacts.add(refCalcium)

        val refIron = NutritionFactsItem(
            id = REF_IRON_ID,
            nutrientName = "Iron",
            unitSymbol = Milligrams.symbol,
            value = 0.0,
            isAdded = false
        )
        otherNutritionFacts.add(refIron)

        val refPotassium = NutritionFactsItem(
            id = REF_POTASSIUM_ID,
            nutrientName = "Potassium",
            unitSymbol = Milligrams.symbol,
            value = 0.0,
            isAdded = false
        )
        otherNutritionFacts.add(refPotassium)
    }


    fun setPhotoPath(path: String) {
        this.photoPath = path
        _photoPathEvent.postValue(path)
    }

    fun setToUpdateLog(loggedRecord: FoodRecord) {
        this.loggedRecord = loggedRecord
    }

    fun setDataToEdit(foodRecord: FoodRecord) {
//        val nutritionFacts = nutritionFactsPair.first
        this.passioIDEntityType = PassioIDEntityType.fromString(foodRecord.passioIDEntityType)

//        Log.d("nutritionFacts====", Gson().toJson(nutritionFacts))
//        productName = nutritionFactsPair.second

//        foodRecord.servingSizes
//        setWeightGram(foodRecord.weightInGrams))
//        nutritionFacts.servingSize?.let { servingSize ->
//            val pair = splitServingSize(servingSize)
//            setServingSize(pair.first)
//            setServingUnit(pair.second)
//        }
//        nutritionFacts.servingSizeQuantity?.let {
//            setWeightGram(it)
//        }

//        nutritionFacts.servingSizeQuantity
//        nutritionFacts.servingSizeUnitName
//        nutritionFacts.servingSize
//        nutritionFacts.sugarAlcohol

        foodRecord.foodImagePath?.let {
            setPhotoPath(it)
        }

        val nutritionFacts = foodRecord.nutrientsReference()
        requiredNutritionFacts.setValue(REF_CARBS_ID, nutritionFacts.carbs()?.value ?: 0.0)
        requiredNutritionFacts.setValue(REF_CALORIES_ID, nutritionFacts.calories()?.value ?: 0.0)
        requiredNutritionFacts.setValue(REF_PROTEIN_ID, nutritionFacts.protein()?.value ?: 0.0)
        requiredNutritionFacts.setValue(REF_FAT_ID, nutritionFacts.fat()?.value ?: 0.0)

        otherNutritionFacts.setValue(REF_SAT_FAT_ID, nutritionFacts.satFat()?.value ?: 0.0)
        otherNutritionFacts.setValue(REF_CHOLESTEROL_ID, nutritionFacts.cholesterol()?.value ?: 0.0)
        otherNutritionFacts.setValue(REF_SODIUM_ID, nutritionFacts.sodium()?.value ?: 0.0)
        otherNutritionFacts.setValue(REF_FIBERS_ID, nutritionFacts.fibers()?.value ?: 0.0)
        otherNutritionFacts.setValue(REF_TRANS_FAT_ID, nutritionFacts.transFat()?.value ?: 0.0)
        otherNutritionFacts.setValue(REF_SUGARS_ID, nutritionFacts.sugars()?.value ?: 0.0)
        otherNutritionFacts.setValue(
            REF_SUGARS_ADDED_ID,
            nutritionFacts.sugarsAdded()?.value ?: 0.0
        )

        otherNutritionFacts.setValue(REF_IRON_ID, nutritionFacts.iron()?.value ?: 0.0)
        otherNutritionFacts.setValue(REF_VITAMIN_D_ID, nutritionFacts.vitaminD()?.value ?: 0.0)
        otherNutritionFacts.setValue(REF_CALCIUM_ID, nutritionFacts.calcium()?.value ?: 0.0)
        otherNutritionFacts.setValue(REF_POTASSIUM_ID, nutritionFacts.potassium()?.value ?: 0.0)
        otherNutritionFacts.setValue(REF_MAGNESIUM_ID, nutritionFacts.magnesium()?.value ?: 0.0)

        customFoodRecord = foodRecord
        _isEditCustomFood.postValue(true)
        _prefillFoodData.postValue(foodRecord)
    }

    fun setDataFromNutritionFacts(nutritionFactsPair: Pair<PassioNutritionFacts, String>) {
        val nutritionFacts = nutritionFactsPair.first
        this.passioIDEntityType = PassioIDEntityType.nutritionFacts

        Log.d("nutritionFacts====", Gson().toJson(nutritionFacts))
//        productName = nutritionFactsPair.second
        nutritionFacts.servingSize?.let { servingSize ->
            val pair = splitServingSize(servingSize)
            setServingSize(pair.first)
            setServingUnit(pair.second)
        }
        nutritionFacts.servingSizeQuantity?.let {
            setWeightGram(it)
        }

//        nutritionFacts.servingSizeQuantity
//        nutritionFacts.servingSizeUnitName
//        nutritionFacts.servingSize
//        nutritionFacts.sugarAlcohol

        requiredNutritionFacts.setValue(REF_CARBS_ID, nutritionFacts.carbs ?: 0.0)
        requiredNutritionFacts.setValue(REF_CALORIES_ID, nutritionFacts.calories ?: 0.0)
        requiredNutritionFacts.setValue(REF_PROTEIN_ID, nutritionFacts.protein ?: 0.0)
        requiredNutritionFacts.setValue(REF_FAT_ID, nutritionFacts.fat ?: 0.0)

        otherNutritionFacts.setValue(REF_SAT_FAT_ID, nutritionFacts.saturatedFat ?: 0.0)
        otherNutritionFacts.setValue(REF_CHOLESTEROL_ID, nutritionFacts.cholesterol ?: 0.0)
//        otherNutritionFacts.setValue(REF_SODIUM_ID, nutritionFacts.sodium ?: 0.0)
//        otherNutritionFacts.setValue(REF_FIBERS_ID, nutritionFacts.fibers ?: 0.0)
        otherNutritionFacts.setValue(REF_TRANS_FAT_ID, nutritionFacts.transFat ?: 0.0)
        otherNutritionFacts.setValue(REF_SUGARS_ID, nutritionFacts.sugars ?: 0.0)
//        otherNutritionFacts.setValue(REF_SUGARS_ADDED_ID, nutritionFacts.sugarsAdded ?: 0.0)

//        otherNutritionFacts.setValue(REF_IRON_ID, nutritionFacts.iron ?: 0.0)
//        otherNutritionFacts.setValue(REF_VITAMIN_D_ID, nutritionFacts.vitaminD ?: 0.0)
//        otherNutritionFacts.setValue(REF_CALCIUM_ID, nutritionFacts.calcium ?: 0.0)
//        otherNutritionFacts.setValue(REF_POTASSIUM_ID, nutritionFacts.potassium ?: 0.0)
//        otherNutritionFacts.setValue(REF_MAGNESIUM_ID, nutritionFacts.magnesium ?: 0.0)

        val passioNutrientsTemp = PassioNutrients(
            carbs = requiredNutritionFacts.unitMassOf(REF_CARBS_ID),
            calories = requiredNutritionFacts.unitEnergyOf(REF_CALORIES_ID),
            proteins = requiredNutritionFacts.unitMassOf(REF_PROTEIN_ID),
            fat = requiredNutritionFacts.unitMassOf(REF_FAT_ID),
            satFat = otherNutritionFactsAdded.unitMassOf(REF_SAT_FAT_ID),
            monounsaturatedFat = null,
            polyunsaturatedFat = null,
            cholesterol = otherNutritionFactsAdded.unitMassOf(REF_CHOLESTEROL_ID),
            sodium = otherNutritionFactsAdded.unitMassOf(REF_SODIUM_ID),
            fibers = otherNutritionFactsAdded.unitMassOf(REF_FIBERS_ID),
            transFat = otherNutritionFactsAdded.unitMassOf(REF_TRANS_FAT_ID),
            sugars = otherNutritionFactsAdded.unitMassOf(REF_SUGARS_ID),
            sugarsAdded = otherNutritionFactsAdded.unitMassOf(REF_SUGARS_ADDED_ID),
            alcohol = null,
            iron = otherNutritionFactsAdded.unitMassOf(REF_IRON_ID),
            vitaminC = null,
            vitaminD = otherNutritionFactsAdded.unitMassOf(REF_VITAMIN_D_ID),
            vitaminB6 = null,
            vitaminB12 = null,
            vitaminB12Added = null,
            vitaminE = null,
            vitaminEAdded = null,
            iodine = null,
            calcium = otherNutritionFactsAdded.unitMassOf(REF_CALCIUM_ID),
            potassium = otherNutritionFactsAdded.unitMassOf(REF_POTASSIUM_ID),
            magnesium = null,
            phosphorus = null,
            sugarAlcohol = null,
            vitaminA = null
        )
        val passioNutrients = PassioNutrients(
            passioNutrientsTemp,
            UnitMass(if (weightGramUnit == Grams.symbol) Grams else Milliliters, weightGram)
        )

        val customFood = FoodRecord(
            productName = productName,
            brandName = brandName,
            barcode = barcode,
            servingWeight = servingQuantity,
            servingUnit = servingUnit,
            weightInGrams = weightGram,
            weightInGramsUnit = weightGramUnit,
            passioNutrients = passioNutrients,
            passioIDEntityType = passioIDEntityType,
            foodImagePath = photoPath
        )
        customFoodRecord = customFood
        _isEditCustomFood.postValue(false)
        _prefillFoodData.postValue(customFood)
    }

    private fun splitServingSize(servingSize: String): Pair<Double, String> {
        // Find the first space in the string
        val firstSpaceIndex = servingSize.indexOf(' ')

        return if (firstSpaceIndex != -1) {
            // Split at the first space
            val sizeString = servingSize.substring(0, firstSpaceIndex)
            val unit = servingSize.substring(firstSpaceIndex + 1).lowercase()

            // Convert size to Double
            val size = sizeString.toDoubleOrNull() ?: 0.0

            Pair(size, unit)
        } else {
            // No space found, check if the text contains a numeric value
            val containsNumeric = servingSize.any { it.isDigit() }

            if (containsNumeric) {
                // Try to parse the entire string as a Double
                val size = servingSize.toDoubleOrNull() ?: 0.0
                Pair(size, "")
            } else {
                Pair(0.0, servingSize)  // Treat as unit
            }
        }
    }


    fun setServingSize(servingQuantity: Double) {
        this.servingQuantity = servingQuantity
    }

    fun setServingUnit(servingUnit: String) {
        val delta = unitList.find { it.lowercase() == servingUnit.lowercase() }
        if (delta == null) {
            unitList.add(servingUnit)
            this.servingUnit = servingUnit
        } else {
            this.servingUnit = delta
        }
        servingUnitEvent.postValue(this.servingUnit)
    }

    fun setWeightGram(servingWeight: Double) {
        this.weightGram = servingWeight
    }

    fun setWeightGramUnit(servingWeightUnit: String) {
        this.weightGramUnit = servingWeightUnit
    }

    fun setBarcode(barcode: String) {
        if (barcode.isValid()) {
            this.passioIDEntityType = PassioIDEntityType.barcode
        }
        this.barcode = barcode
        barcodeEvent.postValue(this.barcode)
    }


    fun setProductName(productName: String) {
        this.productName = productName
    }

    fun setBrandName(brandName: String) {
        this.brandName = brandName
    }

    fun deleteCustomFood() {
        viewModelScope.launch {
            if (customFoodRecord != null) {
                _showLoading.postValue(true)
                if (useCase.deleteCustomFood(customFoodRecord!!.uuid)) {
                    _showMessageEvent.postValue("Food deleted successfully.")
                    navigateToMyFoods()

                } else {
                    _showMessageEvent.postValue("Failed to delete food. Please try again.")
                }
                _showLoading.postValue(false)
            }
        }
    }

    fun saveCustomFood() {
        viewModelScope.launch {

            if (servingUnit.equals(Grams.symbol, true) || servingUnit.equals(
                    Milliliters.symbol,
                    true
                )
            ) {
                weightGram = servingQuantity
                weightGramUnit = servingUnit
            }

            if (!productName.isValid()) {
                _showMessageEvent.postValue("Please add valid product name.")
            } /*else if (!brandName.isValid()) {
                _showMessageEvent.postValue("Please add valid brand name.")
            }*/ else if (servingQuantity == 0.0) {
                _showMessageEvent.postValue("Please add valid serving size.")
            } else if (!servingUnit.isValid()) {
                _showMessageEvent.postValue("Please add valid serving unit.")
            } else if (weightGram == 0.0) {
                _showMessageEvent.postValue("Please add valid weight.")
            } else if (!weightGramUnit.isValid()) {
                _showMessageEvent.postValue("Please add valid weight unit.")
            } else if (!isAddedNutrientsValid()) {
                _showMessageEvent.postValue("Please add valid information of nutrients.")
            } else {
                _showLoading.postValue(true)

                val passioNutrientsTemp = PassioNutrients(
                    carbs = requiredNutritionFacts.unitMassOf(REF_CARBS_ID),
                    calories = requiredNutritionFacts.unitEnergyOf(REF_CALORIES_ID),
                    proteins = requiredNutritionFacts.unitMassOf(REF_PROTEIN_ID),
                    fat = requiredNutritionFacts.unitMassOf(REF_FAT_ID),
                    satFat = otherNutritionFactsAdded.unitMassOf(REF_SAT_FAT_ID),
                    monounsaturatedFat = null,
                    polyunsaturatedFat = null,
                    cholesterol = otherNutritionFactsAdded.unitMassOf(REF_CHOLESTEROL_ID),
                    sodium = otherNutritionFactsAdded.unitMassOf(REF_SODIUM_ID),
                    fibers = otherNutritionFactsAdded.unitMassOf(REF_FIBERS_ID),
                    transFat = otherNutritionFactsAdded.unitMassOf(REF_TRANS_FAT_ID),
                    sugars = otherNutritionFactsAdded.unitMassOf(REF_SUGARS_ID),
                    sugarsAdded = otherNutritionFactsAdded.unitMassOf(REF_SUGARS_ADDED_ID),
                    alcohol = null,
                    iron = otherNutritionFactsAdded.unitMassOf(REF_IRON_ID),
                    vitaminC = null,
                    vitaminD = otherNutritionFactsAdded.unitMassOf(REF_VITAMIN_D_ID),
                    vitaminB6 = null,
                    vitaminB12 = null,
                    vitaminB12Added = null,
                    vitaminE = null,
                    vitaminEAdded = null,
                    iodine = null,
                    calcium = otherNutritionFactsAdded.unitMassOf(REF_CALCIUM_ID),
                    potassium = otherNutritionFactsAdded.unitMassOf(REF_POTASSIUM_ID),
                    magnesium = null,
                    phosphorus = null,
                    sugarAlcohol = null,
                    vitaminA = null
                )
                val passioNutrients = PassioNutrients(
                    passioNutrientsTemp,
                    UnitMass(if (weightGramUnit == Grams.symbol) Grams else Milliliters, weightGram)
                )

                val customFood =
                    if (customFoodRecord != null) {
                        customFoodRecord!!.editCustomFood(
                            productName = productName,
                            brandName = brandName,
                            barcode = barcode,
                            servingWeight = servingQuantity,
                            servingUnit = servingUnit,
                            weightInGrams = weightGram,
                            weightInGramsUnit = weightGramUnit,
                            passioNutrients = passioNutrients,
                            passioIDEntityType = passioIDEntityType,
                            foodImagePath = photoPath
                        )
                    } else {
                        FoodRecord(
                            productName = productName,
                            brandName = brandName,
                            barcode = barcode,
                            servingWeight = servingQuantity,
                            servingUnit = servingUnit,
                            weightInGrams = weightGram,
                            weightInGramsUnit = weightGramUnit,
                            passioNutrients = passioNutrients,
                            passioIDEntityType = passioIDEntityType,
                            foodImagePath = photoPath
                        )
                    }

                val customFoodNew: FoodRecord = if (!customFood.isCustomFood()) {
                    customFood.copyAsCustomFood()
                } else {
                    customFood
                }
                if (useCase.saveCustomFood(customFoodNew)) {
                    if (loggedRecord != null) {
                        loggedRecord?.apply {
                            this.name = customFoodNew.name
                            this.ingredients = customFoodNew.ingredients
                            this.foodImagePath = customFoodNew.foodImagePath
                            this.iconId = customFoodNew.iconId
                            this.id = customFoodNew.uuid
                            this.passioIDEntityType = customFoodNew.passioIDEntityType
                            this.servingSizes.clear()
                            this.servingSizes.addAll(customFoodNew.servingSizes)
                            this.servingUnits.clear()
                            this.servingUnits.addAll(customFoodNew.servingUnits)
                            this.setSelectedQuantity(customFoodNew.getSelectedQuantity())
                            this.setSelectedUnit(customFoodNew.getSelectedUnit())
                            editFoodUseCase.logFoodRecord(this, true)
                        }
                    }
                    _showMessageEvent.postValue("Food saved successfully.")

                    navigateOnSave()
                } else {
                    _showMessageEvent.postValue("Error while saving food.")
                }
                _showLoading.postValue(false)
            }
        }
    }

    private fun isAddedNutrientsValid(): Boolean {
//        if (requiredNutritionFacts.any { it.value == 0.0 }) {
        if (requiredNutritionFacts.any { it.value < 0.0 }) {
            return false
//        } else if (otherNutritionFactsAdded.any { it.value == 0.0 }) {
        } else if (otherNutritionFactsAdded.any { it.value < 0.0 }) {
            return false
        }
        return true
    }

    private fun navigateOnSave() {
        if (loggedRecord != null) //update log upon create or save
        {
            navigateToDiary()
        }
        /*else if (isEditRecipe && loggedRecord == null)
        {
            navigateBack()
        }
        else if (isEditRecipe)
        {
            navigateBack()
        }*/
        else {
            navigateToMyFoods()
        }
    }

    fun navigateToMyFoods() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(FoodCreatorFragmentDirections.foodCreatorToMyFoods())
        }
    }

    fun navigateToDiary() {
        viewModelScope.launch(Dispatchers.Main) {
            navigate(FoodCreatorFragmentDirections.foodCreatorToDiary())
        }
    }

    fun navigateToTakePhoto() {
        navigate(FoodCreatorFragmentDirections.foodCreatorToTakePhoto())
    }

    fun navigateToScanBarcode() {
        navigate(FoodCreatorFragmentDirections.foodCreatorToScanBarcode())
    }

}