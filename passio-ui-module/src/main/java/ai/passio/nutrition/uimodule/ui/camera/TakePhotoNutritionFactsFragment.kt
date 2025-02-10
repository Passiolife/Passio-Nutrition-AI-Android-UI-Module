package ai.passio.nutrition.uimodule.ui.camera

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.data.SharedPrefUtils
import ai.passio.nutrition.uimodule.databinding.FragmentTakePhotoNutritionFactsBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.image.PhotoTipDialog
import ai.passio.nutrition.uimodule.ui.util.CaptureNutritionFactsLabelInfoDialog
import ai.passio.nutrition.uimodule.ui.util.PermissionUtil
import ai.passio.nutrition.uimodule.ui.util.toast
import ai.passio.nutrition.uimodule.ui.view.BitmapAnalyzer
import android.Manifest
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

internal class TakePhotoNutritionFactsFragment : BaseFragment<TakePhotoNutritionFactsViewModel>() {

    companion object {
        private const val TAG = "CameraXApp"
    }

    private var isInfoShown = false
    private lateinit var imageAnalyzer: ImageAnalysis

    private var _binding: FragmentTakePhotoNutritionFactsBinding? = null
    private val binding get() = _binding!!

    private val permissionUtil =
        PermissionUtil(this@TakePhotoNutritionFactsFragment, Manifest.permission.CAMERA)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTakePhotoNutritionFactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        with(binding) {

            captureButton.setOnClickListener {
                bitmapAnalyzer.takePhoto()
            }
            binding.toolbar.apply {
                setup(getString(R.string.barcode_scan), toolbarListener)
                hideRightIcon()
            }
        }

        permissionUtil.checkAndRequestPermission(onGranted = {
            startCamera()
        }, onDenied = {
            requireContext().toast("To access this feature you need to grant camera permission.")
            viewModel.navigateBack()
        })

        if (!isInfoShown) {
            isInfoShown = true

            CaptureNutritionFactsLabelInfoDialog(requireContext()).show()
        }

    }

    private fun initObservers() {
        sharedViewModel.barcodeToTakeNutritionFactsPhotoLD.observe(viewLifecycleOwner){
            viewModel.setBarcode(it)
        }
    }

    val toolbarListener = object : BaseToolbar.ToolbarListener {
        override fun onBack() {
            viewModel.navigateBack()
        }

        override fun onRightIconClicked() {

        }

    }


    private val bitmapAnalyzer = BitmapAnalyzer { bitmap ->
        // Use the bitmap here, e.g., display it or process it
        lifecycleScope.launch(Dispatchers.Main) {
            if (bitmap != null) {
                // Handle the bitmap
                onImageCaptured(bitmap)
            }
        }
    }

    private fun startCamera() {

        if (!SharedPrefUtils.get("isPhotoTipShown", Boolean::class.java)) {
            SharedPrefUtils.put("isPhotoTipShown", true)
            PhotoTipDialog().show(childFragmentManager, "PhotoTipDialog")
        }
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor(), bitmapAnalyzer)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onImageCaptured(capturedBitmap: Bitmap) {
        lifecycleScope.launch(Dispatchers.Main)
        {
            sharedViewModel.addNutritionFactsPhotoToResult(capturedBitmap, viewModel.getBarcode())
            viewModel.navigateToBarcodeImageFoodResult()
        }
    }
}