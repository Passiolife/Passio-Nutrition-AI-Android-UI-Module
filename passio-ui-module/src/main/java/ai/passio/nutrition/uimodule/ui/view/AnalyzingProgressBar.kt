package ai.passio.nutrition.uimodule.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar
import kotlinx.coroutines.*

class AnalyzingProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ProgressBar(context, attrs, defStyleAttr) {

    private var progressStatus = 0
    private var isProgressStopped = false
    private var progressJob: Job? = null
    private val progressScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Starts progress until 90%
    fun startProgress() {
        progressJob?.cancel()
        isProgressStopped = false
        progressStatus = 0
        progressJob = progressScope.launch {
            while (progressStatus < 90 && !isProgressStopped) {
                progressStatus++
                progress = progressStatus
                delay(100)
            }
        }
    }

    // Gradually completes progress from 90% to 100%
    fun completeProgress(shouldAnimate: Boolean = false) {
        progressJob?.cancel()
        if (shouldAnimate) {
            if (progressStatus >= 90) {
                progressJob = progressScope.launch {
                    while (progressStatus < 100) {
                        progressStatus++
                        progress = progressStatus
                        delay(100)
                    }
                }
            }
        } else {
            progressStatus = 100
            progress = progressStatus
        }
    }

    // Stops the progress at its current state
    fun stopProgress() {
        isProgressStopped = true
        progressJob?.cancel()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        progressScope.cancel()
    }
}
