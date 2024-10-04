package ai.passio.nutrition.uimodule.ui.util

import ai.passio.nutrition.uimodule.R
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton.OnCheckedChangeListener
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat

object ViewEXT {
    fun View.sentEnable(isEnable: Boolean) {
        if (isEnable)
            this.enable()
        else
            this.disable()
    }

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

    fun SwitchCompat.setOnChangeListener(listened: OnCheckedChangeListener) {
        if (this.isChecked) {
            this.markSwitchOn()
        } else {
            this.markSwitchOff()
        }
        this.setOnCheckedChangeListener { view, isChecked ->
            listened.onCheckedChanged(view, isChecked)
            if (isChecked) {
                this.markSwitchOn()
            } else {
                this.markSwitchOff()
            }


        }
    }


    fun SwitchCompat.markSwitchOn() {
        val context = this.context
        this.thumbTintList =
            ContextCompat.getColorStateList(context, R.color.passio_primary)
        this.trackTintList =
            ContextCompat.getColorStateList(context, R.color.passio_primary40p)
    }

    fun SwitchCompat.markSwitchOff() {
        val context = this.context
        this.thumbTintList =
            ContextCompat.getColorStateList(context, R.color.passio_white)
        this.trackTintList =
            ContextCompat.getColorStateList(context, R.color.passio_gray300)
    }

    fun AppCompatEditText.showKeyboard() {
        this.requestFocus()
        this.post {
            val imm =
                this.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}