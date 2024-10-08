package ai.passio.nutrition.uimodule.ui.camera

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.RecognitionResultViewBinding
import ai.passio.nutrition.uimodule.domain.camera.RecognitionResult
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.StringKT.isValid
import ai.passio.nutrition.uimodule.ui.util.StringKT.singleDecimal
import ai.passio.nutrition.uimodule.ui.util.loadFoodImage
import ai.passio.nutrition.uimodule.ui.util.loadPassioIcon
import ai.passio.passiosdk.passiofood.DetectedCandidate
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED

class RecognitionResultView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var _binding: RecognitionResultViewBinding? = null
    private val binding: RecognitionResultViewBinding get() = _binding!!
    private var shownId: String? = null
    private val bottomSheetBehavior: BottomSheetBehavior<View>
    private var recognitionResultListener: RecognitionResultListener? = null

    interface RecognitionResultListener {
        fun onLog(result: RecognitionResult)
        fun onEdit(result: RecognitionResult)
        fun onSearchTapped()
        fun onCancelled()
    }

    init {
        _binding = RecognitionResultViewBinding.inflate(LayoutInflater.from(context), this)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
//        bottomSheetBehavior.peekHeight = DesignUtils.dp2px(172f)

        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.visualResultCard)
        constraintSet.constrainMaxHeight(binding.dragSheet.id, (DesignUtils.screenHeight(context) * 0.6f).toInt())

        formatSearchManuallyText()

    }

    private fun enableDrag() {
        binding.bottomView.post {
            val sizeTotal =
                /*binding.bottomView.height +*/ binding.foodResultCard.height + DesignUtils.dp2px(32f)
//            val sizeTotal = binding.bottomView.height + DesignUtils.dp2px(32f)
            bottomSheetBehavior.peekHeight = sizeTotal
//            binding.bottomSheet.setPadding(0, 0, 0, sizeTotal)
        }

//        (binding.bottomSheet.layoutParams as LayoutParams).behavior = bottomSheetBehavior
        binding.root.isEnabled = true

        binding.bottomSheet.isEnabled = true
        bottomSheetBehavior.isDraggable = true
    }

    private fun disableDrag() {
        binding.bottomView.post {
//            bottomSheetBehavior.peekHeight = binding.bottomView.height
//            binding.bottomSheet.setPadding(0, 0, 0, binding.bottomView.height)
        }
//        (binding.bottomSheet.layoutParams as LayoutParams).behavior = null
        /*binding.root.setOnClickListener {

        }*/
        binding.root.isEnabled = false
        bottomSheetBehavior.isDraggable = false
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
            // Set a ClickableSpan
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // Handle the click event for "Search Manually" text
                    recognitionResultListener?.onSearchTapped()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false // Optional: Remove underline if you don't want it
                }
            }
            spannableString.setSpan(
                clickableSpan,
                startIndex1,
                endIndex1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

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
        binding.searchManually.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    fun reset() {
        shownId = null
    }


    fun showVisualResult(result: RecognitionResult.VisualRecognition, saveTxt: String) {
        if (shownId == result.visualCandidate.passioID) {
            return
        }

        _binding?.let {
            it.nutritionFactsResultCard.isVisible = false
            it.barcodeResultCard.isVisible = false
            it.visualResultCard.isVisible = true
            it.topView.isVisible = true
            it.searchManually.isVisible = true
            shownId = result.visualCandidate.passioID
            it.viewDragUp.isVisible = true
            it.foodName.text = result.visualCandidate.foodName.capitalized()
            it.foodImage.loadPassioIcon(result.visualCandidate.passioID)
            enableDrag()
            bottomSheetBehavior.state = STATE_COLLAPSED

            fun logVisualCandidate(detectedCandidate: DetectedCandidate) {
                recognitionResultListener?.onLog(
                    RecognitionResult.VisualRecognition(
                        detectedCandidate
                    )
                )
            }

            fun editVisualCandidate(detectedCandidate: DetectedCandidate) {
                recognitionResultListener?.onEdit(
                    RecognitionResult.VisualRecognition(
                        detectedCandidate
                    )
                )
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

            it.foodLog.text = saveTxt
            it.foodEdit.text = resources.getString(R.string.edit)
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

    @SuppressLint("SetTextI18n")
    fun showFoodRecordRecognition(result: RecognitionResult.FoodRecordRecognition, saveTxt: String) {
        if (shownId == result.foodItem.id) {
            return
        }

        _binding?.let {


            it.nutritionFactsResultCard.isVisible = false
            it.barcodeResultCard.isVisible = true
            it.visualResultCard.isVisible = false
            it.topView.isVisible = false
            it.searchManually.isVisible = false

            shownId = result.foodItem.id
            it.viewDragUp.isVisible = false
            val foodRecord = result.foodItem
            it.barcodeName.text = foodRecord.name.capitalized()
            it.barcodeId.text = if (foodRecord.barcode.isValid()) {
                "UPC:${foodRecord.barcode}"
            } else if (foodRecord.packagedFoodCode.isValid()) {
                "UPC:${foodRecord.packagedFoodCode}"
            } else {
                foodRecord.additionalData
            }
            it.barcodeImage.loadFoodImage(result.foodItem)
            disableDrag()
//            bottomSheetBehavior.state = STATE_COLLAPSED
            it.rvAlternatives.adapter = null

            it.foodLog.text = saveTxt
            it.foodEdit.text = resources.getString(R.string.edit)
            it.foodLog.setOnClickListener {
                recognitionResultListener?.onLog(result)
            }
            it.foodEdit.setOnClickListener {
                recognitionResultListener?.onEdit(result)
            }

            it.barcodeResultView.setOnClickListener {
                recognitionResultListener?.onEdit(result)
            }
        }
    }

    fun showNutritionFactsResult(result: RecognitionResult.NutritionFactRecognition) {
        if (shownId == result.nutritionFactsPair.second) {
            return
        }

        fun getLblValueFormat(
            lblText: String,
            lblValueTemp: Double?,
            lblUnitTemp: String
        ): SpannableString {
            var lblValue = "-"
            var lblUnit = ""
            if (lblValueTemp != null && lblValueTemp > 0.0) {
                lblValue = lblValueTemp.singleDecimal()
                lblUnit = lblUnitTemp

            }
            val fullText = "$lblValue $lblUnit\n$lblText"
            val spannableString = SpannableString(fullText)
            // Apply color to lblValue (which is at the start of the string)
            val start = 0
            val end = lblValue.length

            spannableString.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        context,
                        R.color.passio_primary
                    )
                ), // Color to apply
                start, // Start index
                end, // End index
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE // Flag
            )
            // Set the text style to bold
            spannableString.setSpan(
                StyleSpan(Typeface.BOLD),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannableString
        }

        _binding?.let {


            it.nutritionFactsResultCard.isVisible = true
            it.barcodeResultCard.isVisible = false
            it.visualResultCard.isVisible = false
            it.topView.isVisible = false
            it.searchManually.isVisible = false

            shownId = result.nutritionFactsPair.second

            val nutritionFacts = result.nutritionFactsPair.first

            it.tvCalories.text = getLblValueFormat(
                context.resources.getString(R.string.calories),
                nutritionFacts.calories,
                ""
            )
            it.tvCarbs.text = getLblValueFormat(
                context.resources.getString(R.string.carbs),
                nutritionFacts.carbs,
                nutritionFacts.servingSizeUnitName ?: "g"
            )
            it.tvProtein.text = getLblValueFormat(
                context.resources.getString(R.string.protein),
                nutritionFacts.protein,
                nutritionFacts.servingSizeUnitName ?: "g"
            )
            it.tvFat.text = getLblValueFormat(
                context.resources.getString(R.string.fat),
                nutritionFacts.fat,
                nutritionFacts.servingSizeUnitName ?: "g"
            )


            it.foodLog.text = resources.getString(R.string.next_str)
            it.foodEdit.text = resources.getString(R.string.cancel)
            it.foodLog.setOnClickListener {
            recognitionResultListener?.onEdit(result)
            }
            it.foodEdit.setOnClickListener {
            recognitionResultListener?.onCancelled()
            }

            it.viewTopCandidate.setOnClickListener {
//                recognitionResultListener?.onEditProduct(result)
            }
            disableDrag()
//            bottomSheetBehavior.state = STATE_COLLAPSED
        }
    }

    override fun onDetachedFromWindow() {
//        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
        _binding = null
        super.onDetachedFromWindow()
    }


}