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
import android.view.ViewConfiguration;
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
    private float mTouchDownX;
    private float mTouchDownY;
    private int mTrackColor = DEFAULT_TRACK_COLOR;
    private int mProgressColor = DEFAULT_PROGRESS_COLOR;
    private int mThumbColorNormal = DEFAULT_THUMB_COLOR;
    private int mThumbColorPressed = DEFAULT_THUMB_COLOR;
    private final Paint mTrackPaint = new Paint(1);
    private final Paint mProgressPaint = new Paint(1);
    private final Paint mThumbPaint = new Paint(1);
    private final Paint mHaloPaint = new Paint(1);
    private final Paint mBubblePaint = new Paint(1);
    private final Paint mKnobPaint = new Paint(1);
    private final Paint mBubbleTextPaint = new Paint(1);
    private final Paint.FontMetrics mBubbleFontMetrics = new Paint.FontMetrics();
    private final Path mBubblePath = new Path();
    private final RectF mRect = new RectF();
    private PopupWindow mOverlayPopup;
    private OverlayView mOverlayView;

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
        mKnobPaint.setStyle(Paint.Style.FILL);
    }

    public int getMin() {
        return mMin;
    }

    public int getMax() {
        return mMax;
    }

    public void setMin(int i) {
        /* adw-parity: a bound setter never silently inverts the range; the
         * other bound only moves if it would be left outside. */
        if (mMax < i) {
            mMax = i;
        }
        mMin = i;
        setValue(mValue);
    }

    public void setMax(int i) {
        if (mMin > i) {
            mMin = i;
        }
        mMax = i;
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

    private int touchSlop() {
        return ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!isEnabled()) {
            return false;
        }
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            /* Remember the down point; the drag only starts once a sideways
             * move past the touch slop arrives (see the MOVE branch). */
            mTouchDownX = motionEvent.getX();
            mTouchDownY = motionEvent.getY();
            mTracking = false;
            setPressed(true);
            return true;
        } else if (actionMasked == 1 || actionMasked == 3) {
            setPressed(false);
            if (mTracking) {
                mTracking = false;
                if (actionMasked == 1) {
                    setValueFromTouch(motionEvent.getX(), true);
                }
                hideOverlay();
                if (mListener != null) {
                    mListener.onStopTrackingTouch(this);
                }
                return true;
            }
            if (actionMasked == 1) {
                /* Tap without drag: jump to the tapped value, bracketed by
                 * the start/stop callbacks so previews react too. */
                if (mListener != null) {
                    mListener.onStartTrackingTouch(this);
                }
                setValueFromTouch(motionEvent.getX(), true);
                if (mListener != null) {
                    mListener.onStopTrackingTouch(this);
                }
            }
            return true;
        } else if (actionMasked == 2) {
            /* REWORK(gesture): defer taking the drag until the finger has
             * moved sideways past the touch slop (adw-parity inside scroll
             * containers); vertical gestures keep scrolling the page. */
            if (!mTracking) {
                float deltaX = motionEvent.getX() - mTouchDownX;
                float deltaY = motionEvent.getY() - mTouchDownY;
                if (Math.abs(deltaX) > touchSlop() && Math.abs(deltaX) > Math.abs(deltaY)) {
                    mTracking = true;
                    attemptClaimDrag();
                    if (mListener != null) {
                        mListener.onStartTrackingTouch(this);
                    }
                    setValueFromTouch(motionEvent.getX(), true);
                    updateOverlay();
                }
                return true;
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
            /* Round, don't truncate: truncating made the last step half as
             * wide as the others, so max was hard to reach. */
            float fraction = Math.max(0.0f, Math.min(trackLength, f - trackStartX())) / trackLength;
            i += Math.round(fraction * (float) (mMax - mMin));
        }
        int original = mValue;
        setValue(i);
        if (mValue != original && mListener != null) {
            mListener.onProgressChanged(this, mValue, z);
        }
        if (mTracking) {
            updateOverlay();
        }
    }

    /* Overlay (popup layer) draws the press halo + the pin-drop value bubble so
     * neither is clipped by this view's ~27-dip-tall canvas. */
    private void ensureOverlay() {
        int density = (int) (getResources().getDisplayMetrics().density + 0.5f);
        if (mOverlayPopup == null) {
            mOverlayView = new OverlayView(getContext(), density);
            mOverlayPopup = new PopupWindow(mOverlayView, (56 * density), (96 * density), false);
            mOverlayPopup.setTouchable(false);
            mOverlayPopup.setClippingEnabled(false);
            mOverlayPopup.showAtLocation(getRootView(), Gravity.NO_GRAVITY, 0, -9999);
        }
    }

    private void updateOverlay() {
        ensureOverlay();
        int[] locationOnScreen = new int[2];
        getLocationOnScreen(locationOnScreen);
        int density = (int) (getResources().getDisplayMetrics().density + 0.5f);
        mOverlayView.setValue(mValue, density);
        int overlayWidth = 56 * density;
        int overlayHeight = 96 * density;
        int thumbScreenX = locationOnScreen[0] + ((int) thumbCenterX());
        /* overlay bottom edge (96d) = the thumb's top edge: the halo center
         * sits at the overlay bottom; the pin's tip touches the same point */
        int y = (locationOnScreen[1] + ((int) trackCenterY())) - (96 * density);
        int x = thumbScreenX - (overlayWidth / 2);
        int max = getResources().getDisplayMetrics().widthPixels;
        if (x + overlayWidth > max) {
            x = max - overlayWidth;
        }
        if (x < 0) {
            x = 0;
        }
        mOverlayPopup.update(x, y, (-1), (-1));
        mOverlayView.invalidate();
    }

    private void hideOverlay() {
        PopupWindow popupWindow = this.mOverlayPopup;
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.update(0, -9999, (-1), (-1));
        }
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        hideOverlay();
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
        canvas.drawCircle(thumbX, centerY, thumbRadius, mThumbPaint);
    }

    /* Draws, relative to its own canvas: the press halo (a full circle, not
     * clipped) behind the thumb, and the pin-drop value bubble pinned so its
     * tail tip touches the thumb's top. Overlay canvas = 56d x 34d; the seek
     * row itself is anchored at the overlay's bottom edge. */
    private final class OverlayView extends View {
        private int mDensity;
        private String mLabel = "";

        OverlayView(Context context, int density) {
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
            int cx = 28 * density;
            int tipY = 96 * density;
            /* location-pin silhouette in one path: tip at the bottom center,
             * sides sweep up and out into the round head */
            DiscreteSeekBar.this.mBubblePath.reset();
            DiscreteSeekBar.this.mBubblePath.moveTo(cx, tipY);
            DiscreteSeekBar.this.mBubblePath.cubicTo(cx - (2 * density), tipY - (7 * density), cx - (13 * density), tipY - (19 * density), cx - (13 * density), tipY - (32 * density));
            DiscreteSeekBar.this.mBubblePath.cubicTo(cx - (13 * density), tipY - (38 * density), cx - (7 * density), tipY - (44 * density), cx, tipY - (44 * density));
            DiscreteSeekBar.this.mBubblePath.cubicTo(cx + (7 * density), tipY - (44 * density), cx + (13 * density), tipY - (38 * density), cx + (13 * density), tipY - (32 * density));
            DiscreteSeekBar.this.mBubblePath.cubicTo(cx + (13 * density), tipY - (19 * density), cx + (2 * density), tipY - (7 * density), cx, tipY);
            DiscreteSeekBar.this.mBubblePath.close();
            DiscreteSeekBar.this.mBubblePaint.setColor(DiscreteSeekBar.this.mThumbColorPressed);
            canvas.drawPath(DiscreteSeekBar.this.mBubblePath, DiscreteSeekBar.this.mBubblePaint);
            /* hollow knob: white circle in the head + the value inside it */
            int knobY = tipY - (31 * density);
            DiscreteSeekBar.this.mKnobPaint.setColor(0xffffffff);
            canvas.drawCircle(cx, knobY, 9 * density, DiscreteSeekBar.this.mKnobPaint);
            DiscreteSeekBar.this.mBubbleTextPaint.setTextSize(12 * density);
            DiscreteSeekBar.this.mBubbleTextPaint.getFontMetrics(DiscreteSeekBar.this.mBubbleFontMetrics);
            canvas.drawText(this.mLabel, cx, knobY - ((DiscreteSeekBar.this.mBubbleFontMetrics.ascent + DiscreteSeekBar.this.mBubbleFontMetrics.descent) / 2), DiscreteSeekBar.this.mBubbleTextPaint);
        }
    }
}
