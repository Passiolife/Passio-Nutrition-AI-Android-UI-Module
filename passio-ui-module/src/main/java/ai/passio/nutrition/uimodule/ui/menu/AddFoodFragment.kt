package ai.passio.nutrition.uimodule.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.FragmentAddFoodBinding
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import androidx.recyclerview.widget.LinearLayoutManager

class AddFoodFragment : BaseFragment<BaseViewModel>() {

    private val adapter = AddFoodAdapter(
        listOf(
            AddFoodOption(0, R.string.food_scanner, R.drawable.ic_food_scanner),
            AddFoodOption(1, R.string.text_search, R.drawable.icon_search),
            AddFoodOption(2, R.string.use_image, R.drawable.ic_image),
            AddFoodOption(3, R.string.ai_advisor, R.drawable.ic_advisor),
            AddFoodOption(4, R.string.voice_logging, R.drawable.ic_voice),
//            AddFoodOption(5, R.string.favorites, R.drawable.ic_favorites),
//            AddFoodOption(0, R.string.my_foods, R.drawable.ic_my_foods),
        ),
        ::onOptionSelected
    )
    private lateinit var binding: FragmentAddFoodBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            addFoodList.layoutManager = LinearLayoutManager(requireContext()).apply {
                reverseLayout = true
            }
            addFoodList.adapter = adapter

            buttonClose.setOnClickListener {
                viewModel.navigateBack()
            }
        }
    }

    private fun onOptionSelected(id: Int) {
        when (id) {
            0 -> viewModel.navigate(AddFoodFragmentDirections.addFoodToCamera())
            1 -> viewModel.navigate(AddFoodFragmentDirections.addFoodToSearch())
            2 -> viewModel.navigate(AddFoodFragmentDirections.addFoodToPhoto())
            3 -> viewModel.navigate(AddFoodFragmentDirections.addFoodToAdvisor())
            4 -> viewModel.navigate(AddFoodFragmentDirections.addFoodToVoiceLogging())
        }
    }
}