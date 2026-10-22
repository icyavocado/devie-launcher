package com.voxel.simplesearchlauncher.view;

import com.ovacoda.devie.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/* JADX INFO: loaded from: classes2.dex */
public class RoundedFrameLayout extends FrameLayout {
    boolean mClipOutlines;
    boolean mNCompatCheck;
    float[] mRadius;
    Path mRevealPath;
    RectF pathRect;

    public RoundedFrameLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mRevealPath = new Path();
        this.pathRect = new RectF();
        this.mNCompatCheck = false;
        this.mClipOutlines = true;
        this.mRadius = new float[8];
        initializeFromAttributes(context, attributeSet);
    }

    private void initializeFromAttributes(Context context, AttributeSet attributeSet) {
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.RoundedFrameLayout);
        int dimensionPixelSize = typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.RoundedFrameLayout_vxl_radius, 0);
        int i = 0;
        while (true) {
            float[] fArr = this.mRadius;
            if (i >= fArr.length) {
                float dimensionPixelSize2 = typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.RoundedFrameLayout_vxl_radiusTopLeft, (int) this.mRadius[0]);
                fArr[1] = dimensionPixelSize2;
                fArr[0] = dimensionPixelSize2;
                float[] fArr2 = this.mRadius;
                float dimensionPixelSize3 = typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.RoundedFrameLayout_vxl_radiusTopRight, (int) this.mRadius[2]);
                fArr2[3] = dimensionPixelSize3;
                fArr2[2] = dimensionPixelSize3;
                float[] fArr3 = this.mRadius;
                float dimensionPixelSize4 = typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.RoundedFrameLayout_vxl_radiusBottomRight, (int) this.mRadius[4]);
                fArr3[5] = dimensionPixelSize4;
                fArr3[4] = dimensionPixelSize4;
                float[] fArr4 = this.mRadius;
                float dimensionPixelSize5 = typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.RoundedFrameLayout_vxl_radiusBottomLeft, (int) this.mRadius[6]);
                fArr4[7] = dimensionPixelSize5;
                fArr4[6] = dimensionPixelSize5;
                this.mNCompatCheck = typedArrayObtainStyledAttributes.getBoolean(R.styleable.RoundedFrameLayout_vxl_nCompatCheck, false);
                typedArrayObtainStyledAttributes.recycle();
                return;
            }
            fArr[i] = dimensionPixelSize;
            i++;
        }
    }

    @Override // android.view.ViewGroup
    protected boolean drawChild(Canvas canvas, View view, long j) {
        if (!this.mClipOutlines || (this.mNCompatCheck && Build.VERSION.SDK_INT >= 24)) {
            return super.drawChild(canvas, view, j);
        }
        int iSave = canvas.save();
        RectF rectF = this.pathRect;
        rectF.left = 0.0f;
        rectF.right = canvas.getWidth();
        RectF rectF2 = this.pathRect;
        rectF2.top = 0.0f;
        rectF2.bottom = canvas.getHeight();
        this.mRevealPath.reset();
        this.mRevealPath.addRoundRect(this.pathRect, this.mRadius, Path.Direction.CW);
        canvas.clipPath(this.mRevealPath);
        boolean zDrawChild = super.drawChild(canvas, view, j);
        canvas.restoreToCount(iSave);
        return zDrawChild;
    }

    public float[] getCornerRadius() {
        return this.mRadius;
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // android.view.View
    public boolean isOpaque() {
        return true;
    }

    public void setBottomLeftCornerRadius(float f) {
        float[] fArr = this.mRadius;
        fArr[7] = f;
        fArr[6] = f;
        invalidate();
    }

    public void setBottomRightCornerRadius(float f) {
        float[] fArr = this.mRadius;
        fArr[5] = f;
        fArr[4] = f;
        invalidate();
    }

    public void setClipOutlines(boolean z) {
        this.mClipOutlines = z;
    }

    public void setTopLeftCornerRadius(float f) {
        float[] fArr = this.mRadius;
        fArr[1] = f;
        fArr[0] = f;
        invalidate();
    }

    public void setTopRightCornerRadius(float f) {
        float[] fArr = this.mRadius;
        fArr[3] = f;
        fArr[2] = f;
        invalidate();
    }
}
