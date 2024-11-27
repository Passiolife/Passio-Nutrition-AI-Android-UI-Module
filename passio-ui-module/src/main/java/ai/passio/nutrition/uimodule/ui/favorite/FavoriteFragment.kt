package ai.passio.nutrition.uimodule.ui.favorite

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.databinding.FragmentFavoriteBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import ai.passio.nutrition.uimodule.ui.util.toast
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.yanzhenjie.recyclerview.SwipeMenuItem

class FavoriteFragment : BaseFragment<FavoriteViewModel>() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding: FragmentFavoriteBinding get() = _binding!!
    private lateinit var customFoodsAdapter: FavoriteAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private val baseToolbarListener = object : BaseToolbar.ToolbarListener {
        override fun onBack() {
            viewModel.navigateBack()
        }

        override fun onRightIconClicked() {
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObserver()
        with(binding)
        {
            toolbar.setup(getString(R.string.favorites), baseToolbarListener)
            toolbar.hideRightIcon()

            customFoodsAdapter = FavoriteAdapter(::onDetails, ::onLog)
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
                        sharedViewModel.detailsFoodRecord(customFoodsAdapter.getItem(adapterPosition), true)
                        viewModel.navigateToDetails()
                    }

                    1 -> {
                        viewModel.markAsUnFavorite(customFoodsAdapter.getItem(adapterPosition))
                    }
                }
            }
            rvFoods.adapter = customFoodsAdapter
        }
        viewModel.getFavoriteFoods()

    }

    private fun initObserver() {
        viewModel.showLoading.observe(viewLifecycleOwner) {
            binding.loading.isVisible = it
        }
        viewModel.favoriteListEvent.observe(viewLifecycleOwner) {
            binding.noDataFound.isVisible = it.isEmpty()
            customFoodsAdapter.updateItems(it)
        }
        viewModel.logFoodEvent.observe(viewLifecycleOwner, ::foodItemLogged)
    }

    private fun onDetails(foodRecord: FoodRecord) {
        sharedViewModel.detailsFoodRecord(foodRecord)
        viewModel.navigateToDetails()
    }

    private fun onLog(foodRecord: FoodRecord) {
        viewModel.logFood(foodRecord)
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