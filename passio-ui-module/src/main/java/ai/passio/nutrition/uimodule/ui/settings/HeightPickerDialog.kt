package ai.passio.nutrition.uimodule.ui.settings

import ai.passio.nutrition.uimodule.databinding.DialogHeightPickerBinding
import ai.passio.nutrition.uimodule.ui.model.MeasurementUnit
import ai.passio.nutrition.uimodule.ui.model.UserProfile
import ai.passio.nutrition.uimodule.ui.profile.LengthUnit
import ai.passio.nutrition.uimodule.ui.profile.feetInchesToMeters
import ai.passio.nutrition.uimodule.ui.profile.metersCentimetersToMeters
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider

class HeightPickerDialog(private val listener: HeightPickerListener) :
    DialogFragment() {

    interface HeightPickerListener {
        fun onHeightPicked(height: Double /*Meter*/)
    }

    private var _binding: DialogHeightPickerBinding? = null
    private val binding: DialogHeightPickerBinding get() = _binding!!
    private val viewModel: SettingsViewModel by lazy {
        ViewModelProvider(this)[SettingsViewModel::class.java]
    }
    private var measurementUnit: MeasurementUnit = MeasurementUnit()

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            DesignUtils.screenWidth(requireContext()) - DesignUtils.dp2px(20f),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogHeightPickerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.userProfileEvent.observe(viewLifecycleOwner, ::setupPickers)
        viewModel.getMeasurementUnit()

        binding.cancel.setOnClickListener {
            dismiss()
        }

        binding.save.setOnClickListener {
            val height: Double = if (measurementUnit.lengthUnit == LengthUnit.Imperial) {
                feetInchesToMeters(binding.valuePicker.value, binding.subValuePicker.value)
            } else { //LengthUnit.MeterCenti
                metersCentimetersToMeters(binding.valuePicker.value, binding.subValuePicker.value)
            }
            listener.onHeightPicked(height)
            dismiss()
        }
    }

    private fun setupPickers(userProfile: UserProfile) {
        this.measurementUnit = userProfile.measurementUnit
        with(binding) {
            if (measurementUnit.lengthUnit == LengthUnit.Imperial) {
                valuePicker.minValue = 0
                valuePicker.maxValue = 8
                valuePicker.displayedValues = Array(9) { "$it'" }

                subValuePicker.minValue = 0
                subValuePicker.maxValue = 11
                subValuePicker.displayedValues = Array(12) { "$it\"" }
            } else //UserProfile.LengthUnit.MeterCenti
            {
                valuePicker.minValue = 0
                valuePicker.maxValue = 2
                valuePicker.displayedValues = Array(3) { "$it m" }

                subValuePicker.minValue = 0
                subValuePicker.maxValue = 99
                subValuePicker.displayedValues = Array(100) { "$it cm" }
            }
        }
    }
}