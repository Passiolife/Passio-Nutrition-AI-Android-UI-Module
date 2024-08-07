package ai.passio.nutrition.uimodule.ui.base

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.ui.activity.SharedViewModel
import ai.passio.nutrition.uimodule.ui.navigation.NavigationCommand
import ai.passio.nutrition.uimodule.ui.util.toast
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

abstract class BaseFragment<VM : BaseViewModel>(isSharedContext: Boolean = false) : Fragment() {
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
        when (navCommand) {
            is NavigationCommand.ToDirection -> findNavController().navigate(navCommand.directions)
            is NavigationCommand.Back -> findNavController().navigateUp()
        }
    }

    protected fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.dashboard_menu, popupMenu.menu)
        showMenuIcons(popupMenu)
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.my_profile -> {
                    navController.navigate(R.id.my_profile)
//                    viewModel.navigateToMyProfile()
                    true
                }

                R.id.settings -> {
                    navController.navigate(R.id.settings)
//                    viewModel.navigateToSettings()
                    true
                }

                R.id.log_out -> {
                    requireContext().toast("Logout successfully!")
                    requireActivity().finish()
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    fun showMenuIcons(popupMenu: PopupMenu) {
        try {
            val fields: Array<Field> = popupMenu.javaClass.declaredFields
            for (field in fields) {
                if ("mPopup" == field.name) {
                    field.isAccessible = true
                    val menuPopupHelper = field.get(popupMenu)
                    val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                    val setForceIcons =
                        classPopupHelper.getMethod("setForceShowIcon", Boolean::class.java)
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}