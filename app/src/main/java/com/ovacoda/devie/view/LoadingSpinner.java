package com.ovacoda.devie.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import com.ovacoda.devie.R;

/* REWORK(vendor): replaces the decompiled com.github.rahatarmanahmed.cpv
   CircularProgressView library (indeterminate-only). Same call surface the app
   relies on (startAnimation / stopAnimation + the two XML attributes its two
   layouts set). Package kept under the rework's own com.ovacoda.devie id. */
public class LoadingSpinner extends View {
    private static final float ARC_SWEEP = 90.0f;
    private static final long ROTATION_MS = 1000L;
    private final Paint mPaint = new Paint(1);
    private final RectF mArcRect = new RectF();
    private boolean mSpinning;
    private float mRotation;
    private int mColor;

    public LoadingSpinner(Context context) {
        super(context);
        init(context, null);
    }

    public LoadingSpinner(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context, attributeSet);
    }

    public LoadingSpinner(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context, attributeSet);
    }

    private void init(Context context, AttributeSet attributeSet) {
        if (attributeSet != null) {
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.LoadingSpinner);
            this.mColor = obtainStyledAttributes.getColor(R.styleable.LoadingSpinner_spinner_color, 0xFF0BD7BA);
            boolean z = obtainStyledAttributes.getBoolean(R.styleable.LoadingSpinner_spinner_autoStart, false);
            obtainStyledAttributes.recycle();
            this.mSpinning = z;
            if (z) {
                post(new Runnable() {
                    @Override
                    public final void run() {
                        startAnimation();
                    }
                });
            }
        } else {
            this.mColor = 0xFF0BD7BA;
        }
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mPaint.setColor(this.mColor);
        setAlpha(this.mSpinning ? 1.0f : 0.0f);
    }

    public void startAnimation() {
        if (this.mSpinning) {
            return;
        }
        this.mSpinning = true;
        animate().alpha(1.0f).setDuration(200L).start();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 360.0f);
        ofFloat.setDuration(ROTATION_MS);
        ofFloat.setRepeatCount(-1);
        ofFloat.setInterpolator(new LinearInterpolator());
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mRotation = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                invalidate();
            }
        });
        ofFloat.start();
        setTag(R.id.devie_loading_spinner_animator, ofFloat);
    }

    public void stopAnimation() {
        Object tag = getTag(R.id.devie_loading_spinner_animator);
        if (tag instanceof ValueAnimator) {
            ((ValueAnimator) tag).cancel();
            setTag(R.id.devie_loading_spinner_animator, null);
        }
        this.mSpinning = false;
        animate().alpha(0.0f).setDuration(200L).start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!this.mSpinning) {
            return;
        }
        float width = (float) getWidth();
        float height = (float) getHeight();
        float min = Math.min(width, height) / 2.0f;
        float strokeWidth = min / 5.0f;
        this.mPaint.setStrokeWidth(strokeWidth);
        this.mArcRect.set((width / 2.0f) - min + strokeWidth, (height / 2.0f) - min + strokeWidth, (width / 2.0f) + min - strokeWidth, (height / 2.0f) + min - strokeWidth);
        canvas.save();
        canvas.rotate(this.mRotation, width / 2.0f, height / 2.0f);
        canvas.drawArc(this.mArcRect, 0.0f, ARC_SWEEP, false, this.mPaint);
        canvas.restore();
    }

    @Override
    protected void onDetachedFromWindow() {
        Object tag = getTag(R.id.devie_loading_spinner_animator);
        if (tag instanceof ValueAnimator) {
            ((ValueAnimator) tag).cancel();
            setTag(R.id.devie_loading_spinner_animator, null);
        }
        this.mSpinning = false;
        super.onDetachedFromWindow();
    }
}