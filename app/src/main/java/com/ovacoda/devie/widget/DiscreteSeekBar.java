package com.ovacoda.devie.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.PopupWindow;

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
    private PopupWindow mBubblePopup;
    private BubbleView mBubbleView;

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
            hideBubble();
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
        if (mTracking) {
            updateBubble();
        }
    }

    private void ensureBubble() {
        int density = (int) (getResources().getDisplayMetrics().density + 0.5f);
        if (mBubblePopup == null) {
            mBubbleView = new BubbleView(getContext(), density);
            mBubblePopup = new PopupWindow(mBubbleView, (36 * density), (26 * density), false);
            mBubblePopup.setTouchable(false);
            mBubblePopup.setClippingEnabled(false);
            mBubblePopup.showAtLocation(getRootView(), Gravity.NO_GRAVITY, 0, -9999);
        }
    }

    private void updateBubble() {
        ensureBubble();
        int[] locationOnScreen = new int[2];
        getLocationOnScreen(locationOnScreen);
        int density = (int) (getResources().getDisplayMetrics().density + 0.5f);
        int pillWidth = 36 * density;
        int pillHeight = 26 * density;
        int thumbScreenX = locationOnScreen[0] + ((int) thumbCenterX());
        int thumbScreenY = locationOnScreen[1] + ((int) trackCenterY()) - thumbRadius();
        int x = thumbScreenX - (pillWidth / 2);
        int max = getResources().getDisplayMetrics().widthPixels;
        if (x + pillWidth > max) {
            x = max - pillWidth;
        }
        if (x < 0) {
            x = 0;
        }
        mBubbleView.setValue(mValue, density);
        mBubblePopup.update(x, (thumbScreenY - (10 * density)) - pillHeight, (-1), (-1));
    }

    private void hideBubble() {
        PopupWindow popupWindow = this.mBubblePopup;
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.update(0, -9999, (-1), (-1));
        }
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        hideBubble();
        super.onDetachedFromWindow();
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
        }
        canvas.drawCircle(thumbX, centerY, thumbRadius, mThumbPaint);
    }

    private final class BubbleView extends View {
        private int mDensity;
        private String mLabel = "";

        BubbleView(Context context, int density) {
            super(context);
            this.mDensity = density;
        }

        void setValue(int value, int density) {
            this.mDensity = density;
            this.mLabel = String.valueOf(value);
            invalidate();
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            int density = this.mDensity;
            DiscreteSeekBar.this.mRect.set(0.0f, 0.0f, 36 * density, 18 * density);
            canvas.drawRoundRect(DiscreteSeekBar.this.mRect, 3 * density, 3 * density, DiscreteSeekBar.this.mBubblePaint);
            DiscreteSeekBar.this.mBubblePath.reset();
            float centerX = (36 * density) / 2.0f;
            DiscreteSeekBar.this.mBubblePath.moveTo(centerX - (3 * density), (17 * density));
            DiscreteSeekBar.this.mBubblePath.cubicTo(centerX - (3 * density), 22 * density, centerX - (1.5f * density), (23.5f * density), centerX, 26 * density);
            DiscreteSeekBar.this.mBubblePath.cubicTo(centerX + (1.5f * density), (23.5f * density), centerX + (3 * density), 22 * density, centerX + (3 * density), (17 * density));
            DiscreteSeekBar.this.mBubblePath.close();
            canvas.drawPath(DiscreteSeekBar.this.mBubblePath, DiscreteSeekBar.this.mBubblePaint);
            DiscreteSeekBar.this.mBubbleTextPaint.setTextSize(13 * density);
            DiscreteSeekBar.this.mBubbleTextPaint.getFontMetrics(DiscreteSeekBar.this.mBubbleFontMetrics);
            canvas.drawText(this.mLabel, centerX, (9 * density) - ((DiscreteSeekBar.this.mBubbleFontMetrics.ascent + DiscreteSeekBar.this.mBubbleFontMetrics.descent) / 2), DiscreteSeekBar.this.mBubbleTextPaint);
        }
    }
}