package ai.passio.nutrition.uimodule.ui.customfoods

import ai.passio.nutrition.uimodule.databinding.FragmentCustomFoodsBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.copy
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import ai.passio.nutrition.uimodule.ui.view.VerticalSpaceItemDecoration

class CustomFoodsFragment : BaseFragment<CustomFoodsViewModel>() {

    private var _binding: FragmentCustomFoodsBinding? = null
    private val binding: FragmentCustomFoodsBinding get() = _binding!!
    private lateinit var customFoodsAdapter: CustomFoodsAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomFoodsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObserver()
        with(binding)
        {
            createFood.setOnClickListener {
                viewModel.navigateToFoodCreator()
            }
            rvFoods.addItemDecoration(VerticalSpaceItemDecoration(DesignUtils.dp2px(8f)))
            customFoodsAdapter = CustomFoodsAdapter(::onEdit, ::onLog)
            rvFoods.adapter = customFoodsAdapter
        }
        viewModel.getCustomFoods()

    }

    private fun initObserver() {
        viewModel.customFoodListEvent.observe(viewLifecycleOwner) {
            customFoodsAdapter.updateItems(it)
        }
    }

    private fun onEdit(customFood: FoodRecord) {
        sharedViewModel.editFoodRecord(customFood.copy())
        viewModel.navigateToEditFood()
    }

    private fun onLog(customFood: FoodRecord) {

    }


}