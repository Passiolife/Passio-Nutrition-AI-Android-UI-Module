package ai.passio.uimodule

import ai.passio.nutrition.uimodule.TestActivity
import ai.passio.nutrition.uimodule.ui.activity.PassioUiModuleActivity
import ai.passio.passiosdk.core.config.PassioConfiguration
import ai.passio.passiosdk.core.config.PassioMode
import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.uimodule.databinding.ActivityMainBinding
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.text1)

        val passioConfiguration = PassioConfiguration(
            this.applicationContext,
            "Q5mb9Y078mXNsXrtZHD9IvSr4vC6SsjnbvvHUYWG2VEx"
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
        startActivity(Intent(this, PassioUiModuleActivity::class.java))
        finish()
    }
}