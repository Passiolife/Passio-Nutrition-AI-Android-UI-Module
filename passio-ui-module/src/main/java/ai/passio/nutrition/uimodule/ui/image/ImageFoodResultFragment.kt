package ai.passio.nutrition.uimodule.ui.image

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.databinding.FragmentImageFoodResultBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
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

            search.setOnClickListener {
                sharedViewModel.setIsAddIngredientFromSearch(viewModel.getIsAddIngredient())
                viewModel.navigateToSearch()
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
        setIngredientMode(viewModel.getIsAddIngredient())
        sharedViewModel.isAddIngredientFromVoiceLD.observe(viewLifecycleOwner) { isAddIngredient ->
            viewModel.setIsAddIngredient(isAddIngredient)
            setIngredientMode(isAddIngredient)

        }
        sharedViewModel.photoFoodResultLD.observe(viewLifecycleOwner) {
            viewModel.setImageBitmaps(it)
            showFoodImages(uris = it)
        }

        viewModel.isFetchingResult.observe(viewLifecycleOwner) {
            binding.viewLoading.isVisible = it
        }
        viewModel.showLoading.observe(viewLifecycleOwner) {
            binding.loading.isVisible = it
        }
        viewModel.resultFoodInfo.observe(viewLifecycleOwner, ::showResult)
        viewModel.logFoodEvent.observe(viewLifecycleOwner, ::foodItemLogged)
        viewModel.addIngredientEvent.observe(viewLifecycleOwner, ::addIngredients)
    }


    private fun addIngredients(foodRecords: List<FoodRecordIngredient>) {
        sharedViewModel.addFoodIngredients(foodRecords)
        viewModel.navigateBackToRecipe()
    }

    private fun setIngredientMode(isOn: Boolean) {
        if (isOn) {
            binding.log.text = getString(R.string.add_ingredient)
        }
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
            adapter.addData(result, result.indices.toList())
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
            noResultFound.isVisible = !isResultFound
            resultView.isVisible = isResultFound
//            rvResult.isVisible = isResultFound
//            log.isVisible = isResultFound
//            cancel.isVisible = true
//            reselect.isVisible = !isResultFound
        }
    }


}