package com.example.fingerstyleguitartuner.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.fingerstyleguitartuner.R;
import com.example.fingerstyleguitartuner.ui.view.TunerWidgetView;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.ButterKnife;

public class CircleTunerView extends View implements TunerWidgetView {

    public static final int IN_TUNE = 0;
    public static final int OUT_OF_TUNE = 1;
    public static final int UNDEFINED = 2;

    private static final float RADIANS_90 = (float) Math.toRadians(90);
    private static final float RADIANS_360 = (float) Math.toRadians(360);

    private static final float TUNING_STATE_FREQUENCY_THRESHOLD = 1f;

    private final Paint indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint outerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stateCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint innerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Paint centerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);

    private final RectF outerCircleBounds = new RectF();
    private final Rect textBounds = new Rect();

    private final Path indicatorPath = new Path();
    private final PointF indicatorPoint1 = new PointF();
    private final PointF indicatorPoint2 = new PointF();
    private final PointF indicatorPoint3 = new PointF();

    private final List<NotePosition> notePositions = new ArrayList<>();

    @Radian
    private final float angleIntervalRadians;

    @Dimension
    @BindDimen(R.dimen.circle_tuner_view_default_min_size)
    int defaultMinSize;
    @ColorInt
    @BindColor(R.color.circle_tuner_view_default_indicator_color)
    int indicatorColor;
    @ColorInt
    @BindColor(R.color.circle_tuner_view_default_outer_circle_color)
    int outerCircleColor;
    @ColorInt
    @BindColor(R.color.circle_tuner_view_default_in_tune_color)
    int stateInTuneCircleColor;
    @ColorInt
    @BindColor(R.color.circle_tuner_view_default_out_of_tune_color)
    int stateOutOfTuneCircleColor;
    @ColorInt
    @BindColor(R.color.circle_tuner_view_default_inner_circle_color)
    int innerCircleColor;
    @ColorInt
    @BindColor(R.color.circle_tuner_view_default_text_color)
    int textColor;
    @BindArray(R.array.circle_tuner_view_notes)
    String[] notes;
    @ColorInt
    @BindColor(R.color.primary_dark_color)
    int shadowColor;
    @Dimension
    @BindDimen(R.dimen.circle_tuner_view_default_shadow_x)
    int shadowX;
    @Dimension
    @BindDimen(R.dimen.circle_tuner_view_default_shadow_y)
    int shadowY;
    @Dimension
    @BindDimen(R.dimen.circle_tuner_view_default_radius)
    int shadowRadius;

    private boolean showTunerState;

    private int centerX;
    private int centerY;
    private int innerCircleRadius;
    private int stateCircleRadius;
    private int outerCircleWidth;
    private int indicatorBottomRadius;
    private int indicatorRadius;
    private float centerTextY;

    @TuningState
    private int currentState = TuningState.UNDEFINED;
    @Radian
    private float currentAngleRadians;
    private String currentNoteName = "";

    private OnNotePressedListener listener;

    private float downX;
    private float downY;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CircleTunerView(Context context) {
        this(context, null, 0, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CircleTunerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CircleTunerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CircleTunerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        ButterKnife.bind(this);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleTunerView, defStyleAttr, defStyleRes);

        boolean showShadows;

        try {
            indicatorColor = a.getColor(R.styleable.CircleTunerView_indicatorColor, indicatorColor);
            outerCircleColor = a.getColor(R.styleable.CircleTunerView_outerCircleColor, outerCircleColor);
            innerCircleColor = a.getColor(R.styleable.CircleTunerView_indicatorColor, innerCircleColor);
            textColor = a.getColor(R.styleable.CircleTunerView_textColor, textColor);
            stateInTuneCircleColor = a.getColor(R.styleable.CircleTunerView_inTuneColor, stateInTuneCircleColor);
            stateOutOfTuneCircleColor = a.getColor(R.styleable.CircleTunerView_outOfTuneColor, stateOutOfTuneCircleColor);
            showShadows = a.getBoolean(R.styleable.CircleTunerView_showShadows, false);
            showTunerState = a.getBoolean(R.styleable.CircleTunerView_showTuningState, false);
        } finally {
            a.recycle();
        }

        // Setup the paint objects
        indicatorPaint.setColor(indicatorColor);
        indicatorPaint.setStyle(Paint.Style.FILL);
        outerCirclePaint.setColor(outerCircleColor);
        outerCirclePaint.setStyle(Paint.Style.STROKE);
        outerCirclePaint.setStrokeCap(Paint.Cap.ROUND);
        innerCirclePaint.setColor(innerCircleColor);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        centerTextPaint.setColor(textColor);
        centerTextPaint.setTextAlign(Paint.Align.CENTER);

        indicatorPath.setFillType(Path.FillType.EVEN_ODD);

        angleIntervalRadians = (float) Math.toRadians(360f / notes.length);

        if (showShadows) {
            showShadows();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getSideSize(widthMeasureSpec, defaultMinSize);
        int height = getSideSize(heightMeasureSpec, defaultMinSize);

        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        // Get the center coordinates of the view while considering padding as offsets
        centerX = (w / 2) + getPaddingStart() - getPaddingEnd();
        centerY = (h / 2) + getPaddingTop() - getPaddingBottom();

        // Get the smallest side which is the largest diameter of a circle without being cut off.
        // Take padding into consideration.
        int p1 = Math.max(getPaddingStart(), getPaddingTop());
        int p2 = Math.max(getPaddingEnd(), getPaddingBottom());
        int s = Math.min(w, h) - (p1 + p2);

        int outerCircleRadius = (s / 2);
        outerCircleWidth = s / 6;
        innerCircleRadius = (s / 4);

        outerCirclePaint.setStrokeWidth(outerCircleWidth);
        textPaint.setTextSize(outerCircleWidth / 2f);
        centerTextPaint.setTextSize(2 * (outerCircleWidth / 2f));

        // In order to draw a donut shape (circle without the center) we need to use a style of
        // STOKE on the paint and offset the bounds to end the stroke at the radius.
        // The drawArc() method in onDraw() with the useCenter parameter seems to do nothing.
        int outerCircleStrokeRadius = outerCircleRadius - (outerCircleWidth / 2);

        outerCircleBounds.set(centerX - outerCircleStrokeRadius,
                centerY - outerCircleStrokeRadius,
                centerX + outerCircleStrokeRadius,
                centerY + outerCircleStrokeRadius);

        // Determine the state circle radius
        stateCircleRadius = outerCircleRadius - outerCircleWidth;

        indicatorBottomRadius = s / 8;
        indicatorRadius = outerCircleRadius - (outerCircleWidth / 2);

        // Calculate the position of the outer text
        notePositions.clear();

        for (int i = 0; i < notes.length; i++) {
            String name = notes[i];

            float textX = (float) (centerX + (outerCircleStrokeRadius * Math.cos(angleIntervalRadians * i)));
            float textY = (float) (centerY + (outerCircleStrokeRadius * Math.sin(angleIntervalRadians * i)));

            // Retrieve the bounds of the text because the text paint only centers the text
            // horizontally and since each text height is different, it needs to be done per text
            textPaint.getTextBounds(name, 0, name.length(), textBounds);

            // Offset the text bounds since getTextBounds will return negative results
            textBounds.offsetTo(0, 0);

            // Offset y by half the height of the text to vertically center it
            notePositions.add(new NotePosition(name, textX, textY + textBounds.exactCenterY()));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the state circle - undefined state doesn't draw a circle keeping that region transparent
        if (showTunerState() && currentState != TuningState.UNDEFINED) {
            stateCirclePaint.setColor(currentState == TuningState.IN_TUNE ? stateInTuneCircleColor : stateOutOfTuneCircleColor);
            canvas.drawCircle(centerX, centerY, stateCircleRadius, stateCirclePaint);
        }

        // Draw the outer circle
        canvas.drawArc(outerCircleBounds, 0, 360, false, outerCirclePaint);

        // Draw the text on the outer circle
        for (NotePosition position : notePositions) {
            canvas.drawText(position.getName(), position.getX(), position.getY(), textPaint);
        }

        // Draw the indicator
        indicatorPath.reset();
        indicatorPath.moveTo(indicatorPoint1.x, indicatorPoint1.y);
        indicatorPath.lineTo(indicatorPoint2.x, indicatorPoint2.y);
        indicatorPath.lineTo(indicatorPoint3.x, indicatorPoint3.y);
        indicatorPath.close();
        canvas.drawPath(indicatorPath, indicatorPaint);

        // Draw the inner circle
        canvas.drawCircle(centerX, centerY, innerCircleRadius, innerCirclePaint);

        // Draw the text on the inner circle
        canvas.drawText(currentNoteName, centerX, centerTextY, centerTextPaint);
    }

    @Override
    @SuppressLint("WrongConstant")
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState savedState = new SavedState(superState);

        savedState.setState(currentState);
        savedState.setCurrentAngleRadians(currentAngleRadians);
        savedState.setCurrentNoteName(currentNoteName);

        return savedState;
    }

    @Override
    @SuppressLint("WrongConstant")
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (parcelable instanceof SavedState) {
            SavedState savedState = (SavedState) parcelable;

            super.onRestoreInstanceState(savedState.getSuperState());

            this.currentState = savedState.getState();
            this.currentAngleRadians = savedState.getCurrentAngleRadians();
            this.currentNoteName = savedState.getCurrentNoteName();
        } else {
            super.onRestoreInstanceState(parcelable);
        }
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        // We only care about the touch event if a listener is set
        if (listener != null) {
            float x = event.getX();
            float y = event.getY();

            float absoluteX = event.getRawX();
            float absoluteY = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = x;
                    downY = y;
                    return true;
                case MotionEvent.ACTION_UP:
                    String downNote = getTouchedNote(downX, downY);
                    String upNote = getTouchedNote(x, y);

                    if (downNote != null && downNote.equals(upNote) && getNotePosition(upNote) != null) {
                        listener.onNotePressed(new NotePosition(upNote, absoluteX, absoluteY));
                    }
                    break;
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void updateNote(final String noteName, final double frequency, final float percentOffset) {
        int p = 0;
        int l = notes.length;

        for (int i = 0; i < l; i++) {
            if (notes[i].equals(noteName)) {
                p = i;
                break;
            }
        }

        float angle = normalizeAngle(angleIntervalRadians * p + angleIntervalRadians * (percentOffset / 100));

        if (angle != currentAngleRadians) {
            if (!currentNoteName.equals(noteName)) {
                currentNoteName = noteName == null ? "" : noteName;

                // Unfortunately we have to recalculate the height of the text to properly align it in the center.
                // This can be rather slow
                centerTextPaint.getTextBounds(currentNoteName, 0, currentNoteName.length(), textBounds);

                textBounds.offsetTo(0, 0);

                centerTextY = centerY + textBounds.exactCenterY();
            }

            currentAngleRadians = angle;

            updateTuningState(percentOffset);
            updateIndicatorAngle(angle);
        }
    }

    /**
     * Tells this view to draw shadows to make the view appear to have layers. Note that once this
     * is called, it can't be unset. This is because Android doesn't allow Views to enable Hardware
     * Acceleration. To stop showing shadows, after calling this method, enable Hardware
     * Acceleration outside of this view. Shadows are not shown by default.
     */
    public void showShadows() {
        // Disables Hardware Acceleration so shadow can be drawn.
        // NOTE: There is no way to re-enable this on the View level once it is disabled (according
        // to the Android docs).
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        outerCirclePaint.setShadowLayer(shadowRadius, shadowX, shadowY, shadowColor);
        innerCirclePaint.setShadowLayer(shadowRadius, shadowX, shadowY, shadowColor);
        indicatorPaint.setShadowLayer(shadowRadius, shadowX, shadowY, shadowColor);
    }

    /**
     * Retrieves whether the view is setup to display the tuning state (in tune or out of tune).
     *
     * @return True if the tuning state is being displayed, false otherwise.
     */
    public boolean showTunerState() {
        return showTunerState;
    }

    /**
     * Sets whether to display the tuner state (in tune or out of tune).
     *
     * @param showTunerState boolean indicating whether to display the tuner state.
     */
    public void setShowTunerState(final boolean showTunerState) {
        this.showTunerState = showTunerState;
        invalidate();
    }

    @ColorInt
    public int getIndicatorColor() {
        return indicatorColor;
    }

    public void setIndicatorColor(@ColorInt final int indicatorColor) {
        this.indicatorColor = indicatorColor;
        this.indicatorPaint.setColor(indicatorColor);
        invalidate();
    }

    @ColorInt
    public int getOuterCircleColor() {
        return outerCircleColor;
    }

    public void setOuterCircleColor(@ColorInt final int outerCircleColor) {
        this.outerCircleColor = outerCircleColor;
        this.outerCirclePaint.setColor(outerCircleColor);
        invalidate();
    }

    @ColorInt
    public int getInTuneColor() {
        return stateInTuneCircleColor;
    }

    public void setInTuneColor(@ColorInt final int inTuneColor) {
        this.stateInTuneCircleColor = inTuneColor;
        invalidate();
    }

    @ColorInt
    public int getOutOfTuneColor() {
        return outerCircleColor;
    }

    public void setOutOfTuneColor(@ColorInt final int outOfTuneColor) {
        this.stateOutOfTuneCircleColor = outOfTuneColor;
        invalidate();
    }

    @ColorInt
    public int getInnerCircleColor() {
        return innerCircleColor;
    }

    public void setInnerCircleColor(@ColorInt final int innerCircleColor) {
        this.innerCircleColor = innerCircleColor;
        this.innerCirclePaint.setColor(innerCircleColor);
        invalidate();
    }

    @ColorInt
    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(@ColorInt final int textColor) {
        this.textColor = textColor;
        this.textPaint.setColor(textColor);
        invalidate();
    }

    @Nullable
    public OnNotePressedListener getOnNotePressedListener() {
        return listener;
    }

    public void setOnNotePressedListener(@Nullable OnNotePressedListener listener) {
        this.listener = listener;
    }

    private int getSideSize(final int measureSpec, final int defaultMinSize) {
        int mode = View.MeasureSpec.getMode(measureSpec);
        int size = View.MeasureSpec.getSize(measureSpec);

        if (mode == View.MeasureSpec.EXACTLY) {
            return size;
        }

        return (mode == View.MeasureSpec.AT_MOST) ? size : defaultMinSize;
    }

    @Radian
    private void updateIndicatorAngle(@Radian final float angleRadians) {
        // Outer point
        indicatorPoint1.set(centerX + (indicatorRadius * (float) Math.cos(angleRadians)),
                centerY + (indicatorRadius * (float) Math.sin(angleRadians)));

        // 90 degrees difference
        float bottomAngleRadians = normalizeAngle(angleRadians - RADIANS_90);

        indicatorPoint2.set(centerX + (indicatorBottomRadius * (float) Math.cos(bottomAngleRadians)),
                centerY + (indicatorBottomRadius * (float) Math.sin(bottomAngleRadians)));

        // 90 degrees difference
        bottomAngleRadians = normalizeAngle(angleRadians + RADIANS_90);

        indicatorPoint3.set(centerX + (indicatorBottomRadius * (float) Math.cos(bottomAngleRadians)),
                centerY + (indicatorBottomRadius * (float) Math.sin(bottomAngleRadians)));

        invalidate();
    }

    @Radian
    private float normalizeAngle(@Radian final float angleRadians) {
        float normalizedAngle = Math.abs(angleRadians) % RADIANS_360;
        return angleRadians < 0 ? RADIANS_360 - normalizedAngle : normalizedAngle;
    }

    private void updateTuningState(final float percentOffset) {
        currentState = TuningState.UNDEFINED;

        if (showTunerState()) {
            currentState = Math.abs(percentOffset) < TUNING_STATE_FREQUENCY_THRESHOLD ? TuningState.IN_TUNE : TuningState.OUT_OF_TUNE;
        }
    }

    @Nullable
    private String getTouchedNote(float x, float y) {
        // Get the radius of the touch point from the center point
        // (x - x0)^2 + (y - y0)^2 = r^2
        float touchRadius = (float) Math.sqrt(Math.pow((x - centerX), 2) + Math.pow((y - centerY), 2));

        float halfOuterCircleWidth = outerCircleWidth / 2f;

        // Determine if the touch point fell within the outer circle
        if (touchRadius >= (outerCircleBounds.width() / 2f) - halfOuterCircleWidth &&
                touchRadius <= (outerCircleBounds.width() / 2f) + halfOuterCircleWidth) {

            float touchAngleRadians = ((float) Math.atan2(y - centerY, x - centerX) + RADIANS_360) % RADIANS_360;

            int i = (int) (touchAngleRadians / angleIntervalRadians);
            float rem = touchAngleRadians % angleIntervalRadians;

            if (rem >= angleIntervalRadians / 2) {
                i = i + 1;
            }

            return notes[i % notes.length];
        }

        return null;
    }

    @Nullable
    private NotePosition getNotePosition(@Nullable String note) {
        if (note != null) {
            for (NotePosition p : notePositions) {
                if (p.getName().equals(note)) {
                    return p;
                }
            }
        }

        return null;
    }

    @SuppressWarnings("WeakerAccess")
    public static class NotePosition {

        private final String name;
        private final float x;
        private final float y;

        NotePosition(String name, float x, float y) {
            this.name = name;
            this.x = x;
            this.y = y;
        }

        public String getName() {
            return name;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }

    public interface OnNotePressedListener {
        void onNotePressed(NotePosition notePosition);
    }

    /**
     * Save state between orientation changes.
     */
    @SuppressWarnings("WeakerAccess")
    protected static class SavedState extends BaseSavedState {

        @TuningState
        private int state;
        @Radian
        private float currentAngleRadians;
        private String currentNoteName;

        static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        protected SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        protected SavedState(Parcel in) {
            super(in);

            int state = in.readInt();

            // Because state is an IntDef we need to verify it's one of the supported values
            switch (state) {
                case IN_TUNE:
                    this.state = TuningState.IN_TUNE;
                    break;
                case OUT_OF_TUNE:
                    this.state = TuningState.OUT_OF_TUNE;
                    break;
                default:
                case UNDEFINED:
                    this.state = TuningState.UNDEFINED;
                    break;
            }

            this.currentAngleRadians = in.readFloat();
            this.currentNoteName = in.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);

            out.writeInt(state);
            out.writeFloat(currentAngleRadians);
            out.writeString(currentNoteName);
        }

        @TuningState
        int getState() {
            return state;
        }

        void setState(@TuningState final int state) {
            this.state = state;
        }

        @Radian
        float getCurrentAngleRadians() {
            return currentAngleRadians;
        }

        void setCurrentAngleRadians(@Radian final float currentAngleRadians) {
            this.currentAngleRadians = currentAngleRadians;
        }

        String getCurrentNoteName() {
            return currentNoteName;
        }

        void setCurrentNoteName(final String currentNoteName) {
            this.currentNoteName = currentNoteName;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef()
    @interface TuningState {

        int IN_TUNE = 0;
        int OUT_OF_TUNE = 1;
        int UNDEFINED = 2;
    }

    /**
     * An annotation identifier to indicate that a particular value is in radians. No check is done to
     * determine if this annotation is properly used. It is mainly used for better clarity in
     * distinguishing between radian and angle fields.
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.PARAMETER})
    @interface Radian {
    }
}
