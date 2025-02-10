package ai.passio.nutrition.uimodule.ui.camera

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.RecognitionResultViewBinding
import ai.passio.nutrition.uimodule.domain.camera.RecognitionResult
import ai.passio.nutrition.uimodule.ui.model.getShortInfo2
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.loadFoodImage
import ai.passio.passiosdk.passiofood.Barcode
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible

class RecognitionResultView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var _binding: RecognitionResultViewBinding? = null
    private val binding: RecognitionResultViewBinding get() = _binding!!
    private var shownId: String? = null
    private var recognitionResultListener: RecognitionResultListener? = null

    interface RecognitionResultListener {
        fun onLog(result: RecognitionResult)
        fun onEdit(result: RecognitionResult)
        fun onNutritionFactsTapped(barcode: Barcode?)
        fun onSearchTapped()
        fun onCancelled()
    }

    init {
        _binding = RecognitionResultViewBinding.inflate(LayoutInflater.from(context), this)

        formatSearchManuallyText()
        formatNutritionFactsText()

    }


    fun setRecognitionResultListener(recognitionResultListener: RecognitionResultListener) {
        this.recognitionResultListener = recognitionResultListener
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

    private fun formatNutritionFactsText() {
        val searchManuallyFullText = "No barcode? Take a picture of Nutrition Facts"
        val searchManuallyText = "Nutrition Facts"
        val spannableString = SpannableString(searchManuallyFullText)

        val startIndex1 = searchManuallyFullText.indexOf(searchManuallyText)
        val endIndex1 = startIndex1 + searchManuallyText.length
        if (startIndex1 != -1) {
            // Set a ClickableSpan
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // Handle the click event for "Search Manually" text
                    recognitionResultListener?.onNutritionFactsTapped(null)
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
        binding.takeNutritionFacts.text = spannableString
        binding.takeNutritionFacts.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    fun reset() {
        shownId = null
    }


    @SuppressLint("SetTextI18n")
    fun showScanningView() {

        with(binding)
        {
            scanningView.isVisible = true
            bottomView.isVisible = false
            noProductView.isVisible = false
        }

    }

    fun showNoProductView(barcode: Barcode) {
        with(binding)
        {
            scanningView.isVisible = false
            bottomView.isVisible = false
            noProductView.isVisible = true
            cancel.setOnClickListener {
                recognitionResultListener?.onCancelled()
            }
            takePhoto.setOnClickListener {
                recognitionResultListener?.onNutritionFactsTapped(barcode)
            }

        }
    }

    fun showFoodRecordRecognition(
        result: RecognitionResult.FoodRecordRecognition,
        saveTxt: String
    ) {
        if (shownId == result.foodItem.id) {
            return
        }

        with(binding)
        {
            scanningView.isVisible = false
            bottomView.isVisible = true
            noProductView.isVisible = false
        }
        _binding?.let {


            shownId = result.foodItem.id
            val foodRecord = result.foodItem
            it.barcodeName.text = foodRecord.name.capitalized()
            it.barcodeId.text = foodRecord.getShortInfo2()
            it.barcodeImage.loadFoodImage(result.foodItem)

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

    override fun onDetachedFromWindow() {
//        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
        _binding = null
        super.onDetachedFromWindow()
    }


}