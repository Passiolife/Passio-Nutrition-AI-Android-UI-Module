package ai.passio.nutrition.uimodule.ui.customfoods

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.databinding.FragmentCustomFoodsBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import ai.passio.nutrition.uimodule.ui.util.toast
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.yanzhenjie.recyclerview.SwipeMenuItem

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

            customFoodsAdapter = CustomFoodsAdapter(::onDetails, ::onLog)
            rvFoods.adapter = null

            rvFoods.setSwipeMenuCreator { leftMenu, rightMenu, position ->
                val editItem = SwipeMenuItem(requireContext()).apply {
                    text = getString(R.string.edit)
                    setTextColor(Color.WHITE)
                    setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.passio_primary
                        )
                    )
                    width = DesignUtils.dp2px(80f)
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                }
                rightMenu.addMenuItem(editItem)
                val deleteItem = SwipeMenuItem(requireContext()).apply {
                    text = getString(R.string.delete)
                    setTextColor(Color.WHITE)
                    setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.passio_red500
                        )
                    )
                    width = DesignUtils.dp2px(80f)
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                }
                rightMenu.addMenuItem(deleteItem)
            }
            rvFoods.setOnItemMenuClickListener { menuBridge, adapterPosition ->
                menuBridge.closeMenu()
                when (menuBridge.position) {
                    0 -> {
                        sharedViewModel.editCustomFood(
                            customFoodsAdapter.getItem(
                                adapterPosition
                            )
                        )
                        viewModel.navigateToFoodCreator()
                    }

                    1 -> {
                        //delete
                        viewModel.deleteCustomFood(customFoodsAdapter.getItem(adapterPosition).uuid)
                    }
                }
            }


//            rvFoods.addItemDecoration(VerticalSpaceItemDecoration(DesignUtils.dp2px(8f)))
            rvFoods.adapter = customFoodsAdapter
        }
        viewModel.getCustomFoods()

    }

    private fun initObserver() {
        viewModel.showLoading.observe(viewLifecycleOwner) {
            binding.loading.isVisible = it
        }
        viewModel.customFoodListEvent.observe(viewLifecycleOwner) {
            binding.noDataFound.isVisible = it.isEmpty()
            customFoodsAdapter.updateItems(it)
        }
        viewModel.logFoodEvent.observe(viewLifecycleOwner, ::foodItemLogged)
    }

    private fun onDetails(customFood: FoodRecord) {
        sharedViewModel.detailsFoodRecord(customFood)
        viewModel.navigateToEditFood()
    }

    private fun onLog(customFood: FoodRecord) {
        viewModel.logCustomFood(customFood)
    }

    private fun foodItemLogged(resultWrapper: ResultWrapper<Boolean>) {
        when (resultWrapper) {
            is ResultWrapper.Success -> {
                if (resultWrapper.value) {
                    requireContext().toast("Logged food successfully.")
                } else {
                    requireContext().toast("Could not log food item.")
                }
            }

            is ResultWrapper.Error -> {
                requireContext().toast(resultWrapper.error)
            }
        }
    }


}