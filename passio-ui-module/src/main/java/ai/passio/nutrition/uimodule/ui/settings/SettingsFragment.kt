package ai.passio.nutrition.uimodule.ui.settings

import ai.passio.nutrition.uimodule.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.databinding.FragmentSettingsBinding
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.profile.GenericSpinnerAdapter
import ai.passio.nutrition.uimodule.ui.profile.LengthUnit
import ai.passio.nutrition.uimodule.ui.profile.WeightUnit
import android.widget.AdapterView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat

class SettingsFragment : BaseFragment<SettingsViewModel>() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObserver()

        with(binding)
        {
            toolbar.setup(getString(R.string.settings), baseToolbarListener)
            toolbar.hideRightIcon()
            lunch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    markSwitchOn(lunch)
                } else {
                    markSwitchOff(lunch)
                }
            }
            dinner.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    markSwitchOn(dinner)
                } else {
                    markSwitchOff(dinner)
                }
            }
            breakfast.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    markSwitchOn(breakfast)
                } else {
                    markSwitchOff(breakfast)
                }
            }
        }

    }

    private fun initObserver() {
        viewModel.measurementUnitEvent.observe(viewLifecycleOwner) { measurementUnit ->
            run {
                setupLengthView(measurementUnit.lengthUnit)
                setupWeightView(measurementUnit.weightUnit)
            }
        }
    }

    private val baseToolbarListener = object : BaseToolbar.ToolbarListener {
        override fun onBack() {
            viewModel.navigateBack()
        }

        override fun onRightIconClicked() {

        }

    }

    private fun markSwitchOn(switchCompat: SwitchCompat) {
        switchCompat.thumbTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.passio_primary)
        switchCompat.trackTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.passio_primary40p)
    }

    private fun markSwitchOff(switchCompat: SwitchCompat) {
        switchCompat.thumbTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.passio_white)
        switchCompat.trackTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.passio_gray300)
    }

    private fun setupLengthView(selectedLengthUnit: LengthUnit) {
        with(binding)
        {
            val items = listOf(LengthUnit.Imperial, LengthUnit.Metric)
            if (length.adapter == null || length.onItemSelectedListener == null) {
                val adapter = GenericSpinnerAdapter(
                    context = requireContext(),
                    items = items
                ) { item ->
                    item.value
                }
                length.adapter = adapter
                length.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View,
                        position: Int,
                        id: Long
                    ) {
                        val selectedItem =
                            parent.getItemAtPosition(position) as LengthUnit
                        viewModel.updateLengthUnit(selectedItem)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                    }
                }
            }
            length.setSelection(items.indexOf(selectedLengthUnit))
        }
    }

    private fun setupWeightView(selectedWeightUnit: WeightUnit) {
        with(binding)
        {
            val items = listOf(WeightUnit.Imperial, WeightUnit.Metric)
            if (weight.adapter == null || weight.onItemSelectedListener == null) {
                val adapter = GenericSpinnerAdapter(
                    context = requireContext(),
                    items = items
                ) { item ->
                    item.value
                }
                weight.adapter = adapter
                weight.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View,
                        position: Int,
                        id: Long
                    ) {
                        val selectedItem =
                            parent.getItemAtPosition(position) as WeightUnit
                        viewModel.updateWeightUnit(selectedItem)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                    }
                }
            }
            weight.setSelection(items.indexOf(selectedWeightUnit))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}