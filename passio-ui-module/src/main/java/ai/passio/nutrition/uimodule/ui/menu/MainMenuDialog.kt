package ai.passio.nutrition.uimodule.ui.menu

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.FragmentAddFoodBinding
import ai.passio.nutrition.uimodule.ui.activity.PassioUiModuleActivity
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.WindowManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment

internal class MainMenuDialog(
    passioUiModuleActivity: PassioUiModuleActivity
) : Dialog(passioUiModuleActivity) {

    val binding: FragmentAddFoodBinding = FragmentAddFoodBinding.inflate(layoutInflater)

    private val adapter = AddFoodAdapter(
        listOf(
            AddFoodOption(0, R.string.food_scanner, R.drawable.ic_food_scanner),
            AddFoodOption(1, R.string.text_search, R.drawable.icon_search),
            AddFoodOption(2, R.string.use_image, R.drawable.ic_image),
            AddFoodOption(3, R.string.ai_advisor, R.drawable.ic_advisor),
            AddFoodOption(4, R.string.voice_logging, R.drawable.ic_voice),
//            AddFoodOption(5, R.string.favorites, R.drawable.ic_favorites),
            AddFoodOption(6, R.string.my_foods, R.drawable.ic_my_foods),
        ),
        ::onOptionSelected
    )

    private var navController: NavController

    init {
        val navHostFragment =
            passioUiModuleActivity.supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        window?.setDimAmount(0.0f)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(binding.root)
        setCancelable(true)
        val layoutParams = window?.attributes
        layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        window?.attributes = layoutParams


        with(binding)
        {
            root.setBackgroundColor(Color.TRANSPARENT)
            addFoodList.adapter = adapter

            binding.root.setOnClickListener {
                dismiss()
            }
            buttonClose.setOnClickListener {
                dismiss()
            }
        }

        val animatorSet = AnimatorSet()
        val rotation = ObjectAnimator.ofFloat(binding.buttonClose, "rotation", 0f, 45f)
        val translation = ObjectAnimator.ofFloat(binding.addFoodList, "translationY", 300f, -200f, 0f)
        val alpha = ObjectAnimator.ofFloat(binding.addFoodList, "alpha", 0f, 1f)
        animatorSet.duration = 500
        animatorSet.playTogether(rotation, translation, alpha)
        animatorSet.start()

        animateDimAmount(0.0f, 0.5f, 500)
    }

    private fun animateDimAmount(from: Float, to: Float, duration: Long) {
        // Use ValueAnimator to animate the dim amount
        val animator = ValueAnimator.ofFloat(from, to)
        animator.duration = duration

        animator.addUpdateListener { animation ->
            val dimAmount = animation.animatedValue as Float
            val window = window

            if (window != null) {
                val params = window.attributes
                params.dimAmount = dimAmount
                window.attributes = params
            }
        }

        animator.start()
    }
    private fun onOptionSelected(id: Int) {
        dismiss()
        when (id) {
            0 -> navController.navigate(R.id.camera) //viewModel.navigate(AddFoodFragmentDirections.addFoodToCamera())
            1 -> navController.navigate(R.id.search) //viewModel.navigate(AddFoodFragmentDirections.addFoodToSearch())
            2 -> navController.navigate(R.id.take_select_photo) //viewModel.navigate(AddFoodFragmentDirections.addFoodToPhoto())
            3 -> navController.navigate(R.id.advisor) //viewModel.navigate(AddFoodFragmentDirections.addFoodToAdvisor())
            4 -> navController.navigate(R.id.voice_logging) //viewModel.navigate(AddFoodFragmentDirections.addFoodToVoiceLogging())
            6 -> navController.navigate(R.id.my_foods) //viewModel.navigate(AddFoodFragmentDirections.addFoodToMyFoods())
        }
    }
}
