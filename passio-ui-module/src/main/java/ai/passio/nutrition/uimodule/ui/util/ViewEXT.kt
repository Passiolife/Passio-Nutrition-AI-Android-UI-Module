package ai.passio.nutrition.uimodule.ui.util

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.widget.AppCompatEditText

object ViewEXT {
    fun View.enable() {
        this.isEnabled = true
        this.alpha = 1.0f
    }

    fun View.disable() {
        this.isEnabled = false
        this.alpha = 0.6f
    }

    fun AppCompatEditText.setupEditable(
        onValueChanged: (value: String) -> Unit,
    ) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                onValueChanged.invoke(s.toString())
            }

            override fun beforeTextChanged(
                s: CharSequence?, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
            }

        })
    }
}