package ai.passio.nutrition.uimodule.ui.activity

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.data.Repository
import ai.passio.nutrition.uimodule.databinding.ActivityPassioUiModuleBinding
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController

class PassioUiModuleActivity : AppCompatActivity() {

    private var _binding: ActivityPassioUiModuleBinding? = null
    private val binding: ActivityPassioUiModuleBinding get() = _binding!!

    private val navigationIds = listOf(
        R.id.dashboard,
        R.id.diary,
        R.id.mealplan,
        R.id.progress
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Repository.create(this.applicationContext)
        _binding = ActivityPassioUiModuleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
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

        binding.buttonAdd.setOnClickListener {
            navController.navigate(R.id.add_food)
        }
    }

}