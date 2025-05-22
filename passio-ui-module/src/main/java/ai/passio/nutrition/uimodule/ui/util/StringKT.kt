package ai.passio.nutrition.uimodule.ui.util

import ai.passio.passiosdk.passiofood.data.measurement.Grams
import ai.passio.passiosdk.passiofood.data.measurement.Milliliters
import ai.passio.passiosdk.passiofood.data.model.PassioServingSize
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import java.text.DecimalFormat


object StringKT {

    fun PassioServingSize.isGram(): Boolean {
        return this.unitName.equals(Grams.unitName, true) || this.unitName.equals(
            Grams.symbol,
            true
        ) || this.unitName.equals(
            Milliliters.symbol, true
        )
    }

    fun String.isGram(): Boolean {
        val unitName = this
        return unitName.equals(Grams.unitName, true) || unitName.equals(
            Grams.symbol,
            true
        ) || unitName.equals(
            Milliliters.symbol, true
        )
    }

    fun String?.isValid(): Boolean {
        return this != null && this.trim().isNotEmpty()
    }

    fun String.capitalized(): String {
        return this.split(" ").joinToString(" ") { it.capitalizeWord() }
    }

    private fun String.capitalizeWord(): String {
        return this.lowercase().replaceFirstChar {
            if (it.isLowerCase())
                it.titlecase()
            else it.toString()
        }
    }


    //    private val oneDecimalFormat = DecimalFormat("0.#")
    private val twoDecimalFormat = DecimalFormat("0.##")
    fun Double.singleDecimal(): String {
        val value = this
        return if (value % 1 == 0.0) {
            value.toInt().toString()
        } else {
//            String.format("%.2f", value).trimEnd('0').trimEnd('.')
            twoDecimalFormat.format(this)//.trimEnd('0').trimEnd('.')
        }

//        return oneDecimalFormat.format(this)
    }

    fun Float.singleDecimal(): String {
//        return oneDecimalFormat.format(this)

        val value = this
        return if (value % 1 == 0.0f) {
            value.toInt().toString()
        } else {
            twoDecimalFormat.format(this)//.trimEnd('0').trimEnd('.')
        }
    }

    fun AppCompatTextView.setDrawableEnd(drawableResId: Int) {
        val drawable: Drawable? = ContextCompat.getDrawable(this.context, drawableResId)
        this.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
    }

    fun String.setSpannableBold(boldText: String): SpannableString {
        val spannableString = SpannableString(this)
        val startIndex = this.indexOf(boldText)

        if (startIndex != -1) {
            val endIndex = startIndex + boldText.length
            val boldSpan = StyleSpan(Typeface.BOLD)
            spannableString.setSpan(
                boldSpan,
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return spannableString
    }

    // Function to load JSON from assets
   /* fun loadJsonFromAssets(fileName: String): String? {
        return try {
            val inputStream = PassioUiModuleActivity.getContext().assets.open(fileName)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            bufferedReader.use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }*/


}