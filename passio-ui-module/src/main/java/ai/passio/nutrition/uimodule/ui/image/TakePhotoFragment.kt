package ai.passio.nutrition.uimodule.ui.image

import ai.passio.nutrition.uimodule.data.SharedPrefUtils
import ai.passio.nutrition.uimodule.databinding.FragmentTakePhotoBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseViewModel
import ai.passio.nutrition.uimodule.ui.view.BitmapAnalyzer
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors


class TakePhotoFragment : BaseFragment<BaseViewModel>() {

    companion object {
        private const val TAG = "CameraXApp"
        private const val REQUEST_CODE_PERMISSIONS = 10
        const val MAX_IMAGES = 7
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private lateinit var imageAdapter: ImageAdapter
    private val imageList: MutableList<Bitmap> = mutableListOf()
    private lateinit var imageAnalyzer: ImageAnalysis

    private var _binding: FragmentTakePhotoBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTakePhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            imageAdapter = ImageAdapter(imageList) {
                imageList.removeAt(it)
                imageAdapter.notifyItemRemoved(it)
                validateImageCount()
            }
            recyclerView.adapter = imageAdapter
            captureButton.setOnClickListener {
                bitmapAnalyzer.takePhoto()
            }
            next.setOnClickListener {
                navigateToFindResult(imageList)
            }
            cancel.setOnClickListener {
                viewModel.navigateBack()
            }
        }

        validateImageCount()
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

    }

    private fun validateImageCount() {
        if (imageList.size >= MAX_IMAGES) {
            binding.captureButton.isEnabled = false
            binding.captureButton.alpha = 0.6f
        } else {
            binding.captureButton.isEnabled = true
            binding.captureButton.alpha = 1.0f
        }
    }

    private val bitmapAnalyzer = BitmapAnalyzer { bitmap ->
        // Use the bitmap here, e.g., display it or process it
        lifecycleScope.launch(Dispatchers.Main) {
            if (bitmap != null) {
                // Handle the bitmap
                imageList.add(bitmap)
                imageAdapter.notifyItemInserted(imageList.size - 1)
                binding.recyclerView.smoothScrollToPosition(imageList.size - 1)
                validateImageCount()
//            imageList.add(bitmap.copy(bitmap.config, true))
            }
        }
    }

    private fun startCamera() {

        if (!SharedPrefUtils.get("isPhotoTipShown", Boolean::class.java)) {
            SharedPrefUtils.put("isPhotoTipShown",true)
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

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                viewModel.navigateBack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun navigateToFindResult(imageUris: List<Bitmap>) {
        lifecycleScope.launch(Dispatchers.Main)
        {
            sharedViewModel.addPhotoFoodResult(imageUris)
            viewModel.navigate(TakePhotoFragmentDirections.takePhotoToImageFoodResult())
        }
    }

}