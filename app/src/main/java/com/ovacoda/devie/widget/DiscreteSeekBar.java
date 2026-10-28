package com.ovacoda.devie.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

public class DiscreteSeekBar extends View {
    private static final int DEFAULT_TRACK_COLOR = 0x80b8b8b8;
    private static final int DEFAULT_PROGRESS_COLOR = 0xff3bdab6;
    private static final int DEFAULT_THUMB_COLOR = 0xff0bd7ba;
    private static final int DISABLED_THUMB_COLOR = 0xff78909c;

    private int mMin;
    private int mMax = 100;
    private int mValue;
    private OnProgressChangeListener mListener;
    private boolean mTracking;
    private int mTrackColor = DEFAULT_TRACK_COLOR;
    private int mProgressColor = DEFAULT_PROGRESS_COLOR;
    private int mThumbColorNormal = DEFAULT_THUMB_COLOR;
    private int mThumbColorPressed = DEFAULT_THUMB_COLOR;
    private final Paint mTrackPaint = new Paint(1);
    private final Paint mProgressPaint = new Paint(1);
    private final Paint mThumbPaint = new Paint(1);
    private final Paint mHaloPaint = new Paint(1);
    private final Paint mBubblePaint = new Paint(1);
    private final Paint mBubbleTextPaint = new Paint(1);
    private final Paint.FontMetrics mBubbleFontMetrics = new Paint.FontMetrics();
    private final Path mBubblePath = new Path();
    private final RectF mRect = new RectF();

    public interface OnProgressChangeListener {
        void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser);

        void onStartTrackingTouch(DiscreteSeekBar seekBar);

        void onStopTrackingTouch(DiscreteSeekBar seekBar);
    }

    public DiscreteSeekBar(Context context) {
        super(context);
        initialize();
    }

    public DiscreteSeekBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initialize();
    }

    public DiscreteSeekBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        initialize();
    }

    public DiscreteSeekBar(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        initialize();
    }

    private void initialize() {
        mTrackPaint.setColor(mTrackColor);
        mProgressPaint.setColor(mProgressColor);
        mThumbPaint.setColor(mThumbColorNormal);
        mBubblePaint.setColor(mThumbColorPressed);
        mBubbleTextPaint.setColor(0xff1f2a38);
        mBubbleTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    public int getMin() {
        return mMin;
    }

    public int getMax() {
        return mMax;
    }

    public void setMin(int i) {
        setRange(i, mMax);
    }

    public void setMax(int i) {
        setRange(mMin, i);
    }

    private void setRange(int i, int i2) {
        int i3;
        if (i > i2) {
            i3 = i2;
            i2 = i;
        } else {
            i3 = i;
        }
        mMin = i3;
        mMax = i2;
        setValue(mValue);
    }

    public void setValue(int i) {
        int clamp = clamp(i);
        if (mValue == clamp) {
            return;
        }
        mValue = clamp;
        invalidate();
    }

    public int getValue() {
        return mValue;
    }

    private int clamp(int i) {
        if (i < mMin) {
            return mMin;
        }
        if (i > mMax) {
            return mMax;
        }
        return i;
    }

    public void setOnProgressChangeListener(OnProgressChangeListener onProgressChangeListener) {
        this.mListener = onProgressChangeListener;
    }

    public void setScrubberColor(int i) {
        mProgressColor = i;
        mProgressPaint.setColor(i);
        invalidate();
    }

    public void setThumbColor(int i, int i2) {
        mThumbColorNormal = i;
        mThumbColorPressed = i2;
        mThumbPaint.setColor(mThumbColorNormal);
        mBubblePaint.setColor(mThumbColorPressed);
        invalidate();
    }

    @Override // android.view.View
    public void setEnabled(boolean z) {
        super.setEnabled(z);
        invalidate();
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int density = (int) (getResources().getDisplayMetrics().density + 0.5f);
        int heightSpecMode = View.MeasureSpec.getMode(i2);
        int heightSpecSize = View.MeasureSpec.getSize(i2);
        int defaultHeight = 27 * density;
        int measuredHeight;
        if (heightSpecMode == 1073741824) {
            measuredHeight = heightSpecSize;
        } else if (heightSpecMode == -2147483648) {
            measuredHeight = Math.min(defaultHeight, heightSpecSize);
        } else {
            measuredHeight = defaultHeight;
        }
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), i), measuredHeight);
    }

    private int thumbRadius() {
        return ((int) (getResources().getDisplayMetrics().density + 0.5f)) * 5;
    }

    private float trackStartX() {
        return getPaddingLeft() + thumbRadius();
    }

    private float trackLength() {
        return Math.max(0.0f, ((getWidth() - getPaddingLeft()) - getPaddingRight()) - (thumbRadius() * 2));
    }

    private float valueFraction() {
        return mMax == mMin ? 0.0f : (mValue - mMin) / (float) (mMax - mMin);
    }

    private float thumbCenterX() {
        return trackStartX() + (valueFraction() * trackLength());
    }

    private float trackCenterY() {
        return getHeight() / 2.0f;
    }

    private void attemptClaimDrag() {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!isEnabled()) {
            return false;
        }
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            attemptClaimDrag();
            mTracking = true;
            setPressed(true);
            setValueFromTouch(motionEvent.getX(), true);
            return true;
        } else if (actionMasked == 1 || actionMasked == 3) {
            if (!mTracking) {
                return false;
            }
            mTracking = false;
            setPressed(false);
            if (actionMasked == 1) {
                setValueFromTouch(motionEvent.getX(), true);
            }
            if (mListener != null) {
                mListener.onStopTrackingTouch(this);
            }
            return true;
        } else if (actionMasked == 2) {
            if (!mTracking) {
                return false;
            }
            setValueFromTouch(motionEvent.getX(), true);
            return true;
        } else {
            return super.onTouchEvent(motionEvent);
        }
    }

    private void setValueFromTouch(float f, boolean z) {
        float trackLength = trackLength();
        int i = mMin;
        if (trackLength > 0.0f) {
            i += (int) ((Math.max(0.0f, Math.min(trackLength, f - trackStartX())) / trackLength) * ((mMax - mMin) + 0.5f));
        }
        int original = mValue;
        setValue(i);
        if (mValue != original && mListener != null) {
            mListener.onProgressChanged(this, mValue, z);
        }
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        int density = (int) (getResources().getDisplayMetrics().density + 0.5f);
        float trackStart = trackStartX();
        float trackLength = trackLength();
        float centerY = trackCenterY();
        float thumbRadius = 5 * density;
        float trackHalf = 1.5f * density;
        if (isPressed()) {
            thumbRadius = 6 * density;
        }
        mTrackPaint.setColor(mTrackColor);
        mProgressPaint.setColor(mProgressColor);
        if (!isEnabled()) {
            mThumbPaint.setColor(DISABLED_THUMB_COLOR);
        } else if (isPressed()) {
            mThumbPaint.setColor(mThumbColorPressed);
        } else {
            mThumbPaint.setColor(mThumbColorNormal);
        }
        mRect.set(trackStart, centerY - trackHalf, trackStart + trackLength, centerY + trackHalf);
        canvas.drawRoundRect(mRect, trackHalf, trackHalf, mTrackPaint);
        float thumbX = thumbCenterX();
        if (mValue > mMin && trackLength > 0.0f) {
            mRect.set(trackStart, centerY - trackHalf, thumbX, centerY + trackHalf);
            canvas.drawRoundRect(mRect, trackHalf, trackHalf, mProgressPaint);
        }
        if (isPressed() && isEnabled()) {
            mHaloPaint.setColor(mThumbPaint.getColor());
            mHaloPaint.setAlpha(64);
            canvas.drawCircle(thumbX, centerY, thumbRadius * 3.0f, mHaloPaint);
            mHaloPaint.setAlpha(255);
            drawValueBubble(canvas, thumbX, centerY - thumbRadius - (4 * density), density);
        }
        canvas.drawCircle(thumbX, centerY, thumbRadius, mThumbPaint);
    }

    private void drawValueBubble(Canvas canvas, float f, float f2, int density) {
        String label = String.valueOf(mValue);
        mBubbleTextPaint.setTextSize(13 * density);
        float width = mBubbleTextPaint.measureText(label);
        mBubbleTextPaint.getFontMetrics(mBubbleFontMetrics);
        int bubbleWidth = ((int) width) + (8 * density);
        int bubbleHeight = 15 * density;
        int arrowHalf = 2 * density;
        int arrowHeight = 3 * density;
        int left = ((int) f) - (bubbleWidth / 2);
        if (left < 0) {
            left = 0;
        } else if (left + bubbleWidth > getWidth()) {
            left = getWidth() - bubbleWidth;
        }
        int top = ((int) f2) - arrowHeight - bubbleHeight;
        if (top < 0) {
            top = 0;
        }
        int bottom = top + bubbleHeight;
        mRect.set(left, top, left + bubbleWidth, bottom);
        canvas.drawRoundRect(mRect, 3 * density, 3 * density, mBubblePaint);
        mBubblePath.reset();
        mBubblePath.moveTo(f - arrowHalf, bottom - 1);
        mBubblePath.lineTo(f, bottom + arrowHeight);
        mBubblePath.lineTo(f + arrowHalf, bottom - 1);
        mBubblePath.close();
        canvas.drawPath(mBubblePath, mBubblePaint);
        canvas.drawText(label, f, top + (bubbleHeight / 2) - ((mBubbleFontMetrics.ascent + mBubbleFontMetrics.descent) / 2), mBubbleTextPaint);
    }
}