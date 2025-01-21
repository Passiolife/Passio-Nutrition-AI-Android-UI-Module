package ai.passio.nutrition.uimodule

import ai.passio.nutrition.uimodule.data.PassioConnector
import ai.passio.nutrition.uimodule.ui.activity.PassioUiModuleActivity
import android.content.Context
import android.content.Intent

object NutritionUIModule {

    private var connector: PassioConnector? = null
    private var nutritionUIConfiguration: NutritionUIConfiguration = NutritionUIConfiguration()


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

data class NutritionUIConfiguration(
    val languageCode: String = "en",
    val shouldUseLegacySearch: Boolean = false
)

