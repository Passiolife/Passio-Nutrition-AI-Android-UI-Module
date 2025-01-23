package ai.passio.nutrition.uimodule.ui.util

import ai.passio.nutrition.uimodule.databinding.DialogCommonBinding
import ai.passio.nutrition.uimodule.ui.util.StringKT.isValid
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.WindowManager
import androidx.core.view.isVisible
import ai.passio.nutrition.uimodule.R
import android.annotation.SuppressLint

internal interface OnCommonDialogListener {
    fun onNegativeAction()
    fun onPositiveAction()
}


@SuppressLint("SetTextI18n")
internal class CustomFoodCreatedInfoDialog(
    context: Context,
) : Dialog(context) {

    val binding: DialogCommonBinding = DialogCommonBinding.inflate(layoutInflater)

    init {
        window?.setLayout(
            DesignUtils.screenWidth(context) - DesignUtils.dp2px(20f),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(binding.root)
        setCancelable(false)
        binding.cancel.isVisible = false
        binding.title.text = "A Custom Food Has Been Created"
        val text = "The edits have been saved for future logs. You can edit this item from your My Foods lists."
        val spannable = SpannableString(text)

// Find the start and end indices of "My Foods"
        val start = text.indexOf("My Foods")
        val end = start + "My Foods".length

// Apply bold style to "My Foods"
        if (start >= 0) {
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        binding.description.text = spannable
        binding.create.text = context.getString(R.string.ok)
        binding.create.setOnClickListener {
            dismiss()
        }
    }
}

internal class CommonDialog(
    context: Context,
    positiveActionText: String?,
    negativeActionText: String,
    title: String?,
    description: String,
    listener: OnCommonDialogListener,
) : Dialog(context) {

    val binding: DialogCommonBinding = DialogCommonBinding.inflate(layoutInflater)

    init {
        window?.setLayout(
            DesignUtils.screenWidth(context) - DesignUtils.dp2px(20f),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(binding.root)
        setCancelable(false) // Make it non-cancelable
//        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        val layoutParams = window?.attributes
//        layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
//        layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
//        window?.attributes = layoutParams

        if (!title.isValid()) {
            binding.title.isVisible = false
        } else {
            binding.title.text = title
        }

        if (!positiveActionText.isValid()) {
            binding.create.isVisible = false
        } else {
            binding.create.text = positiveActionText
        }

        binding.description.text = description
        binding.cancel.text = negativeActionText
        binding.create.text = positiveActionText
        binding.cancel.setOnClickListener {
            listener.onNegativeAction()
            dismiss()
        }
        binding.create.setOnClickListener {
            listener.onPositiveAction()
            dismiss()
        }
    }

    companion object {
        fun show(
            context: Context,
            positiveActionText: String?,
            negativeActionText: String,
            title: String?,
            description: String,
            listener: OnCommonDialogListener
        ) {
            val dialog = CommonDialog(
                context,
                positiveActionText,
                negativeActionText,
                title,
                description,
                listener
            )
            dialog.show()

        }


    }

}
