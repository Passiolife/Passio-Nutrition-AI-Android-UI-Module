package ai.passio.nutrition.uimodule.ui.camera

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.databinding.FragmentCameraRecognitionBinding
import ai.passio.nutrition.uimodule.domain.camera.RecognitionResult
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
import ai.passio.nutrition.uimodule.ui.util.PermissionUtil
import ai.passio.nutrition.uimodule.ui.util.ProgressDialog
import ai.passio.nutrition.uimodule.ui.util.toast
import ai.passio.passiosdk.core.camera.PassioCameraViewProvider
import ai.passio.passiosdk.passiofood.data.model.PassioFoodItem
import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import ai.passio.nutrition.uimodule.ui.view.tickseekbar.OnSeekChangeListener
import ai.passio.nutrition.uimodule.ui.view.tickseekbar.SeekParams
import ai.passio.nutrition.uimodule.ui.view.tickseekbar.TickSeekBar
import ai.passio.passiosdk.passiofood.Barcode
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class CameraRecognitionFragment : BaseFragment<CameraRecognitionViewModel>(),
    PassioCameraViewProvider,
    BaseToolbar.ToolbarListener, View.OnClickListener {

    private var _binding: FragmentCameraRecognitionBinding? = null
    private val binding: FragmentCameraRecognitionBinding get() = _binding!!
    private val permissionUtil =
        PermissionUtil(this@CameraRecognitionFragment, Manifest.permission.CAMERA)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraRecognitionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObserver()
        binding.recognitionResult.visibility = View.GONE
        viewModel.showLoading.observe(viewLifecycleOwner) { isLoading ->

            if (isLoading) {
                ProgressDialog.show(requireContext())
            } else {
                ProgressDialog.hide()
            }
        }
        setupToolbar()
        initOnClickCallback()

//        binding.recognitionResult.layoutParams.height = (resources.displayMetrics.heightPixels * 0.6).toInt()
        binding.recognitionResult.setRecognitionResultListener(recognitionResultListener)

        // Check for camera permission
        permissionUtil.checkAndRequestPermission(onGranted = {
            cameraPermissionGranted()
        }, onDenied = {
            requireContext().toast("Camera permission is required to use this feature.")
            viewModel.navigateBack()
        })

        binding.cameraFlash.setOnClickListener {
            viewModel.toggleCameraFlash()
        }

        binding.cameraZoomLevel.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams?) {
                if (seekParams != null && seekParams.fromUser) {
                    viewModel.setCameraZoomLevel(seekParams.progressFloat)
                }
            }

            override fun onStartTrackingTouch(seekBar: TickSeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: TickSeekBar?) {
            }

        }

    }

    private fun initObserver() {
        sharedViewModel.isAddIngredientFromSearchLD.observe(viewLifecycleOwner) { isAddIngredient ->
            viewModel.setIsAddIngredient(isAddIngredient)
        }
        sharedViewModel.nutritionFactsPhotoLoggedLD.observe(viewLifecycleOwner) { loggedRecord ->
            lifecycleScope.launch {
                delay(300)
                viewModel.stopDetection()
                foodItemLogged(ResultWrapper.Success(true))
            }
        }

        viewModel.recognitionResults.observe(viewLifecycleOwner, ::onRecognitionResult)
        viewModel.foodItemResult.observe(viewLifecycleOwner, ::editFoodItem)
        viewModel.logFoodEvent.observe(viewLifecycleOwner, ::foodItemLogged)
        viewModel.cameraZoomLevelRangeEvent.observe(viewLifecycleOwner, ::setupCameraZoomMode)
        viewModel.cameraFlashToggleEvent.observe(viewLifecycleOwner) { isON ->
            binding.cameraFlash.setImageResource(if (isON) R.drawable.ic_camera_flash_on else R.drawable.ic_camera_flash_off)
        }
        viewModel.addIngredientEvent.observe(viewLifecycleOwner) { foodRecord ->
            sharedViewModel.addFoodIngredient(FoodRecordIngredient(foodRecord))
            viewModel.navigateBackToEditRecipe()

        }
    }

    private fun setupCameraZoomMode(zoomLevel: Triple<Float, Float?, Float?>) {
        with(binding) {
            if (zoomLevel.second == null || zoomLevel.third == null) {
                cameraZoomLevel.visibility = View.GONE
            } else {
                cameraZoomLevel.visibility = View.VISIBLE
                cameraZoomLevel.min = zoomLevel.second!!
                cameraZoomLevel.max = zoomLevel.third!!
                cameraZoomLevel.setProgress(zoomLevel.first)
            }
        }
    }


    private fun initOnClickCallback() {
        with(binding)
        {
            keepScanning.setOnClickListener(this@CameraRecognitionFragment)
            viewDiary.setOnClickListener(this@CameraRecognitionFragment)
        }
    }

    private fun foodItemLogged(resultWrapper: ResultWrapper<Boolean>) {
        when (resultWrapper) {
            is ResultWrapper.Success -> {
                if (resultWrapper.value) {
                    with(binding)
                    {
                        recognitionResult.visibility = View.GONE
                        viewAddedToDiary.visibility = View.VISIBLE
                        recognitionResult.reset()
                    }
                } else {
                    requireContext().toast("Could not log food item.")
                }
            }

            is ResultWrapper.Error -> {
                requireContext().toast(resultWrapper.error)
            }
        }
    }

    private fun editFoodItem(resultWrapper: ResultWrapper<PassioFoodItem>) {

        when (resultWrapper) {
            is ResultWrapper.Success -> {
                editFoodRecord(FoodRecord(resultWrapper.value))
            }

            is ResultWrapper.Error -> {
                requireContext().toast(resultWrapper.error)
            }

        }
    }

    private fun editFoodRecord(foodRecord: FoodRecord) {

        if (viewModel.getIsAddIngredient()) {
            sharedViewModel.editIngredient(FoodRecordIngredient(foodRecord))
            viewModel.navigateToEditIngredient()
        } else {
            sharedViewModel.detailsFoodRecord(foodRecord)
            viewModel.navigateToEdit()
        }
    }

    private val recognitionResultListener = object :
        RecognitionResultView.RecognitionResultListener {

        override fun onNutritionFactsTapped(barcode: Barcode?) {
            sharedViewModel.addBarcodeToTakeNutritionFactsPhoto(barcode)
            viewModel.navigateToTakePhoto()
        }

        override fun onCancelled() {
            viewModel.startOrUpdateDetection()
        }

        override fun onLog(result: RecognitionResult) {
            viewModel.stopDetection()
            when (result) {
                /*is RecognitionResult.VisualRecognition -> {
                    viewModel.logFood(result.visualCandidate.passioID)
                }*/

                is RecognitionResult.FoodRecordRecognition -> {
                    viewModel.logFoodRecord(result.foodItem)
                }

//                is RecognitionResult.NutritionFactRecognition -> {
//
//                }

                else -> {

                }
            }
        }

        override fun onEdit(result: RecognitionResult) {
            viewModel.stopDetection()
            when (result) {
//                is RecognitionResult.VisualRecognition -> {
//                    viewModel.fetchFoodItemToEdit(result.visualCandidate.passioID)
//                }

                is RecognitionResult.FoodRecordRecognition -> {
                    editFoodRecord(result.foodItem)
                }

//                is RecognitionResult.NutritionFactRecognition -> {
//                    sharedViewModel.sendNutritionFactsToFoodCreator(result.nutritionFactsPair)
//                    viewModel.navigateToFoodCreator()
//                }

                else -> {

                }
            }
        }

        override fun onSearchTapped() {
            sharedViewModel.setIsAddIngredientFromScanning(viewModel.getIsAddIngredient())
            viewModel.navigateToSearch()
        }
    }


    private fun setupToolbar() {
        binding.toolbar.apply {
            setup(getString(R.string.barcode_scan), this@CameraRecognitionFragment)
            hideRightIcon()
//            setRightIcon(R.drawable.ic_info)
        }
    }

    private fun cameraPermissionGranted() {
        // Your code to start the camera
//        ScanInfoDialog.show(requireContext())
    }

    override fun onStart() {
        super.onStart()
        viewModel.startRecognitionSession(this)
    }

    override fun onStop() {
        viewModel.stopRecognitionSession()
        super.onStop()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun requestCameraLifecycleOwner(): LifecycleOwner = this

    override fun requestPreviewView(): PreviewView = binding.cameraPreview

    private fun onRecognitionResult(result: RecognitionResult) {
        binding.let {
            val isIngredient = viewModel.getIsAddIngredient()
            when (result) {
                is RecognitionResult.NoProductRecognition -> {
                    it.recognitionResult.visibility = View.VISIBLE
                    it.viewAddedToDiary.visibility = View.GONE
                    it.recognitionResult.reset()
                    it.recognitionResult.showNoProductView(result.barcode)
                }

                RecognitionResult.NoRecognition -> {
                    it.recognitionResult.visibility = View.VISIBLE
                    it.viewAddedToDiary.visibility = View.GONE
                    it.recognitionResult.reset()
                    it.recognitionResult.showScanningView()
                }

                is RecognitionResult.FoodRecordRecognition -> {
                    it.recognitionResult.visibility = View.VISIBLE
                    it.viewAddedToDiary.visibility = View.GONE
                    it.recognitionResult.showFoodRecordRecognition(
                        result,
                        if (isIngredient) resources.getString(R.string.add_ingredient) else resources.getString(
                            R.string.log
                        )
                    )
                }

                /* is RecognitionResult.VisualRecognition -> {
                     it.viewAddedToDiary.visibility = View.GONE
                     it.recognitionResult.visibility = View.VISIBLE
                     it.scanningMessage.visibility = View.GONE

                     it.recognitionResult.showVisualResult(
                         result,
                         if (isIngredient) resources.getString(R.string.add_ingredient) else resources.getString(
                             R.string.log
                         )
                     )
                 }

                 is RecognitionResult.NutritionFactRecognition -> {
                     it.viewAddedToDiary.visibility = View.GONE
                     it.recognitionResult.visibility = View.VISIBLE
                     it.scanningMessage.visibility = View.GONE

                     it.recognitionResult.showNutritionFactsResult(result)
                 }*/
            }
        }
    }

    override fun onBack() {
        viewModel.navigateBack()
    }

    override fun onRightIconClicked() {
//        ScanInfoDialog.show(requireContext(), true)
    }

    override fun onClick(p0: View?) {
        when (p0) {

            binding.keepScanning -> {
                viewModel.startOrUpdateDetection()
            }

            binding.viewDiary -> {
                viewModel.navigateToDiary()
            }

        }
    }
}