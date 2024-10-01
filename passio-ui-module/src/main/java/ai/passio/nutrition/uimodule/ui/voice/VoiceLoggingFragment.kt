package ai.passio.nutrition.uimodule.ui.voice

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.data.ResultWrapper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.databinding.FragmentVoiceLoggingBinding
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
import ai.passio.nutrition.uimodule.ui.util.ViewEXT.disable
import ai.passio.nutrition.uimodule.ui.util.ViewEXT.enable
import ai.passio.nutrition.uimodule.ui.util.toast
import ai.passio.passiosdk.passiofood.data.model.PassioSpeechRecognitionModel
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import java.util.Locale


private val PERMISSION = arrayOf(
    Manifest.permission.RECORD_AUDIO
)

class VoiceLoggingFragment : BaseFragment<VoiceLoggingViewModel>() {

    enum class VoiceLoggingState {
        START_LISTENING,
        LISTENING,
        FETCHING_RESULT,
        RESULT
    }

    private var _binding: FragmentVoiceLoggingBinding? = null
    private val binding: FragmentVoiceLoggingBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVoiceLoggingBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        with(binding)
        {
            toolbar.setup(getString(R.string.voice_logging), baseToolbarListener)


            prepareRecognizer()

            startListening.setOnClickListener {
                checkPermissions()
                if (!permissionGranted) {
                    return@setOnClickListener
                }
                viewModel.updateVoiceLoggingState(VoiceLoggingState.LISTENING)
                speechRecognizer?.startListening(intent)
            }
            stopListening.setOnClickListener {
                speechRecognizer?.stopListening()
//                viewModel.updateVoiceLoggingState(VoiceLoggingState.FETCHING_RESULT)
//                viewModel.updateVoiceLoggingState(VoiceLoggingState.START_LISTENING)
            }
            tryAgain.setOnClickListener {
                viewModel.updateVoiceLoggingState(VoiceLoggingState.START_LISTENING)
            }
            log.setOnClickListener {
                viewModel.logRecords((rvResult.adapter as SpeechRecognitionAdapter).getSelectedItems())
            }
            clearSelected.setOnClickListener {
                (rvResult.adapter as SpeechRecognitionAdapter).clearSelection()
            }
            searchManually.setOnClickListener {
                sharedViewModel.setIsAddIngredientFromSearch(viewModel.getIsAddIngredient())
                viewModel.navigateToSearch()
            }
            formatSearchManuallyText()
        }

    }

    private fun formatSearchManuallyText() {
        val searchManuallyFullText = "Not what you’re looking for? Search Manually"
        val searchManuallyText = "Search Manually"
        val spannableString = SpannableString(searchManuallyFullText)

        val startIndex1 = searchManuallyFullText.indexOf(searchManuallyText)
        val endIndex1 = startIndex1 + searchManuallyText.length
        if (startIndex1 != -1) {
            spannableString.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.passio_primary
                    )
                ),
                startIndex1,
                endIndex1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                StyleSpan(Typeface.BOLD),
                startIndex1,
                endIndex1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        binding.searchManually.text = spannableString
    }

    private var permissionGranted = false
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            var allGranted = true
            permissions.entries.forEach {
                if (!it.value) {
                    allGranted = false
                    requireContext().toast("Permission: ${it.key} needed")
                }
            }
            if (allGranted) {
                permissionGranted = true
                viewModel.updateVoiceLoggingState(VoiceLoggingState.LISTENING)
                speechRecognizer?.startListening(intent)
            }
        }

    private fun checkPermissions() {
        val notGranted = PERMISSION.filterNot { permission ->
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) {
            permissionGranted = true
            return
        }

        // You can directly ask for the permission.
        // The registered ActivityResultCallback gets the result of this request.
        requestPermissionLauncher.launch(notGranted.toTypedArray())
    }

    private val intent: Intent by lazy {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3 * 1000L)
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                10 * 1000L
            )
        }
    }
    private var speechRecognizer: SpeechRecognizer? = null
    private fun prepareRecognizer() {
        if (speechRecognizer != null) {
            Log.d("VoiceLoggingFragment", "speechRecognizer != null")
            return
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        Log.d("VoiceLoggingFragment", "init")
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
//                changeRecordingState(2)
                Log.d("VoiceLoggingFragment", "onReadyForSpeech: ${params.toString()}")

            }

            override fun onBeginningOfSpeech() {
                Log.d("VoiceLoggingFragment", "onBeginningOfSpeech")
//                adapter.clear()
//                isRecording = true
            }

            override fun onRmsChanged(rmsdB: Float) {
                Log.d("VoiceLoggingFragment", "onRmsChanged")
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                Log.d("VoiceLoggingFragment", "onBufferReceived")
            }

            override fun onEndOfSpeech() {
                Log.d("VoiceLoggingFragment", "onEndOfSpeech")
//                isRecording = false
            }

            override fun onError(error: Int) {
                if (context == null) return
                Log.d("VoiceLoggingFragment", "osnError $error")
                requireContext().toast("Speech recognizer error: $error")
                viewModel.errorVoiceRecognition()
            }

            override fun onResults(results: Bundle?) {
                Log.d("VoiceLoggingFragment", "onResults ${results.toString()}")
                val line = parseResult(results)
                viewModel.fetchResult(line)
                /*val line = parseResult(results)
                binding.recognizedSpeech.text = line

                changeRecordingState(1)
                PassioSDK.instance.recognizeSpeechRemote(line) { result ->
                    adapter.update(result)
                    changeRecordingState(0)
                }*/
            }

            override fun onPartialResults(partialResults: Bundle?) {
                Log.d("VoiceLoggingFragment", "onPartialResults ${partialResults.toString()}")

                val line = parseResult(partialResults)
                binding.voiceQuery.text = line
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d(
                    "VoiceLoggingFragment",
                    "onEvent eventType $eventType parms ${params.toString()}"
                )
            }

        })
    }

    private fun parseResult(bundle: Bundle?): String {
        if (bundle == null) return ""
        val data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return ""
        return data[0]
    }

    private fun setIngredientMode(isOn: Boolean) {
        if (isOn) {
            binding.log.text = getString(R.string.add_ingredient)
            binding.toolbar.hideRightIcon()
        }
    }

    private fun initObserver() {
        setIngredientMode(viewModel.getIsAddIngredient())
        sharedViewModel.isAddIngredientFromVoiceLD.observe(viewLifecycleOwner) { isAddIngredient ->
            viewModel.setIsAddIngredient(isAddIngredient)
            setIngredientMode(isAddIngredient)

        }
        viewModel.voiceLoggingStateEvent.observe(viewLifecycleOwner, ::showCurrentState)
        viewModel.voiceQueryEvent.observe(viewLifecycleOwner) { query ->
            binding.voiceQuery.text = query
        }
        viewModel.showLoading.observe(viewLifecycleOwner) {
            binding.loading.isVisible = it
        }
        viewModel.resultFoodInfo.observe(viewLifecycleOwner, ::showVoiceResult)
        viewModel.logFoodEvent.observe(viewLifecycleOwner, ::foodItemLogged)
        viewModel.addIngredientEvent.observe(viewLifecycleOwner, ::addIngredients)
    }

    private fun addIngredients(foodRecords: List<FoodRecordIngredient>)
    {
        sharedViewModel.addFoodIngredients(foodRecords)
        viewModel.navigateBackToRecipe()
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

    private fun showVoiceResult(passioRecognitionResult: List<PassioSpeechRecognitionModel>) {
        with(binding)
        {
            if (rvResult.adapter == null) {
                rvResult.adapter = SpeechRecognitionAdapter(::foodItemSelection)
            }
            val adapter = (rvResult.adapter as SpeechRecognitionAdapter)
            adapter.addData(passioRecognitionResult, passioRecognitionResult.indices.toList())
            if (passioRecognitionResult.isEmpty()) {
                clearSelected.visibility = View.GONE
                noResult.visibility = View.VISIBLE
                log.disable()
            } else {
                clearSelected.visibility = View.VISIBLE
                noResult.visibility = View.GONE
            }
        }
    }

    private fun foodItemSelection(count: Int) {
        if (count > 0) {
            binding.log.enable()
        } else {
            binding.log.disable()
        }
    }

    private fun showCurrentState(voiceLoggingState: VoiceLoggingState) {

        with(binding)
        {
            when (voiceLoggingState) {
                VoiceLoggingState.START_LISTENING -> {
                    binding.voiceQuery.text = ""
                    groupStartListening.visibility = View.VISIBLE
                    groupStopListening.visibility = View.GONE
                    resultContainer.visibility = View.GONE
                }

                VoiceLoggingState.LISTENING -> {
                    groupStartListening.visibility = View.GONE
                    groupStopListening.visibility = View.VISIBLE
                    resultContainer.visibility = View.GONE
                }

                VoiceLoggingState.FETCHING_RESULT -> {
                    groupStartListening.visibility = View.GONE
                    groupStopListening.visibility = View.GONE
                    resultContainer.visibility = View.VISIBLE
                    viewLoadingResult.visibility = View.VISIBLE
                    resultView.visibility = View.GONE
                }

                VoiceLoggingState.RESULT -> {
                    groupStartListening.visibility = View.GONE
                    groupStopListening.visibility = View.GONE
                    resultContainer.visibility = View.VISIBLE
                    viewLoadingResult.visibility = View.GONE
                    resultView.visibility = View.VISIBLE
                }
            }
        }

    }

    private val baseToolbarListener = object : BaseToolbar.ToolbarListener {
        override fun onBack() {
            viewModel.navigateBack()
        }

        override fun onRightIconClicked() {
            showPopupMenu(binding.toolbar.findViewById(R.id.toolbarMenu))
        }

    }

}