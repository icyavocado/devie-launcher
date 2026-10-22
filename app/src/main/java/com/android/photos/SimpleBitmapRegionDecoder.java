package com.android.photos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

/* JADX INFO: compiled from: BitmapRegionTileSource.java */
/* JADX INFO: loaded from: classes.dex */
interface SimpleBitmapRegionDecoder {
    Bitmap decodeRegion(Rect rect, BitmapFactory.Options options);

    int getHeight();

    int getWidth();
}
