package com.android.photos.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.View;
import android.view.WindowManager;
import androidx.core.util.Pools;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.glrenderer.BasicTexture;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.glrenderer.UploadedTexture;

/* JADX INFO: loaded from: classes.dex */
public class TiledImageRenderer {
    static Pools.Pool<Bitmap> sTilePool = new Pools.SynchronizedPool(64);
    private boolean mBackgroundTileUploaded;
    protected int mCenterX;
    protected int mCenterY;
    private boolean mLayoutTiles;
    protected int mLevelCount;
    TileSource mModel;
    private int mOffsetX;
    private int mOffsetY;
    private View mParent;
    private BasicTexture mPreview;
    private boolean mRenderComplete;
    protected int mRotation;
    protected float mScale;
    int mTileSize;
    private int mUploadQuota;
    private int mViewHeight;
    private int mViewWidth;
    int mLevel = 0;
    private final RectF mSourceRect = new RectF();
    private final RectF mTargetRect = new RectF();
    private final LongSparseArray<Tile> mActiveTiles = new LongSparseArray<>();
    final Object mQueueLock = new Object();
    private final TileQueue mRecycledQueue = new TileQueue();
    private final TileQueue mUploadQueue = new TileQueue();
    final TileQueue mDecodeQueue = new TileQueue();
    protected int mImageWidth = -1;
    protected int mImageHeight = -1;
    private final Rect mTileRange = new Rect();
    private final Rect[] mActiveRange = {new Rect(), new Rect()};
    private TileDecoder mTileDecoder = new TileDecoder();

    private class Tile extends UploadedTexture {
        public Bitmap mDecodedTile;
        public Tile mNext;
        public int mTileLevel;
        public volatile int mTileState = 1;
        public int mX;
        public int mY;

        public Tile(int i, int i2, int i3) {
            this.mX = i;
            this.mY = i2;
            this.mTileLevel = i3;
        }

        boolean decode() {
            try {
                Bitmap bitmapAcquire = TiledImageRenderer.sTilePool.acquire();
                if (bitmapAcquire != null && bitmapAcquire.getWidth() != TiledImageRenderer.this.mTileSize) {
                    bitmapAcquire = null;
                }
                this.mDecodedTile = TiledImageRenderer.this.mModel.getTile(this.mTileLevel, this.mX, this.mY, bitmapAcquire);
            } catch (Throwable th) {
                Log.w("TiledImageRenderer", "fail to decode tile", th);
            }
            return this.mDecodedTile != null;
        }

        public Tile getParentTile() {
            if (this.mTileLevel + 1 == TiledImageRenderer.this.mLevelCount) {
                return null;
            }
            int i = TiledImageRenderer.this.mTileSize;
            int i2 = this.mTileLevel;
            int i3 = i << (i2 + 1);
            return TiledImageRenderer.this.getTile((this.mX / i3) * i3, i3 * (this.mY / i3), i2 + 1);
        }

        @Override // com.android.gallery3d.glrenderer.BasicTexture
        public int getTextureHeight() {
            return TiledImageRenderer.this.mTileSize;
        }

        @Override // com.android.gallery3d.glrenderer.BasicTexture
        public int getTextureWidth() {
            return TiledImageRenderer.this.mTileSize;
        }

        @Override // com.android.gallery3d.glrenderer.UploadedTexture
        protected void onFreeBitmap(Bitmap bitmap) {
            TiledImageRenderer.sTilePool.release(bitmap);
        }

        @Override // com.android.gallery3d.glrenderer.UploadedTexture
        protected Bitmap onGetBitmap() {
            Utils.assertTrue(this.mTileState == 8);
            setSize(Math.min(TiledImageRenderer.this.mTileSize, (TiledImageRenderer.this.mImageWidth - this.mX) >> this.mTileLevel), Math.min(TiledImageRenderer.this.mTileSize, (TiledImageRenderer.this.mImageHeight - this.mY) >> this.mTileLevel));
            Bitmap bitmap = this.mDecodedTile;
            this.mDecodedTile = null;
            this.mTileState = 1;
            return bitmap;
        }

        public String toString() {
            return String.format("tile(%s, %s, %s / %s)", Integer.valueOf(this.mX / TiledImageRenderer.this.mTileSize), Integer.valueOf(this.mY / TiledImageRenderer.this.mTileSize), Integer.valueOf(TiledImageRenderer.this.mLevel), Integer.valueOf(TiledImageRenderer.this.mLevelCount));
        }

        public void update(int i, int i2, int i3) {
            this.mX = i;
            this.mY = i2;
            this.mTileLevel = i3;
            invalidateContent();
        }
    }

    class TileDecoder extends Thread {
        TileDecoder() {
        }

        /* REWORK: jadx emitted a de-facto infinite wait loop without a return;
         * restore the AOSP intent: pop a tile, wait while none is queued. */
        private Tile waitForTile() throws InterruptedException {
            while (true) {
                synchronized (TiledImageRenderer.this.mQueueLock) {
                    Tile tilePop = TiledImageRenderer.this.mDecodeQueue.pop();
                    if (tilePop != null) {
                        return tilePop;
                    }
                    TiledImageRenderer.this.mQueueLock.wait();
                }
            }
        }

        public void finishAndWait() {
            interrupt();
            try {
                join();
            } catch (InterruptedException unused) {
                Log.w("TiledImageRenderer", "Interrupted while waiting for TileDecoder thread to finish!");
            }
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (!isInterrupted()) {
                try {
                    TiledImageRenderer.this.decodeTile(waitForTile());
                } catch (InterruptedException unused) {
                    return;
                }
            }
        }
    }

    static class TileQueue {
        private Tile mHead;

        TileQueue() {
        }

        private boolean contains(Tile tile) {
            for (Tile tile2 = this.mHead; tile2 != null; tile2 = tile2.mNext) {
                if (tile2 == tile) {
                    return true;
                }
            }
            return false;
        }

        public void clean() {
            this.mHead = null;
        }

        public Tile pop() {
            Tile tile = this.mHead;
            if (tile != null) {
                this.mHead = tile.mNext;
            }
            return tile;
        }

        public boolean push(Tile tile) {
            if (contains(tile)) {
                Log.w("TiledImageRenderer", "Attempting to add a tile already in the queue!");
                return false;
            }
            boolean z = this.mHead == null;
            tile.mNext = this.mHead;
            this.mHead = tile;
            return z;
        }
    }

    public interface TileSource {
        int getImageHeight();

        int getImageWidth();

        BasicTexture getPreview();

        int getRotation();

        Bitmap getTile(int i, int i2, int i3, Bitmap bitmap);

        int getTileSize();
    }

    public TiledImageRenderer(View view) {
        this.mParent = view;
        this.mTileDecoder.start();
    }

    private void activateTile(int i, int i2, int i3) {
        long jMakeTileKey = makeTileKey(i, i2, i3);
        Tile tile = this.mActiveTiles.get(jMakeTileKey);
        if (tile == null) {
            this.mActiveTiles.put(jMakeTileKey, obtainTile(i, i2, i3));
        } else if (tile.mTileState == 2) {
            tile.mTileState = 1;
        }
    }

    private void calculateLevelCount() {
        BasicTexture basicTexture = this.mPreview;
        if (basicTexture != null) {
            this.mLevelCount = Math.max(0, Utils.ceilLog2(this.mImageWidth / basicTexture.getWidth()));
            return;
        }
        int iMax = Math.max(this.mImageWidth, this.mImageHeight);
        int i = this.mTileSize;
        int i2 = 1;
        while (i < iMax) {
            i <<= 1;
            i2++;
        }
        this.mLevelCount = i2;
    }

    private void drawTile(GLCanvas gLCanvas, int i, int i2, int i3, float f, float f2, float f3) {
        RectF rectF = this.mSourceRect;
        RectF rectF2 = this.mTargetRect;
        rectF2.set(f, f2, f + f3, f3 + f2);
        int i4 = this.mTileSize;
        rectF.set(0.0f, 0.0f, i4, i4);
        Tile tile = getTile(i, i2, i3);
        if (tile != null) {
            if (!tile.isContentValid()) {
                if (tile.mTileState == 8) {
                    int i5 = this.mUploadQuota;
                    if (i5 > 0) {
                        this.mUploadQuota = i5 - 1;
                        tile.updateContent(gLCanvas);
                    } else {
                        this.mRenderComplete = false;
                    }
                } else if (tile.mTileState != 16) {
                    this.mRenderComplete = false;
                    queueForDecode(tile);
                }
            }
            if (drawTile(tile, gLCanvas, rectF, rectF2)) {
                return;
            }
        }
        BasicTexture basicTexture = this.mPreview;
        if (basicTexture != null) {
            int i6 = this.mTileSize << i3;
            float width = basicTexture.getWidth() / this.mImageWidth;
            float height = this.mPreview.getHeight() / this.mImageHeight;
            rectF.set(i * width, i2 * height, (i + i6) * width, (i2 + i6) * height);
            gLCanvas.drawTexture(this.mPreview, rectF, rectF2);
        }
    }

    private boolean drawTile(Tile tile, GLCanvas gLCanvas, RectF rectF, RectF rectF2) {
        while (!tile.isContentValid()) {
            Tile parentTile = tile.getParentTile();
            if (parentTile == null) {
                return false;
            }
            if (tile.mX == parentTile.mX) {
                rectF.left /= 2.0f;
                rectF.right /= 2.0f;
            } else {
                rectF.left = (this.mTileSize + rectF.left) / 2.0f;
                rectF.right = (this.mTileSize + rectF.right) / 2.0f;
            }
            if (tile.mY == parentTile.mY) {
                rectF.top /= 2.0f;
                rectF.bottom /= 2.0f;
            } else {
                rectF.top = (this.mTileSize + rectF.top) / 2.0f;
                rectF.bottom = (this.mTileSize + rectF.bottom) / 2.0f;
            }
            tile = parentTile;
        }
        gLCanvas.drawTexture(tile, rectF, rectF2);
        return true;
    }

    private void getRange(Rect rect, int i, int i2, int i3, float f, int i4) {
        double radians = Math.toRadians(-i4);
        double d = this.mViewWidth;
        double d2 = this.mViewHeight;
        double dCos = Math.cos(radians);
        double dSin = Math.sin(radians);
        Double.isNaN(d);
        double d3 = dCos * d;
        Double.isNaN(d2);
        double d4 = dSin * d2;
        int iCeil = (int) Math.ceil(Math.max(Math.abs(d3 - d4), Math.abs(d3 + d4)));
        Double.isNaN(d);
        double d5 = dSin * d;
        Double.isNaN(d2);
        double d6 = dCos * d2;
        int iCeil2 = (int) Math.ceil(Math.max(Math.abs(d5 + d6), Math.abs(d5 - d6)));
        float f2 = iCeil;
        float f3 = 2.0f * f;
        int iFloor = (int) Math.floor(i - (f2 / f3));
        float f4 = iCeil2;
        int iFloor2 = (int) Math.floor(i2 - (f4 / f3));
        int iCeil3 = (int) Math.ceil(iFloor + (f2 / f));
        int iCeil4 = (int) Math.ceil(iFloor2 + (f4 / f));
        int i5 = this.mTileSize << i3;
        rect.set(Math.max(0, (iFloor / i5) * i5), Math.max(0, i5 * (iFloor2 / i5)), Math.min(this.mImageWidth, iCeil3), Math.min(this.mImageHeight, iCeil4));
    }

    private void getRange(Rect rect, int i, int i2, int i3, int i4) {
        getRange(rect, i, i2, i3, 1.0f / (1 << (i3 + 1)), i4);
    }

    private void invalidate() {
        this.mParent.postInvalidate();
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    private void invalidateTiles() {
        synchronized (this.mQueueLock) {
            this.mDecodeQueue.clean();
            this.mUploadQueue.clean();
            int size = this.mActiveTiles.size();
            for (int i = 0; i < size; i++) {
                recycleTile(this.mActiveTiles.valueAt(i));
            }
            this.mActiveTiles.clear();
        }
    }

    private static boolean isHighResolution(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels > 2048 || displayMetrics.widthPixels > 2048;
    }

    private void layoutTiles() {
        int i;
        if (this.mViewWidth == 0 || this.mViewHeight == 0 || !this.mLayoutTiles) {
            return;
        }
        int i2 = 0;
        this.mLayoutTiles = false;
        this.mLevel = Utils.clamp(Utils.floorLog2(1.0f / this.mScale), 0, this.mLevelCount);
        int i3 = this.mLevel;
        if (i3 != this.mLevelCount) {
            Rect rect = this.mTileRange;
            getRange(rect, this.mCenterX, this.mCenterY, i3, this.mScale, this.mRotation);
            this.mOffsetX = Math.round((this.mViewWidth / 2.0f) + ((rect.left - this.mCenterX) * this.mScale));
            this.mOffsetY = Math.round((this.mViewHeight / 2.0f) + ((rect.top - this.mCenterY) * this.mScale));
            float f = this.mScale;
            i = this.mLevel;
            if (f * (1 << i) > 0.75f) {
                i--;
            }
        } else {
            i = i3 - 2;
            this.mOffsetX = Math.round((this.mViewWidth / 2.0f) - (this.mCenterX * this.mScale));
            this.mOffsetY = Math.round((this.mViewHeight / 2.0f) - (this.mCenterY * this.mScale));
        }
        int iMax = Math.max(0, Math.min(i, this.mLevelCount - 2));
        int iMin = Math.min(iMax + 2, this.mLevelCount);
        Rect[] rectArr = this.mActiveRange;
        for (int i4 = iMax; i4 < iMin; i4++) {
            getRange(rectArr[i4 - iMax], this.mCenterX, this.mCenterY, i4, this.mRotation);
        }
        if (this.mRotation % 90 != 0) {
            return;
        }
        synchronized (this.mQueueLock) {
            this.mDecodeQueue.clean();
            this.mUploadQueue.clean();
            this.mBackgroundTileUploaded = false;
            int size = this.mActiveTiles.size();
            while (i2 < size) {
                Tile tileValueAt = this.mActiveTiles.valueAt(i2);
                int i5 = tileValueAt.mTileLevel;
                if (i5 < iMax || i5 >= iMin || !rectArr[i5 - iMax].contains(tileValueAt.mX, tileValueAt.mY)) {
                    this.mActiveTiles.removeAt(i2);
                    i2--;
                    size--;
                    recycleTile(tileValueAt);
                }
                i2++;
            }
        }
        for (int i6 = iMax; i6 < iMin; i6++) {
            int i7 = this.mTileSize << i6;
            Rect rect2 = rectArr[i6 - iMax];
            int i8 = rect2.bottom;
            for (int i9 = rect2.top; i9 < i8; i9 += i7) {
                int i10 = rect2.right;
                for (int i11 = rect2.left; i11 < i10; i11 += i7) {
                    activateTile(i11, i9, i6);
                }
            }
        }
        invalidate();
    }

    private static long makeTileKey(int i, int i2, int i3) {
        return (((((long) i) << 16) | ((long) i2)) << 16) | ((long) i3);
    }

    private Tile obtainTile(int i, int i2, int i3) {
        synchronized (this.mQueueLock) {
            Tile tilePop = this.mRecycledQueue.pop();
            if (tilePop == null) {
                return new Tile(i, i2, i3);
            }
            tilePop.mTileState = 1;
            tilePop.update(i, i2, i3);
            return tilePop;
        }
    }

    private void queueForDecode(Tile tile) {
        synchronized (this.mQueueLock) {
            if (tile.mTileState == 1) {
                tile.mTileState = 2;
                if (this.mDecodeQueue.push(tile)) {
                    this.mQueueLock.notifyAll();
                }
            }
        }
    }

    private void recycleTile(Tile tile) {
        synchronized (this.mQueueLock) {
            if (tile.mTileState == 4) {
                tile.mTileState = 32;
                return;
            }
            tile.mTileState = 64;
            if (tile.mDecodedTile != null) {
                sTilePool.release(tile.mDecodedTile);
                tile.mDecodedTile = null;
            }
            this.mRecycledQueue.push(tile);
        }
    }

    public static int suggestedTileSize(Context context) {
        return isHighResolution(context) ? 512 : 256;
    }

    private void uploadBackgroundTiles(GLCanvas gLCanvas) {
        this.mBackgroundTileUploaded = true;
        int size = this.mActiveTiles.size();
        for (int i = 0; i < size; i++) {
            Tile tileValueAt = this.mActiveTiles.valueAt(i);
            if (!tileValueAt.isContentValid()) {
                queueForDecode(tileValueAt);
            }
        }
    }

    private void uploadTiles(GLCanvas gLCanvas) {
        int i = 1;
        Tile tilePop = null;
        while (i > 0) {
            synchronized (this.mQueueLock) {
                tilePop = this.mUploadQueue.pop();
            }
            if (tilePop == null) {
                break;
            }
            if (!tilePop.isContentValid()) {
                if (tilePop.mTileState == 8) {
                    tilePop.updateContent(gLCanvas);
                    i--;
                } else {
                    Log.w("TiledImageRenderer", "Tile in upload queue has invalid state: " + tilePop.mTileState);
                }
            }
        }
        if (tilePop != null) {
            invalidate();
        }
    }

    void decodeTile(Tile tile) {
        synchronized (this.mQueueLock) {
            if (tile.mTileState != 2) {
                return;
            }
            tile.mTileState = 4;
            boolean zDecode = tile.decode();
            synchronized (this.mQueueLock) {
                if (tile.mTileState == 32) {
                    tile.mTileState = 64;
                    if (tile.mDecodedTile != null) {
                        sTilePool.release(tile.mDecodedTile);
                        tile.mDecodedTile = null;
                    }
                    this.mRecycledQueue.push(tile);
                    return;
                }
                tile.mTileState = zDecode ? 8 : 16;
                if (zDecode) {
                    this.mUploadQueue.push(tile);
                    invalidate();
                }
            }
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public boolean draw(GLCanvas gLCanvas) {
        layoutTiles();
        uploadTiles(gLCanvas);
        this.mUploadQuota = 1;
        this.mRenderComplete = true;
        int i = this.mLevel;
        int i2 = this.mRotation;
        int i3 = i2 != 0 ? 2 : 0;
        if (i3 != 0) {
            gLCanvas.save(i3);
            if (i2 != 0) {
                int i4 = this.mViewWidth / 2;
                int i5 = this.mViewHeight / 2;
                gLCanvas.translate(i4, i5);
                gLCanvas.rotate(i2, 0.0f, 0.0f, 1.0f);
                gLCanvas.translate(-i4, -i5);
            }
        }
        try {
            if (i != this.mLevelCount) {
                int i6 = this.mTileSize << i;
                float f = i6 * this.mScale;
                Rect rect = this.mTileRange;
                int i7 = rect.top;
                int i8 = 0;
                while (i7 < rect.bottom) {
                    float f2 = this.mOffsetY + (i8 * f);
                    int i9 = rect.left;
                    int i10 = 0;
                    while (i9 < rect.right) {
                        drawTile(gLCanvas, i9, i7, i, this.mOffsetX + (i10 * f), f2, f);
                        i9 += i6;
                        i10++;
                        i7 = i7;
                        i8 = i8;
                        rect = rect;
                    }
                    i7 += i6;
                    i8++;
                }
            } else if (this.mPreview != null) {
                this.mPreview.draw(gLCanvas, this.mOffsetX, this.mOffsetY, Math.round(this.mImageWidth * this.mScale), Math.round(this.mImageHeight * this.mScale));
            }
            if (i3 != 0) {
                gLCanvas.restore();
            }
            if (!this.mRenderComplete) {
                invalidate();
            } else if (!this.mBackgroundTileUploaded) {
                uploadBackgroundTiles(gLCanvas);
            }
            return this.mRenderComplete || this.mPreview != null;
        } catch (Throwable th) {
            if (i3 != 0) {
                gLCanvas.restore();
            }
            throw th;
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public void freeTextures() {
        this.mLayoutTiles = true;
        this.mTileDecoder.finishAndWait();
        synchronized (this.mQueueLock) {
            this.mUploadQueue.clean();
            this.mDecodeQueue.clean();
            Tile tilePop = this.mRecycledQueue.pop();
            while (tilePop != null) {
                tilePop.recycle();
                tilePop = this.mRecycledQueue.pop();
            }
        }
        int size = this.mActiveTiles.size();
        for (int i = 0; i < size; i++) {
            this.mActiveTiles.valueAt(i).recycle();
        }
        this.mActiveTiles.clear();
        this.mTileRange.set(0, 0, 0, 0);
        while (sTilePool.acquire() != null) {
        }
    }

    Tile getTile(int i, int i2, int i3) {
        return this.mActiveTiles.get(makeTileKey(i, i2, i3));
    }

    public void notifyModelInvalidated() {
        invalidateTiles();
        TileSource tileSource = this.mModel;
        if (tileSource == null) {
            this.mImageWidth = 0;
            this.mImageHeight = 0;
            this.mLevelCount = 0;
            this.mPreview = null;
        } else {
            this.mImageWidth = tileSource.getImageWidth();
            this.mImageHeight = this.mModel.getImageHeight();
            this.mPreview = this.mModel.getPreview();
            this.mTileSize = this.mModel.getTileSize();
            calculateLevelCount();
        }
        this.mLayoutTiles = true;
    }

    public void setModel(TileSource tileSource, int i) {
        if (this.mModel != tileSource) {
            this.mModel = tileSource;
            notifyModelInvalidated();
        }
        if (this.mRotation != i) {
            this.mRotation = i;
            this.mLayoutTiles = true;
        }
    }

    public void setPosition(int i, int i2, float f) {
        if (this.mCenterX == i && this.mCenterY == i2 && this.mScale == f) {
            return;
        }
        this.mCenterX = i;
        this.mCenterY = i2;
        this.mScale = f;
        this.mLayoutTiles = true;
    }

    public void setViewSize(int i, int i2) {
        this.mViewWidth = i;
        this.mViewHeight = i2;
    }
}
