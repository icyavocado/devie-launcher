package com.voxel.launcher3;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Toast;
import com.android.gallery3d.common.BitmapCropTask;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.photos.BitmapRegionTileSource;
import com.android.photos.views.TiledImageRenderer;
import com.voxel.launcher3.base.BaseActivity;
import com.voxel.launcher3.util.WallpaperUtils;
import com.ovacoda.devie.R;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/* JADX INFO: loaded from: classes.dex */
public class WallpaperCropActivity extends BaseActivity implements Handler.Callback {
    protected CropView mCropView;
    LoadRequest mCurrentLoadRequest;
    private Handler mLoaderHandler;
    private HandlerThread mLoaderThread;
    protected View mProgressView;
    protected View mSetWallpaperButton;
    private byte[] mTempStorageForDecoding = new byte[16384];
    Set<Bitmap> mReusableBitmaps = Collections.newSetFromMap(new WeakHashMap());
    private final DialogInterface.OnCancelListener mOnDialogCancelListener = new DialogInterface.OnCancelListener() { // from class: com.voxel.launcher3.-$$Lambda$WallpaperCropActivity$8hk3JIXTSE7nqmF4Sy8M5x6FulU
        @Override // android.content.DialogInterface.OnCancelListener
        public final void onCancel(DialogInterface dialogInterface) {
            WallpaperCropActivity.this.lambda$new$0( dialogInterface);
        }
    };

    public static class CropViewScaleAndOffsetProvider {
        public float getParallaxOffset() {
            return 0.5f;
        }

        public float getScale(Point point, RectF rectF) {
            return 1.0f;
        }

        public void updateCropView(WallpaperCropActivity wallpaperCropActivity, TiledImageRenderer.TileSource tileSource) {
            Point defaultWallpaperSize = WallpaperUtils.getDefaultWallpaperSize(wallpaperCropActivity.getResources(), wallpaperCropActivity.getWindowManager());
            RectF maxCropRect = Utils.getMaxCropRect(tileSource.getImageWidth(), tileSource.getImageHeight(), defaultWallpaperSize.x, defaultWallpaperSize.y, false);
            float scale = getScale(defaultWallpaperSize, maxCropRect);
            PointF center = wallpaperCropActivity.mCropView.getCenter();
            float fMax = Math.max(0.0f, Math.min(getParallaxOffset(), 1.0f));
            float width = wallpaperCropActivity.mCropView.getWidth() / scale;
            center.x = (width / 2.0f) + (fMax * (maxCropRect.width() - width)) + maxCropRect.left;
            wallpaperCropActivity.mCropView.setScaleAndCenter(scale, center.x, center.y);
        }
    }

    static class LoadRequest {
        boolean moveToLeft;
        Runnable postExecute;
        TiledImageRenderer.TileSource result;
        CropViewScaleAndOffsetProvider scaleAndOffsetProvider;
        BitmapRegionTileSource.BitmapSource src;
        boolean touchEnabled;

        LoadRequest() {
        }
    }

    public void lambda$cropImageAndSetWallpaper$7(boolean z, boolean z2, boolean z3) {
        this.updateWallpaperDimensions(0, 0);
        if (z) {
            this.setResult(-1);
            this.finish();
            if (z3 && z2) {
                this.overridePendingTransition(0, R.anim.fade_out);
            }
        }
    }

    public void lambda$cropImageAndSetWallpaper$8(int i, int i2, boolean z, boolean z2, boolean z3) {
        this.updateWallpaperDimensions(i, i2);
        if (z) {
            this.setResult(-1);
            this.finish();
        }
        if (z3 && z2) {
            this.overridePendingTransition(0, R.anim.fade_out);
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public Bitmap lambda$handleMessage$3(int i) {
        Bitmap bitmap;
        synchronized (this.mReusableBitmaps) {
            int i2 = Integer.MAX_VALUE;
            bitmap = null;
            for (Bitmap bitmap2 : this.mReusableBitmaps) {
                int width = bitmap2.getWidth() * bitmap2.getHeight();
                if (width >= i && width < i2) {
                    bitmap = bitmap2;
                    i2 = width;
                }
            }
            if (bitmap != null) {
                this.mReusableBitmaps.remove(bitmap);
            }
        }
        return bitmap;
    }

    public void lambda$handleMessage$4(LoadRequest loadRequest) {
        if (loadRequest == this.mCurrentLoadRequest) {
            this.onLoadRequestComplete(loadRequest, loadRequest.src.getLoadingState() == BitmapRegionTileSource.BitmapSource.State.LOADED);
        } else {
            this.addReusableBitmap(loadRequest.result);
        }
    }

    public void lambda$init$1(ActionBar actionBar, Uri uri, View view) {
        actionBar.hide();
        this.cropImageAndSetWallpaper(uri, (BitmapCropTask.OnBitmapCroppedHandler) null, true, false);
    }

    public void lambda$init$2(BitmapRegionTileSource.UriBitmapSource uriBitmapSource) {
        if (uriBitmapSource.getLoadingState() == BitmapRegionTileSource.BitmapSource.State.LOADED) {
            this.mSetWallpaperButton.setEnabled(true);
        } else {
            Toast.makeText(this.getContext(), R.string.wallpaper_load_fail, 1).show();
            this.finish();
        }
    }

    public void lambda$new$0(DialogInterface dialogInterface) {
        this.getActionBar().show();
        View viewFindViewById = this.findViewById(R.id.wallpaper_strip);
        if (viewFindViewById != null) {
            viewFindViewById.setVisibility(0);
        }
    }

    public void lambda$setCropViewTileSource$5(LoadRequest loadRequest) {
        if (this.mCurrentLoadRequest == loadRequest) {
            this.mProgressView.setVisibility(0);
        }
    }

    public void lambda$setWallpaper$6(Point point, boolean z, boolean z2, boolean z3) {
        this.updateWallpaperDimensions(point.x, point.y);
        if (z) {
            this.setResult(-1);
            this.finish();
            if (z3 && z2) {
                this.overridePendingTransition(0, R.anim.fade_out);
            }
        }
    }

    void addReusableBitmap(TiledImageRenderer.TileSource tileSource) {
        Bitmap bitmap;
        synchronized (this.mReusableBitmaps) {
            if (Utilities.ATLEAST_KITKAT && (tileSource instanceof BitmapRegionTileSource) && (bitmap = ((BitmapRegionTileSource) tileSource).getBitmap()) != null && bitmap.isMutable()) {
                this.mReusableBitmaps.add(bitmap);
            }
        }
    }

    protected void cropImageAndSetWallpaper(Resources resources, int i, final boolean z, final boolean z2) {
        int rotationFromExif = BitmapUtils.getRotationFromExif(resources, i);
        Point sourceDimensions = this.mCropView.getSourceDimensions();
        Point defaultWallpaperSize = WallpaperUtils.getDefaultWallpaperSize(getResources(), getWindowManager());
        NycWallpaperUtils.executeCropTaskAfterPrompt(this, new BitmapCropTask(getContext(), resources, i, Utils.getMaxCropRect(sourceDimensions.x, sourceDimensions.y, defaultWallpaperSize.x, defaultWallpaperSize.y, false), rotationFromExif, defaultWallpaperSize.x, defaultWallpaperSize.y, true, false, new BitmapCropTask.OnEndCropHandler() { // from class: com.voxel.launcher3.-$$Lambda$WallpaperCropActivity$l0nNsLECiZGr0zCeKLglgEZ6v9o
            @Override // com.android.gallery3d.common.BitmapCropTask.OnEndCropHandler
            public final void run(boolean z3) {
                WallpaperCropActivity.this.lambda$cropImageAndSetWallpaper$7( z, z2, z3);
            }
        }), getOnDialogCancelListener());
    }

    @TargetApi(17)
    protected void cropImageAndSetWallpaper(Uri uri, BitmapCropTask.OnBitmapCroppedHandler onBitmapCroppedHandler, final boolean z, final boolean z2) {
        float fMin;
        this.mProgressView.setVisibility(0);
        boolean z3 = getResources().getBoolean(R.bool.center_crop);
        boolean z4 = this.mCropView.getLayoutDirection() == 0;
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        boolean z5 = point.x < point.y;
        Point defaultWallpaperSize = WallpaperUtils.getDefaultWallpaperSize(getResources(), getWindowManager());
        RectF crop = this.mCropView.getCrop();
        Point sourceDimensions = this.mCropView.getSourceDimensions();
        int imageRotation = this.mCropView.getImageRotation();
        float width = this.mCropView.getWidth() / crop.width();
        Matrix matrix = new Matrix();
        matrix.setRotate(imageRotation);
        float[] fArr = {sourceDimensions.x, sourceDimensions.y};
        matrix.mapPoints(fArr);
        fArr[0] = Math.abs(fArr[0]);
        fArr[1] = Math.abs(fArr[1]);
        crop.left = Math.max(0.0f, crop.left);
        crop.right = Math.min(fArr[0], crop.right);
        crop.top = Math.max(0.0f, crop.top);
        crop.bottom = Math.min(fArr[1], crop.bottom);
        if (z3) {
            fMin = Math.min(fArr[0] - crop.right, crop.left) * 2.0f;
        } else {
            fMin = z4 ? fArr[0] - crop.right : crop.left;
        }
        float fMin2 = Math.min(fMin, (defaultWallpaperSize.x / width) - crop.width());
        if (z3) {
            float f = fMin2 / 2.0f;
            crop.left -= f;
            crop.right += f;
        } else if (z4) {
            crop.right += fMin2;
        } else {
            crop.left -= fMin2;
        }
        if (z5) {
            crop.bottom = crop.top + (defaultWallpaperSize.y / width);
        } else {
            float fMin3 = Math.min(Math.min(fArr[1] - crop.bottom, crop.top), ((defaultWallpaperSize.y / width) - crop.height()) / 2.0f);
            crop.top -= fMin3;
            crop.bottom += fMin3;
        }
        final int iRound = Math.round(crop.width() * width);
        final int iRound2 = Math.round(crop.height() * width);
        BitmapCropTask bitmapCropTask = new BitmapCropTask(getContext(), uri, crop, imageRotation, iRound, iRound2, true, false, new BitmapCropTask.OnEndCropHandler() { // from class: com.voxel.launcher3.-$$Lambda$WallpaperCropActivity$7njoxc4A1MtZuif8P5B-I9l5DOk
            @Override // com.android.gallery3d.common.BitmapCropTask.OnEndCropHandler
            public final void run(boolean z6) {
                WallpaperCropActivity.this.lambda$cropImageAndSetWallpaper$8( iRound, iRound2, z, z2, z6);
            }
        });
        if (onBitmapCroppedHandler != null) {
            bitmapCropTask.setOnBitmapCropped(onBitmapCroppedHandler);
        }
        NycWallpaperUtils.executeCropTaskAfterPrompt(this, bitmapCropTask, getOnDialogCancelListener());
    }

    public boolean enableRotation() {
        return getResources().getBoolean(R.bool.allow_rotation);
    }

    public DialogInterface.OnCancelListener getOnDialogCancelListener() {
        return this.mOnDialogCancelListener;
    }

    @Override // android.os.Handler.Callback
    public boolean handleMessage(Message message) {
        if (message.what != 1) {
            return false;
        }
        final LoadRequest loadRequest = (LoadRequest) message.obj;
        try {
            loadRequest.src.loadInBackground(new BitmapRegionTileSource.BitmapSource.InBitmapProvider() { // from class: com.voxel.launcher3.-$$Lambda$WallpaperCropActivity$dmopjmxGYU2fI6v_aP6yiJU_400
                @Override // com.android.photos.BitmapRegionTileSource.BitmapSource.InBitmapProvider
                public final Bitmap forPixelCount(int i) {
                    return WallpaperCropActivity.this.lambda$handleMessage$3( i);
                }
            });
            loadRequest.result = new BitmapRegionTileSource(getContext(), loadRequest.src, this.mTempStorageForDecoding);
            runOnUiThread(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$WallpaperCropActivity$ObVGik7LJHDntanQPdeXiLdzvtc
                @Override // java.lang.Runnable
                public final void run() {
                    WallpaperCropActivity.this.lambda$handleMessage$4( loadRequest);
                }
            });
            return true;
        } catch (SecurityException e) {
            if (isActivityDestroyed()) {
                return true;
            }
            throw e;
        }
    }

    protected void init() {
        setContentView(R.layout.wallpaper_cropper);
        this.mCropView = (CropView) findViewById(R.id.cropView);
        this.mProgressView = findViewById(R.id.loading);
        final Uri data = getIntent().getData();
        if (data == null) {
            Log.e("Launcher3.CropActivity", "No URI passed in intent, exiting WallpaperCropActivity");
            finish();
            return;
        }
        final ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.actionbar_set_wallpaper);
        actionBar.getCustomView().setOnClickListener(new View.OnClickListener() { // from class: com.voxel.launcher3.-$$Lambda$WallpaperCropActivity$Xd-mPfSmAd0JINazc8Pltff1HKo
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WallpaperCropActivity.this.lambda$init$1( actionBar, data, view);
            }
        });
        this.mSetWallpaperButton = findViewById(R.id.set_wallpaper_button);
        final BitmapRegionTileSource.UriBitmapSource uriBitmapSource = new BitmapRegionTileSource.UriBitmapSource(getContext(), data);
        this.mSetWallpaperButton.setEnabled(false);
        setCropViewTileSource(uriBitmapSource, true, false, null, new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$WallpaperCropActivity$dkkLoDe8H66Yt2fVIKRZVWly2k8
            @Override // java.lang.Runnable
            public final void run() {
                WallpaperCropActivity.this.lambda$init$2( uriBitmapSource);
            }
        });
    }

    @TargetApi(17)
    protected boolean isActivityDestroyed() {
        return Utilities.ATLEAST_JB_MR1 && isDestroyed();
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mLoaderThread = new HandlerThread("wallpaper_loader");
        this.mLoaderThread.start();
        this.mLoaderHandler = new Handler(this.mLoaderThread.getLooper(), this);
        init();
        if (enableRotation()) {
            return;
        }
        setRequestedOrientation(1);
    }

    @Override // android.app.Activity
    public void onDestroy() {
        CropView cropView = this.mCropView;
        if (cropView != null) {
            cropView.destroy();
        }
        HandlerThread handlerThread = this.mLoaderThread;
        if (handlerThread != null) {
            handlerThread.quit();
        }
        super.onDestroy();
    }

    protected void onLoadRequestComplete(LoadRequest loadRequest, boolean z) {
        this.mCurrentLoadRequest = null;
        if (z) {
            TiledImageRenderer.TileSource tileSource = this.mCropView.getTileSource();
            this.mCropView.setTileSource(loadRequest.result, null);
            this.mCropView.setTouchEnabled(loadRequest.touchEnabled);
            if (loadRequest.moveToLeft) {
                this.mCropView.moveToLeft();
            }
            if (loadRequest.scaleAndOffsetProvider != null) {
                loadRequest.scaleAndOffsetProvider.updateCropView(this, loadRequest.result);
            }
            if (tileSource != null) {
                tileSource.getPreview().yield();
            }
            addReusableBitmap(tileSource);
        }
        if (loadRequest.postExecute != null) {
            loadRequest.postExecute.run();
        }
        this.mProgressView.setVisibility(8);
    }

    public final void setCropViewTileSource(BitmapRegionTileSource.BitmapSource bitmapSource, boolean z, boolean z2, CropViewScaleAndOffsetProvider cropViewScaleAndOffsetProvider, Runnable runnable) {
        final LoadRequest loadRequest = new LoadRequest();
        loadRequest.moveToLeft = z2;
        loadRequest.src = bitmapSource;
        loadRequest.touchEnabled = z;
        loadRequest.postExecute = runnable;
        loadRequest.scaleAndOffsetProvider = cropViewScaleAndOffsetProvider;
        this.mCurrentLoadRequest = loadRequest;
        this.mLoaderHandler.removeMessages(1);
        Message.obtain(this.mLoaderHandler, 1, loadRequest).sendToTarget();
        this.mProgressView.postDelayed(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$WallpaperCropActivity$YcgonMuHkdeUBQqI1HlxrQs4Avk
            @Override // java.lang.Runnable
            public final void run() {
                WallpaperCropActivity.this.lambda$setCropViewTileSource$5( loadRequest);
            }
        }, 1000L);
    }

    protected void setWallpaper(Uri uri, final boolean z, final boolean z2) {
        BitmapCropTask bitmapCropTask = new BitmapCropTask(getContext(), uri, null, BitmapUtils.getRotationFromExif(getContext(), uri), 0, 0, true, false, null);
        final Point imageBounds = bitmapCropTask.getImageBounds();
        bitmapCropTask.setOnEndRunnable(new BitmapCropTask.OnEndCropHandler() { // from class: com.voxel.launcher3.-$$Lambda$WallpaperCropActivity$IOaPui0W_4Cya9x-Jws7fyWWAvI
            @Override // com.android.gallery3d.common.BitmapCropTask.OnEndCropHandler
            public final void run(boolean z3) {
                WallpaperCropActivity.this.lambda$setWallpaper$6( imageBounds, z, z2, z3);
            }
        });
        bitmapCropTask.setNoCrop(true);
        NycWallpaperUtils.executeCropTaskAfterPrompt(this, bitmapCropTask, getOnDialogCancelListener());
    }

    protected void updateWallpaperDimensions(int i, int i2) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("com.voxel.launcher3.WallpaperCropActivity", 4);
        SharedPreferences.Editor editorEdit = sharedPreferences.edit();
        if (i == 0 || i2 == 0) {
            editorEdit.remove("wallpaper.width");
            editorEdit.remove("wallpaper.height");
        } else {
            editorEdit.putInt("wallpaper.width", i);
            editorEdit.putInt("wallpaper.height", i2);
        }
        editorEdit.apply();
        WallpaperUtils.suggestWallpaperDimension(getResources(), sharedPreferences, getWindowManager(), WallpaperManager.getInstance(getApplicationContext()), true);
    }
}
