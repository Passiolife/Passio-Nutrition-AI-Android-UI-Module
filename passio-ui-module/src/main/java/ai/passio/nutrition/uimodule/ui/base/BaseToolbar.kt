package ai.passio.nutrition.uimodule.ui.base

import ai.passio.nutrition.uimodule.databinding.ToolbarLayoutBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible

class BaseToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    interface ToolbarListener {
        fun onBack()
        fun onRightIconClicked()
    }

    private val binding: ToolbarLayoutBinding =
        ToolbarLayoutBinding.inflate(LayoutInflater.from(context), this)
    private var listener: ToolbarListener? = null

    init {

        binding.toolbarMenu.setOnClickListener {
            this.listener?.onRightIconClicked()
        }
    }

    fun setRightIcon(resId: Int) {
        binding.toolbarMenu.setImageResource(resId)
    }
    fun hideRightIcon() {
        binding.toolbarMenu.isVisible = false
    }
    fun showRightIcon() {
        binding.toolbarMenu.isVisible = true
    }

    fun setup(title: String, listener: ToolbarListener) {
        this.listener = listener

        binding.toolbarTitle.text = title
        binding.toolbarBack.setOnClickListener {
            listener.onBack()
        }
    }
}