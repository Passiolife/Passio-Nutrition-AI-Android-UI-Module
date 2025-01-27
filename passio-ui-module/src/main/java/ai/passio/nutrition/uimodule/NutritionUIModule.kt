package ai.passio.nutrition.uimodule

import ai.passio.nutrition.uimodule.data.PassioConnector
import ai.passio.nutrition.uimodule.ui.activity.PassioUiModuleActivity
import android.content.Context
import android.content.Intent

object NutritionUIModule {

    private var connector: PassioConnector? = null
    private var nutritionUIConfiguration: NutritionUIConfiguration = NutritionUIConfiguration()


    /**
     * Launches the Passio Nutrition UI module with optional configurations.
     *
     * @param context The context to be used for launching the module.
     * @param connector An optional [PassioConnector] instance, which serves as a datastore
     *                  providing callbacks to store and retrieve data used in the Passio
     *                  Nutrition UI module features. If not provided, a default connector
     *                  will be used. Use the default connector if you do not require direct
     *                  data access on your end.
     * @param nutritionUIConfiguration An optional [NutritionUIConfiguration] instance.
     *                                 If not provided, default configurations will be used.
     */
    fun launch(
        context: Context,
        connector: PassioConnector? = null,
        nutritionUIConfiguration: NutritionUIConfiguration? = null
    ) {
        this.connector = connector
        if (nutritionUIConfiguration != null) {
            this.nutritionUIConfiguration = nutritionUIConfiguration
        }
        context.startActivity(Intent(context, PassioUiModuleActivity::class.java))
    }

    internal fun getConnector(): PassioConnector? = connector
    internal fun getConfiguration(): NutritionUIConfiguration = nutritionUIConfiguration

}

/**
 * Data class representing the UI configuration for Nutrition-UI Module.
 *
 * @property languageCode Specifies the language code for the Search, Voice Logging... features.
 *                        Default is "en" (English).
 * @property shouldUseLegacySearch Determines whether to use the legacy search approach.
 *                                 Default is `false`, meaning Semantic Search will be used.
 */
data class NutritionUIConfiguration(
    val languageCode: String = "en",
    val shouldUseLegacySearch: Boolean = false
)

