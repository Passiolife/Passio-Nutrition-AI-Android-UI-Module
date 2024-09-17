package ai.passio.nutrition.uimodule.ui.editrecipe

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.DialogPickIngredientMenuBinding
import ai.passio.nutrition.uimodule.ui.menu.AddFoodAdapter
import ai.passio.nutrition.uimodule.ui.menu.AddFoodOption
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager

interface OnPickIngredientOption {
    fun onPickIngredient(addFoodOption: AddFoodOption)
}

class PickIngredientMenuDialog(private val onPickIngredientOption: OnPickIngredientOption) :
    DialogFragment() {

    private val menuItems = listOf(
//        AddFoodOption(5, R.string.favorites, R.drawable.ic_favorites),
        AddFoodOption(4, R.string.voice_logging, R.drawable.ic_voice),
        AddFoodOption(1, R.string.text_search, R.drawable.icon_search),
//        AddFoodOption(0, R.string.food_scanner, R.drawable.ic_food_scanner) //implemented code but this function no longer needed.
    )
    private val adapter = AddFoodAdapter(
        menuItems,
        ::onOptionSelected
    )

    private var _binding: DialogPickIngredientMenuBinding? = null
    private val binding: DialogPickIngredientMenuBinding get() = _binding!!


    override fun onStart() {
        super.onStart()
//        dialog?.window?.setLayout(
//            DesignUtils.screenWidth(requireContext()) - DesignUtils.dp2px(20f),
//            WindowManager.LayoutParams.WRAP_CONTENT
//        )
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPickIngredientMenuBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            addFoodList.layoutManager = LinearLayoutManager(requireContext()).apply {
                reverseLayout = true
            }
            addFoodList.adapter = adapter

            buttonClose.setOnClickListener {
                dismiss()
            }
            root.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun onOptionSelected(id: Int) {
        menuItems.find { it.id == id }?.let {
            dismiss()
            onPickIngredientOption.onPickIngredient(it)
        }
        /*when (id) {
            0 -> viewModel.navigate(AddFoodFragmentDirections.addFoodToCamera())
            1 -> viewModel.navigate(AddFoodFragmentDirections.addFoodToSearch())
            2 -> viewModel.navigate(AddFoodFragmentDirections.addFoodToPhoto())
            3 -> viewModel.navigate(AddFoodFragmentDirections.addFoodToAdvisor())
            4 -> viewModel.navigate(AddFoodFragmentDirections.addFoodToVoiceLogging())
            6 -> viewModel.navigate(AddFoodFragmentDirections.addFoodToMyFoods())
        }*/
    }
}