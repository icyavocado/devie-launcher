package com.voxel.launcher3;

import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.android.gallery3d.common.BitmapCropTask;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.photos.BitmapRegionTileSource;

import com.ovacoda.devie.R;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: loaded from: classes.dex */
public class WallpaperPickerActivity extends WallpaperCropActivity {
    ActionMode mActionMode;
    ActionMode.Callback mActionModeCallback;
    boolean mIgnoreNextTap;
    View.OnLongClickListener mLongClickListener;
    private SavedWallpaperImages mSavedImages;
    View mSelectedTile;
    View.OnClickListener mThumbnailOnClickListener;
    private float mWallpaperParallaxOffset;
    HorizontalScrollView mWallpaperScrollContainer;
    View mWallpaperStrip;
    LinearLayout mWallpapersView;
    ArrayList<Uri> mTempWallpaperTiles = new ArrayList<>();
    int mSelectedIndex = -1;

    /* JADX INFO: renamed from: com.voxel.launcher3.WallpaperPickerActivity$1, reason: invalid class name */
    class AnonymousClass1 implements CropView.TouchCallback {
        ViewPropertyAnimator mAnim;

        AnonymousClass1() {
        }

        @Override // com.voxel.launcher3.CropView.TouchCallback
        public void onTap() {
            boolean z = WallpaperPickerActivity.this.mIgnoreNextTap;
            WallpaperPickerActivity.this.mIgnoreNextTap = false;
            if (z) {
                return;
            }
            ViewPropertyAnimator viewPropertyAnimator = this.mAnim;
            if (viewPropertyAnimator != null) {
                viewPropertyAnimator.cancel();
            }
            WallpaperPickerActivity.this.mWallpaperStrip.setVisibility(0);
            this.mAnim = WallpaperPickerActivity.this.mWallpaperStrip.animate();
            this.mAnim.alpha(1.0f).setDuration(150L).setInterpolator(new DecelerateInterpolator(0.75f));
            this.mAnim.start();
        }

        @Override // com.voxel.launcher3.CropView.TouchCallback
        public void onTouchDown() {
            ViewPropertyAnimator viewPropertyAnimator = this.mAnim;
            if (viewPropertyAnimator != null) {
                viewPropertyAnimator.cancel();
            }
            if (WallpaperPickerActivity.this.mWallpaperStrip.getAlpha() == 1.0f) {
                WallpaperPickerActivity.this.mIgnoreNextTap = true;
            }
            this.mAnim = WallpaperPickerActivity.this.mWallpaperStrip.animate();
            this.mAnim.alpha(0.0f).setDuration(150L).withEndAction(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$WallpaperPickerActivity$1$UkMjStDwRkqPG1tuD8auFC9GddM
                @Override // java.lang.Runnable
                public final void run() {
                    WallpaperPickerActivity.this.mWallpaperStrip.setVisibility(4);
                }
            });
            this.mAnim.setInterpolator(new AccelerateInterpolator(0.75f));
            this.mAnim.start();
        }

        @Override // com.voxel.launcher3.CropView.TouchCallback
        public void onTouchUp() {
            WallpaperPickerActivity.this.mIgnoreNextTap = false;
        }
    }

    @TargetApi(19)
    public static class DefaultWallpaperInfo extends WallpaperTileInfo {
        public DefaultWallpaperInfo(Drawable drawable) {
            this.mThumb = drawable;
        }

        private BitmapCropTask getDefaultWallpaperCropTask(final WallpaperPickerActivity wallpaperPickerActivity, BitmapCropTask.OnEndCropHandler onEndCropHandler) {
            return new BitmapCropTask(wallpaperPickerActivity, null, null, -1, -1, -1, true, false, onEndCropHandler) { // from class: com.voxel.launcher3.WallpaperPickerActivity.DefaultWallpaperInfo.1
                /* JADX INFO: Access modifiers changed from: protected */
                /* JADX WARN: Can't rename method to resolve collision */
                @Override // com.android.gallery3d.common.BitmapCropTask, android.os.AsyncTask
                public Boolean doInBackground(Integer... numArr) {
                    int iIntValue = numArr[0].intValue();
                    boolean z = true;
                    try {
                        if (iIntValue == 2) {
                            Bitmap bitmap = ((BitmapDrawable) WallpaperManager.getInstance(wallpaperPickerActivity.getApplicationContext()).getBuiltInDrawable()).getBitmap();
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(2048);
                            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)) {
                                NycWallpaperUtils.setStream(wallpaperPickerActivity.getApplicationContext(), new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), null, true, 2);
                            }
                        } else {
                            NycWallpaperUtils.clear(wallpaperPickerActivity, iIntValue);
                        }
                    } catch (IOException e) {
                        Log.e("WallpaperPickerActivity", "Setting wallpaper to default threw exception", e);
                        z = false;
                    } catch (SecurityException e2) {
                        Log.w("WallpaperPickerActivity", "Setting wallpaper to default threw exception", e2);
                    }
                    return Boolean.valueOf(z);
                }
            };
        }

        static /* synthetic */ void lambda$onSave$0(WallpaperPickerActivity wallpaperPickerActivity, boolean z) {
            if (z) {
                wallpaperPickerActivity.setResult(-1);
            }
            wallpaperPickerActivity.finish();
        }

        @Override // com.voxel.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public boolean isNamelessWallpaper() {
            return true;
        }

        @Override // com.voxel.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public boolean isSelectable() {
            return true;
        }

        @Override // com.voxel.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onClick(WallpaperPickerActivity wallpaperPickerActivity) {
            CropView cropView = wallpaperPickerActivity.getCropView();
            Drawable builtInDrawable = WallpaperManager.getInstance(wallpaperPickerActivity.getApplicationContext()).getBuiltInDrawable(cropView.getWidth(), cropView.getHeight(), false, 0.5f, 0.5f);
            if (builtInDrawable == null) {
                Log.w("WallpaperPickerActivity", "Null default wallpaper encountered.");
                cropView.setTileSource(null, null);
                return;
            }
            WallpaperCropActivity.LoadRequest loadRequest = new WallpaperCropActivity.LoadRequest();
            loadRequest.moveToLeft = false;
            loadRequest.touchEnabled = false;
            loadRequest.scaleAndOffsetProvider = new WallpaperCropActivity.CropViewScaleAndOffsetProvider();
            loadRequest.result = new DrawableTileSource(wallpaperPickerActivity.getContext(), builtInDrawable, 1024);
            wallpaperPickerActivity.onLoadRequestComplete(loadRequest, true);
        }

        @Override // com.voxel.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onSave(final WallpaperPickerActivity wallpaperPickerActivity) {
            if (Utilities.ATLEAST_N) {
                NycWallpaperUtils.executeCropTaskAfterPrompt(wallpaperPickerActivity, getDefaultWallpaperCropTask(wallpaperPickerActivity, new BitmapCropTask.OnEndCropHandler() { // from class: com.voxel.launcher3.-$$Lambda$WallpaperPickerActivity$DefaultWallpaperInfo$VKuPb_7OFhF5QgkvTkKxlJMaGKE
                    @Override // com.android.gallery3d.common.BitmapCropTask.OnEndCropHandler
                    public final void run(boolean z) {
                        WallpaperPickerActivity.DefaultWallpaperInfo.lambda$onSave$0(wallpaperPickerActivity, z);
                    }
                }), wallpaperPickerActivity.getOnDialogCancelListener());
                return;
            }
            try {
                WallpaperManager.getInstance(wallpaperPickerActivity.getApplicationContext()).clear();
                wallpaperPickerActivity.setResult(-1);
            } catch (IOException e) {
                Log.e("WallpaperPickerActivity", "Setting wallpaper to default threw exception", e);
            } catch (SecurityException e2) {
                Log.w("WallpaperPickerActivity", "Setting wallpaper to default threw exception", e2);
                wallpaperPickerActivity.setResult(-1);
            }
            wallpaperPickerActivity.finish();
        }
    }

    public static class FileWallpaperInfo extends WallpaperTileInfo {
        protected File mFile;

        public FileWallpaperInfo(File file, Drawable drawable) {
            this.mFile = file;
            this.mThumb = drawable;
        }

        static /* synthetic */ void lambda$onClick$0(BitmapRegionTileSource.UriBitmapSource uriBitmapSource, WallpaperPickerActivity wallpaperPickerActivity) {
            if (uriBitmapSource.getLoadingState() == BitmapRegionTileSource.BitmapSource.State.LOADED) {
                wallpaperPickerActivity.setWallpaperButtonEnabled(true);
            }
        }

        protected WallpaperCropActivity.CropViewScaleAndOffsetProvider getCropViewScaleAndOffsetProvider() {
            return null;
        }

        @Override // com.voxel.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public boolean isNamelessWallpaper() {
            return true;
        }

        @Override // com.voxel.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public boolean isSelectable() {
            return true;
        }

        @Override // com.voxel.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onClick(final WallpaperPickerActivity wallpaperPickerActivity) {
            wallpaperPickerActivity.setWallpaperButtonEnabled(false);
            final BitmapRegionTileSource.UriBitmapSource uriBitmapSource = new BitmapRegionTileSource.UriBitmapSource(wallpaperPickerActivity.getContext(), Uri.fromFile(this.mFile));
            wallpaperPickerActivity.setCropViewTileSource(uriBitmapSource, false, true, getCropViewScaleAndOffsetProvider(), new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$WallpaperPickerActivity$FileWallpaperInfo$oPrIwckNRKGOzRDrfSd86dVYWe0
                @Override // java.lang.Runnable
                public final void run() {
                    WallpaperPickerActivity.FileWallpaperInfo.lambda$onClick$0(uriBitmapSource, wallpaperPickerActivity);
                }
            });
        }

        @Override // com.voxel.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onSave(WallpaperPickerActivity wallpaperPickerActivity) {
            wallpaperPickerActivity.setWallpaper(Uri.fromFile(this.mFile), true, wallpaperPickerActivity.getWallpaperParallaxOffset() == 0.0f);
        }
    }

    public static class PickImageInfo extends WallpaperTileInfo {
        @Override // com.voxel.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onClick(WallpaperPickerActivity wallpaperPickerActivity) {
            Intent intent = new Intent("android.intent.action.GET_CONTENT");
            intent.setType("image/*");
            wallpaperPickerActivity.startActivityForResultSafely(intent, 5);
        }
    }

    public static class ResourceWallpaperInfo extends WallpaperTileInfo {
        private int mResId;
        private Resources mResources;

        public ResourceWallpaperInfo(Resources resources, int i, Drawable drawable) {
            this.mResources = resources;
            this.mResId = i;
            this.mThumb = drawable;
        }

        static /* synthetic */ void lambda$onClick$0(BitmapRegionTileSource.ResourceBitmapSource resourceBitmapSource, WallpaperPickerActivity wallpaperPickerActivity) {
            if (resourceBitmapSource.getLoadingState() == BitmapRegionTileSource.BitmapSource.State.LOADED) {
                wallpaperPickerActivity.setWallpaperButtonEnabled(true);
            }
        }

        @Override // com.voxel.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public boolean isNamelessWallpaper() {
            return true;
        }

        @Override // com.voxel.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public boolean isSelectable() {
            return true;
        }

        @Override // com.voxel.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onClick(final WallpaperPickerActivity wallpaperPickerActivity) {
            wallpaperPickerActivity.setWallpaperButtonEnabled(false);
            final BitmapRegionTileSource.ResourceBitmapSource resourceBitmapSource = new BitmapRegionTileSource.ResourceBitmapSource(this.mResources, this.mResId);
            wallpaperPickerActivity.setCropViewTileSource(resourceBitmapSource, false, false, new WallpaperCropActivity.CropViewScaleAndOffsetProvider() { // from class: com.voxel.launcher3.WallpaperPickerActivity.ResourceWallpaperInfo.1
                @Override // com.voxel.launcher3.WallpaperCropActivity.CropViewScaleAndOffsetProvider
                public float getParallaxOffset() {
                    return wallpaperPickerActivity.getWallpaperParallaxOffset();
                }

                @Override // com.voxel.launcher3.WallpaperCropActivity.CropViewScaleAndOffsetProvider
                public float getScale(Point point, RectF rectF) {
                    return point.x / rectF.width();
                }
            }, new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$WallpaperPickerActivity$ResourceWallpaperInfo$ygAF1T3RUwL_OGef3HMXo5ew-Rw
                @Override // java.lang.Runnable
                public final void run() {
                    WallpaperPickerActivity.ResourceWallpaperInfo.lambda$onClick$0(resourceBitmapSource, wallpaperPickerActivity);
                }
            });
        }

        @Override // com.voxel.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onSave(WallpaperPickerActivity wallpaperPickerActivity) {
            wallpaperPickerActivity.cropImageAndSetWallpaper(this.mResources, this.mResId, true, true);
        }
    }

    private static class SimpleWallpapersAdapter extends ArrayAdapter<WallpaperTileInfo> {
        private final LayoutInflater mLayoutInflater;

        SimpleWallpapersAdapter(Context context, ArrayList<WallpaperTileInfo> arrayList) {
            super(context, R.layout.wallpaper_picker_item, arrayList);
            this.mLayoutInflater = LayoutInflater.from(context);
        }

        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            Drawable drawable = getItem(i).mThumb;
            if (drawable == null) {
                Log.e("WallpaperPickerActivity", "Error decoding thumbnail for wallpaper #" + i);
            }
            return WallpaperPickerActivity.createImageTileView(this.mLayoutInflater, view, viewGroup, drawable);
        }
    }

    public static class UriWallpaperInfo extends WallpaperTileInfo {
        private Uri mUri;

        public UriWallpaperInfo(Uri uri) {
            this.mUri = uri;
        }

        public void lambda$onClick$0(BitmapRegionTileSource.UriBitmapSource uriBitmapSource, WallpaperPickerActivity wallpaperPickerActivity) {
            if (uriBitmapSource.getLoadingState() == BitmapRegionTileSource.BitmapSource.State.LOADED) {
                wallpaperPickerActivity.selectTile(this.mView);
                wallpaperPickerActivity.setWallpaperButtonEnabled(true);
                return;
            }
            ViewGroup viewGroup = (ViewGroup) this.mView.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(this.mView);
                Toast.makeText(wallpaperPickerActivity.getContext(), R.string.image_load_fail, 0).show();
            }
        }

        public void lambda$onSave$1(WallpaperPickerActivity wallpaperPickerActivity, byte[] bArr, Rect rect) {
            Point defaultThumbnailSize = WallpaperPickerActivity.getDefaultThumbnailSize(wallpaperPickerActivity.getResources());
            if (bArr != null) {
                wallpaperPickerActivity.getSavedImages().writeImage(WallpaperPickerActivity.createThumbnail(defaultThumbnailSize, null, null, bArr, null, 0, 0, true), bArr);
                return;
            }
            Bitmap bitmapDecodeRegion = null;
            try {
                Point defaultThumbnailSize2 = WallpaperPickerActivity.getDefaultThumbnailSize(wallpaperPickerActivity.getResources());
                Rect rect2 = new Rect();
                Utils.getMaxCropRect(rect.width(), rect.height(), defaultThumbnailSize2.x, defaultThumbnailSize2.y, false).roundOut(rect2);
                rect2.offset(rect.left, rect.top);
                InputStream inputStreamOpenInputStream = wallpaperPickerActivity.getContentResolver().openInputStream(this.mUri);
                BitmapRegionDecoder bitmapRegionDecoderNewInstance = BitmapRegionDecoder.newInstance(inputStreamOpenInputStream, true);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = rect2.width() / defaultThumbnailSize2.x;
                bitmapDecodeRegion = bitmapRegionDecoderNewInstance.decodeRegion(rect2, options);
                bitmapRegionDecoderNewInstance.recycle();
                Utils.closeSilently(inputStreamOpenInputStream);
                if (bitmapDecodeRegion != null) {
                    bitmapDecodeRegion = Bitmap.createScaledBitmap(bitmapDecodeRegion, defaultThumbnailSize2.x, defaultThumbnailSize2.y, true);
                }
            } catch (IOException unused) {
            }
            PointF center = wallpaperPickerActivity.mCropView.getCenter();
            wallpaperPickerActivity.getSavedImages().writeImage(bitmapDecodeRegion, this.mUri, new Float[]{Float.valueOf(wallpaperPickerActivity.mCropView.getScale()), Float.valueOf(center.x), Float.valueOf(center.y)});
        }

        @Override // com.voxel.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public boolean isNamelessWallpaper() {
            return true;
        }

        @Override // com.voxel.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public boolean isSelectable() {
            return true;
        }

        @Override // com.voxel.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onClick(final WallpaperPickerActivity wallpaperPickerActivity) {
            wallpaperPickerActivity.setWallpaperButtonEnabled(false);
            final BitmapRegionTileSource.UriBitmapSource uriBitmapSource = new BitmapRegionTileSource.UriBitmapSource(wallpaperPickerActivity.getContext(), this.mUri);
            wallpaperPickerActivity.setCropViewTileSource(uriBitmapSource, true, false, null, new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$WallpaperPickerActivity$UriWallpaperInfo$rMUQrRMlP-1SeTqJaAFlK-Rsq2Y
                @Override // java.lang.Runnable
                public final void run() {
                    UriWallpaperInfo.this.lambda$onClick$0( uriBitmapSource, wallpaperPickerActivity);
                }
            });
        }

        @Override // com.voxel.launcher3.WallpaperPickerActivity.WallpaperTileInfo
        public void onSave(final WallpaperPickerActivity wallpaperPickerActivity) {
            wallpaperPickerActivity.cropImageAndSetWallpaper(this.mUri, new BitmapCropTask.OnBitmapCroppedHandler() { // from class: com.voxel.launcher3.-$$Lambda$WallpaperPickerActivity$UriWallpaperInfo$p-2yE8VWio8YcAFiWumi51I_8Sg
                @Override // com.android.gallery3d.common.BitmapCropTask.OnBitmapCroppedHandler
                public final void onBitmapCropped(byte[] bArr, Rect rect) {
                    UriWallpaperInfo.this.lambda$onSave$1( wallpaperPickerActivity, bArr, rect);
                }
            }, true, wallpaperPickerActivity.getWallpaperParallaxOffset() == 0.0f);
        }
    }

    public static abstract class WallpaperTileInfo {
        public Drawable mThumb;
        protected View mView;

        public boolean isNamelessWallpaper() {
            return false;
        }

        public boolean isSelectable() {
            return false;
        }

        public void onClick(WallpaperPickerActivity wallpaperPickerActivity) {
        }

        public void onDelete(WallpaperPickerActivity wallpaperPickerActivity) {
        }

        public void onIndexUpdated(CharSequence charSequence) {
            if (isNamelessWallpaper()) {
                this.mView.setContentDescription(charSequence);
            }
        }

        public void onSave(WallpaperPickerActivity wallpaperPickerActivity) {
        }

        public void setView(View view) {
            this.mView = view;
        }
    }

    private void addLongPressHandler(View view) {
        view.setOnLongClickListener(this.mLongClickListener);
    }

    /* JADX WARN: Type inference failed for: r1v10, types: [com.voxel.launcher3.WallpaperPickerActivity$6] */
    private void addTemporaryWallpaperTile(final Uri uri, boolean z) {
        FrameLayout frameLayout;
        FrameLayout frameLayout2;
        int i = 0;
        while (true) {
            if (i >= this.mWallpapersView.getChildCount()) {
                frameLayout = null;
                break;
            }
            frameLayout = (FrameLayout) this.mWallpapersView.getChildAt(i);
            Object tag = frameLayout.getTag();
            if ((tag instanceof UriWallpaperInfo) && ((UriWallpaperInfo) tag).mUri.equals(uri)) {
                break;
            } else {
                i++;
            }
        }
        if (frameLayout != null) {
            this.mWallpapersView.removeViewAt(i);
            this.mWallpapersView.addView(frameLayout, 0);
            frameLayout2 = frameLayout;
        } else {
            FrameLayout frameLayout3 = (FrameLayout) getLayoutInflater().inflate(R.layout.wallpaper_picker_item, this.mWallpapersView, false);
            frameLayout3.setVisibility(8);
            this.mWallpapersView.addView(frameLayout3, 0);
            this.mTempWallpaperTiles.add(uri);
            frameLayout2 = frameLayout3;
        }
        final ImageView imageView = (ImageView) frameLayout2.findViewById(R.id.wallpaper_image);
        final Point defaultThumbnailSize = getDefaultThumbnailSize(getResources());
        final Context context = getContext();
        final FrameLayout frameLayout4 = frameLayout2;
        new AsyncTask<Void, Bitmap, Bitmap>() { // from class: com.voxel.launcher3.WallpaperPickerActivity.6
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Bitmap doInBackground(Void... voidArr) {
                try {
                    return WallpaperPickerActivity.createThumbnail(defaultThumbnailSize, context, uri, null, null, 0, BitmapUtils.getRotationFromExif(context, uri), false);
                } catch (SecurityException e) {
                    if (!WallpaperPickerActivity.this.isActivityDestroyed()) {
                        throw e;
                    }
                    cancel(false);
                    return null;
                }
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(Bitmap bitmap) {
                if (!isCancelled() && bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    imageView.getDrawable().setDither(true);
                    frameLayout4.setVisibility(0);
                } else {
                    Log.e("WallpaperPickerActivity", "Error loading thumbnail for uri=" + uri);
                }
            }
        }.execute(new Void[0]);
        UriWallpaperInfo uriWallpaperInfo = new UriWallpaperInfo(uri);
        frameLayout2.setTag(uriWallpaperInfo);
        uriWallpaperInfo.setView(frameLayout2);
        addLongPressHandler(frameLayout2);
        updateTileIndices();
        frameLayout2.setOnClickListener(this.mThumbnailOnClickListener);
        if (z) {
            return;
        }
        this.mThumbnailOnClickListener.onClick(frameLayout2);
    }

    private void addWallpapers(ArrayList<WallpaperTileInfo> arrayList, Resources resources, String str, int i) {
        for (String str2 : resources.getStringArray(i)) {
            int identifier = resources.getIdentifier(str2, "drawable", str);
            if (identifier != 0) {
                int identifier2 = resources.getIdentifier(str2 + "_small", "drawable", str);
                if (identifier2 != 0) {
                    arrayList.add(new ResourceWallpaperInfo(resources, identifier, resources.getDrawable(identifier2)));
                }
            } else {
                Log.e("WallpaperPickerActivity", "Couldn't find wallpaper " + str2);
            }
        }
    }

    public static View createImageTileView(LayoutInflater layoutInflater, View view, ViewGroup viewGroup, Drawable drawable) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.wallpaper_picker_item, viewGroup, false);
        }
        ImageView imageView = (ImageView) view.findViewById(R.id.wallpaper_image);
        if (drawable != null) {
            imageView.setImageDrawable(drawable);
            drawable.setDither(true);
        }
        return view;
    }

    static Bitmap createThumbnail(Point point, Context context, Uri uri, byte[] bArr, Resources resources, int i, int i2, boolean z) {
        BitmapCropTask bitmapCropTask;
        int i3 = point.x;
        int i4 = point.y;
        if (uri != null) {
            bitmapCropTask = new BitmapCropTask(context, uri, null, i2, i3, i4, false, true, null);
        } else {
            bitmapCropTask = bArr != null ? new BitmapCropTask(bArr, null, i2, i3, i4, false, true, null) : new BitmapCropTask(context, resources, i, null, i2, i3, i4, false, true, null);
        }
        Point imageBounds = bitmapCropTask.getImageBounds();
        if (imageBounds == null || imageBounds.x == 0 || imageBounds.y == 0) {
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(i2);
        float[] fArr = {imageBounds.x, imageBounds.y};
        matrix.mapPoints(fArr);
        fArr[0] = Math.abs(fArr[0]);
        fArr[1] = Math.abs(fArr[1]);
        bitmapCropTask.setCropBounds(Utils.getMaxCropRect((int) fArr[0], (int) fArr[1], i3, i4, z));
        if (bitmapCropTask.cropBitmap(1)) {
            return bitmapCropTask.getCroppedBitmap();
        }
        return null;
    }

    private ArrayList<WallpaperTileInfo> findBundledWallpapers() {
        PackageManager packageManager = getContext().getPackageManager();
        ArrayList<WallpaperTileInfo> arrayList = new ArrayList<>(24);
        Partner partner = Partner.get(packageManager);
        if (partner != null) {
            Resources resources = partner.getResources();
            int identifier = resources.getIdentifier("partner_wallpapers", "array", partner.getPackageName());
            if (identifier != 0) {
                addWallpapers(arrayList, resources, partner.getPackageName(), identifier);
            }
            File wallpaperDirectory = partner.getWallpaperDirectory();
            if (wallpaperDirectory != null && wallpaperDirectory.isDirectory()) {
                for (File file : wallpaperDirectory.listFiles()) {
                    if (file.isFile()) {
                        String name = file.getName();
                        int iLastIndexOf = name.lastIndexOf(46);
                        String strSubstring = "";
                        if (iLastIndexOf >= -1) {
                            strSubstring = name.substring(iLastIndexOf);
                            name = name.substring(0, iLastIndexOf);
                        }
                        if (!name.endsWith("_small")) {
                            Bitmap bitmapDecodeFile = BitmapFactory.decodeFile(new File(wallpaperDirectory, name + "_small" + strSubstring).getAbsolutePath());
                            if (bitmapDecodeFile != null) {
                                arrayList.add(new FileWallpaperInfo(file, new BitmapDrawable(bitmapDecodeFile)));
                            }
                        }
                    }
                }
            }
        }
        Pair<ApplicationInfo, Integer> wallpaperArrayResourceId = getWallpaperArrayResourceId();
        if (wallpaperArrayResourceId != null) {
            try {
                addWallpapers(arrayList, getContext().getPackageManager().getResourcesForApplication((ApplicationInfo) wallpaperArrayResourceId.first), ((ApplicationInfo) wallpaperArrayResourceId.first).packageName, ((Integer) wallpaperArrayResourceId.second).intValue());
            } catch (PackageManager.NameNotFoundException unused) {
            }
        }
        if (partner == null || !partner.hideDefaultWallpaper()) {
            WallpaperTileInfo defaultWallpaper = Utilities.ATLEAST_KITKAT ? getDefaultWallpaper() : getPreKKDefaultWallpaperInfo();
            if (defaultWallpaper != null) {
                arrayList.add(0, defaultWallpaper);
            }
        }
        return arrayList;
    }

    private File getDefaultThumbFile() {
        return new File(getContext().getFilesDir(), Build.VERSION.SDK_INT + "-" /* REWORK: was crittercism EventsFilesManager constant */ + "default_thumb2.jpg");
    }

    static Point getDefaultThumbnailSize(Resources resources) {
        return new Point(resources.getDimensionPixelSize(R.dimen.wallpaperThumbnailWidth), resources.getDimensionPixelSize(R.dimen.wallpaperThumbnailHeight));
    }

    @TargetApi(19)
    private DefaultWallpaperInfo getDefaultWallpaper() {
        Bitmap bitmapDecodeFile;
        File defaultThumbFile = getDefaultThumbFile();
        boolean zSaveDefaultWallpaperThumb = false;
        if (defaultThumbFile.exists()) {
            bitmapDecodeFile = BitmapFactory.decodeFile(defaultThumbFile.getAbsolutePath());
            zSaveDefaultWallpaperThumb = true;
        } else {
            Point defaultThumbnailSize = getDefaultThumbnailSize(getResources());
            Drawable builtInDrawable = WallpaperManager.getInstance(getApplicationContext()).getBuiltInDrawable(defaultThumbnailSize.x, defaultThumbnailSize.y, true, 0.5f, 0.5f);
            if (builtInDrawable != null) {
                Bitmap bitmapCreateBitmap = Bitmap.createBitmap(defaultThumbnailSize.x, defaultThumbnailSize.y, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmapCreateBitmap);
                builtInDrawable.setBounds(0, 0, defaultThumbnailSize.x, defaultThumbnailSize.y);
                builtInDrawable.draw(canvas);
                canvas.setBitmap(null);
                bitmapDecodeFile = bitmapCreateBitmap;
            } else {
                bitmapDecodeFile = null;
            }
            if (bitmapDecodeFile != null) {
                zSaveDefaultWallpaperThumb = saveDefaultWallpaperThumb(bitmapDecodeFile);
            }
        }
        if (zSaveDefaultWallpaperThumb) {
            return new DefaultWallpaperInfo(new BitmapDrawable(bitmapDecodeFile));
        }
        return null;
    }

    private ResourceWallpaperInfo getPreKKDefaultWallpaperInfo() {
        Bitmap bitmapCreateThumbnail;
        boolean zSaveDefaultWallpaperThumb;
        Resources system = Resources.getSystem();
        int identifier = system.getIdentifier("default_wallpaper", "drawable", "android" /* REWORK: was facebook SDK AbstractSpiCall constant */);
        File defaultThumbFile = getDefaultThumbFile();
        if (defaultThumbFile.exists()) {
            bitmapCreateThumbnail = BitmapFactory.decodeFile(defaultThumbFile.getAbsolutePath());
            zSaveDefaultWallpaperThumb = true;
        } else {
            Resources resources = getResources();
            bitmapCreateThumbnail = createThumbnail(getDefaultThumbnailSize(resources), getContext(), null, null, system, identifier, BitmapUtils.getRotationFromExif(resources, identifier), false);
            zSaveDefaultWallpaperThumb = bitmapCreateThumbnail != null ? saveDefaultWallpaperThumb(bitmapCreateThumbnail) : false;
        }
        if (zSaveDefaultWallpaperThumb) {
            return new ResourceWallpaperInfo(system, identifier, new BitmapDrawable(bitmapCreateThumbnail));
        }
        return null;
    }

    public void lambda$init$1(View view) {
        if (this.mActionMode != null) {
            if (view.isLongClickable()) {
                this.mLongClickListener.onLongClick(view);
            }
        } else {
            WallpaperTileInfo wallpaperTileInfo = (WallpaperTileInfo) view.getTag();
            if (wallpaperTileInfo.isSelectable() && view.getVisibility() == 0) {
                this.selectTile(view);
                this.setWallpaperButtonEnabled(true);
            }
            wallpaperTileInfo.onClick(this);
        }
    }

    public boolean lambda$init$2(View view) {
        ((CheckableFrameLayout) view).toggle();
        ActionMode actionMode = this.mActionMode;
        if (actionMode != null) {
            actionMode.invalidate();
            return true;
        }
        this.mActionMode = this.startActionMode(this.mActionModeCallback);
        int childCount = this.mWallpapersView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            this.mWallpapersView.getChildAt(i).setSelected(false);
        }
        return true;
    }

    public void lambda$init$3(ActionBar actionBar, View view) {
        if (this.mSelectedTile == null || this.mCropView.getTileSource() == null) {
            Log.w("WallpaperPickerActivity", "\"Set wallpaper\" was clicked when no tile was selected");
            return;
        }
        this.mWallpaperStrip.setVisibility(8);
        actionBar.hide();
        ((WallpaperTileInfo) this.mSelectedTile.getTag()).onSave(this);
    }

    public void lambda$setSystemWallpaperVisiblity$0(boolean z) {
        if (z) {
            this.mCropView.setVisibility(4);
        } else {
            this.changeWallpaperFlags(z);
        }
    }

    private boolean saveDefaultWallpaperThumb(Bitmap bitmap) {
        new File(getContext().getFilesDir(), "default_thumb.jpg").delete();
        new File(getContext().getFilesDir(), "default_thumb2.jpg").delete();
        for (int i = 16; i < Build.VERSION.SDK_INT; i++) {
            new File(getContext().getFilesDir(), i + "-" /* REWORK: was crittercism EventsFilesManager constant */ + "default_thumb2.jpg").delete();
        }
        return writeImageToFileAsJpeg(getDefaultThumbFile(), bitmap);
    }

    private boolean writeImageToFileAsJpeg(File file, Bitmap bitmap) {
        try {
            file.createNewFile();
            FileOutputStream fileOutputStreamOpenFileOutput = getContext().openFileOutput(file.getName(), 0);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fileOutputStreamOpenFileOutput);
            fileOutputStreamOpenFileOutput.close();
            return true;
        } catch (IOException e) {
            Log.e("WallpaperPickerActivity", "Error while writing bitmap to file " + e);
            file.delete();
            return false;
        }
    }

    void changeWallpaperFlags(boolean z) {
        int i = z ? 1048576 : 0;
        if (i != (getWindow().getAttributes().flags & 1048576)) {
            getWindow().setFlags(i, 1048576);
        }
    }

    @Override // com.voxel.launcher3.WallpaperCropActivity
    public boolean enableRotation() {
        return true;
    }

    public CropView getCropView() {
        return this.mCropView;
    }

    public SavedWallpaperImages getSavedImages() {
        return this.mSavedImages;
    }

    protected Bitmap getThumbnailOfLastPhoto() {
        Bitmap thumbnail = null;
        if (!(getActivity().checkPermission("android.permission.READ_EXTERNAL_STORAGE", Process.myPid(), Process.myUid()) == 0)) {
            return null;
        }
        Cursor cursorQuery = MediaStore.Images.Media.query(getContext().getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{"_id", "datetaken"}, null, null, "datetaken DESC LIMIT 1");
        if (cursorQuery != null) {
            if (cursorQuery.moveToNext()) {
                thumbnail = MediaStore.Images.Thumbnails.getThumbnail(getContext().getContentResolver(), cursorQuery.getInt(0), 1, null);
            }
            cursorQuery.close();
        }
        return thumbnail;
    }

    public Pair<ApplicationInfo, Integer> getWallpaperArrayResourceId() {
        try {
            return new Pair<>(getContext().getPackageManager().getApplicationInfo(getResources().getResourcePackageName(R.array.wallpapers), 0), Integer.valueOf(R.array.wallpapers));
        } catch (PackageManager.NameNotFoundException unused) {
            return null;
        }
    }

    public float getWallpaperParallaxOffset() {
        return this.mWallpaperParallaxOffset;
    }

    @Override // com.voxel.launcher3.WallpaperCropActivity
    protected void init() {
        setContentView(R.layout.wallpaper_picker);
        this.mCropView = (CropView) findViewById(R.id.cropView);
        this.mCropView.setVisibility(4);
        this.mProgressView = findViewById(R.id.loading);
        this.mWallpaperScrollContainer = (HorizontalScrollView) findViewById(R.id.wallpaper_scroll_container);
        this.mWallpaperStrip = findViewById(R.id.wallpaper_strip);
        this.mCropView.setTouchCallback(new AnonymousClass1());
        this.mThumbnailOnClickListener = new View.OnClickListener() { // from class: com.voxel.launcher3.-$$Lambda$WallpaperPickerActivity$kTokmGYwEwBmNkp9BRYRdlVRSOE
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WallpaperPickerActivity.this.lambda$init$1( view);
            }
        };
        this.mLongClickListener = new View.OnLongClickListener() { // from class: com.voxel.launcher3.-$$Lambda$WallpaperPickerActivity$w_9preD-RsrDbWUpm6_-NWIJMLc
            @Override // android.view.View.OnLongClickListener
            public final boolean onLongClick(View view) {
                return WallpaperPickerActivity.this.lambda$init$2( view);
            }
        };
        this.mWallpaperParallaxOffset = getIntent().getFloatExtra("com.voxel.launcher3.WALLPAPER_OFFSET", 0.0f);
        ArrayList<WallpaperTileInfo> arrayListFindBundledWallpapers = findBundledWallpapers();
        this.mWallpapersView = (LinearLayout) findViewById(R.id.wallpaper_list);
        populateWallpapersFromAdapter(this.mWallpapersView, new SimpleWallpapersAdapter(getContext(), arrayListFindBundledWallpapers), false);
        this.mSavedImages = new SavedWallpaperImages(getContext());
        this.mSavedImages.loadThumbnailsAndImageIdList();
        populateWallpapersFromAdapter(this.mWallpapersView, this.mSavedImages, true);
        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.live_wallpaper_list);
        final LiveWallpaperListAdapter liveWallpaperListAdapter = new LiveWallpaperListAdapter(getContext());
        liveWallpaperListAdapter.registerDataSetObserver(new DataSetObserver() { // from class: com.voxel.launcher3.WallpaperPickerActivity.2
            @Override // android.database.DataSetObserver
            public void onChanged() {
                linearLayout.removeAllViews();
                WallpaperPickerActivity.this.populateWallpapersFromAdapter(linearLayout, liveWallpaperListAdapter, false);
                WallpaperPickerActivity.this.initializeScrollForRtl();
                WallpaperPickerActivity.this.updateTileIndices();
            }
        });
        populateWallpapersFromAdapter((LinearLayout) findViewById(R.id.third_party_wallpaper_list), new ThirdPartyWallpaperPickerListAdapter(getContext()), false);
        LinearLayout linearLayout2 = (LinearLayout) findViewById(R.id.master_wallpaper_list);
        FrameLayout frameLayout = (FrameLayout) getLayoutInflater().inflate(R.layout.wallpaper_picker_image_picker_item, (ViewGroup) linearLayout2, false);
        linearLayout2.addView(frameLayout, 0);
        Bitmap thumbnailOfLastPhoto = getThumbnailOfLastPhoto();
        if (thumbnailOfLastPhoto != null) {
            ImageView imageView = (ImageView) frameLayout.findViewById(R.id.wallpaper_image);
            imageView.setImageBitmap(thumbnailOfLastPhoto);
            imageView.setColorFilter(getResources().getColor(R.color.wallpaper_picker_translucent_gray), PorterDuff.Mode.SRC_ATOP);
        }
        PickImageInfo pickImageInfo = new PickImageInfo();
        frameLayout.setTag(pickImageInfo);
        pickImageInfo.setView(frameLayout);
        frameLayout.setOnClickListener(this.mThumbnailOnClickListener);
        this.mCropView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.voxel.launcher3.WallpaperPickerActivity.3
            @Override // android.view.View.OnLayoutChangeListener
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                if (i3 - i <= 0 || i4 - i2 <= 0) {
                    return;
                }
                if (WallpaperPickerActivity.this.mSelectedIndex >= 0 && WallpaperPickerActivity.this.mSelectedIndex < WallpaperPickerActivity.this.mWallpapersView.getChildCount()) {
                    WallpaperPickerActivity.this.mThumbnailOnClickListener.onClick(WallpaperPickerActivity.this.mWallpapersView.getChildAt(WallpaperPickerActivity.this.mSelectedIndex));
                    WallpaperPickerActivity.this.setSystemWallpaperVisiblity(false);
                }
                view.removeOnLayoutChangeListener(this);
            }
        });
        updateTileIndices();
        initializeScrollForRtl();
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(200L);
        layoutTransition.setStartDelay(1, 0L);
        layoutTransition.setAnimator(3, null);
        this.mWallpapersView.setLayoutTransition(layoutTransition);
        final ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.actionbar_set_wallpaper);
        actionBar.getCustomView().setOnClickListener(new View.OnClickListener() { // from class: com.voxel.launcher3.-$$Lambda$WallpaperPickerActivity$l8psbcdjh_bFPhtYdsn4GuBeGe4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                WallpaperPickerActivity.this.lambda$init$3( actionBar, view);
            }
        });
        this.mSetWallpaperButton = findViewById(R.id.set_wallpaper_button);
        this.mActionModeCallback = new ActionMode.Callback() { // from class: com.voxel.launcher3.WallpaperPickerActivity.4
            private int numCheckedItems() {
                int childCount = WallpaperPickerActivity.this.mWallpapersView.getChildCount();
                int i = 0;
                for (int i2 = 0; i2 < childCount; i2++) {
                    if (((CheckableFrameLayout) WallpaperPickerActivity.this.mWallpapersView.getChildAt(i2)).isChecked()) {
                        i++;
                    }
                }
                return i;
            }

            @Override // android.view.ActionMode.Callback
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                if (menuItem.getItemId() != R.id.menu_delete) {
                    return false;
                }
                int childCount = WallpaperPickerActivity.this.mWallpapersView.getChildCount();
                ArrayList arrayList = new ArrayList();
                boolean z = false;
                for (int i = 0; i < childCount; i++) {
                    CheckableFrameLayout checkableFrameLayout = (CheckableFrameLayout) WallpaperPickerActivity.this.mWallpapersView.getChildAt(i);
                    if (checkableFrameLayout.isChecked()) {
                        ((WallpaperTileInfo) checkableFrameLayout.getTag()).onDelete(WallpaperPickerActivity.this);
                        arrayList.add(checkableFrameLayout);
                        if (i == WallpaperPickerActivity.this.mSelectedIndex) {
                            z = true;
                        }
                    }
                }
                Iterator it = arrayList.iterator();
                while (it.hasNext()) {
                    WallpaperPickerActivity.this.mWallpapersView.removeView((View) it.next());
                }
                if (z) {
                    WallpaperPickerActivity wallpaperPickerActivity = WallpaperPickerActivity.this;
                    wallpaperPickerActivity.mSelectedIndex = -1;
                    wallpaperPickerActivity.mSelectedTile = null;
                    wallpaperPickerActivity.setSystemWallpaperVisiblity(true);
                }
                WallpaperPickerActivity.this.updateTileIndices();
                actionMode.finish();
                return true;
            }

            @Override // android.view.ActionMode.Callback
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                actionMode.getMenuInflater().inflate(R.menu.cab_delete_wallpapers, menu);
                return true;
            }

            @Override // android.view.ActionMode.Callback
            public void onDestroyActionMode(ActionMode actionMode) {
                int childCount = WallpaperPickerActivity.this.mWallpapersView.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    ((CheckableFrameLayout) WallpaperPickerActivity.this.mWallpapersView.getChildAt(i)).setChecked(false);
                }
                if (WallpaperPickerActivity.this.mSelectedTile != null) {
                    WallpaperPickerActivity.this.mSelectedTile.setSelected(true);
                }
                WallpaperPickerActivity.this.mActionMode = null;
            }

            @Override // android.view.ActionMode.Callback
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                int iNumCheckedItems = numCheckedItems();
                if (iNumCheckedItems == 0) {
                    actionMode.finish();
                    return true;
                }
                actionMode.setTitle(WallpaperPickerActivity.this.getResources().getQuantityString(R.plurals.number_of_items_selected, iNumCheckedItems, Integer.valueOf(iNumCheckedItems)));
                return true;
            }
        };
    }

    void initializeScrollForRtl() {
        if (Utilities.isRtl(getResources())) {
            this.mWallpaperScrollContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() { // from class: com.voxel.launcher3.WallpaperPickerActivity.5
                @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
                public void onGlobalLayout() {
                    WallpaperPickerActivity.this.mWallpaperScrollContainer.scrollTo(((LinearLayout) WallpaperPickerActivity.this.findViewById(R.id.master_wallpaper_list)).getWidth(), 0);
                    WallpaperPickerActivity.this.mWallpaperScrollContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
    }

    @Override // android.app.Activity
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == 5 && i2 == -1) {
            if (intent == null || intent.getData() == null) {
                return;
            }
            addTemporaryWallpaperTile(intent.getData(), false);
            return;
        }
        if (i == 6 && i2 == -1) {
            setResult(-1);
            finish();
        }
    }

    @Override // com.voxel.launcher3.WallpaperCropActivity
    protected void onLoadRequestComplete(WallpaperCropActivity.LoadRequest loadRequest, boolean z) {
        super.onLoadRequestComplete(loadRequest, z);
        if (z) {
            setSystemWallpaperVisiblity(false);
        }
    }

    @Override // android.app.Activity
    protected void onRestoreInstanceState(Bundle bundle) {
        Iterator it = bundle.getParcelableArrayList("TEMP_WALLPAPER_TILES").iterator();
        while (it.hasNext()) {
            addTemporaryWallpaperTile((Uri) it.next(), true);
        }
        this.mSelectedIndex = bundle.getInt("SELECTED_INDEX", -1);
    }

    @Override // android.app.Activity
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putParcelableArrayList("TEMP_WALLPAPER_TILES", this.mTempWallpaperTiles);
        bundle.putInt("SELECTED_INDEX", this.mSelectedIndex);
    }

    @Override // android.app.Activity
    public void onStop() {
        super.onStop();
        this.mWallpaperStrip = findViewById(R.id.wallpaper_strip);
        if (this.mWallpaperStrip.getAlpha() < 1.0f) {
            this.mWallpaperStrip.setAlpha(1.0f);
            this.mWallpaperStrip.setVisibility(0);
        }
    }

    void populateWallpapersFromAdapter(ViewGroup viewGroup, BaseAdapter baseAdapter, boolean z) {
        for (int i = 0; i < baseAdapter.getCount(); i++) {
            FrameLayout frameLayout = (FrameLayout) baseAdapter.getView(i, null, viewGroup);
            viewGroup.addView(frameLayout, i);
            WallpaperTileInfo wallpaperTileInfo = (WallpaperTileInfo) baseAdapter.getItem(i);
            frameLayout.setTag(wallpaperTileInfo);
            wallpaperTileInfo.setView(frameLayout);
            if (z) {
                addLongPressHandler(frameLayout);
            }
            frameLayout.setOnClickListener(this.mThumbnailOnClickListener);
        }
    }

    void selectTile(View view) {
        View view2 = this.mSelectedTile;
        if (view2 != null) {
            view2.setSelected(false);
            this.mSelectedTile = null;
        }
        this.mSelectedTile = view;
        view.setSelected(true);
        this.mSelectedIndex = this.mWallpapersView.indexOfChild(view);
        view.announceForAccessibility(getContext().getString(R.string.announce_selection, view.getContentDescription()));
    }

    protected void setSystemWallpaperVisiblity(final boolean z) {
        if (z) {
            changeWallpaperFlags(z);
        } else {
            this.mCropView.setVisibility(0);
        }
        this.mCropView.postDelayed(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$WallpaperPickerActivity$Czzje--dKEg7TWQ9pGJUBiVVTmU
            @Override // java.lang.Runnable
            public final void run() {
                WallpaperPickerActivity.this.lambda$setSystemWallpaperVisiblity$0( z);
            }
        }, 200L);
    }

    public void setWallpaperButtonEnabled(boolean z) {
        this.mSetWallpaperButton.setEnabled(z);
    }

    public void startActivityForResultSafely(Intent intent, int i) {
        Utilities.startActivityForResultSafely(getActivity(), intent, i);
    }

    void updateTileIndices() {
        int childCount;
        LinearLayout linearLayout;
        int i;
        LinearLayout linearLayout2 = (LinearLayout) findViewById(R.id.master_wallpaper_list);
        int childCount2 = linearLayout2.getChildCount();
        Resources resources = getResources();
        int i2 = 0;
        int i3 = 0;
        while (i2 < 2) {
            int i4 = i3;
            int i5 = 0;
            for (int i6 = 0; i6 < childCount2; i6++) {
                View childAt = linearLayout2.getChildAt(i6);
                if (childAt.getTag() instanceof WallpaperTileInfo) {
                    linearLayout = linearLayout2;
                    childCount = i6 + 1;
                    i = i6;
                } else {
                    LinearLayout linearLayout3 = (LinearLayout) childAt;
                    childCount = linearLayout3.getChildCount();
                    linearLayout = linearLayout3;
                    i = 0;
                }
                while (i < childCount) {
                    WallpaperTileInfo wallpaperTileInfo = (WallpaperTileInfo) linearLayout.getChildAt(i).getTag();
                    if (wallpaperTileInfo.isNamelessWallpaper()) {
                        if (i2 == 0) {
                            i4++;
                        } else {
                            i5++;
                            wallpaperTileInfo.onIndexUpdated(resources.getString(R.string.wallpaper_accessibility_name, Integer.valueOf(i5), Integer.valueOf(i4)));
                        }
                    }
                    i++;
                }
            }
            i2++;
            i3 = i4;
        }
    }
}
