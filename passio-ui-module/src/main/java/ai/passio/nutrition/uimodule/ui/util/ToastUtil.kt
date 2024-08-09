package ai.passio.nutrition.uimodule.ui.util

import android.content.Context
import android.widget.Toast

internal fun Context.toast(message: String?) {
    if (message.isNullOrEmpty()) return
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}