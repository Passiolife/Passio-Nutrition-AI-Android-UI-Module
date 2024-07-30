package ai.passio.nutrition.uimodule.ui.image

import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.databinding.FragmentImageFoodResultBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.util.ViewEXT.disable
import ai.passio.nutrition.uimodule.ui.util.ViewEXT.enable
import ai.passio.nutrition.uimodule.ui.util.toast
import ai.passio.passiosdk.passiofood.data.model.PassioAdvisorFoodInfo
import android.graphics.Bitmap
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ImageFoodResultFragment : BaseFragment<ImageFoodResultViewModel>() {

    private var _binding: FragmentImageFoodResultBinding? = null
    private val binding: FragmentImageFoodResultBinding get() = _binding!!

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
        with(binding)
        {
            enableLogButton(false)
            rvResult.adapter = FoodImageResultAdapter { selectedCount ->
                enableLogButton(selectedCount != 0)
            }
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
        showResultView(result.isNotEmpty())
        with(binding) {
            val adapter = rvResult.adapter as FoodImageResultAdapter
            adapter.addData(result)
        }
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