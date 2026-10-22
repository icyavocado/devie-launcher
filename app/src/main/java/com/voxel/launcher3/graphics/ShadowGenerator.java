package com.voxel.launcher3.graphics;

import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import com.voxel.launcher3.LauncherAppState;

/* JADX INFO: loaded from: classes.dex */
public class ShadowGenerator {
    private static final Object LOCK = new Object();
    private static ShadowGenerator sShadowGenerator;
    private final Paint mDrawPaint;
    private final int mIconSize = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().unscaledIconSizePx;
    private final Canvas mCanvas = new Canvas();
    private final Paint mBlurPaint = new Paint(3);

    private ShadowGenerator() {
        this.mBlurPaint.setMaskFilter(new BlurMaskFilter(this.mIconSize * 0.010416667f, BlurMaskFilter.Blur.NORMAL));
        this.mDrawPaint = new Paint(3);
    }

    public static Bitmap createPillWithShadow(int i, int i2, int i3) {
        float f = i3 * 1.0f;
        float f2 = f / 32.0f;
        float f3 = f / 16.0f;
        int i4 = i3 / 2;
        Canvas canvas = new Canvas();
        Paint paint = new Paint(3);
        paint.setMaskFilter(new BlurMaskFilter(f2, BlurMaskFilter.Blur.NORMAL));
        int i5 = i2 / 2;
        float f4 = i4;
        int iMax = Math.max(Math.round(i5 + f2), Math.round(f2 + f4 + f3));
        int i6 = iMax * 2;
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(i6, i6, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmapCreateBitmap);
        paint.setAlpha(30);
        float f5 = iMax - i5;
        float f6 = iMax - i4;
        float f7 = i5 + iMax;
        float f8 = iMax + i4;
        canvas.drawRoundRect(new RectF(f5, f6, f7, f8), f4, f4, paint);
        paint.setAlpha(61);
        canvas.drawRoundRect(new RectF(f5, f6 + f3, f7, f3 + f8), f4, f4, paint);
        Paint paint2 = new Paint(3);
        paint2.setColor(i);
        canvas.drawRoundRect(new RectF(f5, f6, f7, f8), f4, f4, paint2);
        return bitmapCreateBitmap;
    }

    public static ShadowGenerator getInstance() {
        synchronized (LOCK) {
            if (sShadowGenerator == null) {
                sShadowGenerator = new ShadowGenerator();
            }
        }
        return sShadowGenerator;
    }

    public synchronized Bitmap recreateIcon(Bitmap bitmap) {
        Bitmap bitmapCreateBitmap;
        int[] iArr = new int[2];
        Bitmap bitmapExtractAlpha = bitmap.extractAlpha(this.mBlurPaint, iArr);
        bitmapCreateBitmap = Bitmap.createBitmap(this.mIconSize, this.mIconSize, Bitmap.Config.ARGB_8888);
        this.mCanvas.setBitmap(bitmapCreateBitmap);
        this.mDrawPaint.setAlpha(30);
        this.mCanvas.drawBitmap(bitmapExtractAlpha, iArr[0], iArr[1], this.mDrawPaint);
        this.mDrawPaint.setAlpha(61);
        this.mCanvas.drawBitmap(bitmapExtractAlpha, iArr[0], iArr[1] + (this.mIconSize * 0.020833334f), this.mDrawPaint);
        this.mDrawPaint.setAlpha(255);
        this.mCanvas.drawBitmap(bitmap, 0.0f, 0.0f, this.mDrawPaint);
        this.mCanvas.setBitmap(null);
        return bitmapCreateBitmap;
    }
}
