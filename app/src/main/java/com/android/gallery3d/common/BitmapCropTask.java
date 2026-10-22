package com.android.gallery3d.common;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.voxel.launcher3.NycWallpaperUtils;
import com.voxel.launcher3.Utilities;
import com.ovacoda.devie.R;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/* JADX INFO: loaded from: classes.dex */
public class BitmapCropTask extends AsyncTask<Integer, Void, Boolean> {
    Context mContext;
    RectF mCropBounds;
    Bitmap mCroppedBitmap;
    String mInFilePath;
    byte[] mInImageBytes;
    int mInResId;
    Uri mInUri;
    boolean mNoCrop;
    OnBitmapCroppedHandler mOnBitmapCroppedHandler;
    OnEndCropHandler mOnEndCropHandler;
    int mOutHeight;
    int mOutWidth;
    Resources mResources;
    int mRotation;
    boolean mSaveCroppedBitmap;
    boolean mSetWallpaper;

    public interface OnBitmapCroppedHandler {
        void onBitmapCropped(byte[] bArr, Rect rect);
    }

    public interface OnEndCropHandler {
        void run(boolean z);
    }

    public BitmapCropTask(Context context, Resources resources, int i, RectF rectF, int i2, int i3, int i4, boolean z, boolean z2, OnEndCropHandler onEndCropHandler) {
        this.mInUri = null;
        this.mInResId = 0;
        this.mCropBounds = null;
        this.mContext = context;
        this.mInResId = i;
        this.mResources = resources;
        init(rectF, i2, i3, i4, z, z2, onEndCropHandler);
    }

    public BitmapCropTask(Context context, Uri uri, RectF rectF, int i, int i2, int i3, boolean z, boolean z2, OnEndCropHandler onEndCropHandler) {
        this.mInUri = null;
        this.mInResId = 0;
        this.mCropBounds = null;
        this.mContext = context;
        this.mInUri = uri;
        init(rectF, i, i2, i3, z, z2, onEndCropHandler);
    }

    public BitmapCropTask(byte[] bArr, RectF rectF, int i, int i2, int i3, boolean z, boolean z2, OnEndCropHandler onEndCropHandler) {
        this.mInUri = null;
        this.mInResId = 0;
        this.mCropBounds = null;
        this.mInImageBytes = bArr;
        init(rectF, i, i2, i3, z, z2, onEndCropHandler);
    }

    private void init(RectF rectF, int i, int i2, int i3, boolean z, boolean z2, OnEndCropHandler onEndCropHandler) {
        this.mCropBounds = rectF;
        this.mRotation = i;
        this.mOutWidth = i2;
        this.mOutHeight = i3;
        this.mSetWallpaper = z;
        this.mSaveCroppedBitmap = z2;
        this.mOnEndCropHandler = onEndCropHandler;
    }

    private InputStream regenerateInputStream() {
        if (this.mInUri == null && this.mInResId == 0 && this.mInFilePath == null && this.mInImageBytes == null) {
            Log.w("BitmapCropTask", "cannot read original file, no input URI, resource ID, or image byte array given");
            return null;
        }
        try {
            if (this.mInUri != null) {
                return new BufferedInputStream(this.mContext.getContentResolver().openInputStream(this.mInUri));
            }
            if (this.mInFilePath != null) {
                return this.mContext.openFileInput(this.mInFilePath);
            }
            return this.mInImageBytes != null ? new BufferedInputStream(new ByteArrayInputStream(this.mInImageBytes)) : new BufferedInputStream(this.mResources.openRawResource(this.mInResId));
        } catch (FileNotFoundException e) {
            Log.w("BitmapCropTask", "cannot read file: " + this.mInUri.toString(), e);
            return null;
        }
    }

    private void setWallpaper(InputStream inputStream, Rect rect, int i) throws IOException {
        if (Utilities.ATLEAST_N) {
            NycWallpaperUtils.setStream(this.mContext, inputStream, rect, true, i);
        } else {
            WallpaperManager.getInstance(this.mContext.getApplicationContext()).setStream(inputStream);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r12v0, types: [int] */
    /* JADX WARN: Type inference failed for: r12v1 */
    /* JADX WARN: Type inference failed for: r12v3, types: [java.io.Closeable] */
    /* JADX WARN: Type inference failed for: r9v3, types: [int] */
    public boolean cropBitmap(int i) { /* REWORK: every failure path is caught inside */
        /* REWORK: jadx again dropped the exception-flow locals and reintroduced a
         * register ("r12") through Utils.closeSilently; reimplemented with a plain
         * try/catch/finally per stage. */
        boolean z = false;
        if (this.mSetWallpaper && this.mNoCrop) {
            InputStream in = null;
            try {
                in = regenerateInputStream();
                setWallpaper(in, null, i);
            } catch (IOException e) {
                Log.w("BitmapCropTask", "cannot write stream to wallpaper", e);
                z = true;
            } finally {
                Utils.closeSilently(in);
            }
            return !z;
        }
        if (this.mSetWallpaper && Utilities.ATLEAST_N && this.mRotation == 0 && this.mOutWidth > 0 && this.mOutHeight > 0) {
            Rect rect = new Rect();
            this.mCropBounds.roundOut(rect);
            InputStream in = null;
            try {
                in = regenerateInputStream();
                if (in == null) {
                    Log.w("BitmapCropTask", "cannot get input stream for uri=" + this.mInUri.toString());
                    return false;
                }
                WallpaperManager.getInstance(this.mContext.getApplicationContext()).suggestDesiredDimensions(this.mOutWidth, this.mOutHeight);
                setWallpaper(in, rect, i);
                if (this.mOnBitmapCroppedHandler != null) {
                    this.mOnBitmapCroppedHandler.onBitmapCropped(null, rect);
                }
            } catch (IOException e2) {
                Log.w("BitmapCropTask", "cannot open region decoder for file: " + this.mInUri.toString(), e2);
            } finally {
                Utils.closeSilently(in);
            }
            return !z;
        }
        Rect rect2 = new Rect();
        Matrix matrix = new Matrix();
        Matrix matrix2 = new Matrix();
        Point imageBounds = getImageBounds();
        int i2 = this.mRotation;
        if (i2 > 0) {
            matrix.setRotate(i2);
            matrix2.setRotate(-this.mRotation);
            this.mCropBounds.roundOut(rect2);
            this.mCropBounds = new RectF(rect2);
            if (imageBounds == null) {
                Log.w("BitmapCropTask", "cannot get bounds for image");
                return false;
            }
            float[] fArr = {imageBounds.x, imageBounds.y};
            matrix.mapPoints(fArr);
            fArr[0] = Math.abs(fArr[0]);
            fArr[1] = Math.abs(fArr[1]);
            this.mCropBounds.offset((-fArr[0]) / 2.0f, (-fArr[1]) / 2.0f);
            matrix2.mapRect(this.mCropBounds);
            this.mCropBounds.offset(imageBounds.x / 2, imageBounds.y / 2);
        }
        this.mCropBounds.roundOut(rect2);
        if (rect2.width() <= 0 || rect2.height() <= 0) {
            Log.w("BitmapCropTask", "crop has bad values for full size image");
            return false;
        }
        int iWidth = rect2.width() / this.mOutWidth;
        int iMax = Math.max(1, Math.min(iWidth, rect2.height() / this.mOutHeight));
        InputStream in = null;
        try {
            in = regenerateInputStream();
            if (in == null) {
                Log.w("BitmapCropTask", "cannot get input stream for uri=" + this.mInUri.toString());
                return false;
            }
            BitmapRegionDecoder decoder = null;
            try {
                decoder = BitmapRegionDecoder.newInstance(in, false);
            } catch (IOException e4) {
                Log.w("BitmapCropTask", "cannot open region decoder for file: " + this.mInUri.toString(), e4);
            }
            Utils.closeSilently(in);
            Bitmap bitmapCreateBitmap = decoder != null ? decoder.decodeRegion(rect2, this.optionsFor(iMax)) : null;
            if (decoder != null) {
                decoder.recycle();
            }
            if (bitmapCreateBitmap == null) {
                InputStream in2 = regenerateInputStream();
                if (in2 == null) {
                    Log.w("BitmapCropTask", "cannot get input stream for uri=" + this.mInUri.toString());
                    return false;
                }
                try {
                    bitmapCreateBitmap = BitmapFactory.decodeStream(in2, null, this.optionsFor(iMax));
                } finally {
                    Utils.closeSilently(in2);
                }
            }
            if (bitmapCreateBitmap != null) {
                int width = imageBounds.x / bitmapCreateBitmap.getWidth();
                float f = width;
                this.mCropBounds.left /= f;
                this.mCropBounds.top /= f;
                this.mCropBounds.bottom /= f;
                this.mCropBounds.right /= f;
                this.mCropBounds.roundOut(rect2);
                if (rect2.width() > bitmapCreateBitmap.getWidth()) {
                    rect2.right = rect2.left + bitmapCreateBitmap.getWidth();
                }
                if (rect2.right > bitmapCreateBitmap.getWidth()) {
                    rect2.offset(-(rect2.right - bitmapCreateBitmap.getWidth()), 0);
                }
                if (rect2.height() > bitmapCreateBitmap.getHeight()) {
                    rect2.bottom = rect2.top + bitmapCreateBitmap.getHeight();
                }
                if (rect2.bottom > bitmapCreateBitmap.getHeight()) {
                    rect2.offset(0, -(rect2.bottom - bitmapCreateBitmap.getHeight()));
                }
                bitmapCreateBitmap = Bitmap.createBitmap(bitmapCreateBitmap, rect2.left, rect2.top, rect2.width(), rect2.height());
            }
            if (bitmapCreateBitmap == null) {
                Log.w("BitmapCropTask", "cannot decode file: " + this.mInUri.toString());
                return false;
            }
            if ((this.mOutWidth > 0 && this.mOutHeight > 0) || this.mRotation > 0) {
                float[] fArr2 = {bitmapCreateBitmap.getWidth(), bitmapCreateBitmap.getHeight()};
                matrix.mapPoints(fArr2);
                fArr2[0] = Math.abs(fArr2[0]);
                fArr2[1] = Math.abs(fArr2[1]);
                if (this.mOutWidth <= 0 || this.mOutHeight <= 0) {
                    this.mOutWidth = Math.round(fArr2[0]);
                    this.mOutHeight = Math.round(fArr2[1]);
                }
                RectF rectF = new RectF(0.0f, 0.0f, fArr2[0], fArr2[1]);
                RectF rectF2 = new RectF(0.0f, 0.0f, this.mOutWidth, this.mOutHeight);
                Matrix matrix3 = new Matrix();
                if (this.mRotation == 0) {
                    matrix3.setRectToRect(rectF, rectF2, Matrix.ScaleToFit.FILL);
                } else {
                    Matrix matrix4 = new Matrix();
                    matrix4.setTranslate((-bitmapCreateBitmap.getWidth()) / 2.0f, (-bitmapCreateBitmap.getHeight()) / 2.0f);
                    Matrix matrix5 = new Matrix();
                    matrix5.setRotate(this.mRotation);
                    Matrix matrix6 = new Matrix();
                    matrix6.setTranslate(fArr2[0] / 2.0f, fArr2[1] / 2.0f);
                    Matrix matrix7 = new Matrix();
                    matrix7.setRectToRect(rectF, rectF2, Matrix.ScaleToFit.FILL);
                    Matrix matrix8 = new Matrix();
                    matrix8.setConcat(matrix5, matrix4);
                    Matrix matrix9 = new Matrix();
                    matrix9.setConcat(matrix7, matrix6);
                    matrix3.setConcat(matrix9, matrix8);
                }
                Bitmap bitmapCreateBitmap2 = Bitmap.createBitmap((int) rectF2.width(), (int) rectF2.height(), Bitmap.Config.ARGB_8888);
                if (bitmapCreateBitmap2 != null) {
                    Canvas canvas = new Canvas(bitmapCreateBitmap2);
                    Paint paint = new Paint();
                    paint.setFilterBitmap(true);
                    canvas.drawBitmap(bitmapCreateBitmap, matrix3, paint);
                    bitmapCreateBitmap = bitmapCreateBitmap2;
                }
            }
            if (this.mSaveCroppedBitmap) {
                this.mCroppedBitmap = bitmapCreateBitmap;
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(2048);
            if (!bitmapCreateBitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)) {
                Log.w("BitmapCropTask", "cannot compress bitmap");
                z = true;
            } else if (this.mSetWallpaper) {
                try {
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    setWallpaper(new ByteArrayInputStream(byteArray), null, i);
                    if (this.mOnBitmapCroppedHandler != null) {
                        this.mOnBitmapCroppedHandler.onBitmapCropped(byteArray, new Rect(0, 0, bitmapCreateBitmap.getWidth(), bitmapCreateBitmap.getHeight()));
                    }
                } catch (IOException e5) {
                    Log.w("BitmapCropTask", "cannot write stream to wallpaper", e5);
                    z = true;
                }
            }
        } finally {
            Utils.closeSilently(in);
        }
        return !z;
    }

    public BitmapFactory.Options optionsFor(int sampleSize) {
        /* REWORK: helper extracted from cropBitmap's inline option setup. */
        BitmapFactory.Options options = new BitmapFactory.Options();
        if (sampleSize > 1) {
            options.inSampleSize = sampleSize;
        }
        return options;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public Boolean doInBackground(Integer... numArr) {
        return Boolean.valueOf(cropBitmap(numArr.length == 0 ? 1 : numArr[0].intValue()));
    }

    public Bitmap getCroppedBitmap() {
        return this.mCroppedBitmap;
    }

    public Point getImageBounds() {
        InputStream inputStreamRegenerateInputStream = regenerateInputStream();
        if (inputStreamRegenerateInputStream != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStreamRegenerateInputStream, null, options);
            Utils.closeSilently(inputStreamRegenerateInputStream);
            if (options.outWidth != 0 && options.outHeight != 0) {
                return new Point(options.outWidth, options.outHeight);
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public void onPostExecute(Boolean bool) {
        if (!bool.booleanValue()) {
            Toast.makeText(this.mContext, R.string.wallpaper_set_fail, 0).show();
        }
        OnEndCropHandler onEndCropHandler = this.mOnEndCropHandler;
        if (onEndCropHandler != null) {
            onEndCropHandler.run(bool.booleanValue());
        }
    }

    public void setCropBounds(RectF rectF) {
        this.mCropBounds = rectF;
    }

    public void setNoCrop(boolean z) {
        this.mNoCrop = z;
    }

    public void setOnBitmapCropped(OnBitmapCroppedHandler onBitmapCroppedHandler) {
        this.mOnBitmapCroppedHandler = onBitmapCroppedHandler;
    }

    public void setOnEndRunnable(OnEndCropHandler onEndCropHandler) {
        this.mOnEndCropHandler = onEndCropHandler;
    }
}
