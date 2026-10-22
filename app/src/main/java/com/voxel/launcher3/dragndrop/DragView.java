package com.voxel.launcher3.dragndrop;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.FloatArrayEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import com.voxel.launcher3.Launcher;
import com.voxel.launcher3.LauncherAnimUtils;
import com.voxel.launcher3.Utilities;
import com.ovacoda.devie.R;
import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
public class DragView extends View {
    static float sDragAlpha = 1.0f;
    ValueAnimator mAnim;
    private int mAnimatedShiftX;
    private int mAnimatedShiftY;
    private boolean mAnimationCancelled;
    private Bitmap mBitmap;
    private Bitmap mCrossFadeBitmap;
    float mCrossFadeProgress;
    float[] mCurrentFilter;
    final DragController mDragController;
    private final DragLayer mDragLayer;
    private Rect mDragRegion;
    private Point mDragVisualizeOffset;
    private ValueAnimator mFilterAnimator;
    private boolean mHasDrawn;
    private float mIntrinsicIconScale;
    private int mLastTouchX;
    private int mLastTouchY;
    Paint mPaint;
    private int mRegistrationX;
    private int mRegistrationY;

    @TargetApi(21)
    public DragView(Launcher launcher, Bitmap bitmap, int i, int i2, final float f, float f2) {
        super(launcher);
        this.mDragVisualizeOffset = null;
        this.mDragRegion = null;
        this.mHasDrawn = false;
        this.mCrossFadeProgress = 0.0f;
        this.mAnimationCancelled = false;
        this.mIntrinsicIconScale = 1.0f;
        this.mDragLayer = launcher.getDragLayer();
        this.mDragController = launcher.getDragController();
        final float width = (bitmap.getWidth() + f2) / bitmap.getWidth();
        setScaleX(f);
        setScaleY(f);
        this.mAnim = LauncherAnimUtils.ofFloat(this, 0.0f, 1.0f);
        this.mAnim.setDuration(150L);
        this.mAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.launcher3.dragndrop.-$$Lambda$DragView$E18pLBqCeTBGWvVhbQMrssMMWYk
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                DragView.this.lambda$new$0( f, width, valueAnimator);
            }
        });
        this.mAnim.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.launcher3.dragndrop.DragView.1
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (DragView.this.mAnimationCancelled) {
                    return;
                }
                DragView.this.mDragController.onDragViewAnimationEnd();
            }
        });
        this.mBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
        setDragRegion(new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));
        this.mRegistrationX = i;
        this.mRegistrationY = i2;
        int iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
        measure(iMakeMeasureSpec, iMakeMeasureSpec);
        this.mPaint = new Paint(2);
        if (Utilities.ATLEAST_LOLLIPOP) {
            setElevation(getResources().getDimension(R.dimen.drag_elevation));
        }
    }

    @TargetApi(21)
    private void animateFilterTo(float[] fArr) {
        float[] array = this.mCurrentFilter;
        if (array == null) {
            array = new ColorMatrix().getArray();
        }
        this.mCurrentFilter = Arrays.copyOf(array, array.length);
        ValueAnimator valueAnimator = this.mFilterAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        this.mFilterAnimator = ValueAnimator.ofObject(new FloatArrayEvaluator(this.mCurrentFilter), array, fArr);
        this.mFilterAnimator.setDuration(120L);
        this.mFilterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.launcher3.dragndrop.-$$Lambda$DragView$aLc24ceAXQ_6gdk3n0pjYhqI0QE
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator2) {
                DragView.this.lambda$animateFilterTo$2( valueAnimator2);
            }
        });
        this.mFilterAnimator.start();
    }

    private void applyTranslation() {
        setTranslationX((this.mLastTouchX - this.mRegistrationX) + this.mAnimatedShiftX);
        setTranslationY((this.mLastTouchY - this.mRegistrationY) + this.mAnimatedShiftY);
    }

    public void lambda$animateFilterTo$2( ValueAnimator valueAnimator) {
        this.mPaint.setColorFilter(new ColorMatrixColorFilter(this.mCurrentFilter));
        this.invalidate();
    }

    public void lambda$animateShiftFrom$4( int i, int i2, ValueAnimator valueAnimator) {
        float animatedFraction = 1.0f - valueAnimator.getAnimatedFraction();
        this.mAnimatedShiftX = (int) (i * animatedFraction);
        this.mAnimatedShiftY = (int) (animatedFraction * i2);
        this.applyTranslation();
    }

    public void lambda$animateShiftTo$5( int i, int i2, int i3, int i4, ValueAnimator valueAnimator) {
        float animatedFraction = valueAnimator.getAnimatedFraction();
        this.mRegistrationX = i + ((int) (i2 * animatedFraction));
        this.mRegistrationY = i3 + ((int) (animatedFraction * i4));
        this.applyTranslation();
    }

    public void lambda$crossFade$1( ValueAnimator valueAnimator) {
        this.mCrossFadeProgress = valueAnimator.getAnimatedFraction();
        this.invalidate();
    }

    public void lambda$new$0( float f, float f2, ValueAnimator valueAnimator) {
        float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        float f3 = f + ((f2 - f) * fFloatValue);
        this.setScaleX(f3);
        this.setScaleY(f3);
        float f4 = sDragAlpha;
        if (f4 != 1.0f) {
            this.setAlpha((f4 * fFloatValue) + (1.0f - fFloatValue));
        }
        if (this.getParent() == null) {
            valueAnimator.cancel();
        }
    }

    public static void setColorScale(int i, ColorMatrix colorMatrix) {
        colorMatrix.setScale(Color.red(i) / 255.0f, Color.green(i) / 255.0f, Color.blue(i) / 255.0f, Color.alpha(i) / 255.0f);
    }

    public void animateShiftFrom(final int i, final int i2) {
        if (this.mAnim.isStarted()) {
            return;
        }
        this.mAnimatedShiftX = i;
        this.mAnimatedShiftY = i2;
        applyTranslation();
        this.mAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.launcher3.dragndrop.-$$Lambda$DragView$LPVWmpD1bLqzFII8HPCP6DkEiDk
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                DragView.this.lambda$animateShiftFrom$4( i, i2, valueAnimator);
            }
        });
    }

    public void animateShiftTo(final int i, final int i2) {
        ValueAnimator valueAnimatorOfFloat = LauncherAnimUtils.ofFloat(this, 0.0f, 1.0f);
        valueAnimatorOfFloat.setDuration(150L);
        valueAnimatorOfFloat.setInterpolator(this.mAnim.getInterpolator());
        final int i3 = this.mRegistrationX;
        final int i4 = this.mRegistrationY;
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.launcher3.dragndrop.-$$Lambda$DragView$2Ljd3Q6NK7ItiuN1Cl7WzT1ZwG4
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                DragView.this.lambda$animateShiftTo$5( i3, i, i4, i2, valueAnimator);
            }
        });
        valueAnimatorOfFloat.start();
    }

    public void cancelAnimation() {
        this.mAnimationCancelled = true;
        ValueAnimator valueAnimator = this.mAnim;
        if (valueAnimator == null || !valueAnimator.isRunning()) {
            return;
        }
        this.mAnim.cancel();
    }

    public void crossFade(int i) {
        ValueAnimator valueAnimatorOfFloat = LauncherAnimUtils.ofFloat(this, 0.0f, 1.0f);
        valueAnimatorOfFloat.setDuration(i);
        valueAnimatorOfFloat.setInterpolator(new DecelerateInterpolator(1.5f));
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.launcher3.dragndrop.-$$Lambda$DragView$-mPKBBfq6JuawfgIvMPIdpaZ1g0
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                DragView.this.lambda$crossFade$1( valueAnimator);
            }
        });
        valueAnimatorOfFloat.start();
    }

    public Rect getDragRegion() {
        return this.mDragRegion;
    }

    public int getDragRegionHeight() {
        return this.mDragRegion.height();
    }

    public int getDragRegionLeft() {
        return this.mDragRegion.left;
    }

    public int getDragRegionTop() {
        return this.mDragRegion.top;
    }

    public int getDragRegionWidth() {
        return this.mDragRegion.width();
    }

    public Point getDragVisualizeOffset() {
        return this.mDragVisualizeOffset;
    }

    public float getIntrinsicIconScaleFactor() {
        return this.mIntrinsicIconScale;
    }

    public boolean hasDrawn() {
        return this.mHasDrawn;
    }

    public void move(int i, int i2) {
        this.mLastTouchX = i;
        this.mLastTouchY = i2;
        applyTranslation();
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        this.mHasDrawn = true;
        boolean z = this.mCrossFadeProgress > 0.0f && this.mCrossFadeBitmap != null;
        if (z) {
            this.mPaint.setAlpha(z ? (int) ((1.0f - this.mCrossFadeProgress) * 255.0f) : 255);
        }
        canvas.drawBitmap(this.mBitmap, 0.0f, 0.0f, this.mPaint);
        if (z) {
            this.mPaint.setAlpha((int) (this.mCrossFadeProgress * 255.0f));
            int iSave = canvas.save();
            canvas.scale((this.mBitmap.getWidth() * 1.0f) / this.mCrossFadeBitmap.getWidth(), (this.mBitmap.getHeight() * 1.0f) / this.mCrossFadeBitmap.getHeight());
            canvas.drawBitmap(this.mCrossFadeBitmap, 0.0f, 0.0f, this.mPaint);
            canvas.restoreToCount(iSave);
        }
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        setMeasuredDimension(this.mBitmap.getWidth(), this.mBitmap.getHeight());
    }

    public void remove() {
        if (getParent() != null) {
            this.mDragLayer.removeView(this);
        }
    }

    @Override // android.view.View
    public void setAlpha(float f) {
        super.setAlpha(f);
        this.mPaint.setAlpha((int) (f * 255.0f));
        invalidate();
    }

    public void setColor(int i) {
        if (this.mPaint == null) {
            this.mPaint = new Paint(2);
        }
        if (i == 0) {
            if (Utilities.ATLEAST_LOLLIPOP && this.mCurrentFilter != null) {
                animateFilterTo(new ColorMatrix().getArray());
                return;
            } else {
                this.mPaint.setColorFilter(null);
                invalidate();
                return;
            }
        }
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0.0f);
        ColorMatrix colorMatrix2 = new ColorMatrix();
        setColorScale(i, colorMatrix2);
        colorMatrix.postConcat(colorMatrix2);
        if (Utilities.ATLEAST_LOLLIPOP) {
            animateFilterTo(colorMatrix.getArray());
        } else {
            this.mPaint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
            invalidate();
        }
    }

    public void setCrossFadeBitmap(Bitmap bitmap) {
        this.mCrossFadeBitmap = bitmap;
    }

    public void setDragRegion(Rect rect) {
        this.mDragRegion = rect;
    }

    public void setDragVisualizeOffset(Point point) {
        this.mDragVisualizeOffset = point;
    }

    public void setIntrinsicIconScaleFactor(float f) {
        this.mIntrinsicIconScale = f;
    }

    public void show(int i, int i2) {
        this.mDragLayer.addView(this);
        DragLayer.LayoutParams layoutParams = new DragLayer.LayoutParams(0, 0);
        layoutParams.width = this.mBitmap.getWidth();
        layoutParams.height = this.mBitmap.getHeight();
        layoutParams.customPosition = true;
        setLayoutParams(layoutParams);
        move(i, i2);
        post(new Runnable() { // from class: com.voxel.launcher3.dragndrop.-$$Lambda$DragView$FDA4B-FRwZNYjGge7xJUZrOrHWk
            @Override // java.lang.Runnable
            public final void run() {
                DragView.this.mAnim.start();
            }
        });
    }
}
