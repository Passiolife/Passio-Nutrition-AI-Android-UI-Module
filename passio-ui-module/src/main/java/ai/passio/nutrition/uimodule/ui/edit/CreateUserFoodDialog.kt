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

internal enum class CreateUserFoodType {
    USER_FOOD,
    PASSIO_FOOD,
    USER_RECIPE,
    PASSIO_RECIPE,
}

interface OnCreateFoodListener {
    fun onEdit(isUpdateLog: Boolean)
    fun onCreate(isUpdateLog: Boolean)
}

internal class CreateUserFoodDialog(
    private val isEditLogMode: Boolean,
    private val foodDetailType: CreateUserFoodType,
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

        with(binding) {

            when (foodDetailType) {
                CreateUserFoodType.USER_FOOD -> {
                    title.text = "Create or Edit User Food?"
                    description.text =
                        "Do you want to create a new user food based off this one, or edit the existing user food?"
                    edit.isVisible = isEditLogMode
                }

                CreateUserFoodType.PASSIO_FOOD -> {
                    title.text = "Create User Food?"
                    description.text = "You are about to create a user food from this food"
                    edit.isVisible = false
                }

                CreateUserFoodType.USER_RECIPE -> {
                    title.text = "Create or Edit User Recipe?"
                    description.text =
                        "Do you want to create a new user recipe based off this one, or edit the existing recipe?"
                    edit.isVisible = isEditLogMode
                }

                CreateUserFoodType.PASSIO_RECIPE -> {
                    title.text = "Create User Recipe?"
                    description.text = "You are about to create a user recipe from this food"
                    edit.isVisible = false
                }
            }

            updateLog.isVisible = isEditLogMode
            updateLog.isChecked = true
            cancel.setOnClickListener {
                dismiss()
            }
            edit.setOnClickListener {
                dismiss()
                onCreateFoodListener.onEdit(updateLog.isChecked && isEditLogMode)
            }
            create.setOnClickListener {
                dismiss()
                onCreateFoodListener.onCreate(updateLog.isChecked && isEditLogMode)
            }

            updateLog.setOnChangeListener { _, _ ->

            }
        }


    }
}