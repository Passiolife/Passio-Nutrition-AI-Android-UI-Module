package ai.passio.nutrition.uimodule.ui.edit

import ai.passio.nutrition.uimodule.databinding.DialogCreateUserFoodBinding
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import ai.passio.nutrition.uimodule.ui.util.ViewEXT.setOnChangeListener
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment

interface OnCreateFoodListener {
    fun onCreateFood(isUpdateLog: Boolean)
}

class CreateUserFoodDialog(
    private val isEditMode: Boolean,
    private val onCreateFoodListener: OnCreateFoodListener
) : DialogFragment() {

    private var _binding: DialogCreateUserFoodBinding? = null
    private val binding: DialogCreateUserFoodBinding get() = _binding!!


    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            DesignUtils.screenWidth(requireContext()) - DesignUtils.dp2px(20f),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreateUserFoodBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.updateLog.isVisible = isEditMode
        binding.updateLog.isChecked = true
        binding.cancel.setOnClickListener {
            dismiss()
        }
        binding.create.setOnClickListener {
            dismiss()
            onCreateFoodListener.onCreateFood(binding.updateLog.isChecked && isEditMode)
        }

        binding.updateLog.setOnChangeListener { _, _ ->

        }
    }
}