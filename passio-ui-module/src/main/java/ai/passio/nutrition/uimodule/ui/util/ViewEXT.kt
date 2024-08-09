package ai.passio.nutrition.uimodule.ui.util

import android.view.View

object ViewEXT {
    fun View.enable() {
        this.isEnabled = true
        this.alpha = 1.0f
    }

    fun View.disable() {
        this.isEnabled = false
        this.alpha = 0.6f
    }
}