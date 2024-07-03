package ai.passio.nutrition.uimodule.ui.progress

import ai.passio.nutrition.uimodule.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.databinding.FragmentProgressBinding
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import com.google.android.material.tabs.TabLayoutMediator

class ProgressFragment : BaseFragment<ProgressViewModel>() {

    private var _binding: FragmentProgressBinding? = null
    private val binding: FragmentProgressBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding)
        {
            viewPager.adapter = MacrosViewPagerAdapter(this@ProgressFragment)
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                when (position) {
                    0 -> tab.text = requireContext().getString(R.string.macros)
                    1 -> tab.text = requireContext().getString(R.string.micros)
                }
            }.attach()
        }

    }

}