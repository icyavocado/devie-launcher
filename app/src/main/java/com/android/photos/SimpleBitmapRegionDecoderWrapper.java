package com.android.photos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;

/* JADX INFO: compiled from: BitmapRegionTileSource.java */
/* JADX INFO: loaded from: classes.dex */
class SimpleBitmapRegionDecoderWrapper implements SimpleBitmapRegionDecoder {
    BitmapRegionDecoder mDecoder;

    private SimpleBitmapRegionDecoderWrapper(BitmapRegionDecoder bitmapRegionDecoder) {
        this.mDecoder = bitmapRegionDecoder;
    }

    public static SimpleBitmapRegionDecoderWrapper newInstance(InputStream inputStream, boolean z) {
        try {
            BitmapRegionDecoder bitmapRegionDecoderNewInstance = BitmapRegionDecoder.newInstance(inputStream, z);
            if (bitmapRegionDecoderNewInstance != null) {
                return new SimpleBitmapRegionDecoderWrapper(bitmapRegionDecoderNewInstance);
            }
            return null;
        } catch (IOException e) {
            Log.w("BitmapRegionTileSource", "getting decoder failed", e);
            return null;
        }
    }

    @Override // com.android.photos.SimpleBitmapRegionDecoder
    public Bitmap decodeRegion(Rect rect, BitmapFactory.Options options) {
        return this.mDecoder.decodeRegion(rect, options);
    }

    @Override // com.android.photos.SimpleBitmapRegionDecoder
    public int getHeight() {
        return this.mDecoder.getHeight();
    }

    @Override // com.android.photos.SimpleBitmapRegionDecoder
    public int getWidth() {
        return this.mDecoder.getWidth();
    }
}
