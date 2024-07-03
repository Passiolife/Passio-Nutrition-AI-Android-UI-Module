package ai.passio.nutrition.uimodule.ui.base

import ai.passio.nutrition.uimodule.databinding.ToolbarLayoutBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout

class BaseToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    interface ToolbarListener {
        fun onBack()
        fun onRightIconClicked()
    }

    private val binding: ToolbarLayoutBinding
    private var listener: ToolbarListener? = null

    init {
        binding = ToolbarLayoutBinding.inflate(LayoutInflater.from(context), this)

        binding.toolbarMenu.setOnClickListener {
            this.listener?.onRightIconClicked()
        }
    }

    fun setRightIcon(resId: Int) {
        binding.toolbarMenu.setImageResource(resId)
    }

    fun setup(title: String, listener: ToolbarListener) {
        this.listener = listener

        binding.toolbarTitle.text = title
        binding.toolbarBack.setOnClickListener {
            listener.onBack()
        }
    }
}