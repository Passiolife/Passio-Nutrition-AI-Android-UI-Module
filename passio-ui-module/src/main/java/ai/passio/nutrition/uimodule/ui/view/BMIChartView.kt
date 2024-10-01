package ai.passio.nutrition.uimodule.ui.view

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.ui.util.DesignUtils

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat

class BMIChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    // Define your colors
    private val underweightColor = ContextCompat.getColor(context, R.color.passio_under_weight)
    private val normalColor = ContextCompat.getColor(context, R.color.passio_normal_weight)
    private val overweightColor = ContextCompat.getColor(context, R.color.passio_over_weight)
    private val obeseColor = ContextCompat.getColor(context, R.color.passio_obese_weight)

    // Define the height of the chart and indicator
    private val chartHeight = DesignUtils.dp2pxFloat(20f)//20f * resources.displayMetrics.density
    private val indicatorHeight = DesignUtils.dp2pxFloat(40f) //40f * resources.displayMetrics.density

    // Indicator properties
    private var currentProgress: Float = 6f
    private var currentBmiLevel: Float = 0f

    // Indicator paint
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 14f * resources.displayMetrics.density
        isFakeBoldText = true
    }

    init {
        indicatorPaint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val sectionWidth = width * 0.15f
        val gradientWidth = width * 0.13333f

        // Draw indicator
        drawIndicator(canvas)

        // Draw underweight section with rounded start
        drawSection(canvas, 0f, sectionWidth, underweightColor, true, false)

        // Draw gradient between underweight and normal
        drawGradientSection(
            canvas,
            sectionWidth,
            sectionWidth + gradientWidth,
            underweightColor,
            normalColor
        )

        // Draw normal section
        drawSection(
            canvas,
            sectionWidth + gradientWidth,
            sectionWidth * 2 + gradientWidth,
            normalColor,
            false,
            false
        )

        // Draw gradient between normal and overweight
        drawGradientSection(
            canvas,
            sectionWidth * 2 + gradientWidth,
            sectionWidth * 2 + gradientWidth * 2,
            normalColor,
            overweightColor
        )

        // Draw overweight section
        drawSection(
            canvas,
            sectionWidth * 2 + gradientWidth * 2,
            sectionWidth * 3 + gradientWidth * 2,
            overweightColor,
            false,
            false
        )

        // Draw gradient between overweight and obese
        drawGradientSection(
            canvas,
            sectionWidth * 3 + gradientWidth * 2,
            sectionWidth * 3 + gradientWidth * 3,
            overweightColor,
            obeseColor
        )

        // Draw obese section with rounded end
        drawSection(canvas, sectionWidth * 3 + gradientWidth * 3, width, obeseColor, false, true)
    }

    private fun drawGradientSection(
        canvas: Canvas,
        startX: Float,
        endX: Float,
        startColor: Int,
        endColor: Int
    ) {
        val gradient = LinearGradient(
            startX, indicatorHeight, endX, indicatorHeight,
            startColor, endColor,
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient

        rect.set(startX, indicatorHeight, endX, indicatorHeight + chartHeight)
        canvas.drawRect(rect, paint)
    }

    private fun drawSection(
        canvas: Canvas,
        startX: Float,
        endX: Float,
        color: Int,
        roundStart: Boolean,
        roundEnd: Boolean
    ) {
        paint.shader = null
        paint.color = color

        rect.set(startX, indicatorHeight, endX, indicatorHeight + chartHeight)
        val radii = if (roundStart || roundEnd) {
            floatArrayOf(
                if (roundStart) chartHeight / 2 else 0f, if (roundStart) chartHeight / 2 else 0f,
                if (roundEnd) chartHeight / 2 else 0f, if (roundEnd) chartHeight / 2 else 0f,
                if (roundEnd) chartHeight / 2 else 0f, if (roundEnd) chartHeight / 2 else 0f,
                if (roundStart) chartHeight / 2 else 0f, if (roundStart) chartHeight / 2 else 0f
            )
        } else {
            null
        }

        if (radii != null) {
            val path = Path()
            path.addRoundRect(
                rect,
                radii,
                Path.Direction.CW
            )
            canvas.drawPath(path, paint)
        } else {
            canvas.drawRect(rect, paint)
        }
    }

    private fun Float.toFixed(decimalPlaces: Int): String {
        return "%.${decimalPlaces}f".format(this)
    }
    private fun drawIndicator(canvas: Canvas) {
        // Calculate indicator position
        var indicatorX = width * currentProgress
        val indicatorY = 0f // Place indicator at the top of the view

        // Determine color based on progress
        val indicatorColor = getColorForProgress(currentProgress)

        // Draw text background with rounded corners and opacity
//        val text = (currentProgress * 100).toInt().toString()
        val text = currentBmiLevel.toFixed(1)
        val textBgWidth = 50f * resources.displayMetrics.density
        if (indicatorX < textBgWidth/2) {
            indicatorX = textBgWidth/2
        }
        val textBgHeight = indicatorHeight / 2

        val textBgLeft = indicatorX - textBgWidth / 2
        val textBgRight = indicatorX + textBgWidth / 2
        Log.d("kkkkkk", "indicatorX:$indicatorX , textBgWidth: $textBgWidth,  textBgLeft: $textBgLeft,  textBgRight: $textBgRight")
        val textBgTop = indicatorY
        val textBgBottom = indicatorY + textBgHeight
        rect.set(textBgLeft, textBgTop, textBgRight, textBgBottom)
        paint.color = adjustAlpha(indicatorColor, 0.2f) // 40% opacity
        canvas.drawRoundRect(rect, 60f, 60f, paint) // Rounded corners

        // Draw text
        indicatorPaint.color = indicatorColor // Change text color to match indicator
        canvas.drawText(
            text, indicatorX, textBgTop + textBgHeight / 2 + indicatorPaint.textSize / 2.5f,
            indicatorPaint
        )

        // Draw triangle below the text background
        val trianglePath = Path()
        val triangleHeight = indicatorHeight / 2
        val trianglePadding = 8f
//        trianglePath.moveTo(indicatorX, textBgBottom) // Top of the triangle
//        trianglePath.lineTo(indicatorX - triangleHeight / 2, textBgBottom + triangleHeight) // Bottom left
//        trianglePath.lineTo(indicatorX + triangleHeight / 2, textBgBottom + triangleHeight) // Bottom right

        trianglePath.moveTo(
            indicatorX,
            textBgBottom + triangleHeight - trianglePadding
        ) // Top of the triangle
        trianglePath.lineTo(
            indicatorX - (triangleHeight / 2) - (trianglePadding / 2),
            textBgBottom + trianglePadding
        ) // Bottom left
        trianglePath.lineTo(
            indicatorX + (triangleHeight / 2) - (trianglePadding / 2),
            textBgBottom + trianglePadding
        ) // Bottom right

        trianglePath.close()
        paint.color = indicatorColor // Triangle color same as indicator color
        canvas.drawPath(trianglePath, paint)
    }

    private fun getColorForProgress(progress: Float): Int {
        return when {
            progress <= 0.25f -> underweightColor
            progress <= 0.5f -> normalColor
            progress <= 0.75f -> overweightColor
            else -> obeseColor
        }
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).toInt()
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    fun setCurrentBMI(bmi: Float) {
        var progress: Double = when {
            bmi <= 0.0 -> {
                0.0
            }

            bmi < 18.5 -> {
                (bmi * 32.5) / 18.5
            }

            bmi >= 18.5 && bmi < 25 -> {
                (bmi * 42.5) / 25
            }

            bmi >= 25 && bmi < 30 -> {
                (bmi * 62.5) / 30
            }

            bmi >= 30 && bmi < 40 -> {
                (bmi * 100.0) / 40
            }

            else -> {
                100.0
            }
        }
        if (progress < 6) {
            progress = 6.0
        } else if (progress > 94) {
            progress = 94.0
        }
        setCurrentProgress(progress.toFloat() / 100, bmi)
    }

    private fun setCurrentProgress(progress: Float, bmiLevel: Float) {
//        Log.d("setCurrentProgress==", "bmiLevel:$bmiLevel , progress:${progress * 100}")
        if (progress < 0f || progress > 1f) {
            throw IllegalArgumentException("Progress must be between 0 and 1")
        }
        currentBmiLevel = bmiLevel
        currentProgress = progress
        invalidate() // Redraw the view
    }
}







