package ai.passio.nutrition.uimodule.ui.weight

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
import ai.passio.nutrition.uimodule.ui.model.WeightRecord
import ai.passio.nutrition.uimodule.ui.util.showDatePickerDialog
import ai.passio.nutrition.uimodule.ui.util.showTimePickerDialog
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast

class SaveWeightFragment : BaseFragment<WeightTrackingViewModel>() {

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
            toolbar.setup(getString(R.string.weight_tracking), baseToolbarListener)
            toolbar.hideRightIcon()

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
        viewModel.removeRecord.observe(viewLifecycleOwner, ::recordRemoved)
        sharedViewModel.addWeightLD.observe(viewLifecycleOwner){
            viewModel.initRecord(it)
        }
    }

    private fun recordSaved(resultWrapper: ResultWrapper<Boolean>) {
        when (resultWrapper) {
            is ResultWrapper.Success -> {
                if (resultWrapper.value) {
                    Toast.makeText(
                        requireContext(),
                        "Weight record saved!",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.navigateBack()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Could not record weight. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            is ResultWrapper.Error -> {
                Toast.makeText(
                    requireContext(),
                    resultWrapper.error,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun recordRemoved(resultWrapper: ResultWrapper<Boolean>) {
        when (resultWrapper) {
            is ResultWrapper.Success -> {
                if (resultWrapper.value) {
                    Toast.makeText(
                        requireContext(),
                        "Weight record removed!",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.navigateBack()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Could not remove weight. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            is ResultWrapper.Error -> {
                Toast.makeText(
                    requireContext(),
                    resultWrapper.error,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateRecord(weightRecord: WeightRecord) {
        with(binding)
        {
            weight.setText(weightRecord.getDisplayWeight())
            weightUnit.text = UserCache.getProfile().measurementUnit.weightUnit.value
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