package ai.passio.uimodule

import ai.passio.nutrition.uimodule.NutritionUIModule
import ai.passio.nutrition.uimodule.data.PassioConnector
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.passiosdk.core.config.PassioConfiguration
import ai.passio.passiosdk.core.config.PassioMode
import ai.passio.passiosdk.passiofood.PassioSDK
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.util.Date

class MainActivity : ComponentActivity() {

    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.text1)

        val passioConfiguration = PassioConfiguration(
            this.applicationContext,
            BuildConfig.SDK_KEY
        ).apply {
            sdkDownloadsModels = true
            debugMode = -333
        }

        PassioSDK.instance.configure(passioConfiguration) { passioStatus ->
            Log.d("HHHH", passioStatus.toString())
            when (passioStatus.mode) {
                PassioMode.NOT_READY -> onSDKError("Not ready")
                PassioMode.FAILED_TO_CONFIGURE -> onSDKError(getString(passioStatus.error!!.errorRes))
                PassioMode.IS_READY_FOR_DETECTION -> onSDKReady()
                PassioMode.IS_BEING_CONFIGURED -> {
                }
                PassioMode.IS_DOWNLOADING_MODELS -> {}
            }
        }
    }

    private fun onSDKError(error: String) {
        textView.text = "ERROR: $error"
    }

    private fun onSDKReady() {
        NutritionUIModule.launch(this)
        finish()
    }
}