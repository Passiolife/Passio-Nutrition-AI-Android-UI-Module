package ai.passio.nutrition.uimodule.ui.progress

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MacrosViewPagerAdapter(parentFragment: Fragment) : FragmentStateAdapter(parentFragment) {
    private val fragmentList = listOf(
        MacrosFragment(),
        MicrosFragment()
    )

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}