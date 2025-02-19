package ai.passio.nutrition.uimodule.ui.foodcreator

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.FragmentScanBarcodeBinding
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.model.BarcodeScanResult
import ai.passio.nutrition.uimodule.ui.util.PermissionUtil
import ai.passio.nutrition.uimodule.ui.util.toast
import ai.passio.passiosdk.core.camera.PassioCameraViewProvider
import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner

internal class ScanBarcodeFragment : BaseFragment<ScanBarcodeViewModel>(),
    PassioCameraViewProvider,
    BaseToolbar.ToolbarListener {

    private var _binding: FragmentScanBarcodeBinding? = null
    private val binding: FragmentScanBarcodeBinding get() = _binding!!
    private val permissionUtil =
        PermissionUtil(this@ScanBarcodeFragment, Manifest.permission.CAMERA)

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

        // Check for camera permission
        permissionUtil.checkAndRequestPermission(onGranted = {
            cameraPermissionGranted()
        }, onDenied = {

            viewModel.navigateBack()
        })

    }

//    private fun sendResult(result: Pair<Barcode, FoodRecord?>) {
//        sharedViewModel.sendBarcodeScanResult(result.first, result.second)
//        viewModel.navigateBack()
//    }

    private fun sendResult(barcodeScanResult: BarcodeScanResult) {
        sharedViewModel.sendBarcodeScanResult(barcodeScanResult)
        viewModel.navigateBack()
    }

    private fun showBarcodeInSystemView() {
        with(binding) {
            viewResult.isVisible = true
            resultTitle.text = getString(R.string.barcode_in_system)
            resultInfo.text = getString(R.string.barcode_in_system_info)
            importExisting.text = getString(R.string.import_existing_data)
            viewItem.text = getString(R.string.use_barcode_only)
        }
    }

    private fun showCustomFoodAlreadyExistView() {
        with(binding) {
            viewResult.isVisible = true
            resultTitle.text = getString(R.string.custom_food_already_exists)
            resultInfo.text = getString(R.string.custom_food_already_exists_info)
            importExisting.text = getString(R.string.edit_existing_data)
            viewItem.text = getString(R.string.create_new_item)
        }
    }


    private fun setupToolbar() {
        binding.toolbar.apply {
            setup(getString(R.string.food_creator), this@ScanBarcodeFragment)
            hideRightIcon()
        }
    }


    private fun cameraPermissionGranted() {
        // Your code to start the camera
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

    private fun onRecognitionResult(results: Pair<ScanBarcodeStatus, BarcodeScanResult?>) {
        val result = results.first
        val barcodeScanResult = results.second
        with(binding) {
            viewItem.setOnClickListener {
            }
            importExisting.setOnClickListener {
            }
            when (result) {
                ScanBarcodeStatus.SCANNING -> {
                    viewResult.visibility = View.GONE
                    scanningMessage.visibility = View.VISIBLE
                }

                ScanBarcodeStatus.NEW_BARCODE -> {
                    viewResult.visibility = View.GONE
                    scanningMessage.visibility = View.GONE
                    barcodeScanResult?.let {
                        sendResult(barcodeScanResult)
                    }
                }

                ScanBarcodeStatus.BARCODE_IN_SYSTEM -> {
                    viewResult.visibility = View.VISIBLE
                    scanningMessage.visibility = View.GONE
                    showBarcodeInSystemView()
                    viewItem.setOnClickListener {
                        //use barcode only
                        barcodeScanResult?.let {
                            barcodeScanResult.record = null
                            sendResult(barcodeScanResult)
//                            sharedViewModel.detailsFoodRecord(it)
//                            viewModel.navigateToFoodDetails()
                        }
                    }
                    importExisting.setOnClickListener {
                        //import existing data
                        barcodeScanResult?.let {
                            sendResult(it)
//                            viewModel.navigateBack()
                        }
                    }
                }

                ScanBarcodeStatus.CUSTOM_FOOD_ALREADY_EXIST -> {
                    viewResult.visibility = View.VISIBLE
                    scanningMessage.visibility = View.GONE
                    showCustomFoodAlreadyExistView()
                    viewItem.setOnClickListener {
                        //Create new item, do not import this item or barcode
                        viewModel.navigateBack()
                    }

                    importExisting.setOnClickListener {
                        //edit existing
                        barcodeScanResult?.let {
//                            barcodeScanResult.barcode = ""
                            sendResult(it)
//                            viewModel.navigateBack()
                        }
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