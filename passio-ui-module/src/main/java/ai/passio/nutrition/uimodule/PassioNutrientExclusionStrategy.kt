package ai.passio.nutrition.uimodule

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes

class PassioNutrientsExclusionStrategy : ExclusionStrategy {
    override fun shouldSkipField(f: FieldAttributes): Boolean {
        // Exclude the nutrientDefaults field by name
        return f.name == "nutrientDefaults"
    }

    override fun shouldSkipClass(clazz: Class<*>): Boolean {
        // No class-level exclusion needed
        return false
    }
}