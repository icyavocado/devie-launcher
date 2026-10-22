package com.voxel.simplesearchlauncher.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import com.voxel.launcher3.LauncherAppState;
import java.nio.ByteBuffer;

/* JADX INFO: loaded from: classes2.dex */
public class IconNormalizer {
    private static final Object LOCK = new Object();
    private static IconNormalizer sIconNormalizer;
    private final Bitmap mBitmap;
    private final Canvas mCanvas;
    private final float[] mLeftBorder;
    private final int mMaxSize = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().unscaledIconSizePx * 2;
    private final byte[] mPixels;
    private final float[] mRightBorder;

    private IconNormalizer() {
        int i = this.mMaxSize;
        this.mBitmap = Bitmap.createBitmap(i, i, Bitmap.Config.ALPHA_8);
        this.mCanvas = new Canvas(this.mBitmap);
        int i2 = this.mMaxSize;
        this.mPixels = new byte[i2 * i2];
        this.mLeftBorder = new float[i2];
        this.mRightBorder = new float[i2];
    }

    private static void convertToConvexArray(float[] fArr, int i, int i2, int i3) {
        float[] fArr2 = new float[fArr.length - 1];
        float f = Float.MAX_VALUE;
        int i4 = -1;
        for (int i5 = i2 + 1; i5 <= i3; i5++) {
            if (fArr[i5] > -1.0f) {
                if (f != Float.MAX_VALUE) {
                    float f2 = ((fArr[i5] - fArr[i4]) / (i5 - i4)) - f;
                    float f3 = i;
                    if (f2 * f3 < 0.0f) {
                        while (i4 > i2) {
                            i4--;
                            if ((((fArr[i5] - fArr[i4]) / (i5 - i4)) - fArr2[i4]) * f3 >= 0.0f) {
                                break;
                            }
                        }
                    }
                } else {
                    i4 = i2;
                }
                f = (fArr[i5] - fArr[i4]) / (i5 - i4);
                for (int i6 = i4; i6 < i5; i6++) {
                    fArr2[i6] = f;
                    fArr[i6] = fArr[i4] + ((i6 - i4) * f);
                }
                i4 = i5;
            }
        }
    }

    public static IconNormalizer getInstance() {
        synchronized (LOCK) {
            if (sIconNormalizer == null) {
                sIconNormalizer = new IconNormalizer();
            }
        }
        return sIconNormalizer;
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public synchronized float getScale(Drawable drawable, RectF rectF) {
        int i;
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            if (intrinsicWidth <= 0 || intrinsicWidth > this.mMaxSize) {
                intrinsicWidth = this.mMaxSize;
            }
            if (intrinsicHeight <= 0 || intrinsicHeight > this.mMaxSize) {
                intrinsicHeight = this.mMaxSize;
            }
        } else if (intrinsicWidth > this.mMaxSize || intrinsicHeight > this.mMaxSize) {
            int iMax = Math.max(intrinsicWidth, intrinsicHeight);
            intrinsicWidth = (this.mMaxSize * intrinsicWidth) / iMax;
            intrinsicHeight = (this.mMaxSize * intrinsicHeight) / iMax;
        }
        this.mBitmap.eraseColor(0);
        drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
        drawable.draw(this.mCanvas);
        ByteBuffer byteBufferWrap = ByteBuffer.wrap(this.mPixels);
        byteBufferWrap.rewind();
        this.mBitmap.copyPixelsToBuffer(byteBufferWrap);
        int i2 = this.mMaxSize + 1;
        int i3 = this.mMaxSize - intrinsicWidth;
        int iMin = i2;
        int i4 = 0;
        int i5 = -1;
        int iMax2 = -1;
        int i6 = -1;
        int i7 = 0;
        while (i4 < intrinsicHeight) {
            int i8 = i7;
            int i9 = -1;
            int i10 = 0;
            int i11 = -1;
            while (i10 < intrinsicWidth) {
                int i12 = intrinsicWidth;
                if ((this.mPixels[i8] & 255) > 40) {
                    if (i11 == -1) {
                        i11 = i10;
                    }
                    i9 = i10;
                }
                i8++;
                i10++;
                intrinsicWidth = i12;
            }
            int i13 = intrinsicWidth;
            i7 = i8 + i3;
            this.mLeftBorder[i4] = i11;
            this.mRightBorder[i4] = i9;
            if (i11 != -1) {
                if (i5 == -1) {
                    i5 = i4;
                }
                i6 = i4;
                iMin = Math.min(iMin, i11);
                iMax2 = Math.max(iMax2, i9);
            }
            i4++;
            intrinsicWidth = i13;
        }
        int i14 = intrinsicWidth;
        if (i5 != -1 && iMax2 != -1) {
            convertToConvexArray(this.mLeftBorder, 1, i5, i6);
            convertToConvexArray(this.mRightBorder, -1, i5, i6);
            float f = 0.0f;
            for (int i15 = 0; i15 < intrinsicHeight; i15++) {
                if (this.mLeftBorder[i15] > -1.0f) {
                    f += (this.mRightBorder[i15] - this.mLeftBorder[i15]) + 1.0f;
                }
            }
            float f2 = f / (((i6 + 1) - i5) * ((iMax2 + 1) - iMin));
            float f3 = f2 < 0.7853982f ? 0.6597222f : ((1.0f - f2) * 0.040449437f) + 0.6510417f;
            if (rectF != null) {
                i = i14;
                float f4 = i;
                rectF.left = iMin / f4;
                rectF.right = 1.0f - (iMax2 / f4);
                float f5 = intrinsicHeight;
                rectF.top = i5 / f5;
                rectF.bottom = 1.0f - (i6 / f5);
            } else {
                i = i14;
            }
            float f6 = f / (i * intrinsicHeight);
            return f6 > f3 ? (float) Math.sqrt(f3 / f6) : 1.0f;
        }
        return 1.0f;
    }
}
