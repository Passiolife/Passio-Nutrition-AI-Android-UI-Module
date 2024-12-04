package ai.passio.nutrition.uimodule.ui.activity

import ai.passio.nutrition.uimodule.data.SharedPrefUtils
import ai.passio.nutrition.uimodule.ui.util.SingleLiveEvent
import ai.passio.passiosdk.passiofood.PassioAccountListener
import ai.passio.passiosdk.passiofood.PassioSDK
import ai.passio.passiosdk.passiofood.data.model.PassioTokenBudget
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

internal const val KEY_TOKEN_TRACKING_STATUS = "TokenTrackingStatus"

internal class TokenTrackingViewModel : ViewModel() {

    private val _isTokenTrackingEnabled = SingleLiveEvent<Boolean>()
    val isTokenTrackingEnabled: LiveData<Boolean> = _isTokenTrackingEnabled

    private val _tokenInfo = SingleLiveEvent<String>()
    val tokenInfo: LiveData<String> = _tokenInfo

    fun checkTokenTrackingStatus() {
        val isEnabled = SharedPrefUtils.get(KEY_TOKEN_TRACKING_STATUS, Boolean::class.java)
        if (isEnabled) {
            enableTokenTracking()
        } else {
            disableTokenTracking()
        }
    }

    private fun enableTokenTracking() {
        SharedPrefUtils.put(KEY_TOKEN_TRACKING_STATUS, true)
        _isTokenTrackingEnabled.postValue(true)
        PassioSDK.instance.setAccountListener(passioAccountListener)
    }

    fun disableTokenTracking() {
        SharedPrefUtils.put(KEY_TOKEN_TRACKING_STATUS, false)
        _isTokenTrackingEnabled.postValue(false)
        PassioSDK.instance.setAccountListener(null)
    }

    private val passioAccountListener = object : PassioAccountListener {
        override fun onTokenBudgetUpdate(tokenBudget: PassioTokenBudget) {
            val info = "API: ${tokenBudget.apiName}\n" +
                    "tokensUsed: ${tokenBudget.tokensUsed}\n" +
                    "budgetCap: ${tokenBudget.budgetCap}\n" +
                    "periodUsage: ${tokenBudget.periodUsage}\n" +
                    "usedPercent: ${tokenBudget.usedPercent()}\n"

            _tokenInfo.postValue(info)
        }

    }

}

