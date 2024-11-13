package ai.passio.nutrition.uimodule.ui.reel

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.FragmentImageReelBinding
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.util.RoundedSlicesPieChartRenderer
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.StringKT.singleDecimal
import ai.passio.nutrition.uimodule.ui.util.loadPassioIcon
import ai.passio.passiosdk.passiofood.PassioSearchNutritionPreview
import ai.passio.passiosdk.passiofood.data.model.PassioAdvisorFoodInfo
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.math.pow
import kotlin.math.roundToInt

class ImageReelFragment : BaseFragment<ImageReelViewModel>() {

    private var _binding: FragmentImageReelBinding? = null

    private var caloriesColor: Int = -1
    private var carbColor: Int = -1
    private var proteinColor: Int = -1
    private var fatColor: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageReelBinding.inflate(layoutInflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        caloriesColor = ContextCompat.getColor(requireContext(), R.color.passio_calories)
        carbColor = ContextCompat.getColor(requireContext(), R.color.passio_carbs)
        proteinColor = ContextCompat.getColor(requireContext(), R.color.passio_protein)
        fatColor = ContextCompat.getColor(requireContext(), R.color.passio_fat)

        initObserver()
    }

    private fun initObserver() {
        sharedViewModel.reelDataLD.observe(viewLifecycleOwner) { data ->
            startAnimation(data.first, data.second)
        }
    }

    private fun startAnimation(bitmap: Bitmap, results: List<PassioAdvisorFoodInfo>) {
        _binding?.run {
            reelImage.setImageBitmap(bitmap)
            placeIngredients(results)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun placeIngredients(results: List<PassioAdvisorFoodInfo>) {
        val layoutIndex = (0..11).toList().shuffled()

        _binding?.run {
            results.forEachIndexed { index, result ->
                if (index >= 10) {
                    return@run
                }

                val foodInfo = result.foodDataInfo!!
                val nutrients = foodInfo.nutritionPreview
                val servingSize = if (nutrients.servingUnit == "gram") {
                    "${nutrients.weightQuantity.singleDecimal()}${nutrients.weightUnit}"
                } else {
                    "${nutrients.servingQuantity.singleDecimal()} ${nutrients.servingUnit} (${nutrients.weightQuantity.singleDecimal()}${nutrients.weightUnit})"
                }
                val view = LayoutInflater.from(requireContext()).inflate(R.layout.item_reel_ingredient, reelContainer, false)
                view.findViewById<CircleImageView>(R.id.reelItemImage).loadPassioIcon(foodInfo.iconID)
                view.findViewById<TextView>(R.id.reelItemName).text = "${foodInfo.foodName.capitalized()}"
                view.findViewById<TextView>(R.id.reelItemWeight).text = servingSize
                view.findViewById<TextView>(R.id.reelItemMacros).text = getMacroString(
                    nutrients.calories,
                    nutrients.carbs.singleDecimal(),
                    nutrients.protein.singleDecimal(),
                    nutrients.fat.singleDecimal()
                )

                val childIndex = layoutIndex[index]
                val container = _binding?.reelGridContainer?.getChildAt(childIndex) as? FrameLayout ?: return@run
                container.addView(view)
                (view.layoutParams as FrameLayout.LayoutParams).apply {
                    gravity = Gravity.CENTER
                }
                (view as ViewGroup).apply {
                    clipChildren = false
                    clipToPadding = false
                }
                view.alpha = 0f
            }

            setupChart()
            val calories = results.mapNotNull { it.foodDataInfo?.nutritionPreview?.calories }.reduce { acc, i -> acc + i }
            val carbs = results.mapNotNull { it.foodDataInfo?.nutritionPreview?.carbs }.reduce { acc, d -> acc + d }
            val protein = results.mapNotNull { it.foodDataInfo?.nutritionPreview?.protein }.reduce { acc, d -> acc + d }
            val fat = results.mapNotNull { it.foodDataInfo?.nutritionPreview?.fat }.reduce { acc, d -> acc + d }
            renderNutrients(calories, carbs, protein, fat)

            var delay = 0L
            _binding?.reelContainer?.postDelayed({
                _binding?.reelGridContainer?.children?.forEach {
                    val container = (it as ViewGroup)
                    if (container.childCount == 0) {
                        return@forEach
                    }

                    val view = container.getChildAt(0)
                    getViewAnimation(view, 1000L, delay)
                    delay += 800L
                }
                animateMacrosLayout(1000L, delay)
            }, 2000L)
        }
    }

    private fun animateMacrosLayout(animationDuration: Long, delay: Long) {
        _binding?.macrosLayout?.run {
            val location = IntArray(2)
            this.getLocationOnScreen(location)
            val x = location[0].toFloat()
            val y = location[1].toFloat()
            val centerX = resources.displayMetrics.widthPixels / 2f
            val centerY = resources.displayMetrics.heightPixels / 2f
            this.translationX = centerX - x - this.width / 2
            this.translationY = centerY - y - this.height / 2

            this.scaleX = 0f
            this.scaleY = 0f
            this.alpha = 1f

            this.pivotX = this.measuredWidth / 2f
            this.pivotY = this.measuredHeight / 2f

            val scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f)
            val scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f)
            val translateY = ObjectAnimator.ofFloat(this, "translationY", 0f).apply {
                interpolator = AccelerateDecelerateInterpolator()
            }
            val translateX = ObjectAnimator.ofFloat(this, "translationX", 0f).apply {
                interpolator = AccelerateDecelerateInterpolator()
            }

            AnimatorSet().apply {
                playTogether(scaleX, scaleY, translateX, translateY)
                duration = animationDuration
                startDelay = delay
                start()
            }
        }
    }

    private fun getMacroString(
        calories: Int,
        carbs: String,
        protein: String,
        fat: String
    ): SpannableString {
        // Convert each value to a string
        val values = listOf(calories.toString(), carbs, protein, fat)

        // Define colors for each value (customize as needed)
        val colors = listOf(
            caloriesColor, carbColor, proteinColor, fatColor
        )

        // Join the values into a single string with spaces between them
        val formattedText = values.joinToString(" ")
        val spannableString = SpannableString(formattedText)

        // Track the start index for each value in the formatted text
        var currentIndex = 0
        for ((index, value) in values.withIndex()) {
            val endIndex = currentIndex + value.length

            // Apply a color span to each value
            spannableString.setSpan(
                ForegroundColorSpan(colors[index % colors.size]),
                currentIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Move to the next start index (adding 1 for the space character)
            currentIndex = endIndex + 1
        }

        return spannableString
    }

    private fun getViewAnimation(view: View, animationDuration: Long, delay: Long) {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = location[0].toFloat()
        val y = location[1].toFloat()
        val centerX = resources.displayMetrics.widthPixels / 2f
        val centerY = resources.displayMetrics.heightPixels / 2f
        view.translationX = centerX - x - view.width / 2
        view.translationY = centerY - y - view.height / 2

        view.scaleX = 0f
        view.scaleY = 0f
        view.alpha = 1f

        view.pivotX = view.measuredWidth / 2f
        view.pivotY = view.measuredHeight / 2f
        val randomAngle = (-20..20).random().toFloat()

        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f)
        val translateY = ObjectAnimator.ofFloat(view, "translationY", 0f).apply {
            interpolator = ArcInterpolator()
        }
        val translateX = ObjectAnimator.ofFloat(view, "translationX", 0f).apply {
            interpolator = ArcInterpolator()
        }
        val rotate = ObjectAnimator.ofFloat(view, "rotation", 0f, randomAngle)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, translateX, translateY, rotate)
            duration = animationDuration
            startDelay = delay
            start()
        }
    }

    private fun setupChart() {
        _binding?.macrosChart?.run {
            renderer = RoundedSlicesPieChartRenderer(this, animator, viewPortHandler)
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 80f
            transparentCircleRadius = 80f
            description.isEnabled = false
            legend.isEnabled = false
            setDrawEntryLabels(false)
            setDrawSliceText(false)
            setDrawMarkers(false)
            setTouchEnabled(false)
        }
    }

    private fun renderNutrients(
        calories: Int,
        carbs: Double,
        protein: Double,
        fat: Double
    ) {
        _binding?.run {
            val sum = 4 * carbs + 4 * protein + 9 * fat
            var carbPercent = if (sum != 0.0) {
                (((4 * carbs) / sum) * 100).roundToInt()
            } else {
                0
            }
            val proteinPercent = if (sum != 0.0) {
                (((4 * protein) / sum) * 100).roundToInt()
            } else {
                0
            }
            val fatPercent = if (sum != 0.0) {
                (((9 * fat) / sum) * 100).roundToInt()
            } else {
                0
            }
            if (carbPercent + proteinPercent + fatPercent == 99) {
                carbPercent += 1
            } else if (carbPercent + proteinPercent + fatPercent == 101) {
                carbPercent -= 1
            }

            val carbGrams = "${carbs.singleDecimal()} g"
            val proteinGrams = "${protein.singleDecimal()} g"
            val fatGrams = "${fat.singleDecimal()} g"

            val carbString = SpannableString(" $carbGrams ($carbPercent%)")
            carbString.setSpan(
                ForegroundColorSpan(carbColor),
                0,
                carbGrams.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            carbsValue.text = carbString

            val proteinString = SpannableString(" $proteinGrams ($proteinPercent%)")
            proteinString.setSpan(
                ForegroundColorSpan(proteinColor),
                0,
                proteinGrams.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            proteinValue.text = proteinString

            val fatString = SpannableString(" $fatGrams ($fatPercent%)")
            fatString.setSpan(
                ForegroundColorSpan(fatColor),
                0,
                fatGrams.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            fatValue.text = fatString

            renderChart(carbPercent, proteinPercent, fatPercent)

            caloriesValue.text = calories.toString()
        }
    }

    private fun renderChart(carbPercent: Int, proteinPercent: Int, fatPercent: Int) {
        val entries = mutableListOf<PieEntry>()
        entries.add(PieEntry(carbPercent.toFloat(), "carbs"))
        entries.add(PieEntry(proteinPercent.toFloat(), "protein"))
        entries.add(PieEntry(fatPercent.toFloat(), "fat"))

        val dataSet = PieDataSet(entries, "Macros")
        val colors = listOf(carbColor, proteinColor, fatColor)
        dataSet.colors = colors
        dataSet.valueTextSize = 0f
        dataSet.sliceSpace = 8f

        val data = PieData(dataSet)
        _binding?.run {
            macrosChart.data = data
            macrosChart.invalidate()
        }
    }

    private class ArcInterpolator : Interpolator {
        override fun getInterpolation(input: Float): Float {
            return -2.38f * input.pow(2) + 3.38f * input
        }

    }
}