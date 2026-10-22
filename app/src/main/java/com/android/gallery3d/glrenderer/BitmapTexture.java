package com.android.gallery3d.glrenderer;

import android.graphics.Bitmap;
import com.android.gallery3d.common.Utils;

/* JADX INFO: loaded from: classes.dex */
public class BitmapTexture extends UploadedTexture {
    protected Bitmap mContentBitmap;

    public BitmapTexture(Bitmap bitmap) {
        this(bitmap, false);
    }

    public BitmapTexture(Bitmap bitmap, boolean z) {
        super(z);
        Utils.assertTrue((bitmap == null || bitmap.isRecycled()) ? false : true);
        this.mContentBitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return this.mContentBitmap;
    }

    @Override // com.android.gallery3d.glrenderer.UploadedTexture
    protected void onFreeBitmap(Bitmap bitmap) {
    }

    @Override // com.android.gallery3d.glrenderer.UploadedTexture
    protected Bitmap onGetBitmap() {
        return this.mContentBitmap;
    }
}
