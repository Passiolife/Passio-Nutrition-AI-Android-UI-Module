package ai.passio.nutrition.uimodule.ui.myfood

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.FragmentMyFoodsBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import com.google.android.material.tabs.TabLayoutMediator

class MyFoodsFragment : BaseFragment<MyFoodsViewModel>() {

    private var _binding: FragmentMyFoodsBinding? = null
    private val binding: FragmentMyFoodsBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyFoodsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        with(binding)
        {
            toolbar.setup(getString(R.string.my_foods), baseToolbarListener)
            toolbar.hideRightIcon()
            viewPager.adapter = MyFoodsPagerAdapter(this@MyFoodsFragment)
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                when (position) {
                    0 -> tab.text = requireContext().getString(R.string.custom_foods)
                    1 -> tab.text = requireContext().getString(R.string.recipes)
                }
            }.attach()
        }

        arguments?.getBoolean("isRecipeShow", false)?.let {
            if (it) {
                binding.tabLayout.post {
                    binding.tabLayout.getTabAt(1)?.select()
                }
            }
        }

    }

    private val baseToolbarListener = object : BaseToolbar.ToolbarListener {
        override fun onBack() {
            viewModel.navigateBack()
        }

        override fun onRightIconClicked() {
            showPopupMenu(binding.toolbar.findViewById(R.id.toolbarMenu))
        }

    }

}