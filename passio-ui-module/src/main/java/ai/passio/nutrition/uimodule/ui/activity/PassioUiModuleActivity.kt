package ai.passio.nutrition.uimodule.ui.activity

import ai.passio.nutrition.uimodule.NutritionUIModule
import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.data.Repository
import ai.passio.nutrition.uimodule.data.RoomDbPassioConnector
import ai.passio.nutrition.uimodule.databinding.ActivityPassioUiModuleBinding
import ai.passio.nutrition.uimodule.ui.menu.MainMenuDialog
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController

internal class PassioUiModuleActivity : AppCompatActivity() {

    private var _binding: ActivityPassioUiModuleBinding? = null
    private val binding: ActivityPassioUiModuleBinding get() = _binding!!

    private val sharedViewModel: SharedViewModel by viewModels()
    private val tokenTrackingViewModel: TokenTrackingViewModel by viewModels()
    private val navigationIds = listOf(
        R.id.dashboard,
        R.id.diary,
        R.id.mealplan,
        R.id.progress
    )

    // Variables to store drag offsets and bounds
    private var offsetX = 0f
    private var offsetY = 0f
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private lateinit var parentRect: Rect

    companion object {

        private lateinit var context: Context
        internal fun getContext(): Context {
            return context
        }
    }

    override fun onStart() {
        super.onStart()
        context = applicationContext
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val connector =
            NutritionUIModule.getConnector() ?: RoomDbPassioConnector(applicationContext)
//            NutritionUIModule.getConnector() ?: SharedPrefsPassioConnector(applicationContext)
        Repository.create(applicationContext, connector)

        _binding = ActivityPassioUiModuleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenTrackingObserver()
        setupDraggableBox()
        sharedViewModel.userProfileCacheEvent.observe(this) {
            setupNav()
            tokenTrackingViewModel.checkTokenTrackingStatus()
        }

        sharedViewModel.checkAndMigrateDataFromOldDB()
    }

    private fun tokenTrackingObserver() {
        tokenTrackingViewModel.isTokenTrackingEnabled.observe(this) { isEnabled ->
            if (isEnabled) {
                binding.draggableBox.visibility = View.VISIBLE
            } else {
                binding.draggableBox.visibility = View.GONE
            }
        }
        tokenTrackingViewModel.tokenInfo.observe(this) { info ->
            binding.tokenInfoText.text = info
        }
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

        val navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setRestoreState(true)
            .setPopUpTo(navController.graph.startDestinationId, false)
            .build()
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dashboard -> {
                    navController.navigate(R.id.dashboard, null, navOptions)
                    true
                }

                R.id.diary -> {
                    navController.navigate(R.id.diary, null, navOptions)
                    true
                }

                R.id.mealplan -> {
                    navController.navigate(R.id.mealplan, null, navOptions)
                    true
                }

                R.id.progress -> {
                    navController.navigate(R.id.progress, null, navOptions)
                    true
                }

                else -> false

            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDraggableBox() {
        with(binding) {
            draggableBox.setOnTouchListener { view, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Store initial touch positions
                        initialTouchX = motionEvent.rawX
                        initialTouchY = motionEvent.rawY
                        offsetX = view.x
                        offsetY = view.y

                        // Capture parent view bounds
                        val parentView = view.parent as View
                        parentRect = Rect(0, 0, parentView.width, parentView.height)
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        // Calculate new position
                        val deltaX = motionEvent.rawX - initialTouchX
                        val deltaY = motionEvent.rawY - initialTouchY

                        // Clamp within parent bounds
                        val newX = (offsetX + deltaX).coerceIn(0f, parentRect.width().toFloat() - view.width)
                        val newY =
                            (offsetY + deltaY).coerceIn(0f, parentRect.height().toFloat() - view.height)

                        // Update position
                        view.x = newX
                        view.y = newY
                        true
                    }

                    else -> false
                }
            }
        }
    }

}