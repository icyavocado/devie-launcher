package com.voxel.simplesearchlauncher.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.ViewParent;
import android.widget.FrameLayout;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class MaskRoundedFrameLayout extends FrameLayout {
    Bitmap mBitmap;
    BitmapShader mBitmapShader;
    Canvas mCanvas;
    Bitmap mMaskBitmap;
    Paint mPaint;
    float mRadius;

    public MaskRoundedFrameLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPaint = new Paint();
        this.mRadius = 0.0f;
        initializeFromAttributes(context, attributeSet);
        setWillNotDraw(false);
    }

    private void initializeFromAttributes(Context context, AttributeSet attributeSet) {
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.MaskRoundedFrameLayout);
        this.mRadius = typedArrayObtainStyledAttributes.getDimensionPixelSize(0, 0);
        typedArrayObtainStyledAttributes.recycle();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        Bitmap bitmap = this.mMaskBitmap;
        if (bitmap == null || bitmap.getHeight() != getHeight() || this.mMaskBitmap.getWidth() != getWidth()) {
            this.mMaskBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ALPHA_8);
            Paint paint = new Paint();
            paint.setColor(-1);
            paint.setAntiAlias(true);
            Canvas canvas2 = new Canvas(this.mMaskBitmap);
            RectF rectF = new RectF(0.0f, 0.0f, getWidth(), getHeight());
            float f = this.mRadius;
            canvas2.drawRoundRect(rectF, f, f, paint);
        }
        Bitmap bitmap2 = this.mBitmap;
        if (bitmap2 == null || bitmap2.getHeight() != getHeight() || this.mBitmap.getWidth() != getWidth()) {
            this.mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            this.mCanvas = new Canvas(this.mBitmap);
            this.mBitmapShader = new BitmapShader(this.mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            this.mPaint.setShader(this.mBitmapShader);
        }
        super.dispatchDraw(this.mCanvas);
        canvas.drawBitmap(this.mMaskBitmap, 0.0f, 0.0f, this.mPaint);
    }

    public float getCornerRadius() {
        return this.mRadius;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public ViewParent invalidateChildInParent(int[] iArr, Rect rect) {
        invalidate();
        return super.invalidateChildInParent(iArr, rect);
    }

    public void setCornerRadius(float f) {
        this.mRadius = f;
        this.mMaskBitmap = null;
    }
}
