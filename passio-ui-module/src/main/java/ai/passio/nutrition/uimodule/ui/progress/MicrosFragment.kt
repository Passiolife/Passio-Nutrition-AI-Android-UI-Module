package ai.passio.nutrition.uimodule.ui.progress

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.FragmentMicrosBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import android.app.DatePickerDialog
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.Date
import java.util.Locale

class MicrosFragment : BaseFragment<MicrosViewModel>() {

    private var _binding: FragmentMicrosBinding? = null
    private val binding: FragmentMicrosBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMicrosBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObserver()

        binding.timeTitle.setOnClickListener {
            showDatePickerDialog()
        }
        binding.moveNext.setOnClickListener {
            viewModel.setNextDay()
        }
        binding.movePrevious.setOnClickListener {
            viewModel.setPreviousDay()
        }

    }

    private fun initObserver() {
        viewModel.currentDateEvent.observe(viewLifecycleOwner, ::updateDate)
        viewModel.logsLD.observe(viewLifecycleOwner, ::updateLogs)
    }

    private fun updateLogs(records: List<FoodRecord>) {

    }

    private fun updateDate(currentDate: Date) {

        val selectedDate = DateTime(currentDate.time)
        val formattedDate = if (selectedDate.toLocalDate() == DateTime.now().toLocalDate()) {
            requireContext().getString(R.string.today)
        } else {
            val dateFormat =
                DateTimeFormat.forPattern("MMMM dd, yyyy").withLocale(Locale.getDefault())
            dateFormat.print(selectedDate)
        }

        binding.timeTitle.text = formattedDate
    }

    private fun showDatePickerDialog() {
        val now = DateTime.now()
        val year = now.year
        val month = now.monthOfYear - 1 // DatePickerDialog uses 0-based month
        val day = now.dayOfMonth

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format the selected date
                val selectedDate = DateTime(selectedYear, selectedMonth + 1, selectedDay, 0, 0)
                viewModel.setCurrentDate(selectedDate.toDate())
            },
            year, month, day
        )
        datePickerDialog.show()
    }


}