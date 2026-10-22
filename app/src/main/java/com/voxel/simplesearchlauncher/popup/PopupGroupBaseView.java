package com.voxel.simplesearchlauncher.popup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import com.voxel.launcher3.LogAccelerateInterpolator;
import com.voxel.launcher3.Utilities;
import com.voxel.launcher3.util.PillRevealOutlineProvider;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public abstract class PopupGroupBaseView extends FrameLayout implements ValueAnimator.AnimatorUpdateListener {
    protected static final Point sTempPoint = new Point();
    private final Paint mBackgroundClipPaint;
    protected final boolean mIsRtl;
    private final Matrix mMatrix;
    protected float mOpenAnimationProgress;
    protected final Rect mPillRect;
    private Bitmap mRoundedCornerBitmap;

    protected static class CloseInterpolator extends LogAccelerateInterpolator {
        private float mRemainingProgress;
        private float mStartProgress;

        public CloseInterpolator(float f) {
            super(100, 0);
            this.mStartProgress = 1.0f - f;
            this.mRemainingProgress = f;
        }

        @Override // com.voxel.launcher3.LogAccelerateInterpolator, android.animation.TimeInterpolator
        public float getInterpolation(float f) {
            return this.mStartProgress + (super.getInterpolation(f) * this.mRemainingProgress);
        }
    }

    protected static class ZoomRevealOutlineProvider extends PillRevealOutlineProvider {
        private final float mArrowCenter;
        private final float mFullHeight;
        private final boolean mPivotLeft;
        private final View mTranslateView;
        private final float mTranslateX;
        private final float mTranslateYMultiplier;
        private final View mZoomView;

        public ZoomRevealOutlineProvider(int i, int i2, Rect rect, PopupGroupBaseView popupGroupBaseView, View view, boolean z, boolean z2, float f) {
            super(i, i2, rect, popupGroupBaseView.getBackgroundRadius());
            this.mTranslateView = popupGroupBaseView;
            this.mZoomView = view;
            this.mFullHeight = rect.height();
            this.mTranslateYMultiplier = z ? 0.5f : -0.5f;
            this.mPivotLeft = z2;
            this.mTranslateX = z2 ? f : rect.right - f;
            this.mArrowCenter = f;
        }

        @Override // com.voxel.launcher3.util.PillRevealOutlineProvider, com.voxel.launcher3.util.RevealOutlineAnimation
        public void setProgress(float f) {
            super.setProgress(f);
            View view = this.mZoomView;
            if (view != null) {
                view.setScaleX(f);
                this.mZoomView.setScaleY(f);
            }
            this.mTranslateView.setTranslationY(this.mTranslateYMultiplier * (this.mFullHeight - this.mOutline.height()));
            float fMin = Math.min(this.mOutline.width(), this.mArrowCenter);
            this.mTranslateView.setTranslationX(this.mTranslateX - (this.mPivotLeft ? this.mOutline.left + fMin : this.mOutline.right - fMin));
        }
    }

    public PopupGroupBaseView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public PopupGroupBaseView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mBackgroundClipPaint = new Paint(5);
        this.mMatrix = new Matrix();
        this.mPillRect = new Rect();
        int backgroundRadius = (int) getBackgroundRadius();
        this.mRoundedCornerBitmap = Bitmap.createBitmap(backgroundRadius, backgroundRadius, Bitmap.Config.ALPHA_8);
        Canvas canvas = new Canvas();
        canvas.setBitmap(this.mRoundedCornerBitmap);
        float f = backgroundRadius * 2;
        canvas.drawArc(new RectF(0.0f, 0.0f, f, f), 180.0f, 90.0f, true, this.mBackgroundClipPaint);
        this.mBackgroundClipPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        this.mIsRtl = Utilities.isRtl(getResources());
    }

    public abstract Animator createCloseAnimation(boolean z, boolean z2, long j);

    public abstract Animator createOpenAnimation(boolean z, boolean z2);

    protected Animator createZoomCloseAnimation(View view, boolean z, boolean z2, long j) {
        ValueAnimator valueAnimatorCreateRevealAnimator;
        Point iconCenter = getIconCenter();
        int dimensionPixelSize = getResources().getDimensionPixelSize(this.mIsRtl ^ z2 ? R.dimen.popup_arrow_horizontal_center_start : R.dimen.popup_arrow_horizontal_center_end);
        if (Utilities.ATLEAST_LOLLIPOP) {
            valueAnimatorCreateRevealAnimator = new ZoomRevealOutlineProvider(iconCenter.x, iconCenter.y, this.mPillRect, this, view, z, z2, dimensionPixelSize).createRevealAnimator(this, true);
        } else {
            ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
            valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.simplesearchlauncher.popup.-$$Lambda$PopupGroupBaseView$bD13EgvPpKf43xu1KlCHszhiBIE
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    PopupGroupBaseView.this.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
                }
            });
            valueAnimatorCreateRevealAnimator = valueAnimatorOfFloat;
        }
        valueAnimatorCreateRevealAnimator.setDuration((long) (j * this.mOpenAnimationProgress));
        valueAnimatorCreateRevealAnimator.setInterpolator(new CloseInterpolator(this.mOpenAnimationProgress));
        valueAnimatorCreateRevealAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.simplesearchlauncher.popup.PopupGroupBaseView.1
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                PopupGroupBaseView.this.mOpenAnimationProgress = 0.0f;
            }
        });
        return valueAnimatorCreateRevealAnimator;
    }

    protected Animator createZoomOpenAnimation(View view, boolean z, boolean z2) {
        ValueAnimator valueAnimatorOfFloat;
        Point iconCenter = getIconCenter();
        int dimensionPixelSize = getResources().getDimensionPixelSize(this.mIsRtl ^ z2 ? R.dimen.popup_arrow_horizontal_center_start : R.dimen.popup_arrow_horizontal_center_end);
        if (Utilities.ATLEAST_LOLLIPOP) {
            valueAnimatorOfFloat = new ZoomRevealOutlineProvider(iconCenter.x, iconCenter.y, this.mPillRect, this, view, z, z2, dimensionPixelSize).createRevealAnimator(this, false);
        } else {
            valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.simplesearchlauncher.popup.-$$Lambda$PopupGroupBaseView$qjG6Hrdu8YNl5u0mMLkAKn03rNM
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    PopupGroupBaseView.this.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
                }
            });
        }
        this.mOpenAnimationProgress = 0.0f;
        valueAnimatorOfFloat.addUpdateListener(this);
        return valueAnimatorOfFloat;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        RectF rectF = new RectF(0.0f, 0.0f, getWidth(), getHeight());
        int iSaveLayer = Utilities.isAtLeastO() ? canvas.saveLayer(rectF, null) : canvas.saveLayer(rectF, null, 31);
        super.dispatchDraw(canvas);
        int width = this.mRoundedCornerBitmap.getWidth();
        int height = this.mRoundedCornerBitmap.getHeight();
        this.mMatrix.reset();
        canvas.drawBitmap(this.mRoundedCornerBitmap, this.mMatrix, this.mBackgroundClipPaint);
        float f = width / 2.0f;
        float f2 = height / 2.0f;
        this.mMatrix.setRotate(90.0f, f, f2);
        this.mMatrix.postTranslate(canvas.getWidth() - width, 0.0f);
        canvas.drawBitmap(this.mRoundedCornerBitmap, this.mMatrix, this.mBackgroundClipPaint);
        this.mMatrix.setRotate(180.0f, f, f2);
        this.mMatrix.postTranslate(canvas.getWidth() - width, canvas.getHeight() - height);
        canvas.drawBitmap(this.mRoundedCornerBitmap, this.mMatrix, this.mBackgroundClipPaint);
        this.mMatrix.setRotate(270.0f, f, f2);
        this.mMatrix.postTranslate(0.0f, canvas.getHeight() - height);
        canvas.drawBitmap(this.mRoundedCornerBitmap, this.mMatrix, this.mBackgroundClipPaint);
        canvas.restoreToCount(iSaveLayer);
    }

    public abstract int getArrowColor(boolean z);

    protected float getBackgroundRadius() {
        return getResources().getDimensionPixelSize(R.dimen.bg_round_rect_radius);
    }

    public Point getIconCenter() {
        sTempPoint.y = getMeasuredHeight() / 2;
        sTempPoint.x = getResources().getDimensionPixelSize(R.dimen.bg_popup_item_height) / 2;
        if (Utilities.isRtl(getResources())) {
            sTempPoint.x = getMeasuredWidth() - sTempPoint.x;
        }
        return sTempPoint;
    }

    public boolean isOpenOrOpening() {
        return this.mOpenAnimationProgress > 0.0f;
    }

    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        this.mOpenAnimationProgress = valueAnimator.getAnimatedFraction();
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        this.mPillRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }
}
