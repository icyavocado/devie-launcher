package com.voxel.simplesearchlauncher.drawable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;

/* JADX INFO: loaded from: classes2.dex */
public class RoundedCornerDrawable extends Drawable {
    Bitmap mBitmap;

    public enum CornerType {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT
    }

    public RoundedCornerDrawable(int i, CornerType cornerType) {
        float f;
        this.mBitmap = null;
        this.mBitmap = Bitmap.createBitmap(i, i, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(this.mBitmap);
        canvas.drawColor(-16777216);
        Paint paint = new Paint();
        paint.setColor(0);
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        float f2 = 0.0f;
        switch (cornerType) {
            case TOP_LEFT:
                f2 = i;
                f = f2;
                break;
            case TOP_RIGHT:
                f = i;
                break;
            case BOTTOM_LEFT:
                f2 = i;
                f = 0.0f;
                break;
            default:
                f = 0.0f;
                break;
        }
        canvas.drawCircle(f2, f, i, paint);
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        canvas.drawBitmap(this.mBitmap, 0.0f, 0.0f, (Paint) null);
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
    }
}
