package com.voxel.simplesearchlauncher.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

/* JADX INFO: loaded from: classes2.dex */
public class CenterCropOffsetImageView extends ImageView {
    private boolean mHaveFrame;
    private Matrix mMatrix;
    private float mOffsetX;
    private float mOffsetY;

    public CenterCropOffsetImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mOffsetX = 0.0f;
        this.mOffsetY = 0.0f;
        this.mMatrix = new Matrix();
        init();
    }

    private void configureMatrix() {
        float f;
        float f2;
        Drawable drawable = getDrawable();
        if (drawable == null || !this.mHaveFrame) {
            return;
        }
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        int width = (getWidth() - getPaddingLeft()) - getPaddingRight();
        int height = (getHeight() - getPaddingTop()) - getPaddingBottom();
        float f3 = 0.0f;
        if (intrinsicWidth * height > width * intrinsicHeight) {
            float f4 = height / (float) intrinsicHeight;
            f2 = width - (intrinsicWidth * f4);
            f = f4;
        } else {
            f = width / (float) intrinsicWidth;
            f3 = height - (intrinsicHeight * f);
            f2 = 0.0f;
        }
        this.mMatrix.reset();
        this.mMatrix.setScale(f, f);
        this.mMatrix.postTranslate(f2 * this.mOffsetX, f3 * this.mOffsetY);
        super.setImageMatrix(this.mMatrix);
    }

    private void init() {
        super.setScaleType(ImageView.ScaleType.MATRIX);
    }

    @Override // android.widget.ImageView
    protected boolean setFrame(int i, int i2, int i3, int i4) {
        boolean frame = super.setFrame(i, i2, i3, i4);
        this.mHaveFrame = true;
        configureMatrix();
        return frame;
    }

    @Override // android.widget.ImageView
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        configureMatrix();
    }

    @Override // android.widget.ImageView
    public void setImageMatrix(Matrix matrix) {
    }

    @Override // android.widget.ImageView
    public void setImageResource(int i) {
        super.setImageResource(i);
        configureMatrix();
    }

    @Override // android.widget.ImageView
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        configureMatrix();
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
}
