package ai.passio.nutrition.uimodule.ui.image

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.databinding.FragmentImageFoodResultBinding
import ai.passio.nutrition.uimodule.service.RecordService
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.util.ViewEXT.disable
import ai.passio.nutrition.uimodule.ui.util.ViewEXT.enable
import ai.passio.nutrition.uimodule.ui.util.toast
import ai.passio.passiosdk.passiofood.data.model.PassioAdvisorFoodInfo
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

private const val REQUEST_CODE_CAPTURE_PERMISSION = 1471
private const val REQUEST_CODE_STORAGE = 3212

class ImageFoodResultFragment : BaseFragment<ImageFoodResultViewModel>() {

    private var _binding: FragmentImageFoodResultBinding? = null
    private val binding: FragmentImageFoodResultBinding get() = _binding!!

    private lateinit var projectionManager: MediaProjectionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageFoodResultBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()

        projectionManager = requireContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        with(binding)
        {
            enableLogButton(false)
            rvResult.adapter = FoodImageResultAdapter(object : OnItemSelectChange {
                override fun onItemSelectChange(selectedCount: Int) {
                    enableLogButton(selectedCount != 0)
                }

                override fun onIndexSelect(index: Int) {
                }

                override fun onIndexDeselect(index: Int) {
                }

            })
            cancel.setOnClickListener {
                viewModel.navigateBack()
            }
            reselect.setOnClickListener {
                viewModel.navigateBack()
            }
            log.setOnClickListener {
                viewModel.logRecords((rvResult.adapter as FoodImageResultAdapter).getSelectedItems())
            }
        }

    }

    private fun enableLogButton(isEnable: Boolean) {
        with(binding)
        {
            if (isEnable) {
                log.enable()
            } else {
                log.disable()
            }
        }
    }

    private fun initObserver() {
        sharedViewModel.photoFoodResultLD.observe(viewLifecycleOwner) {
            viewModel.setImageBitmaps(it)
            showFoodImages(uris = it)
        }

        viewModel.isProcessing.observe(viewLifecycleOwner) {
            binding.viewLoading.isVisible = it
        }
        viewModel.showLoading.observe(viewLifecycleOwner) {
            binding.loading.isVisible = it
        }
        viewModel.resultFoodInfo.observe(viewLifecycleOwner, ::showResult)
        viewModel.logFoodEvent.observe(viewLifecycleOwner, ::foodItemLogged)
    }

    private fun foodItemLogged(resultWrapper: ResultWrapper<Boolean>) {
        when (resultWrapper) {
            is ResultWrapper.Success -> {
                if (resultWrapper.value) {
                    requireContext().toast("Food item(s) logged.")
                    viewModel.navigateToDiary()
                } else {
                    requireContext().toast("Could not log food item(s).")
                }
            }

            is ResultWrapper.Error -> {
                requireContext().toast(resultWrapper.error)
            }
        }
    }

    private fun showResult(result: List<PassioAdvisorFoodInfo>) {
        //        showResultView(result.isNotEmpty())
        //        with(binding) {
        //            val adapter = rvResult.adapter as FoodImageResultAdapter
        //            adapter.addData(result, result.indices.toList())
        //        }
        sharedViewModel.setReelData(viewModel.getFirstBitmap(), result)
        viewModel.navigateToReel()
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
//            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_STORAGE)
//            }
//        }
//        startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CODE_CAPTURE_PERMISSION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_STORAGE) {
            return
        }

        if (requestCode != REQUEST_CODE_CAPTURE_PERMISSION) {
            Log.e("RRRR", "Unknown request code: $requestCode");
            return
        }
        if (resultCode == RESULT_OK) {
            startRecordingService(resultCode, data!!)
            binding.rvImages.postDelayed({
                stopRecordingService()
            }, 10 * 1000L)
        } else {
            Toast.makeText(requireContext(), "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
            return
        }
    }

    private fun startRecordingService(resultCode: Int, data: Intent) {
        Log.d("RRRR", "startRecordingService")
        val intent = RecordService.newIntent(requireContext(), resultCode, data)
        requireContext().startService(intent)
    }

    private fun stopRecordingService() {
        Log.d("RRRR", "stopRecordingService")
        val intent = Intent(requireContext(), RecordService::class.java)
        requireContext().stopService(intent)
    }

    private fun showFoodImages(uris: List<Bitmap>) {
        lifecycleScope.launch {
            showResultView(uris.isNotEmpty())

            binding.rvImages.adapter = FoodImageAdapter(uris)
        }
    }

    private fun showResultView(isResultFound: Boolean) {
        with(binding)
        {
            rvResult.isVisible = isResultFound
            log.isVisible = isResultFound
            cancel.isVisible = true
            reselect.isVisible = !isResultFound
            noResult.isVisible = !isResultFound
        }
    }

}