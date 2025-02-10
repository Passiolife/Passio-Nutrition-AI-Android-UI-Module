package ai.passio.nutrition.uimodule.ui.camera

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.FragmentNutritionFactsPhotoResultBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.image.EditNutritionFactsDialog
import ai.passio.nutrition.uimodule.ui.image.OnEditNutritionFactsListener
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
import ai.passio.nutrition.uimodule.ui.model.ImageFoodResult
import ai.passio.nutrition.uimodule.ui.util.NoNutritionFactsResultDialog
import ai.passio.nutrition.uimodule.ui.util.OnCommonDialogListener
import android.graphics.Bitmap
import coil.load
import coil.transform.RoundedCornersTransformation

internal class NutritionFactsPhotoResultFragment :
    BaseFragment<NutritionFactsPhotoResultViewModel>() {

    private var _binding: FragmentNutritionFactsPhotoResultBinding? = null
    private val binding: FragmentNutritionFactsPhotoResultBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentNutritionFactsPhotoResultBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        with(binding)
        {
            toolbar.setup(getString(R.string.barcode_scan), baseToolbarListener)
            toolbar.hideRightIcon()

            cancelFetch.setOnClickListener {
                viewModel.navigateBack()
            }

        }

    }

    private val onEditNutritionFactsListener = object : OnEditNutritionFactsListener {
        override fun onChanged(
            editedIndex: Int,
            updatedImageFoodResult: ImageFoodResult,
            isNewCreated: Boolean
        ) {
//            if (isNewCreated) {
//                showCustomFoodCreatedInfo()
//            }
            sharedViewModel.setLoggedNutritionFactsResult(updatedImageFoodResult.record)
            viewModel.navigateBackToScanBarcode()
        }

        override fun onCancelled(
            editedIndex: Int,
            imageFoodResult: ImageFoodResult
        ) {
            viewModel.navigateBack()
        }
    }


    private fun showEditNutritionFactsDialog(indexToEdit: Int, imageFoodResult: ImageFoodResult) {
        EditNutritionFactsDialog(
            editIndex = indexToEdit,
            imageFoodRecordResult = imageFoodResult,
            isFromBarcodeScan = true,
            onEditNutritionFactsListener = onEditNutritionFactsListener
        ).show(
            childFragmentManager,
            "EditNutritionFactsDialog"
        )
    }


    private val baseToolbarListener = object : BaseToolbar.ToolbarListener {
        override fun onBack() {
            viewModel.navigateBack()
        }

        override fun onRightIconClicked() {

        }

    }


    private fun hideAll() {
        with(binding)
        {
//            viewLoading.isVisible = false

        }
    }

    private fun initObserver() {
        setIngredientMode(viewModel.getIsAddIngredient())

        sharedViewModel.isAddIngredientFromVoiceLD.observe(viewLifecycleOwner) { isAddIngredient ->
            viewModel.setIsAddIngredient(isAddIngredient)
            setIngredientMode(isAddIngredient)

        }
        sharedViewModel.nutritionFactsPhotoLD.observe(viewLifecycleOwner) {
            showImagePreview(it.second)
            viewModel.setImageBitmap(bitmap = it.second, barcode = it.first)
        }

        viewModel.isFetchingResult.observe(viewLifecycleOwner) {
            if (it) {
                hideAll()
//                binding.viewLoading.isVisible = true
                binding.progress.startProgress()
            } else {
                binding.progress.stopProgress()
//                binding.viewLoading.isVisible = false
            }
        }

        viewModel.resultFoodInfoEvent.observe(viewLifecycleOwner, ::showResult)
        viewModel.addIngredientEvent.observe(viewLifecycleOwner, ::addIngredients)
    }


    private fun showImagePreview(bitmap: Bitmap) {
        binding.previewImage.load(bitmap) {
            transformations(RoundedCornersTransformation(16f))
        }
    }


    private fun addIngredients(foodRecords: List<FoodRecordIngredient>) {
        sharedViewModel.addFoodIngredients(foodRecords)
        viewModel.navigateBackToRecipe()
    }

    private fun setIngredientMode(isOn: Boolean) {
        if (isOn) {
//            binding.log.text = getString(R.string.add_ingredient)
//            binding.createRecipe.isVisible = false
        }
    }


    private fun showResult(result: ImageFoodResult?) {
        with(binding) {
            hideAll()
            if (result == null) {
                noResultFound()
            } else {
                showEditNutritionFactsDialog(0, imageFoodResult = result)
            }

        }
    }


    private fun noResultFound() {
        NoNutritionFactsResultDialog(requireContext(), object : OnCommonDialogListener {
            override fun onNegativeAction() {
                viewModel.navigateBack()
            }

            override fun onPositiveAction() {
                viewModel.getCustomFoodByEnterManually()
            }

        }).show()
    }


}