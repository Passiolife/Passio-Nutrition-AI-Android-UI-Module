package ai.passio.nutrition.uimodule.ui.settings

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.data.ResultWrapper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.databinding.FragmentSettingsBinding
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.model.UserProfile
import ai.passio.nutrition.uimodule.ui.profile.GenericSpinnerAdapter
import ai.passio.nutrition.uimodule.ui.profile.LengthUnit
import ai.passio.nutrition.uimodule.ui.profile.WeightUnit
import ai.passio.nutrition.uimodule.ui.util.toast
import ai.passio.passiosdk.passiofood.PassioSDK
import android.widget.AdapterView
import android.widget.Toast
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
            lunch.setOnCheckedChangeListener { checkbox, isChecked ->
                if (isChecked) {
                    markSwitchOn(lunch)
//                    scheduleNotification(requireContext(), 12, 0, "Take your lunch.", 1002)
                } else {
                    markSwitchOff(lunch)
//                    cancelNotification(requireContext(), 1002)
                }
                if (checkbox.isPressed) {
                    viewModel.updateLunchReminder(isChecked)
                }
            }
            dinner.setOnCheckedChangeListener { checkbox, isChecked ->
                if (isChecked) {
                    markSwitchOn(dinner)
//                    scheduleNotification(requireContext(), 17, 0, "Take your dinner.", 1003)
                } else {
                    markSwitchOff(dinner)
//                    cancelNotification(requireContext(), 1003)
                }
                if (checkbox.isPressed) {
                    viewModel.updateDinnerReminder(isChecked)
                }
            }
            breakfast.setOnCheckedChangeListener { checkbox, isChecked ->
                if (isChecked) {
                    markSwitchOn(breakfast)
//                    scheduleNotification(requireContext(), 8, 0, "Take your breakfast.", 1001)
                } else {
                    markSwitchOff(breakfast)
//                    cancelNotification(requireContext(), 1001)
                }
                if (checkbox.isPressed) {
                    viewModel.updateBreakfastReminder(isChecked)
                }
            }
            languageGroup.check(R.id.radioEnglish)
            languageGroup.setOnCheckedChangeListener { _, checkedId ->
                val success = when (checkedId) {
                    R.id.radioEnglish -> PassioSDK.instance.changeLanguage("en")
                    R.id.radioGerman -> PassioSDK.instance.changeLanguage("de")
                    R.id.radioFrench -> PassioSDK.instance.changeLanguage("fr")
                    R.id.radioSpanish -> PassioSDK.instance.changeLanguage("es")
                    else -> throw IllegalStateException("No known radio id: $checkedId")
                }
                if (success) {
                    Toast.makeText(requireContext(), "SDK Language Changed!", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun initObserver() {
        viewModel.userProfileEvent.observe(viewLifecycleOwner, ::setSettingInfo)
        viewModel.updateProfileResult.observe(viewLifecycleOwner, ::settingSaved)
    }

    private fun setSettingInfo(userProfile: UserProfile) {
        with(binding) {
            val measurementUnit = userProfile.measurementUnit

            setupLengthView(measurementUnit.lengthUnit)
            setupWeightView(measurementUnit.weightUnit)

            breakfast.isChecked = userProfile.userReminder.isBreakfastOn
            lunch.isChecked = userProfile.userReminder.isLunchOn
            dinner.isChecked = userProfile.userReminder.isDinnerOn
        }
    }

    private fun settingSaved(resultWrapper: ResultWrapper<Boolean>) {
        when (resultWrapper) {
            is ResultWrapper.Success -> {
                if (resultWrapper.value) {
                    requireContext().toast("User settings saved!")
//                    viewModel.navigateBack()
                } else {
                    requireContext().toast("Could not saved settings. Please try again.")
                }
            }

            is ResultWrapper.Error -> {
                requireContext().toast(resultWrapper.error)
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