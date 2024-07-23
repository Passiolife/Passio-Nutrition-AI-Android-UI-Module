package ai.passio.nutrition.uimodule.ui.util

import android.graphics.drawable.Drawable
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import java.text.DecimalFormat


object StringKT {

    fun String.capitalized(): String {
        return this.replaceFirstChar {
            if (it.isLowerCase())
                it.titlecase()
            else it.toString()
        }
    }


    private val oneDecimalFormat = DecimalFormat("0.#")
    fun Double.singleDecimal(): String {
        return oneDecimalFormat.format(this)
    }
    private val twoDecimalFormat = DecimalFormat("0.##")
    fun Double.twoDecimal(): String {
        return twoDecimalFormat.format(this)
    }

    fun AppCompatTextView.setDrawableEnd(drawableResId: Int) {
        val drawable: Drawable? = ContextCompat.getDrawable(this.context, drawableResId)
        this.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
    }


}