/*
package ai.passio.nutrition.uimodule.ui.model

enum class MeasurementSystem {
    METRIC, // For weight: kg, for height: meters (m) and centimeters (cm), for water: liters (L) or milliliters (mL).
    IMPERIAL // For weight: lbs, for height: feet (ft) and inches (in), for water: ounces (oz).
}

enum class WeightUnits {
    LBS, KG
}

enum class WaterUnits {
    OZ, ML
}

enum class HeightUnit {
    METER, CENTIMETER, FEET, INCHES
}

enum class GenderSelection {
    MALE, FEMALE
}

enum class Conversion(val value: Double) {
    CENTIMETER_TO_METER(100.0),
    METER_TO_INCH(39.3701),
    FEET_TO_INCH(12.0),
    KG_TO_LBS(2.2046),
    ML_TO_OZ(0.0338);
}

enum class ActivityLevel {
    NOT_ACTIVE, LIGHTLY_ACTIVE, MODERATELY_ACTIVE, ACTIVE
}

enum class CalorieDeficit(val value: Double) {
    LOSE_0_5(0.5),
    LOSE_1_0(1.0),
    LOSE_1_5(1.5),
    LOSE_2_0(2.0),
    GAIN_0_5(0.5),
    GAIN_1_0(1.0),
    GAIN_1_5(1.5),
    GAIN_2_0(2.0),
    MAINTAIN_WEIGHT(0.0);

    fun getValue(unit: MeasurementSystem): Double {
        return if (unit == MeasurementSystem.IMPERIAL) value else value / Conversion.KG_TO_LBS.value
    }
}

data class UserProfileModel(
    var id: String? = null,
    var name: String? = null,
    var age: Int? = null,
    var gender: GenderSelection = GenderSelection.MALE,
    var weight: Double? = null,
    var height: Double? = null,
    var heightUnit: MeasurementSystem = MeasurementSystem.IMPERIAL,
    var weightUnit: MeasurementSystem = MeasurementSystem.IMPERIAL,
    var targetWeight: Double = 58.967,
    var targetWater: Double = 591.471,
    var activityLevel: ActivityLevel? = null,
    var calorieDeficit: CalorieDeficit = CalorieDeficit.MAINTAIN_WEIGHT
) {
    var caloriesTarget = 2100
    var carbsPercentage = 50
    var proteinPercentage = 25
    var fatPercentage = 25

    fun setAge(age: Int?) {
        if (this.age == age) return
        this.age = age
        val calories = calculateRecommendedCalorie()
        if (calories > 0) {
            caloriesTarget = calories
        }
    }

    fun getAge() = age

    fun setWeight(weight: Double?) {
        if (this.weight == weight) return
        this.weight = if (weightUnit == MeasurementSystem.IMPERIAL) {
            (weight ?: 0.0) / Conversion.KG_TO_LBS.value
        } else {
            weight
        }
        val calories = calculateRecommendedCalorie()
        if (calories > 0) {
            caloriesTarget = calories
        }
    }

    fun getWeight(): Double? {
        return weight?.let {
            if (weightUnit == MeasurementSystem.IMPERIAL) {
                it * Conversion.KG_TO_LBS.value
            } else {
                it
            }
        }
    }

    fun setTargetWeight(weight: Double) {
        targetWeight = if (weightUnit == MeasurementSystem.IMPERIAL) {
            weight / Conversion.KG_TO_LBS.value
        } else {
            weight
        }
    }

    fun getTargetWeight(): Double? {
        return if (weightUnit == MeasurementSystem.IMPERIAL) {
            targetWeight * Conversion.KG_TO_LBS.value
        } else {
            targetWeight
        }
    }

    fun setTargetWater(water: Double) {
        targetWater = if (weightUnit == MeasurementSystem.IMPERIAL) {
            water / Conversion.ML_TO_OZ.value
        } else {
            water
        }
    }

    fun getTargetWater(): Double? {
        return if (weightUnit == MeasurementSystem.IMPERIAL) {
            targetWater * Conversion.ML_TO_OZ.value
        } else {
            targetWater
        }
    }

    fun setActivityLevel(activityLevel: ActivityLevel?) {
        if (this.activityLevel == activityLevel) return
        this.activityLevel = activityLevel
        val calories = calculateRecommendedCalorie()
        if (calories > 0) {
            caloriesTarget = calories
        }
    }

    fun getActivityLevel() = activityLevel

    fun setHeight(value1: Int, value2: Int) {
        height = if (heightUnit == MeasurementSystem.METRIC) {
            value1 + (value2 / Conversion.CENTIMETER_TO_METER.value)
        } else {
            ((value1 * Conversion.FEET_TO_INCH.value) + value2) / Conversion.METER_TO_INCH.value
        }
    }

    fun getHeight() = height

    fun heightInMeasurementSystem(): Pair<Int, Int> {
        return if (heightUnit == MeasurementSystem.METRIC) {
            val meters = height?.toInt() ?: 0
            val centimeter = (((height ?: 0.0) - (height?.toInt() ?: 0)) * Conversion.CENTIMETER_TO_METER.value).toInt()
            Pair(meters, centimeter)
        } else {
            val feet = ((height ?: 0.0) * Conversion.METER_TO_INCH.value / Conversion.FEET_TO_INCH.value).toInt()
            val inches = (((height ?: 0.0) * Conversion.METER_TO_INCH.value) % Conversion.FEET_TO_INCH.value).toInt()
            Pair(feet, inches)
        }
    }

    fun heightDescription(): String {
        val height = heightInMeasurementSystem()
        return if (heightUnit == MeasurementSystem.METRIC) {
            "${height.first}m ${height.second}cm"
        } else {
            "${height.first}' ${height.second}\""
        }
    }

    fun setCalorieDeficit(calorieDeficit: CalorieDeficit) {
        if (this.calorieDeficit == calorieDeficit) return
        this.calorieDeficit = calorieDeficit
        val calories = calculateRecommendedCalorie()
        if (calories > 0) {
            caloriesTarget = calories
        }
    }

    fun getCalorieDeficit() = calorieDeficit

    fun setPassioMealPlan(mealPlan: PassioMealPlan) {
        _mealPlan = mealPlan
        setCarbsPercentage(mealPlan.carbTarget)
        setProteinPercentage(mealPlan.proteinTarget)
        setFatPercentage(mealPlan.fatTarget)
    }

    fun getPassioMealPlan() = _mealPlan

    fun getRecommendedCalories() = calculateRecommendedCalorie()

    fun getCarbsGram() = caloriesTarget * carbsPercentage / 100 / 4

    fun setCarbsPercentage(carbs: Int) {
        val values = balance3Values(carbs, proteinPercentage, fatPercentage)
        carbsPercentage = values.first
        proteinPercentage = values.second
        fatPercentage = values.third
    }

    fun getProteinGram() = caloriesTarget * proteinPercentage / 100 / 4

    fun setProteinPercentage(protein: Int) {
        val values = balance3Values(protein, fatPercentage, carbsPercentage)
        proteinPercentage = values.first
        fatPercentage = values.second
        carbsPercentage = values.third
    }

    fun getFatGram() = caloriesTarget * fatPercentage / 100 / 9

    fun setFatPercentage(fat: Int) {
        val values = balance3Values(fat, proteinPercentage, carbsPercentage)
        fatPercentage = values.first
        proteinPercentage = values.second
        carbsPercentage = values.third
    }

    private fun balance3Values(first: Int, second: Int, third: Int): Triple<Int, Int, Int> {
        val validateFirst = validatePercent(first)
        return if (validateFirst + third > 100) {
            Triple(validateFirst, 0, 100 - validateFirst)
        } else {
            Triple(validateFirst, 100 - validateFirst - third, third)
        }
    }

    private fun validatePercent(value: Int): Int {
        return when {
            value > 100 -> 100
            value < 0 -> 0
            else -> value
        }
    }

    private fun calculateBMR(): Pair<Double?, ActivityLevel?> {
        if (activityLevel == null || age == null || height == null || weight == null) {
            return Pair(null, null)
        }
        val weightInKg = 10 * (weight ?: 0.0)
        val heightInMeter = (height ?: 0.0) * Conversion.CENTIMETER_TO_METER.value
        val bmr = if (gender == GenderSelection.MALE) {
            weightInKg + (6.25 * heightInMeter) - (5 * age!!) + 5
        } else {
            weightInKg + (6.25 * heightInMeter) - (5 * age!!) - 161
        }
        return Pair(bmr, activityLevel)
    }

    private fun calculateCaloriesBasedOnActivityLevel(bmr: Double): Int {
        return when (activityLevel) {
            ActivityLevel.NOT_ACTIVE -> (bmr * 1.2).toInt()
            ActivityLevel.LIGHTLY_ACTIVE -> (bmr * 1.375).toInt()
            ActivityLevel.MODERATELY_ACTIVE -> (bmr * 1.55).toInt()
            ActivityLevel.ACTIVE -> (bmr * 1.725).toInt()
            else -> 0
        }
    }

    private fun calculateRecommendedCalorie(): Int {
        val bmrAndActivityLevel = calculateBMR()
        val bmr = bmrAndActivityLevel.first
        return if (bmr == null) {
            caloriesTarget
        } else {
            (calculateCaloriesBasedOnActivityLevel(bmr) - calorieDeficit.getValue(weightUnit)).toInt()
        }
    }

    private var _mealPlan: PassioMealPlan? = null
}

data class UserProfileData(val profile: UserProfileModel? = null)

data class PassioMealPlan(
    val caloriesTarget: Int,
    val carbTarget: Int,
    val proteinTarget: Int,
    val fatTarget: Int
)
*/
