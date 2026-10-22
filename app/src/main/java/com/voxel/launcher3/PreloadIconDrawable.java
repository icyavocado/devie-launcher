package com.voxel.launcher3;

import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class PreloadIconDrawable extends Drawable {
    private static final Rect sTempRect = new Rect();
    private ObjectAnimator mAnimator;
    private Drawable mBgDrawable;
    public final Drawable mIcon;
    private boolean mIndicatorRectDirty;
    private int mRingOutset;
    private final RectF mIndicatorRect = new RectF();
    private int mIndicatorColor = 0;
    private int mProgress = 0;
    private float mAnimationProgress = -1.0f;
    private final Paint mPaint = new Paint(1);

    public PreloadIconDrawable(Drawable drawable, Resources.Theme theme) {
        this.mIcon = drawable;
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setStrokeCap(Paint.Cap.ROUND);
        setBounds(drawable.getBounds());
        applyTheme(theme);
        onLevelChange(0);
    }

    private int getIndicatorColor() {
        int i = this.mIndicatorColor;
        if (i != 0) {
            return i;
        }
        Drawable drawable = this.mIcon;
        if (!(drawable instanceof FastBitmapDrawable)) {
            this.mIndicatorColor = -16738680;
            return this.mIndicatorColor;
        }
        this.mIndicatorColor = Utilities.findDominantColorByHue(((FastBitmapDrawable) drawable).getBitmap(), 20);
        float[] fArr = new float[3];
        Color.colorToHSV(this.mIndicatorColor, fArr);
        if (fArr[1] < 0.2f) {
            this.mIndicatorColor = -16738680;
            return this.mIndicatorColor;
        }
        fArr[2] = Math.max(0.6f, fArr[2]);
        this.mIndicatorColor = Color.HSVToColor(fArr);
        return this.mIndicatorColor;
    }

    private void initIndicatorRect() {
        Drawable drawable = this.mBgDrawable;
        Rect bounds = drawable.getBounds();
        drawable.getPadding(sTempRect);
        float fWidth = bounds.width() / drawable.getIntrinsicWidth();
        float fHeight = bounds.height() / drawable.getIntrinsicHeight();
        this.mIndicatorRect.set(bounds.left + (sTempRect.left * fWidth), bounds.top + (sTempRect.top * fHeight), bounds.right - (sTempRect.right * fWidth), bounds.bottom - (sTempRect.bottom * fHeight));
        float strokeWidth = this.mPaint.getStrokeWidth() / 2.0f;
        this.mIndicatorRect.inset(strokeWidth, strokeWidth);
        this.mIndicatorRectDirty = false;
    }

    @Override // android.graphics.drawable.Drawable
    public void applyTheme(Resources.Theme theme) {
        TypedArray typedArrayObtainStyledAttributes = theme.obtainStyledAttributes(R.styleable.PreloadIconDrawable);
        this.mBgDrawable = typedArrayObtainStyledAttributes.getDrawable(0);
        this.mBgDrawable.setFilterBitmap(true);
        this.mPaint.setStrokeWidth(typedArrayObtainStyledAttributes.getDimension(1, 0.0f));
        this.mRingOutset = typedArrayObtainStyledAttributes.getDimensionPixelSize(2, 0);
        typedArrayObtainStyledAttributes.recycle();
        onBoundsChange(getBounds());
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        Rect rect = new Rect(getBounds());
        if (!canvas.getClipBounds(sTempRect) || Rect.intersects(sTempRect, rect)) {
            if (this.mIndicatorRectDirty) {
                initIndicatorRect();
            }
            float f = this.mAnimationProgress;
            float f2 = 0.5f;
            if (f >= 0.0f && f < 1.0f) {
                this.mPaint.setAlpha((int) ((1.0f - f) * 255.0f));
                this.mBgDrawable.setAlpha(this.mPaint.getAlpha());
                this.mBgDrawable.draw(canvas);
                canvas.drawOval(this.mIndicatorRect, this.mPaint);
                f2 = 0.5f + (this.mAnimationProgress * 0.5f);
            } else if (this.mAnimationProgress == -1.0f) {
                this.mPaint.setAlpha(255);
                this.mBgDrawable.setAlpha(255);
                this.mBgDrawable.draw(canvas);
                int i = this.mProgress;
                if (i >= 100) {
                    canvas.drawOval(this.mIndicatorRect, this.mPaint);
                } else if (i > 0) {
                    canvas.drawArc(this.mIndicatorRect, -90.0f, i * 3.6f, false, this.mPaint);
                }
            } else {
                f2 = 1.0f;
            }
            canvas.save();
            canvas.scale(f2, f2, rect.exactCenterX(), rect.exactCenterY());
            this.mIcon.draw(canvas);
            canvas.restore();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return this.mIcon.getIntrinsicHeight();
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.mIcon.getIntrinsicWidth();
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    public int getOutset() {
        return this.mRingOutset;
    }

    public boolean hasNotCompleted() {
        return this.mAnimationProgress < 1.0f;
    }

    public void maybePerformFinishedAnimation() {
        if (this.mAnimationProgress > -1.0f) {
            return;
        }
        ObjectAnimator objectAnimator = this.mAnimator;
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }
        setAnimationProgress(0.0f);
        this.mAnimator = ObjectAnimator.ofFloat(this, "animationProgress", 0.0f, 1.0f);
        this.mAnimator.start();
    }

    @Override // android.graphics.drawable.Drawable
    protected void onBoundsChange(Rect rect) {
        this.mIcon.setBounds(rect);
        if (this.mBgDrawable != null) {
            sTempRect.set(rect);
            Rect rect2 = sTempRect;
            int i = this.mRingOutset;
            rect2.inset(-i, -i);
            this.mBgDrawable.setBounds(sTempRect);
        }
        this.mIndicatorRectDirty = true;
    }

    @Override // android.graphics.drawable.Drawable
    protected boolean onLevelChange(int i) {
        this.mProgress = i;
        ObjectAnimator objectAnimator = this.mAnimator;
        if (objectAnimator != null) {
            objectAnimator.cancel();
            this.mAnimator = null;
        }
        this.mAnimationProgress = -1.0f;
        if (i > 0) {
            this.mPaint.setColor(getIndicatorColor());
        }
        Drawable drawable = this.mIcon;
        if (drawable instanceof FastBitmapDrawable) {
            ((FastBitmapDrawable) drawable).setGhostModeEnabled(i <= 0);
        }
        invalidateSelf();
        return true;
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        this.mIcon.setAlpha(i);
    }

    public void setAnimationProgress(float f) {
        if (f != this.mAnimationProgress) {
            this.mAnimationProgress = f;
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.mIcon.setColorFilter(colorFilter);
    }
}
