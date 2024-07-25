package ai.passio.nutrition.uimodule.ui.util

import android.content.Context
import android.widget.Toast


private var toast: Toast? = null
internal fun Context.toast(message: String?) {
    if (message.isNullOrEmpty()) return

    toast = Toast(this)
    toast?.setText(message)
    toast?.duration = Toast.LENGTH_SHORT
    toast?.show()
}