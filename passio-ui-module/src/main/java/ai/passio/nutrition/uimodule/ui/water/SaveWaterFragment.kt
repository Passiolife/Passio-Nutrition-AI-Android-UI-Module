package ai.passio.nutrition.uimodule.ui.water

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.databinding.FragmentSaveWeightBinding
import ai.passio.nutrition.uimodule.ui.activity.UserCache
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.model.WaterRecord
import ai.passio.nutrition.uimodule.ui.util.StringKT.singleDecimal
import ai.passio.nutrition.uimodule.ui.util.showDatePickerDialog
import ai.passio.nutrition.uimodule.ui.util.showTimePickerDialog
import ai.passio.nutrition.uimodule.ui.util.toast
import android.text.Editable
import android.text.TextWatcher

class SaveWaterFragment : BaseFragment<WaterTrackingViewModel>() {

    private var _binding: FragmentSaveWeightBinding? = null
    private val binding: FragmentSaveWeightBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSaveWeightBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding)
        {
            toolbar.setup(getString(R.string.water_tracking), baseToolbarListener)
            toolbar.hideRightIcon()
            lblWeight.text = getString(R.string.water_consumed)
            weight.hint = getString(R.string.water_consumed)
            weight.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(p0: Editable?) {
                    viewModel.updateWeight(p0.toString())
                }

            })

            dayValue.setOnClickListener {
                showDatePickerDialog(requireContext()) { selectedDate ->
                    viewModel.setDay(selectedDate)
                }
            }

            timeValue.setOnClickListener {
                showTimePickerDialog(requireContext()) { selectedTime ->
                    viewModel.setTime(selectedTime)
                }
            }

            save.setOnClickListener {
                viewModel.updateWeightRecord()
            }
            cancel.setOnClickListener {
                viewModel.navigateBack()
            }
        }

        initObserver()
        viewModel.initRecord(null)

    }

    private fun initObserver() {
        viewModel.weightRecordCurrentEvent.observe(viewLifecycleOwner, ::updateRecord)
        viewModel.saveRecord.observe(viewLifecycleOwner, ::recordSaved)
        sharedViewModel.addWaterLD.observe(viewLifecycleOwner) {
            viewModel.initRecord(it)
        }
    }

    private fun recordSaved(resultWrapper: ResultWrapper<Boolean>) {
        when (resultWrapper) {
            is ResultWrapper.Success -> {
                if (resultWrapper.value) {
                    requireContext().toast("Water record saved!")
                    viewModel.navigateBack()
                } else {
                    requireContext().toast("Could not record water. Please try again.")
                }
            }

            is ResultWrapper.Error -> {
                requireContext().toast(resultWrapper.error)
            }
        }
    }


    private fun updateRecord(weightRecord: WaterRecord) {
        with(binding)
        {
            val currentValue = weightRecord.getWaterInCurrentUnit()
            if (currentValue <= 0.0) {
                weight.text?.clear()
            } else {
                weight.setText(currentValue.singleDecimal())
            }
            weightUnit.text = UserCache.getProfile().measurementUnit.waterUnit.value
            dayValue.text = weightRecord.getDisplayDay()
            timeValue.text = weightRecord.getDisplayTime()
        }
    }

    private val baseToolbarListener = object : BaseToolbar.ToolbarListener {
        override fun onBack() {
            viewModel.navigateBack()
        }

        override fun onRightIconClicked() {

        }

    }

}