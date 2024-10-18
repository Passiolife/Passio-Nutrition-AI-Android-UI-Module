package ai.passio.nutrition.uimodule.ui.activity

import ai.passio.nutrition.uimodule.NutritionUIModule
import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.data.Repository
import ai.passio.nutrition.uimodule.data.RoomDbPassioConnector
import ai.passio.nutrition.uimodule.databinding.ActivityPassioUiModuleBinding
import ai.passio.nutrition.uimodule.ui.menu.MainMenuDialog
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController

internal class PassioUiModuleActivity : AppCompatActivity() {

    private var _binding: ActivityPassioUiModuleBinding? = null
    private val binding: ActivityPassioUiModuleBinding get() = _binding!!

    private val sharedViewModel: SharedViewModel by viewModels()
    private val navigationIds = listOf(
        R.id.dashboard,
        R.id.diary,
        R.id.mealplan,
        R.id.progress
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val connector =
            NutritionUIModule.getConnector() ?: RoomDbPassioConnector(applicationContext)
//            NutritionUIModule.getConnector() ?: SharedPrefsPassioConnector(applicationContext)
        Repository.create(applicationContext, connector)

        _binding = ActivityPassioUiModuleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedViewModel.userProfileCacheEvent.observe(this) {
            setupNav()
        }

        sharedViewModel.checkAndMigrateDataFromOldDB()
    }

    private fun setupNav() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navHostFragment.navController.graph =
            navHostFragment.navController.navInflater.inflate(R.navigation.main_nav_graph)
        val navController = navHostFragment.navController

        setupWithNavController(binding.bottomNavigation, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in navigationIds) {
                binding.bottomNavigation.visibility = View.VISIBLE
                binding.buttonAdd.visibility = View.VISIBLE
            } else {
                binding.bottomNavigation.visibility = View.GONE
                binding.buttonAdd.visibility = View.GONE
            }
        }
//        navController.navigate(R.id.dashboard)
        binding.viewLoading.isVisible = false
        binding.buttonAdd.setOnClickListener {
//            navController.navigate(R.id.add_food)
            MainMenuDialog(this@PassioUiModuleActivity).show()
        }
    }

}