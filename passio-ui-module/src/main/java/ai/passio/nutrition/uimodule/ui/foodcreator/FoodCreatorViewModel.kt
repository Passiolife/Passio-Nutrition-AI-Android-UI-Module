package ai.passio.nutrition.uimodule.ui.foodcreator

import ai.passio.nutrition.uimodule.data.passioGson
import ai.passio.nutrition.uimodule.domain.customfood.CustomFoodUseCase
import ai.passio.nutrition.uimodule.domain.foodimage.FoodImageUseCase
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
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_VITAMIN_A_RAE_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.REF_VITAMIN_D_ID
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.setValue
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.unitEnergyOf
import ai.passio.nutrition.uimodule.ui.foodcreator.NutritionFactsItem.Companion.unitMassOf
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.clone
import ai.passio.nutrition.uimodule.ui.model.copy
import ai.passio.nutrition.uimodule.ui.model.copyAsCustomFood
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.nutrition.uimodule.ui.util.StringKT.isValid
import ai.passio.nutrition.uimodule.ui.util.generateImageID
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
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FoodCreatorViewModel : BaseViewModel() {

    private val useCase = CustomFoodUseCase
    private val editFoodUseCase = EditFoodUseCase
    private val foodImageUseCase = FoodImageUseCase
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
    private var isEditCustomFood: Boolean = false
    private val _isEditCustomFoodEvent = SingleLiveEvent<Boolean>()
    val isEditCustomFoodEvent: LiveData<Boolean> = _isEditCustomFoodEvent
    private val _prefillFoodData = SingleLiveEvent<FoodRecord>()
    val prefillFoodData: LiveData<FoodRecord> = _prefillFoodData


    //    private var photoPath: String? = null
//    private val _photoPathEvent = MutableLiveData<String>()
//    val photoPathEvent: LiveData<String> = _photoPathEvent
    private var iconId: String? = null
    private val _iconIdEvent = MutableLiveData<String>()
    val iconIdEvent: LiveData<String> = _iconIdEvent

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

        val refVitaminARAE = NutritionFactsItem(
            id = REF_VITAMIN_A_RAE_ID,
            nutrientName = "vitamin A RAE",
            unitSymbol = Milligrams.symbol,
            value = 0.0,
            isAdded = false
        )
        otherNutritionFacts.add(refVitaminARAE)
    }


    fun setPhotoBitmap(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            val imgId = generateImageID()
            val result = foodImageUseCase.updateUserFoodImage(imgId, bitmap)
            if (result) {
                setIconId(imgId)
            }
        }
    }

    private fun setIconId(iconId: String) {
        this.iconId = iconId
//        customFoodRecord?.iconId = iconId
        _iconIdEvent.postValue(iconId)
    }

    /*fun setPhotoPath(path: String) {
        this.photoPath = path
        _photoPathEvent.postValue(path)
    }*/

    fun setToUpdateLog(loggedRecord: FoodRecord) {
        this.loggedRecord = loggedRecord
    }

    fun setDataToEdit(foodRecord: FoodRecord, isEditUserFood: Boolean) {

        isEditCustomFood = isEditUserFood //true: edit user food, false: create new food

//        val nutritionFacts = nutritionFactsPair.first
        this.passioIDEntityType = PassioIDEntityType.fromString(foodRecord.entityType)

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

        /*foodRecord.foodImagePath?.let {
            setPhotoPath(it)
        }*/

        setIconId(foodRecord.iconId)

//        foodRecord.servingWeight()
        val ratio = 100 / foodRecord.servingWeight().gramsValue()
        val nutritionFacts = foodRecord.nutrientsReference()
        requiredNutritionFacts.setValue(
            REF_CARBS_ID,
            nutritionFacts.carbs()?.value?.div(ratio) ?: 0.0
        )
        requiredNutritionFacts.setValue(
            REF_CALORIES_ID,
            nutritionFacts.calories()?.value?.div(ratio) ?: 0.0
        )
        requiredNutritionFacts.setValue(
            REF_PROTEIN_ID,
            nutritionFacts.protein()?.value?.div(ratio) ?: 0.0
        )
        requiredNutritionFacts.setValue(REF_FAT_ID, nutritionFacts.fat()?.value?.div(ratio) ?: 0.0)

        otherNutritionFacts.setValue(REF_SAT_FAT_ID, nutritionFacts.satFat()?.value?.div(ratio))
        otherNutritionFacts.setValue(
            REF_CHOLESTEROL_ID,
            nutritionFacts.cholesterol()?.value?.div(ratio)
        )
        otherNutritionFacts.setValue(REF_SODIUM_ID, nutritionFacts.sodium()?.value?.div(ratio))
        otherNutritionFacts.setValue(REF_FIBERS_ID, nutritionFacts.fibers()?.value?.div(ratio))
        otherNutritionFacts.setValue(REF_TRANS_FAT_ID, nutritionFacts.transFat()?.value?.div(ratio))
        otherNutritionFacts.setValue(REF_SUGARS_ID, nutritionFacts.sugars()?.value?.div(ratio))
        otherNutritionFacts.setValue(
            REF_SUGARS_ADDED_ID,
            nutritionFacts.sugarsAdded()?.value?.div(ratio)
        )

        otherNutritionFacts.setValue(REF_IRON_ID, nutritionFacts.iron()?.value?.div(ratio))
        otherNutritionFacts.setValue(REF_VITAMIN_D_ID, nutritionFacts.vitaminD()?.value?.div(ratio))
        otherNutritionFacts.setValue(REF_CALCIUM_ID, nutritionFacts.calcium()?.value?.div(ratio))
        otherNutritionFacts.setValue(
            REF_POTASSIUM_ID,
            nutritionFacts.potassium()?.value?.div(ratio)
        )
        otherNutritionFacts.setValue(
            REF_MAGNESIUM_ID,
            nutritionFacts.magnesium()?.value?.div(ratio)
        )
        otherNutritionFacts.setValue(
            REF_VITAMIN_A_RAE_ID,
            nutritionFacts.vitaminARAE()?.value?.div(ratio)
        )

        customFoodRecord = foodRecord.clone()

        if (customFoodRecord?.name == customFoodRecord?.details)
        {
            customFoodRecord?.details = ""
        }

        if (!isEditCustomFood) {
            customFoodRecord?.id = ""
            customFoodRecord?.uuid = ""
            customFoodRecord?.refCode = ""
        }

        _isEditCustomFoodEvent.postValue(isEditCustomFood)
        _prefillFoodData.postValue(customFoodRecord!!)
    }

    fun setDataFromNutritionFacts(nutritionFactsPair: Pair<PassioNutritionFacts, String>) {
        val nutritionFacts = nutritionFactsPair.first
        this.passioIDEntityType = PassioIDEntityType.nutritionFacts

//        Log.d("nutritionFacts====", passioGson.toJson(nutritionFacts))
//        productName = nutritionFactsPair.second
        nutritionFacts.servingQuantity?.let {
            setServingSize(it)
        }
        nutritionFacts.servingUnit?.let {
            setServingUnit(it)
        }
        var ratio = 1.0
        nutritionFacts.weightQuantity?.let {
            ratio = 100 / it
            setWeightGram(it)
        }

//        nutritionFacts.servingSizeQuantity
//        nutritionFacts.servingSizeUnitName
//        nutritionFacts.servingSize
//        nutritionFacts.sugarAlcohol

        requiredNutritionFacts.setValue(REF_CARBS_ID, nutritionFacts.carbs?.div(ratio) ?: 0.0)
        requiredNutritionFacts.setValue(REF_CALORIES_ID, nutritionFacts.calories?.div(ratio) ?: 0.0)
        requiredNutritionFacts.setValue(REF_PROTEIN_ID, nutritionFacts.protein?.div(ratio) ?: 0.0)
        requiredNutritionFacts.setValue(REF_FAT_ID, nutritionFacts.fat?.div(ratio) ?: 0.0)

        otherNutritionFacts.setValue(REF_SAT_FAT_ID, nutritionFacts.saturatedFat?.div(ratio))
        otherNutritionFacts.setValue(REF_CHOLESTEROL_ID, nutritionFacts.cholesterol?.div(ratio))
//        otherNutritionFacts.setValue(REF_SODIUM_ID, nutritionFacts.sodium)
//        otherNutritionFacts.setValue(REF_FIBERS_ID, nutritionFacts.fibers)
        otherNutritionFacts.setValue(REF_TRANS_FAT_ID, nutritionFacts.transFat?.div(ratio))
        otherNutritionFacts.setValue(REF_SUGARS_ID, nutritionFacts.sugars?.div(ratio))
//        otherNutritionFacts.setValue(REF_SUGARS_ADDED_ID, nutritionFacts.sugarsAdded)

//        otherNutritionFacts.setValue(REF_IRON_ID, nutritionFacts.iron)
//        otherNutritionFacts.setValue(REF_VITAMIN_D_ID, nutritionFacts.vitaminD)
//        otherNutritionFacts.setValue(REF_CALCIUM_ID, nutritionFacts.calcium)
//        otherNutritionFacts.setValue(REF_POTASSIUM_ID, nutritionFacts.potassium)
//        otherNutritionFacts.setValue(REF_MAGNESIUM_ID, nutritionFacts.magnesium)
//        otherNutritionFacts.setValue(REF_VITAMIN_A_RAE_ID, nutritionFacts.a)

        val passioNutrients = PassioNutrients(
            weight = UnitMass(
                if (weightGramUnit == Grams.symbol) Grams else Milliliters,
                weightGram
            ),
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
            vitaminA = null,
            vitaminARAE = otherNutritionFacts.unitMassOf(REF_VITAMIN_A_RAE_ID)
        )
//        val passioNutrients = PassioNutrients(
//            passioNutrientsTemp,
//            UnitMass(if (weightGramUnit == Grams.symbol) Grams else Milliliters, weightGram)
//        )

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
//            foodImagePath = ""photoPath,
            iconId = iconId ?: ""
        )
        customFoodRecord = customFood
        _isEditCustomFoodEvent.postValue(false)
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
        viewModelScope.launch(Dispatchers.IO) {
            if (customFoodRecord != null) {
                _showLoading.postValue(true)
                if (useCase.deleteCustomFood(customFoodRecord!!)) {
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
        viewModelScope.launch(Dispatchers.IO) {

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
            } else if (!isAddedRequiredNutritionFacts()) {
                _showMessageEvent.postValue("Please add valid information of required nutrients.")
            }/* else if (!isAddedOtherNutritionFacts()) {
                _showMessageEvent.postValue("Please add valid information of other nutrients.")
            } */ else {
                _showLoading.postValue(true)

                val passioNutrients = PassioNutrients(
                    weight = UnitMass(Grams, weightGram),
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
                    vitaminA = null,
                    vitaminARAE = otherNutritionFactsAdded.unitMassOf(REF_VITAMIN_A_RAE_ID)
                )
                /*val passioNutrients = PassioNutrients(
                    passioNutrientsTemp,
                    UnitMass(if (weightGramUnit == Grams.symbol) Grams else Milliliters, 100.0)
                )*/

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
//                            foodImagePath = ""photoPath,
                            iconId = iconId ?: ""
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
//                            foodImagePath = ""photoPath,
                            iconId = iconId ?: ""
                        )
                    }

                val customFoodNew: FoodRecord = if (!isEditCustomFood) {
                    customFood.copyAsCustomFood()
                } else {
                    customFood
                }
                if (useCase.saveCustomFood(customFoodNew)) {
                    if (loggedRecord != null) {
                        val loggedRecordNew = customFoodNew.copy()
                        loggedRecordNew.apply {
                            this.create(loggedRecord?.createdAtTime())
                            this.mealLabel = loggedRecord?.mealLabel
                            editFoodUseCase.deleteRecord(loggedRecord!!)
                            editFoodUseCase.logFoodRecord(loggedRecordNew, true)
                        }
                        /*loggedRecord?.apply {
                            this.name = customFoodNew.name
                            this.ingredients = customFoodNew.ingredients
//                            this.foodImagePath = customFoodNew.foodImagePath
                            this.iconId = customFoodNew.iconId
                            this.id = customFoodNew.uuid
                            this.entityType = customFoodNew.entityType
                            this.servingSizes.clear()
                            this.servingSizes.addAll(customFoodNew.servingSizes)
                            this.servingUnits.clear()
                            this.servingUnits.addAll(customFoodNew.servingUnits)
                            this.setSelectedQuantity(customFoodNew.getSelectedQuantity())
                            this.setSelectedUnit(customFoodNew.getSelectedUnit())
                            editFoodUseCase.logFoodRecord(this, true)
                        }*/
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

    private fun isAddedRequiredNutritionFacts(): Boolean {
//        if (requiredNutritionFacts.any { it.value == 0.0 }) {
        //        } else if (otherNutritionFactsAdded.any { it.value == 0.0 }) {
        return !requiredNutritionFacts.any { it.value < 0.0 }
    }

    private fun isAddedOtherNutritionFacts(): Boolean {
//        if (requiredNutritionFacts.any { it.value == 0.0 }) {
        return !otherNutritionFactsAdded.any { it.value <= 0.0 }
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