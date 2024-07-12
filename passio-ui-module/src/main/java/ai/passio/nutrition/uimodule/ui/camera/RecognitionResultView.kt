package ai.passio.nutrition.uimodule.ui.camera

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.RecognitionResultViewBinding
import ai.passio.nutrition.uimodule.domain.camera.RecognitionResult
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.loadPassioIcon
import ai.passio.passiosdk.passiofood.DetectedCandidate
import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN

class RecognitionResultView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CoordinatorLayout(context, attrs, defStyleAttr) {

    private var _binding: RecognitionResultViewBinding? = null
    private val binding: RecognitionResultViewBinding get() = _binding!!
    private var shownId: String? = null
    private val bottomSheetBehavior: BottomSheetBehavior<View>
    private var recognitionResultListener: RecognitionResultListener? = null

    interface RecognitionResultListener {
        fun onLogVisual(detectedCandidate: DetectedCandidate)
        fun onEditVisual(detectedCandidate: DetectedCandidate)
        fun onEditProduct(result: RecognitionResult.ProductRecognition)
        fun onLogProduct(result: RecognitionResult.ProductRecognition)
    }

    init {
        _binding = RecognitionResultViewBinding.inflate(LayoutInflater.from(context), this)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.peekHeight = DesignUtils.dp2px(172f)



        formatSearchManuallyText()

        binding.root.setOnClickListener {
            if (bottomSheetBehavior.state == STATE_EXPANDED) {
                bottomSheetBehavior.state = STATE_HIDDEN
            } else {
                bottomSheetBehavior.state = STATE_EXPANDED
            }
        }

    }

    fun setRecognitionResultListener(recognitionResultListener: RecognitionResultListener) {
        this.recognitionResultListener = recognitionResultListener
    }

    fun addBottomSheetCallback(bottomSheetCallback: BottomSheetCallback) {
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
    }

    fun removeBottomSheetCallback(bottomSheetCallback: BottomSheetCallback) {
        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
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
                        context,
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

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    fun reset() {
        shownId = null
    }


    fun showVisualResult(result: RecognitionResult.VisualRecognition) {
        if (shownId == result.visualCandidate.passioID) {
            return
        }

        _binding?.let {
            shownId = result.visualCandidate.passioID
            it.viewDragUp.isVisible = true
            it.foodName.text = result.visualCandidate.foodName.capitalized()
            it.foodImage.loadPassioIcon(result.visualCandidate.passioID)
            bottomSheetBehavior.state = STATE_COLLAPSED

            fun logVisualCandidate(detectedCandidate: DetectedCandidate) {
                recognitionResultListener?.onLogVisual(detectedCandidate)
            }

            fun editVisualCandidate(detectedCandidate: DetectedCandidate) {
                recognitionResultListener?.onEditVisual(detectedCandidate)
            }

            fun removeDuplicates(candidates: List<DetectedCandidate>): List<DetectedCandidate> {
                val seen = mutableSetOf<String>()
                return candidates.filter { candidate -> seen.add(candidate.foodName) }
            }

            val alternatives = removeDuplicates(result.visualCandidate.alternatives)
            it.rvAlternatives.adapter =
                FoodAlternativeAdapter(
                    alternatives,
                    ::logVisualCandidate,
                    ::editVisualCandidate
                )

            it.foodLog.setOnClickListener {
                logVisualCandidate(result.visualCandidate)
            }
            it.foodEdit.setOnClickListener {
                editVisualCandidate(result.visualCandidate)
            }
            it.viewTopCandidate.setOnClickListener {
                editVisualCandidate(result.visualCandidate)
            }
        }
    }

    fun showProductResult(result: RecognitionResult.ProductRecognition) {
        if (shownId == result.foodItem.id) {
            return
        }

        _binding?.let {
            shownId = result.foodItem.id
            it.viewDragUp.isVisible = false
            it.foodName.text = result.foodItem.name.capitalized()
            it.foodImage.loadPassioIcon(result.foodItem.iconId)
            bottomSheetBehavior.state = STATE_COLLAPSED
            it.rvAlternatives.adapter = null
            it.foodLog.setOnClickListener {
                recognitionResultListener?.onLogProduct(result)
            }
            it.foodEdit.setOnClickListener {
                recognitionResultListener?.onEditProduct(result)
            }

            it.viewTopCandidate.setOnClickListener {
                recognitionResultListener?.onEditProduct(result)
            }
        }
    }

    override fun onDetachedFromWindow() {
//        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
        _binding = null
        super.onDetachedFromWindow()
    }


}