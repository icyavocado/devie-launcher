package com.voxel.launcher3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/* JADX INFO: loaded from: classes.dex */
public class FastBitmapView extends View {
    private Bitmap mBitmap;
    private final Paint mPaint;

    public FastBitmapView(Context context) {
        super(context);
        this.mPaint = new Paint(2);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        Bitmap bitmap = this.mBitmap;
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, this.mPaint);
        }
    }

    public boolean setBitmap(Bitmap bitmap) {
        Bitmap bitmap2 = this.mBitmap;
        if (bitmap == bitmap2) {
            return false;
        }
        if (bitmap2 != null) {
            invalidate(0, 0, bitmap2.getWidth(), this.mBitmap.getHeight());
        }
        this.mBitmap = bitmap;
        Bitmap bitmap3 = this.mBitmap;
        if (bitmap3 == null) {
            return true;
        }
        invalidate(0, 0, bitmap3.getWidth(), this.mBitmap.getHeight());
        return true;
    }
}
