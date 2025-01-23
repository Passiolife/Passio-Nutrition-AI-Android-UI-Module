package ai.passio.nutrition.uimodule.ui.base

import ai.passio.nutrition.uimodule.ui.activity.SharedViewModel
import ai.passio.nutrition.uimodule.ui.navigation.NavigationCommand
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.reflect.ParameterizedType

internal abstract class BaseDialogFragment<VM : BaseViewModel>(isSharedContext: Boolean = false) : DialogFragment() {
    private lateinit var navController: NavController
    protected val viewModel: VM by lazy {
        ViewModelProvider(if (isSharedContext) requireActivity() else this)[getVMClass()]
    }
    protected val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
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
        lifecycleScope.launch(Dispatchers.Main) {

            findNavController().currentBackStackEntry
            when (navCommand) {
                is NavigationCommand.ToDirection -> findNavController().navigate(navCommand.directions)
                is NavigationCommand.Back -> {
                    if (!navController.navigateUp()) {
                        // No destinations in the back stack, finish the activity
                        requireActivity().finish()
                    }
                }
            }
        }
    }

}