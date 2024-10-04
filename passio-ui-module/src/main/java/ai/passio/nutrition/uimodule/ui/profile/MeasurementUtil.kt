package ai.passio.nutrition.uimodule.ui.profile

/**
Represents different measurement systems for units.
 */
enum class MeasurementSystem(val value: String) {
    /**
    The metric measurement system.
    For weight, it uses kilograms (kg).
    For height, it primarily uses meters (m) and centimeters (cm).
    For water, it uses liters (L) or milliliters (mL).
     */
    Metric("Metric"),

    /**
    The imperial measurement system.

    For weight, it uses pounds (lbs).
    For height, it primarily uses feet (ft) and inches (in).
    For water, it uses ounces (oz).
     */
    Imperial("Imperial"),
}

enum class Gender(val value: String) {
    Male("Male"),
    Female("Female"),
}

enum class LengthUnit(val value: String) {
    Imperial("Feet, Inches"),
    Metric("Meter, Centimeter"),
}

enum class WeightUnit(val value: String) {
    Imperial("Lbs"),
    Metric("Kg"),
}

enum class WaterUnit(val value: String) {
    Imperial("oz"),
    Metric("ml"),
}


// Function to convert feet and inches to meters
fun feetInchesToMeters(feet: Int, inches: Int): Double {
    val totalInches = feet * 12 + inches
    val meters = totalInches * 0.0254  // 1 inch = 0.0254 meters
    return meters
}

// Function to convert meters and centimeters to meters
fun metersCentimetersToMeters(meters: Int, centimeters: Int): Double {
    val totalCentimeters = meters * 100 + centimeters
    val metersResult = totalCentimeters / 100.0  // 1 meter = 100 centimeters
    return metersResult
}

// Function to convert meters to feet and inches
fun metersToFeetInches(meters: Double): Pair<Int, Int> {
    val totalInches = (meters / 0.0254).toInt()
    val feet = totalInches / 12
    val inches = totalInches % 12
    return Pair(feet, inches)
}

// Function to convert meters to meters and centimeters
fun metersToMetersCentimeters(meters: Double): Pair<Int, Int> {
    val centimeters = (meters * 100).toInt() % 100
    val metersResult = meters.toInt()
    return Pair(metersResult, centimeters)
}

// Function to convert kilograms to pounds
fun kgToLbs(kg: Double): Double {
    return kg * 2.20462  // 1 kilogram = 2.20462 pounds
}

// Function to convert pounds to kilograms
fun lbsToKg(lbs: Double): Double {
    return lbs / 2.20462  // 1 pound = 0.453592 kilograms
}

// Function to convert milliliters to ounces
fun mlToOz(ml: Double): Double {
    return ml * 0.033814  // 1 milliliter = 0.033814 ounces
}

// Function to convert ounces to milliliters
fun ozToMl(oz: Double): Double {
    return oz / 0.033814  // 1 ounce = 29.5735 milliliters
}


/// Represents different levels of activity.
enum class ActivityLevel(val label: String, val valueDiff: Double) {
    /// Not active.
    NotActive("Not Active", 1.2),

    /// Lightly active.
    LightlyActive("Lightly Active", 1.375),

    /// Moderately active.
    ModeratelyActive("Moderately Active", 1.55),

    /// Very active.
    Active("Active", 1.725)
}

enum class CalorieDeficit(val lblImperial: String, val lblMetric: String, val calorieValue: Int) {
    /// Lose 0.5 lbs per week.
    Lose1("Lose 0.5", "Lose 0.25", -250),
    Lose2("Lose 1.0", "Lose 0.5", -500),
    Lose3("Lose 1.5", "Lose 0.75", -750),
    Lose4("Lose 2.0", "Lose 1.0", -1000),
    Gain1("Gain 0.5", "Gain 0.25", 250),
    Gain2("Gain 1.0", "Gain 0.5", 500),
    Gain3("Gain 1.5", "Gain 0.75", 750),
    Gain4("Gain 2.0", "Gain 1.00", 1000),
    Maintain("Maintain Weight", "Maintain Weight", 0)
}
