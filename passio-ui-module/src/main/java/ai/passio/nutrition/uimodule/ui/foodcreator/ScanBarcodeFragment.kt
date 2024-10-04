package ai.passio.nutrition.uimodule.ui.foodcreator

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.FragmentScanBarcodeBinding
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.util.toast
import ai.passio.passiosdk.core.camera.PassioCameraViewProvider
import ai.passio.passiosdk.passiofood.Barcode
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner

class ScanBarcodeFragment : BaseFragment<ScanBarcodeViewModel>(),
    PassioCameraViewProvider,
    BaseToolbar.ToolbarListener {

    private var _binding: FragmentScanBarcodeBinding? = null
    private val binding: FragmentScanBarcodeBinding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBarcodeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.scanBarcodeStatusEvent.observe(viewLifecycleOwner, ::onRecognitionResult)

        setupToolbar()
        initOnClickCallback()

        // Check for camera permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request permission
            requestCameraPermission()
        } else {
            // Permission is already granted
            cameraPermissionGranted()
        }

    }

    private fun initOnClickCallback() {
        with(binding)
        {
            cancel.setOnClickListener {
                viewModel.navigateBack()
            }


        }
    }


    private fun sendResult(barcode: Barcode) {
        sharedViewModel.sendBarcodeScanResult(barcode)
        viewModel.navigateBack()
    }

    private fun showBarcodeInSystemView() {
        with(binding) {
            viewResult.isVisible = true
            resultTitle.text = getString(R.string.barcode_in_system)
            resultInfo.text = getString(R.string.barcode_in_system_info)
            createFood.text = getString(R.string.create_custom_food_anyway)
        }
    }

    private fun showCustomFoodAlreadyExistView() {
        with(binding) {
            viewResult.isVisible = true
            resultTitle.text = getString(R.string.custom_food_already_exists)
            resultInfo.text = getString(R.string.custom_food_already_exists_info)
            createFood.text = getString(R.string.create_custom_food_without_barcode)
        }
    }


    private fun setupToolbar() {
        binding.toolbar.apply {
            setup(getString(R.string.food_creator), this@ScanBarcodeFragment)
            hideRightIcon()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted
            cameraPermissionGranted()
        } else {
            // Permission is denied
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                // Permission denied without "Don't ask again"
                showPermissionDeniedMessage()
            } else {
                // Permission denied with "Don't ask again"
                showPermissionDeniedPermanentlyMessage()
            }
        }
    }


    private fun requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun cameraPermissionGranted() {
        // Your code to start the camera
    }

    private fun showPermissionDeniedMessage() {
        // Show a message explaining why the permission is needed
        AlertDialog.Builder(requireContext())
            .setTitle("Permission needed")
            .setMessage("Camera permission is needed to access this feature.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                requestCameraPermission()
            }
            .show()
    }

    private fun showPermissionDeniedPermanentlyMessage() {
        // Show a message guiding the user to the app settings
        AlertDialog.Builder(requireContext())
            .setTitle("Permission needed")
            .setMessage("Camera permission is needed to access this feature. Please enable it in the app settings.")
            .setPositiveButton("Open Settings") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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

    private fun onRecognitionResult(result: ScanBarcodeStatus) {
        with(binding) {
            viewItem.setOnClickListener {
            }
            createFood.setOnClickListener {
            }
            when (result) {
                ScanBarcodeStatus.SCANNING -> {
                    viewResult.visibility = View.GONE
                    scanningMessage.visibility = View.VISIBLE
                }

                ScanBarcodeStatus.NEW_BARCODE -> {
                    viewResult.visibility = View.GONE
                    scanningMessage.visibility = View.GONE
                    sendResult(viewModel.geBarcode())
                }

                ScanBarcodeStatus.BARCODE_IN_SYSTEM -> {
                    viewResult.visibility = View.VISIBLE
                    scanningMessage.visibility = View.GONE
                    showBarcodeInSystemView()
                    viewItem.setOnClickListener {
                        viewModel.existingSystemItem?.let {
                            sharedViewModel.detailsFoodRecord(it)
                            viewModel.navigateToFoodDetails()
                        }
                    }
                    createFood.setOnClickListener {
                        sendResult(viewModel.geBarcode())
                        viewModel.navigateBack()
                    }
                }

                ScanBarcodeStatus.CUSTOM_FOOD_ALREADY_EXIST -> {
                    viewResult.visibility = View.VISIBLE
                    scanningMessage.visibility = View.GONE
                    showCustomFoodAlreadyExistView()
                    viewItem.setOnClickListener {
                        viewModel.existingCustomFood?.let {
                            sharedViewModel.detailsFoodRecord(it)
                            viewModel.navigateToFoodDetails()
                        }
                    }

                    createFood.setOnClickListener {
                        sendResult("")
                        viewModel.navigateBack()
                    }
                }

                ScanBarcodeStatus.NOT_FOUND -> {
                    requireContext().toast("Barcode not found. Go back and try again!")
                }
            }
        }
    }

    override fun onBack() {
        viewModel.navigateBack()
    }

    override fun onRightIconClicked() {
    }


}