package ai.passio.nutrition.uimodule

import ai.passio.nutrition.uimodule.data.Repository
import ai.passio.nutrition.uimodule.databinding.ActivityTestBinding
import ai.passio.nutrition.uimodule.ui.camera.CameraRecognitionFragment
import ai.passio.nutrition.uimodule.ui.edit.EditFoodFragment
import ai.passio.nutrition.uimodule.ui.engineering.EngineeringFragment
import ai.passio.nutrition.uimodule.ui.engineering.PhotoFragment
import ai.passio.nutrition.uimodule.ui.engineering.RecognitionFragment
import ai.passio.nutrition.uimodule.ui.engineering.VoiceFragment
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
import ai.passio.nutrition.uimodule.ui.navigation.Router
import ai.passio.nutrition.uimodule.ui.search.FoodSearchFragment
import ai.passio.passiosdk.passiofood.PassioFoodDataInfo
import ai.passio.passiosdk.passiofood.PassioSDK
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment

class TestActivity : AppCompatActivity(), Router {

    private val searchFragment = FoodSearchFragment()
    private val editFragment = EditFoodFragment()
    private val engineeringFragment = EngineeringFragment()
    private val cameraRecognitionFragment = CameraRecognitionFragment()

    private lateinit var binding: ActivityTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Repository.create(this.applicationContext)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replace(cameraRecognitionFragment)
    }

    override fun navigateUp() {

    }

    override fun navigateToEdit(searchResult: PassioFoodDataInfo) {
        replace(editFragment)
    }

    override fun editIngredient(ingredient: FoodRecordIngredient) {
        replace(EditFoodFragment())
    }

    override fun navigateToTop3() {
        replace(RecognitionFragment(0))
    }

    override fun navigateToImageLocal() {
        replace(PhotoFragment(true))
    }

    override fun navigateToBarcode() {
        replace(RecognitionFragment(1))
    }

    override fun navigateToOCR() {
        replace(RecognitionFragment(2))
    }

    override fun navigateToNutritionFacts() {
        replace(RecognitionFragment(3))
    }

    override fun navigateToVoice() {
        replace(VoiceFragment())
    }

    override fun navigateToImageRemote() {
        replace(PhotoFragment(false))
    }

    override fun navigateToAdvisor() {

    }

    private fun replace(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun add(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragmentContainer, fragment)
        transaction.commit()
    }
}