package com.android.photos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import java.io.InputStream;

/* JADX INFO: compiled from: BitmapRegionTileSource.java */
/* JADX INFO: loaded from: classes.dex */
class DumbBitmapRegionDecoder implements SimpleBitmapRegionDecoder {
    Bitmap mBuffer;
    Canvas mTempCanvas;
    Paint mTempPaint;

    private DumbBitmapRegionDecoder(Bitmap bitmap) {
        this.mBuffer = bitmap;
    }

    public static DumbBitmapRegionDecoder newInstance(InputStream inputStream) {
        Bitmap bitmapDecodeStream = BitmapFactory.decodeStream(inputStream);
        if (bitmapDecodeStream != null) {
            return new DumbBitmapRegionDecoder(bitmapDecodeStream);
        }
        return null;
    }

    @Override // com.android.photos.SimpleBitmapRegionDecoder
    public Bitmap decodeRegion(Rect rect, BitmapFactory.Options options) {
        if (this.mTempCanvas == null) {
            this.mTempCanvas = new Canvas();
            this.mTempPaint = new Paint();
            this.mTempPaint.setFilterBitmap(true);
        }
        int iMax = Math.max(options.inSampleSize, 1);
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(rect.width() / iMax, rect.height() / iMax, Bitmap.Config.ARGB_8888);
        this.mTempCanvas.setBitmap(bitmapCreateBitmap);
        this.mTempCanvas.save();
        float f = 1.0f / iMax;
        this.mTempCanvas.scale(f, f);
        this.mTempCanvas.drawBitmap(this.mBuffer, -rect.left, -rect.top, this.mTempPaint);
        this.mTempCanvas.restore();
        this.mTempCanvas.setBitmap(null);
        return bitmapCreateBitmap;
    }

    @Override // com.android.photos.SimpleBitmapRegionDecoder
    public int getHeight() {
        return this.mBuffer.getHeight();
    }

    @Override // com.android.photos.SimpleBitmapRegionDecoder
    public int getWidth() {
        return this.mBuffer.getWidth();
    }
}
