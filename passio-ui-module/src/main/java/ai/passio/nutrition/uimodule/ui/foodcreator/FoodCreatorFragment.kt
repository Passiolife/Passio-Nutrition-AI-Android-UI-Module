package ai.passio.nutrition.uimodule.ui.foodcreator

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.FragmentFoodCreatorBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.util.PhotoPickerListener
import ai.passio.nutrition.uimodule.ui.util.PhotoPickerManager
import android.net.Uri
import coil.load

class FoodCreatorFragment : BaseFragment<FoodCreatorViewModel>() {

    private var _binding: FragmentFoodCreatorBinding? = null
    private val binding: FragmentFoodCreatorBinding get() = _binding!!

    private val photoPickerManager = PhotoPickerManager()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodCreatorBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoPickerManager.init(this, photoPickerListener)
        initObserver()
        with(binding)
        {
            toolbar.setup(getString(R.string.food_creator), baseToolbarListener)
            toolbar.hideRightIcon()

            ivThumb.setOnClickListener {
                photoPickerManager.pickSingleImage()
            }
            lblThumb.setOnClickListener {
                photoPickerManager.pickSingleImage()
            }
        }

    }

    private val baseToolbarListener = object : BaseToolbar.ToolbarListener {
        override fun onBack() {
            viewModel.navigateBack()
        }

        override fun onRightIconClicked() {
        }

    }

    private fun initObserver() {

    }

    private val photoPickerListener = object : PhotoPickerListener {
        override fun onImagePicked(uris: List<Uri>) {
            if (uris.isNotEmpty()) {
                binding.ivThumb.load(uris[0])
            }
        }

    }

}