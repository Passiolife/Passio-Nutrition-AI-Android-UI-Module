package ai.passio.nutrition.uimodule.ui.base

import ai.passio.nutrition.uimodule.TestActivity
import ai.passio.nutrition.uimodule.ui.activity.SharedViewModel
import ai.passio.nutrition.uimodule.ui.navigation.NavigationCommand
import ai.passio.nutrition.uimodule.ui.navigation.Router
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import java.lang.reflect.ParameterizedType

abstract class BaseFragment<VM : BaseViewModel> : Fragment() {

    protected val viewModel: VM by lazy {
        ViewModelProvider(this)[getVMClass()]
    }
    protected val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleNavigation(NavigationCommand.Back)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        observeNavigation()
    }

    @Suppress("UNCHECKED_CAST")
    private fun getVMClass(): Class<VM> {
        return (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<VM>
    }

    private fun observeNavigation() {
        viewModel.navigation.observe(viewLifecycleOwner) { event ->
            handleNavigation(event)
        }
    }

    private fun handleNavigation(navCommand: NavigationCommand) {
        when (navCommand) {
            is NavigationCommand.ToDirection -> findNavController().navigate(navCommand.directions)
            is NavigationCommand.Back -> findNavController().navigateUp()
        }
    }
}