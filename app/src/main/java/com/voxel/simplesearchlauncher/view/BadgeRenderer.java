package com.voxel.simplesearchlauncher.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.SparseArray;
import com.voxel.launcher3.graphics.IconPalette;
import com.voxel.launcher3.graphics.ShadowGenerator;

/* JADX INFO: loaded from: classes2.dex */
public class BadgeRenderer {
    private static BadgeRenderer sInstance;
    private final Paint mBackgroundPaint = new Paint(3);
    private final SparseArray<Bitmap> mBackgroundsWithShadow = new SparseArray<>(3);

    public static BadgeRenderer getInstance() {
        if (sInstance == null) {
            sInstance = new BadgeRenderer();
        }
        return sInstance;
    }

    public void draw(Canvas canvas, IconPalette iconPalette, Rect rect, Rect rect2, int i, float f, float f2) {
        int i2 = (int) (i * 0.38f);
        Bitmap bitmapCreatePillWithShadow = this.mBackgroundsWithShadow.get(1);
        if (bitmapCreatePillWithShadow == null) {
            bitmapCreatePillWithShadow = ShadowGenerator.createPillWithShadow(-1, i2, i2);
            this.mBackgroundsWithShadow.put(1, bitmapCreatePillWithShadow);
        }
        canvas.save();
        int i3 = (int) (i2 * 0.7f * f * 0.55f);
        int i4 = rect.right - i3;
        int i5 = rect.top + i3;
        if (i4 + i3 > rect2.right) {
            i4 = rect2.right - i3;
        }
        if (i5 - i3 < rect2.top) {
            i5 = rect2.top + i3;
        }
        float f3 = f * f2 * 0.7f;
        canvas.translate(i4 + 0, i5 + 0);
        canvas.scale(f3, f3);
        this.mBackgroundPaint.setColorFilter(iconPalette.backgroundColorMatrixFilter);
        int height = bitmapCreatePillWithShadow.getHeight();
        this.mBackgroundPaint.setColorFilter(iconPalette.saturatedBackgroundColorMatrixFilter);
        float f4 = (-height) / 2;
        canvas.drawBitmap(bitmapCreatePillWithShadow, f4, f4, this.mBackgroundPaint);
        canvas.restore();
    }
}
