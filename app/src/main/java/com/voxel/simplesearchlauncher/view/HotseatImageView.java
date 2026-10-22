package com.voxel.simplesearchlauncher.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import com.voxel.simplesearchlauncher.utils.AlphaUtil;

/* JADX INFO: loaded from: classes2.dex */
public class HotseatImageView extends ImageView {
    private Drawable mBlurredDrawable;
    private int mDefaultAlpha;
    private boolean mEnabledBlurredBackground;
    private boolean mHaveFrame;
    private Matrix mMatrix;
    private float mOffsetX;
    private float mOffsetY;
    private Drawable mWhiteDrawable;

    public HotseatImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mOffsetX = 0.0f;
        this.mOffsetY = 0.0f;
        this.mMatrix = new Matrix();
        this.mEnabledBlurredBackground = false;
        init();
    }

    private void configureMatrix() {
        if (this.mEnabledBlurredBackground && this.mHaveFrame) {
            int intrinsicWidth = this.mBlurredDrawable.getIntrinsicWidth();
            int intrinsicHeight = this.mBlurredDrawable.getIntrinsicHeight();
            int width = (getWidth() - getPaddingLeft()) - getPaddingRight();
            int width2 = ((View) getParent()).getWidth();
            int height = ((View) getParent()).getHeight();
            float f = intrinsicWidth * height > width2 * intrinsicHeight ? height / intrinsicHeight : width2 / intrinsicWidth;
            this.mMatrix.reset();
            this.mMatrix.setScale(f, f);
            this.mMatrix.postTranslate((width - (intrinsicWidth * f)) * this.mOffsetX, (-getTop()) - ((int) getTranslationY()));
            super.setImageMatrix(this.mMatrix);
        }
    }

    private void init() {
        super.setScaleType(ImageView.ScaleType.MATRIX);
    }

    public void enableBlurredBackground(boolean z) {
        this.mEnabledBlurredBackground = z;
    }

    public Drawable getWhiteDrawable() {
        return this.mWhiteDrawable;
    }

    @Override // android.widget.ImageView, android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.mEnabledBlurredBackground) {
            canvas.save();
            canvas.concat(this.mMatrix);
            this.mBlurredDrawable.draw(canvas);
            canvas.restore();
        }
        Drawable drawable = this.mWhiteDrawable;
        if (drawable != null) {
            drawable.draw(canvas);
        }
    }

    public void setDefaultAlpha(int i) {
        this.mDefaultAlpha = i;
    }

    @Override // android.widget.ImageView
    protected boolean setFrame(int i, int i2, int i3, int i4) {
        boolean frame = super.setFrame(i, i2, i3, i4);
        this.mHaveFrame = true;
        configureMatrix();
        return frame;
    }

    @Override // android.widget.ImageView
    public void setImageAlpha(int i) {
        super.setImageAlpha(i);
        int iIntToFloatAlpha = (int) (AlphaUtil.intToFloatAlpha(i) * this.mDefaultAlpha);
        if (this.mEnabledBlurredBackground) {
            this.mBlurredDrawable.setAlpha(iIntToFloatAlpha);
        }
        Drawable drawable = this.mWhiteDrawable;
        if (drawable != null) {
            drawable.setAlpha(iIntToFloatAlpha);
        }
    }

    public void setImageDrawables(Drawable drawable, Drawable drawable2) {
        if (drawable2 != null) {
            this.mBlurredDrawable = drawable2;
        } else if (this.mBlurredDrawable == null) {
            this.mEnabledBlurredBackground = false;
        }
        this.mWhiteDrawable = drawable;
        setImageAlpha(255);
        this.mWhiteDrawable.setBounds(new Rect(0, 0, (getWidth() - getPaddingLeft()) - getPaddingRight(), (getHeight() - getPaddingTop()) - getPaddingBottom()));
        if (this.mEnabledBlurredBackground) {
            Drawable drawable3 = this.mBlurredDrawable;
            drawable3.setBounds(new Rect(0, 0, drawable3.getIntrinsicWidth(), this.mBlurredDrawable.getIntrinsicHeight()));
            configureMatrix();
        }
        invalidate();
    }

    @Override // android.widget.ImageView
    public void setImageMatrix(Matrix matrix) {
    }

    public void setOffsets(float f, float f2) {
        float fMax = Math.max(0.0f, Math.min(f, 1.0f));
        float fMax2 = Math.max(0.0f, Math.min(f2, 1.0f));
        if (fMax == this.mOffsetX && fMax2 == this.mOffsetY) {
            return;
        }
        this.mOffsetX = fMax;
        this.mOffsetY = fMax2;
        configureMatrix();
    }

    @Override // android.widget.ImageView
    public void setScaleType(ImageView.ScaleType scaleType) {
    }

    @Override // android.view.View
    public void setTranslationY(float f) {
        super.setTranslationY(f);
        configureMatrix();
    }
}
