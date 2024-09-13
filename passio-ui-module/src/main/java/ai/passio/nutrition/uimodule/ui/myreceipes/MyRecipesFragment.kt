package ai.passio.nutrition.uimodule.ui.myreceipes

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.databinding.FragmentCustomFoodsBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.customfoods.CustomFoodsAdapter
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import ai.passio.nutrition.uimodule.ui.util.toast
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.yanzhenjie.recyclerview.SwipeMenuItem

class MyRecipesFragment : BaseFragment<MyRecipesViewModel>() {

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
            createFood.text = getString(R.string.create_new_recipe)

            createFood.setOnClickListener {
                viewModel.navigateToEditRecipe()
            }

            customFoodsAdapter = CustomFoodsAdapter(::onRecipeDetails, ::onLog)
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
                        sharedViewModel.editRecipe(
                            customFoodsAdapter.getItem(
                                adapterPosition
                            )
                        )
                        viewModel.navigateToEditRecipe()
                    }

                    1 -> {
                        //delete
                        viewModel.deleteRecipe(customFoodsAdapter.getItem(adapterPosition).uuid)
                    }
                }
            }

            rvFoods.adapter = customFoodsAdapter
        }
        viewModel.getRecipes()

    }

    private fun initObserver() {
        viewModel.showLoading.observe(viewLifecycleOwner) {
            binding.loading.isVisible = it
        }
        viewModel.recipeListEvent.observe(viewLifecycleOwner) {
            binding.noDataFound.isVisible = it.isEmpty()
            customFoodsAdapter.updateItems(it)
        }
        viewModel.logRecipeEvent.observe(viewLifecycleOwner, ::foodItemLogged)
    }

    private fun onRecipeDetails(customFood: FoodRecord) {
        sharedViewModel.detailsFoodRecord(customFood)
        viewModel.navigateToDetails()
    }

    private fun onLog(customFood: FoodRecord) {
        viewModel.logRecipe(customFood)
    }

    private fun foodItemLogged(resultWrapper: ResultWrapper<Boolean>) {
        when (resultWrapper) {
            is ResultWrapper.Success -> {
                if (resultWrapper.value) {
                    requireContext().toast("Logged recipe successfully.")
                } else {
                    requireContext().toast("Could not log recipe. please try again.")
                }
            }

            is ResultWrapper.Error -> {
                requireContext().toast(resultWrapper.error)
            }
        }
    }


}