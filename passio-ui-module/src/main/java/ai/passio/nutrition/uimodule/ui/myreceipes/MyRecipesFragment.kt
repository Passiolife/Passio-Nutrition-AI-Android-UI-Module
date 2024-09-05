package ai.passio.nutrition.uimodule.ui.myreceipes

import ai.passio.nutrition.uimodule.databinding.FragmentCustomFoodsBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.ui.base.BaseFragment

class MyRecipesFragment : BaseFragment<MyRecipesViewModel>() {

    private var _binding: FragmentCustomFoodsBinding? = null
    private val binding: FragmentCustomFoodsBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomFoodsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding)
        {

        }

    }



}