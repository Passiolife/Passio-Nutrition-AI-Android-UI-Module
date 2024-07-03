package ai.passio.nutrition.uimodule.ui.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.databinding.FragmentDashboardBinding

class DashboardFragment : BaseFragment<DashboardViewModel>() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding: FragmentDashboardBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            dashButton.setOnClickListener {
                viewModel.navigateToDiary()
            }
        }
    }
}