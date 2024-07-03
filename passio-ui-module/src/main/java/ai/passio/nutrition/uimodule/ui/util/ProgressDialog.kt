package ai.passio.nutrition.uimodule.ui.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.widget.ProgressBar

internal class ProgressDialog(context: Context) : Dialog(context) {

    init {
        setCancelable(false) // Make it non-cancelable
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(ProgressBar(context))
        val layoutParams = window?.attributes
        layoutParams?.width = ViewGroup.LayoutParams.WRAP_CONTENT
        layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        window?.attributes = layoutParams
    }

    companion object {

        private var progressDialog: ProgressDialog? = null

        // Show progress dialog
        fun show(context: Context) {
            if (progressDialog == null) {
                progressDialog = ProgressDialog(context)
                progressDialog!!.show()
            }
        }

        // Hide progress dialog
        fun hide() {
            progressDialog?.dismiss()
            progressDialog = null
        }
    }

}
