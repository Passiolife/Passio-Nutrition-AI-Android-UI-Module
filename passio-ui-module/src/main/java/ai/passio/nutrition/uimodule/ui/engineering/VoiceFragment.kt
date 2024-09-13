package ai.passio.nutrition.uimodule.ui.engineering

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.FragmentSpeechBinding
import ai.passio.nutrition.uimodule.ui.util.toast
import ai.passio.passiosdk.passiofood.PassioSDK
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.Locale

private val PERMISSION = arrayOf(
    Manifest.permission.RECORD_AUDIO
)

class VoiceFragment : Fragment() {

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
            }
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
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var binding: FragmentSpeechBinding
    private var isRecording = false
    private var permissionGranted = false
    private var currentSpeech = ""

    private lateinit var redBackground: Drawable
    private lateinit var orangeBackground: Drawable
    private lateinit var micOff: Drawable
    private lateinit var greenBackground: Drawable
    private lateinit var micOn: Drawable

    private val adapter = VoiceRecognitionAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSpeechBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        redBackground = ContextCompat.getDrawable(requireContext(), R.drawable.rc_8dp_red)!!
        orangeBackground = ContextCompat.getDrawable(requireContext(), R.drawable.rc_8dp_orange)!!
        micOff = ContextCompat.getDrawable(requireContext(), R.drawable.microphone_off)!!
        greenBackground = ContextCompat.getDrawable(requireContext(), R.drawable.rc_8dp_green)!!
        micOn = ContextCompat.getDrawable(requireContext(), R.drawable.microphone_on)!!
        binding.recognitions.adapter = adapter

        prepareRecognizer()
        checkPermissions()

        binding.toggleSpeech.setOnClickListener {
            if (!permissionGranted) {
                checkPermissions()
                return@setOnClickListener
            }

            if (isRecording) {
                speechRecognizer.stopListening()
            } else {
                speechRecognizer.startListening(intent)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun changeRecordingState(state: Int) {
        when (state) {
            2 -> {
                binding.toggleSpeech.background = redBackground
                binding.toggleSpeech.text = "Stop listening"
                binding.toggleSpeech.setCompoundDrawablesWithIntrinsicBounds(null, null, micOff, null)
                currentSpeech = ""
            }
            1 -> {
                binding.toggleSpeech.background = orangeBackground
                binding.toggleSpeech.text = "Fetching result..."
                binding.toggleSpeech.setCompoundDrawablesWithIntrinsicBounds(null, null, micOff, null)
            }
            0 -> {
                binding.toggleSpeech.background = greenBackground
                binding.toggleSpeech.text = "Start listening"
                binding.toggleSpeech.setCompoundDrawablesWithIntrinsicBounds(null, null, micOn, null)
            }
        }
    }

    private fun prepareRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                changeRecordingState(2)
            }

            override fun onBeginningOfSpeech() {
                adapter.clear()
                isRecording = true
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {
                println()
            }

            override fun onEndOfSpeech() {
                isRecording = false
            }

            override fun onError(error: Int) {
                if (context == null) return
                isRecording = false
                requireContext().toast("Speech recognizer error: $error")
            }

            override fun onResults(results: Bundle?) {
                val line = parseResult(results)
                binding.recognizedSpeech.text = line

                changeRecordingState(1)
                PassioSDK.instance.recognizeSpeechRemote(line) { result ->
                    adapter.update(result)
                    changeRecordingState(0)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val line = parseResult(partialResults)
                binding.recognizedSpeech.text = line
            }

            override fun onEvent(eventType: Int, params: Bundle?) {

            }

        })
    }

    private fun parseResult(bundle: Bundle?): String {
        if (bundle == null) return ""
        val data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return ""
        return data[0]
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
}