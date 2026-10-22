package com.android.photos;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.opengl.GLUtils;
import android.util.Log;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.exif.ExifInterface;
import com.android.gallery3d.glrenderer.BasicTexture;
import com.android.gallery3d.glrenderer.BitmapTexture;
import com.android.photos.views.TiledImageRenderer;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/* JADX INFO: loaded from: classes.dex */
@TargetApi(15)
public class BitmapRegionTileSource implements TiledImageRenderer.TileSource {
    SimpleBitmapRegionDecoder mDecoder;
    int mHeight;
    private BitmapFactory.Options mOptions;
    private BasicTexture mPreview;
    private final int mRotation;
    int mTileSize;
    private Rect mWantRegion = new Rect();
    int mWidth;

    public static abstract class BitmapSource {
        private SimpleBitmapRegionDecoder mDecoder;
        private Bitmap mPreview;
        private int mRotation;
        private State mState = State.NOT_LOADED;

        public interface InBitmapProvider {
            Bitmap forPixelCount(int i);
        }

        public enum State {
            NOT_LOADED,
            LOADED,
            ERROR_LOADING
        }

        public SimpleBitmapRegionDecoder getBitmapRegionDecoder() {
            return this.mDecoder;
        }

        public State getLoadingState() {
            return this.mState;
        }

        public Bitmap getPreviewBitmap() {
            return this.mPreview;
        }

        public int getRotation() {
            return this.mRotation;
        }

        public abstract SimpleBitmapRegionDecoder loadBitmapRegionDecoder();

        public boolean loadInBackground(InBitmapProvider inBitmapProvider) {
            Bitmap bitmapForPixelCount;
            Integer tagIntValue;
            ExifInterface exifInterface = new ExifInterface();
            if (readExif(exifInterface) && (tagIntValue = exifInterface.getTagIntValue(ExifInterface.TAG_ORIENTATION)) != null) {
                this.mRotation = ExifInterface.getRotationForOrientationValue(tagIntValue.shortValue());
            }
            this.mDecoder = loadBitmapRegionDecoder();
            SimpleBitmapRegionDecoder simpleBitmapRegionDecoder = this.mDecoder;
            if (simpleBitmapRegionDecoder == null) {
                this.mState = State.ERROR_LOADING;
                return false;
            }
            int width = simpleBitmapRegionDecoder.getWidth();
            int height = this.mDecoder.getHeight();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inPreferQualityOverSpeed = true;
            options.inSampleSize = BitmapUtils.computeSampleSizeLarger(1024.0f / Math.max(width, height));
            options.inJustDecodeBounds = false;
            options.inMutable = true;
            if (inBitmapProvider != null && (bitmapForPixelCount = inBitmapProvider.forPixelCount((width / options.inSampleSize) * (height / options.inSampleSize))) != null) {
                options.inBitmap = bitmapForPixelCount;
                try {
                    this.mPreview = loadPreviewBitmap(options);
                } catch (IllegalArgumentException e) {
                    Log.d("BitmapRegionTileSource", "Unable to reuse bitmap", e);
                    options.inBitmap = null;
                    this.mPreview = null;
                }
            }
            if (this.mPreview == null) {
                this.mPreview = loadPreviewBitmap(options);
            }
            Bitmap bitmap = this.mPreview;
            if (bitmap == null) {
                this.mState = State.ERROR_LOADING;
                return false;
            }
            try {
                GLUtils.getInternalFormat(bitmap);
                GLUtils.getType(this.mPreview);
                this.mState = State.LOADED;
            } catch (IllegalArgumentException e2) {
                Log.d("BitmapRegionTileSource", "Image cannot be rendered on a GL surface", e2);
                this.mState = State.ERROR_LOADING;
            }
            return this.mState == State.LOADED;
        }

        public abstract Bitmap loadPreviewBitmap(BitmapFactory.Options options);

        public abstract boolean readExif(ExifInterface exifInterface);
    }

    public static class ResourceBitmapSource extends BitmapSource {
        private Resources mRes;
        private int mResId;

        public ResourceBitmapSource(Resources resources, int i) {
            this.mRes = resources;
            this.mResId = i;
        }

        private InputStream regenerateInputStream() {
            return new BufferedInputStream(this.mRes.openRawResource(this.mResId));
        }

        @Override // com.android.photos.BitmapRegionTileSource.BitmapSource
        public SimpleBitmapRegionDecoder loadBitmapRegionDecoder() {
            InputStream inputStreamRegenerateInputStream = regenerateInputStream();
            SimpleBitmapRegionDecoderWrapper simpleBitmapRegionDecoderWrapperNewInstance = SimpleBitmapRegionDecoderWrapper.newInstance(inputStreamRegenerateInputStream, false);
            Utils.closeSilently(inputStreamRegenerateInputStream);
            if (simpleBitmapRegionDecoderWrapperNewInstance != null) {
                return simpleBitmapRegionDecoderWrapperNewInstance;
            }
            InputStream inputStreamRegenerateInputStream2 = regenerateInputStream();
            DumbBitmapRegionDecoder dumbBitmapRegionDecoderNewInstance = DumbBitmapRegionDecoder.newInstance(inputStreamRegenerateInputStream2);
            Utils.closeSilently(inputStreamRegenerateInputStream2);
            return dumbBitmapRegionDecoderNewInstance;
        }

        @Override // com.android.photos.BitmapRegionTileSource.BitmapSource
        public Bitmap loadPreviewBitmap(BitmapFactory.Options options) {
            return BitmapFactory.decodeResource(this.mRes, this.mResId, options);
        }

        @Override // com.android.photos.BitmapRegionTileSource.BitmapSource
        public boolean readExif(ExifInterface exifInterface) {
            try {
                InputStream inputStreamRegenerateInputStream = regenerateInputStream();
                exifInterface.readExif(inputStreamRegenerateInputStream);
                Utils.closeSilently(inputStreamRegenerateInputStream);
                return true;
            } catch (IOException e) {
                Log.e("BitmapRegionTileSource", "Error reading resource", e);
                return false;
            }
        }
    }

    public static class UriBitmapSource extends BitmapSource {
        private Context mContext;
        private Uri mUri;

        public UriBitmapSource(Context context, Uri uri) {
            this.mContext = context;
            this.mUri = uri;
        }

        private InputStream regenerateInputStream() throws FileNotFoundException {
            return new BufferedInputStream(this.mContext.getContentResolver().openInputStream(this.mUri));
        }

        @Override // com.android.photos.BitmapRegionTileSource.BitmapSource
        public SimpleBitmapRegionDecoder loadBitmapRegionDecoder() {
            try {
                InputStream inputStreamRegenerateInputStream = regenerateInputStream();
                SimpleBitmapRegionDecoderWrapper simpleBitmapRegionDecoderWrapperNewInstance = SimpleBitmapRegionDecoderWrapper.newInstance(inputStreamRegenerateInputStream, false);
                Utils.closeSilently(inputStreamRegenerateInputStream);
                if (simpleBitmapRegionDecoderWrapperNewInstance != null) {
                    return simpleBitmapRegionDecoderWrapperNewInstance;
                }
                InputStream inputStreamRegenerateInputStream2 = regenerateInputStream();
                DumbBitmapRegionDecoder dumbBitmapRegionDecoderNewInstance = DumbBitmapRegionDecoder.newInstance(inputStreamRegenerateInputStream2);
                Utils.closeSilently(inputStreamRegenerateInputStream2);
                return dumbBitmapRegionDecoderNewInstance;
            } catch (FileNotFoundException e) {
                Log.e("BitmapRegionTileSource", "Failed to load URI " + this.mUri, e);
                return null;
            }
        }

        @Override // com.android.photos.BitmapRegionTileSource.BitmapSource
        public Bitmap loadPreviewBitmap(BitmapFactory.Options options) {
            try {
                InputStream inputStreamRegenerateInputStream = regenerateInputStream();
                Bitmap bitmapDecodeStream = BitmapFactory.decodeStream(inputStreamRegenerateInputStream, null, options);
                Utils.closeSilently(inputStreamRegenerateInputStream);
                return bitmapDecodeStream;
            } catch (FileNotFoundException | OutOfMemoryError e) {
                Log.e("BitmapRegionTileSource", "Failed to load URI " + this.mUri, e);
                return null;
            }
        }

        @Override // com.android.photos.BitmapRegionTileSource.BitmapSource
        public boolean readExif(ExifInterface exifInterface) {
            InputStream inputStreamRegenerateInputStream = null;
            try {
                inputStreamRegenerateInputStream = regenerateInputStream();
                exifInterface.readExif(inputStreamRegenerateInputStream);
                Utils.closeSilently(inputStreamRegenerateInputStream);
                return true;
            } catch (IOException e) { /* REWORK: FileNotFoundException is subsumed */
                Log.d("BitmapRegionTileSource", "Failed to load URI " + this.mUri, e);
                return false;
            } catch (NullPointerException e2) {
                Log.d("BitmapRegionTileSource", "Failed to read EXIF for URI " + this.mUri, e2);
                return false;
            } finally {
                Utils.closeSilently(inputStreamRegenerateInputStream);
            }
        }
    }

    public BitmapRegionTileSource(Context context, BitmapSource bitmapSource, byte[] bArr) {
        this.mTileSize = TiledImageRenderer.suggestedTileSize(context);
        this.mRotation = bitmapSource.getRotation();
        this.mDecoder = bitmapSource.getBitmapRegionDecoder();
        SimpleBitmapRegionDecoder simpleBitmapRegionDecoder = this.mDecoder;
        if (simpleBitmapRegionDecoder != null) {
            this.mWidth = simpleBitmapRegionDecoder.getWidth();
            this.mHeight = this.mDecoder.getHeight();
            this.mOptions = new BitmapFactory.Options();
            this.mOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
            BitmapFactory.Options options = this.mOptions;
            options.inPreferQualityOverSpeed = true;
            options.inTempStorage = bArr;
            Bitmap previewBitmap = bitmapSource.getPreviewBitmap();
            if (previewBitmap != null && previewBitmap.getWidth() <= 2048 && previewBitmap.getHeight() <= 2048) {
                this.mPreview = new BitmapTexture(previewBitmap);
                return;
            }
            Object[] objArr = new Object[4];
            objArr[0] = Integer.valueOf(this.mWidth);
            objArr[1] = Integer.valueOf(this.mHeight);
            objArr[2] = Integer.valueOf(previewBitmap == null ? -1 : previewBitmap.getWidth());
            objArr[3] = Integer.valueOf(previewBitmap != null ? previewBitmap.getHeight() : -1);
            Log.w("BitmapRegionTileSource", String.format("Failed to create preview of apropriate size!  in: %dx%d, out: %dx%d", objArr));
        }
    }

    public Bitmap getBitmap() {
        BasicTexture basicTexture = this.mPreview;
        if (basicTexture instanceof BitmapTexture) {
            return ((BitmapTexture) basicTexture).getBitmap();
        }
        return null;
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public int getImageHeight() {
        return this.mHeight;
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public int getImageWidth() {
        return this.mWidth;
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public BasicTexture getPreview() {
        return this.mPreview;
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public int getRotation() {
        return this.mRotation;
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public Bitmap getTile(int i, int i2, int i3, Bitmap bitmap) {
        int tileSize = getTileSize();
        int i4 = tileSize << i;
        this.mWantRegion.set(i2, i3, i2 + i4, i4 + i3);
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        }
        BitmapFactory.Options options = this.mOptions;
        options.inSampleSize = 1 << i;
        options.inBitmap = bitmap;
        try {
            Bitmap bitmapDecodeRegion = this.mDecoder.decodeRegion(this.mWantRegion, options);
            if (this.mOptions.inBitmap != bitmapDecodeRegion && this.mOptions.inBitmap != null) {
                this.mOptions.inBitmap = null;
            }
            if (bitmapDecodeRegion == null) {
                Log.w("BitmapRegionTileSource", "fail in decoding region");
            }
            return bitmapDecodeRegion;
        } catch (Throwable th) {
            if (this.mOptions.inBitmap != bitmap && this.mOptions.inBitmap != null) {
                this.mOptions.inBitmap = null;
            }
            throw th;
        }
    }

    @Override // com.android.photos.views.TiledImageRenderer.TileSource
    public int getTileSize() {
        return this.mTileSize;
    }
}
