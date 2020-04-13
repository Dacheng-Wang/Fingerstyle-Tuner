package com.example.fingerstyleguitartuner.ui

import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import be.tarsos.dsp.util.PitchConverter
import com.example.fingerstyleguitartuner.R
import com.example.fingerstyleguitartuner.fragment.currentPage
import com.example.fingerstyleguitartuner.frequencyList
import com.example.fingerstyleguitartuner.noteList
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class CircleTunerView: View {
    private var currentAngleRadians = 0f
    private var centerX = 0
    private var centerY = 0
    private var innerCircleRadius = 0
    private var stateCircleRadius = 0
    private var outerCircleWidth = 0
    private var indicatorBottomRadius = 0
    private var indicatorRadius = 0
    private var centerTextY = 0f
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val innerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)
    private val centerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)
    private val outerCircleBounds = RectF()
    private val textBounds = Rect()
    private val indicatorPath = Path()
    private val indicatorPoint1 = PointF()
    private val indicatorPoint2 = PointF()
    private val indicatorPoint3 = PointF()
    private val indicator2Point1 = PointF()
    private val indicator2Point2 = PointF()
    private val indicator2Point3 = PointF()
    private val notePositions = ArrayList<NotePosition>()
    private val notes = resources.getStringArray(R.array.circle_tuner_view_notes)
    private var angleIntervalRadians = 0f
    private var targetNoteName = if (noteList.size > 0) noteList[currentPage] else ""
    private val radian90 = Math.toRadians(90.0).toFloat()
    private val radian360 = Math.toRadians(360.0).toFloat()

    private var indicatorColor = ContextCompat.getColor(context, R.color.circle_tuner_view_default_indicator_color)

    private var indicator2Color = ContextCompat.getColor(context, R.color.circle_tuner_view_default_indicator2_color)

    private var outerCircleColor = ContextCompat.getColor(context, R.color.circle_tuner_view_default_outer_circle_color)

    private var stateInTuneCircleColor = ContextCompat.getColor(context, R.color.circle_tuner_view_default_in_tune_color)

    private var stateOutOfTuneCircleColor = ContextCompat.getColor(context, R.color.circle_tuner_view_default_out_of_tune_color)

    private var innerCircleColor = ContextCompat.getColor(context, R.color.circle_tuner_view_default_inner_circle_color)

    private var textColor = ContextCompat.getColor(context, R.color.circle_tuner_view_default_text_color)

    private var shadowColor = ContextCompat.getColor(context, R.color.black)

    private var shadowX = resources.getDimension(R.dimen.circle_tuner_view_default_shadow_x)

    private var shadowY = resources.getDimension(R.dimen.circle_tuner_view_default_shadow_y)

    private var shadowRadius = resources.getDimension(R.dimen.circle_tuner_view_default_radius)

    private var isInitialized = false

    constructor(context: Context) : super(context) {

    }
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CircleTunerView)

        val showShadows: Boolean

        try {
            indicatorColor = a.getColor(R.styleable.CircleTunerView_indicatorColor, indicatorColor)
            indicator2Color = a.getColor(R.styleable.CircleTunerView_indicatorColor, indicator2Color)
            outerCircleColor = a.getColor(R.styleable.CircleTunerView_outerCircleColor, outerCircleColor)
            innerCircleColor = a.getColor(R.styleable.CircleTunerView_innerCircleColor, innerCircleColor)
            textColor = a.getColor(R.styleable.CircleTunerView_textColor, textColor)
            stateInTuneCircleColor = a.getColor(R.styleable.CircleTunerView_inTuneColor, stateInTuneCircleColor)
            stateOutOfTuneCircleColor = a.getColor(R.styleable.CircleTunerView_outOfTuneColor, stateOutOfTuneCircleColor)
            //showShadows = a.getBoolean(R.styleable.CircleTunerView_showShadows, false)
            showShadows = true
        } finally {
            a.recycle()
        }

        // Setup the paint objects

        // Setup the paint objects
        indicatorPaint.color = indicatorColor
        indicatorPaint.style = Paint.Style.FILL
        outerCirclePaint.color = outerCircleColor
        outerCirclePaint.style = Paint.Style.STROKE
        outerCirclePaint.strokeCap = Paint.Cap.ROUND
        innerCirclePaint.color = innerCircleColor
        textPaint.color = textColor
        textPaint.textAlign = Paint.Align.CENTER
        centerTextPaint.color = textColor
        centerTextPaint.textAlign = Paint.Align.CENTER

        indicatorPath.fillType = Path.FillType.EVEN_ODD

        angleIntervalRadians = Math.toRadians(360 / notes.size.toDouble()).toFloat()

        if (showShadows) {
            showShadows()
        }
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr) {

    }

    private fun showShadows() {
        // Disables Hardware Acceleration so shadow can be drawn.
        // NOTE: There is no way to re-enable this on the View level once it is disabled (according
        // to the Android docs).
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        outerCirclePaint.setShadowLayer(shadowRadius, shadowX, shadowY, shadowColor)
        innerCirclePaint.setShadowLayer(shadowRadius, shadowX, shadowY, shadowColor)
        indicatorPaint.setShadowLayer(shadowRadius, shadowX, shadowY, shadowColor)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
// Get the center coordinates of the view while considering padding as offsets

        // Get the center coordinates of the view while considering padding as offsets
        centerX = w / 2 + paddingStart - paddingEnd
        centerY = h / 2 + paddingTop - paddingBottom

        // Get the smallest side which is the largest diameter of a circle without being cut off.
        // Take padding into consideration.

        // Get the smallest side which is the largest diameter of a circle without being cut off.
        // Take padding into consideration.
        val p1 = paddingStart.coerceAtLeast(paddingTop)
        val p2 = paddingEnd.coerceAtLeast(paddingBottom)
        val s = w.coerceAtMost(h) - (p1 + p2)

        val outerCircleRadius = s / 2
        outerCircleWidth = s / 6
        innerCircleRadius = s / 4

        outerCirclePaint.strokeWidth = outerCircleWidth.toFloat()
        textPaint.textSize = outerCircleWidth / 2.toFloat()
        centerTextPaint.textSize = 2 * (outerCircleWidth / 2).toFloat()

        // In order to draw a donut shape (circle without the center) we need to use a style of
        // STOKE on the paint and offset the bounds to end the stroke at the radius.
        // The drawArc() method in onDraw() with the useCenter parameter seems to do nothing.

        // In order to draw a donut shape (circle without the center) we need to use a style of
        // STOKE on the paint and offset the bounds to end the stroke at the radius.
        // The drawArc() method in onDraw() with the useCenter parameter seems to do nothing.
        val outerCircleStrokeRadius = outerCircleRadius - outerCircleWidth / 2

        outerCircleBounds.set(
            centerX - outerCircleStrokeRadius.toFloat(),
            centerY - outerCircleStrokeRadius.toFloat(),
            centerX + outerCircleStrokeRadius.toFloat(),
            centerY + outerCircleStrokeRadius.toFloat()
        )

        // Determine the state circle radius

        // Determine the state circle radius
        stateCircleRadius = outerCircleRadius - outerCircleWidth

        indicatorBottomRadius = s / 8
        indicatorRadius = outerCircleRadius - outerCircleWidth / 2

        // Calculate the position of the outer text

        // Calculate the position of the outer text
        notePositions.clear()

        for (i in notes.indices) {
            val name: String = notes[i]
            val textX = (centerX + outerCircleStrokeRadius * cos(angleIntervalRadians * i.toDouble())).toFloat()
            val textY = (centerY + outerCircleStrokeRadius * sin(angleIntervalRadians * i.toDouble())).toFloat()

            // Retrieve the bounds of the text because the text paint only centers the text
            // horizontally and since each text height is different, it needs to be done per text
            textPaint.getTextBounds(name, 0, name.length, textBounds)

            // Offset the text bounds since getTextBounds will return negative results
            textBounds.offsetTo(0, 0)

            // Offset y by half the height of the text to vertically center it
            notePositions.add(NotePosition(name, textX, textY + textBounds.exactCenterY()))
        }
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // Draw the outer circle

        // Draw the outer circle
        canvas!!.drawArc(outerCircleBounds, 0f, 360f, false, outerCirclePaint)

        // Draw the text on the outer circle

        // Draw the text on the outer circle
        for (position in notePositions) {
            canvas.drawText(position.name, position.x, position.y, textPaint)
        }

        // Draw the target indicator
        indicatorPath.reset()
        indicatorPath.moveTo(indicatorPoint1.x, indicatorPoint1.y)
        indicatorPath.lineTo(indicatorPoint2.x, indicatorPoint2.y)
        indicatorPath.lineTo(indicatorPoint3.x, indicatorPoint3.y)
        indicatorPath.close()
        indicatorPaint.color = indicatorColor
        canvas.drawPath(indicatorPath, indicatorPaint)
        // Draw the live indicator
        indicatorPath.reset()
        indicatorPath.moveTo(indicator2Point1.x, indicator2Point1.y)
        indicatorPath.lineTo(indicator2Point2.x, indicator2Point2.y)
        indicatorPath.lineTo(indicator2Point3.x, indicator2Point3.y)
        indicatorPath.close()
        indicatorPaint.color = indicator2Color
        canvas.drawPath(indicatorPath, indicatorPaint)

        // Draw the inner circle

        // Draw the inner circle
        canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), innerCircleRadius.toFloat(), innerCirclePaint)

        // Draw the text on the inner circle

        // Draw the text and indicator on the inner circle - this only need to be done once per fragment
        if (noteList.size > 0 && !isInitialized) {
            var p = 0
            val l = notes.size
            for (i in 0 until l) {
                if (notes[i] == targetNoteName) {
                    p = i
                    break
                }
            }
            val angle: Float = normalizeAngle(angleIntervalRadians * p)
            // Unfortunately we have to recalculate the height of the text to properly align it in the center.
            // This can be rather slow
            centerTextPaint.getTextBounds(targetNoteName, 0, targetNoteName.length, textBounds)
            textBounds.offsetTo(0, 0)
            centerTextY = centerY + textBounds.exactCenterY()
            currentAngleRadians = angle
            updateIndicatorAngle(angle, indicatorPoint1, indicatorPoint2, indicatorPoint3)
            isInitialized = true
        }
        canvas.drawText(targetNoteName, centerX.toFloat(), centerTextY, centerTextPaint)
    }

    private fun normalizeAngle(angleRadians: Float): Float {
        val normalizedAngle: Float = abs(angleRadians) % radian360
        return if (angleRadians < 0) radian360 - normalizedAngle else normalizedAngle
    }

    private fun updateIndicatorAngle(angleRadians: Float, point1: PointF, point2: PointF, point3: PointF) {
        // Outer point
        point1[centerX + indicatorRadius * cos(angleRadians.toDouble()).toFloat()] =
            centerY + indicatorRadius * sin(angleRadians.toDouble()).toFloat()

        // 90 degrees difference
        var bottomAngleRadians = normalizeAngle(angleRadians - radian90)
        point2[centerX + indicatorBottomRadius * cos(bottomAngleRadians.toDouble()).toFloat()] =
            centerY + indicatorBottomRadius * sin(bottomAngleRadians.toDouble()).toFloat()

        // 90 degrees difference
        bottomAngleRadians = normalizeAngle(angleRadians + radian90)
        point3[centerX + indicatorBottomRadius * cos(bottomAngleRadians.toDouble()).toFloat()] =
            centerY + indicatorBottomRadius * sin(bottomAngleRadians.toDouble()).toFloat()
        invalidate()
    }
    fun updateIndicator2Angle(view: TextView, targetFrequency: Double): Double {
        val capturedFrequency = (view.tag as Float).toDouble()
        val targetAngle = currentAngleRadians
        if (capturedFrequency >= targetFrequency / 2 && capturedFrequency <= targetFrequency * 2) {
            val capturedCents = PitchConverter.hertzToAbsoluteCent(capturedFrequency)
            val targetCents = PitchConverter.hertzToAbsoluteCent(targetFrequency)
            val centsDiff = capturedCents - targetCents
            val angleDiff = (centsDiff / 1200 * radian360).toFloat()
            val angleResult = normalizeAngle(targetAngle + angleDiff)
            updateIndicatorAngle(angleResult, indicator2Point1, indicator2Point2, indicator2Point3)
        }
        return capturedFrequency - targetFrequency
    }

    fun outOfTuneChangeColor() {
        innerCirclePaint.color = stateOutOfTuneCircleColor
        invalidate()
    }
    fun inRangeChangeColor() {
        innerCirclePaint.color = innerCircleColor
        invalidate()
    }
    fun inTuneChangeColor() {
        innerCirclePaint.color = stateInTuneCircleColor
        invalidate()
    }

    class NotePosition internal constructor(val name: String, val x: Float, val y: Float)
}