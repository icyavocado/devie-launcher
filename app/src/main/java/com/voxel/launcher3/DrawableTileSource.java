package com.voxel.launcher3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import com.android.gallery3d.glrenderer.BasicTexture;
import com.android.gallery3d.glrenderer.BitmapTexture;
import com.android.photos.views.TiledImageRenderer;

/* JADX INFO: loaded from: classes.dex */
public class DrawableTileSource implements TiledImageRenderer.TileSource {
    private Drawable mDrawable;
    private BitmapTexture mPreview;
    private int mPreviewSize;
    private int mTileSize;

    public DrawableTileSource(Context context, Drawable drawable, int i) {
        this.mTileSize = TiledImageRenderer.suggestedTileSize(context);
        this.mDrawable = drawable;
        this.mPreviewSize = Math.min(i, 1024);
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public int getImageHeight() {
        return this.mDrawable.getIntrinsicHeight();
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public int getImageWidth() {
        return this.mDrawable.getIntrinsicWidth();
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public BasicTexture getPreview() {
        if (this.mPreviewSize == 0) {
            return null;
        }
        if (this.mPreview == null) {
            float imageWidth = getImageWidth();
            float imageHeight = getImageHeight();
            while (true) {
                if (imageWidth <= 1024.0f && imageHeight <= 1024.0f) {
                    break;
                }
                imageWidth /= 2.0f;
                imageHeight /= 2.0f;
            }
            int i = (int) imageWidth;
            int i2 = (int) imageHeight;
            Bitmap bitmapCreateBitmap = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmapCreateBitmap);
            this.mDrawable.setBounds(new Rect(0, 0, i, i2));
            this.mDrawable.draw(canvas);
            canvas.setBitmap(null);
            this.mPreview = new BitmapTexture(bitmapCreateBitmap);
        }
        return this.mPreview;
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public int getRotation() {
        return 0;
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public Bitmap getTile(int i, int i2, int i3, Bitmap bitmap) {
        int tileSize = getTileSize();
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        Rect rect = new Rect(0, 0, getImageWidth(), getImageHeight());
        rect.offset(-i2, -i3);
        this.mDrawable.setBounds(rect);
        this.mDrawable.draw(canvas);
        canvas.setBitmap(null);
        return bitmap;
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public int getTileSize() {
        return this.mTileSize;
    }
}
