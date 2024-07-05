package ai.passio.nutrition.uimodule

import ai.passio.nutrition.uimodule.data.PassioConnector
import ai.passio.nutrition.uimodule.ui.activity.PassioUiModuleActivity
import android.content.Context
import android.content.Intent

object NutritionUIModule {

    private var connector: PassioConnector? = null

    fun launch(context: Context, connector: PassioConnector? = null) {
        this.connector = connector
        context.startActivity(Intent(context, PassioUiModuleActivity::class.java))
    }

    internal fun getConnector(): PassioConnector? = connector
}