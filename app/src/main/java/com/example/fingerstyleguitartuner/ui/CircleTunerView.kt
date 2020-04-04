package com.example.fingerstyleguitartuner.ui

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.fingerstyleguitartuner.R
import com.example.fingerstyleguitartuner.frequency
import com.example.fingerstyleguitartuner.note
import kotlin.math.cos
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
    private val notePositions = ArrayList<NotePosition>()
    private val notes = resources.getStringArray(R.array.circle_tuner_view_notes)
    private var angleIntervalRadians = 0f
    private var currentNoteName = ""
    private val RADIANS_90 = Math.toRadians(90.0).toFloat()
    private val RADIANS_360 = Math.toRadians(360.0).toFloat()

    var indicatorColor = ContextCompat.getColor(context, R.color.circle_tuner_view_default_indicator_color)

    var outerCircleColor = ContextCompat.getColor(context, R.color.circle_tuner_view_default_outer_circle_color)

    var stateInTuneCircleColor = ContextCompat.getColor(context, R.color.circle_tuner_view_default_in_tune_color)

    var stateOutOfTuneCircleColor = ContextCompat.getColor(context, R.color.circle_tuner_view_default_out_of_tune_color)

    var innerCircleColor = ContextCompat.getColor(context, R.color.circle_tuner_view_default_inner_circle_color)

    var textColor = ContextCompat.getColor(context, R.color.circle_tuner_view_default_text_color)

    var shadowColor = ContextCompat.getColor(context, R.color.black)

    var shadowX = resources.getDimension(R.dimen.circle_tuner_view_default_shadow_x)

    var shadowY = resources.getDimension(R.dimen.circle_tuner_view_default_shadow_y)

    var shadowRadius = resources.getDimension(R.dimen.circle_tuner_view_default_radius)

    constructor(context: Context) : super(context) {

    }
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CircleTunerView)

        val showShadows: Boolean

        try {
            indicatorColor = a.getColor(R.styleable.CircleTunerView_indicatorColor, indicatorColor)
            outerCircleColor = a.getColor(R.styleable.CircleTunerView_outerCircleColor, outerCircleColor)
            innerCircleColor = a.getColor(R.styleable.CircleTunerView_indicatorColor, innerCircleColor)
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

        indicatorPath.setFillType(Path.FillType.EVEN_ODD)

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

        // Draw the indicator

        // Draw the indicator
        indicatorPath.reset()
        indicatorPath.moveTo(indicatorPoint1.x, indicatorPoint1.y)
        indicatorPath.lineTo(indicatorPoint2.x, indicatorPoint2.y)
        indicatorPath.lineTo(indicatorPoint3.x, indicatorPoint3.y)
        indicatorPath.close()
        canvas.drawPath(indicatorPath, indicatorPaint)

        // Draw the inner circle

        // Draw the inner circle
        canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), innerCircleRadius.toFloat(), innerCirclePaint)

        // Draw the text on the inner circle

        // Draw the text on the inner circle

        updateNote(note[0], frequency[0].toDouble(), 0f)
        canvas.drawText(currentNoteName, centerX.toFloat(), centerTextY, centerTextPaint)
    }

    fun updateNote(noteName: String?, frequency: Double, percentOffset: Float) {
        var p = 0
        val l = notes.size
        for (i in 0 until l) {
            if (notes[i] == noteName) {
                p = i
                break
            }
        }
        val angle: Float = normalizeAngle(angleIntervalRadians * p + angleIntervalRadians * (percentOffset / 100))
        if (currentNoteName != noteName) {
            currentNoteName = noteName ?: ""
            // Unfortunately we have to recalculate the height of the text to properly align it in the center.
            // This can be rather slow
            centerTextPaint.getTextBounds(currentNoteName, 0, currentNoteName.length, textBounds)
            textBounds.offsetTo(0, 0)
            centerTextY = centerY + textBounds.exactCenterY()
            currentAngleRadians = angle
            updateIndicatorAngle(angle)
        }
    }

    private fun normalizeAngle(angleRadians: Float): Float {
        val normalizedAngle: Float = Math.abs(angleRadians) % RADIANS_360
        return if (angleRadians < 0) RADIANS_360 - normalizedAngle else normalizedAngle
    }

    private fun updateIndicatorAngle(angleRadians: Float) {
        // Outer point
        indicatorPoint1[centerX + indicatorRadius * Math.cos(angleRadians.toDouble()).toFloat()] =
            centerY + indicatorRadius * Math.sin(angleRadians.toDouble()).toFloat()

        // 90 degrees difference
        var bottomAngleRadians = normalizeAngle(angleRadians - RADIANS_90)
        indicatorPoint2[centerX + indicatorBottomRadius * cos(bottomAngleRadians.toDouble()).toFloat()] =
            centerY + indicatorBottomRadius * sin(bottomAngleRadians.toDouble()).toFloat()

        // 90 degrees difference
        bottomAngleRadians = normalizeAngle(angleRadians + RADIANS_90)
        indicatorPoint3[centerX + indicatorBottomRadius * cos(bottomAngleRadians.toDouble()).toFloat()] =
            centerY + indicatorBottomRadius * Math.sin(bottomAngleRadians.toDouble()).toFloat()
        invalidate()
    }

    class NotePosition internal constructor(val name: String, val x: Float, val y: Float)
}