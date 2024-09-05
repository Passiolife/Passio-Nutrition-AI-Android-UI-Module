package ai.passio.nutrition.uimodule.ui.myfood

import ai.passio.nutrition.uimodule.ui.customfoods.CustomFoodsFragment
import ai.passio.nutrition.uimodule.ui.myreceipes.MyRecipesFragment
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MyFoodsPagerAdapter(parentFragment: Fragment) : FragmentStateAdapter(parentFragment) {
    private val fragmentList = listOf(
        CustomFoodsFragment(),
        MyRecipesFragment()
    )

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}