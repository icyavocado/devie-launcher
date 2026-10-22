package com.voxel.launcher3;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import com.voxel.launcher3.compat.AppWidgetManagerCompat;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.compat.UserManagerCompat;
import com.voxel.launcher3.util.ComponentKey;
import com.voxel.launcher3.util.SQLiteCacheHelper;
import com.voxel.simplesearchlauncher.widget.LauncherAppWidgetProviderInfo;
import com.voxel.simplesearchlauncher.widget.WidgetCell;
import com.ovacoda.devie.R;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/* JADX INFO: loaded from: classes.dex */
public class WidgetPreviewLoader {
    private final Context mContext;
    private final CacheDb mDb;
    private final IconCache mIconCache;
    private final int mProfileBadgeMargin;
    private final UserManagerCompat mUserManager;
    private final AppWidgetManagerCompat mWidgetManager;
    private final HashMap<String, long[]> mPackageVersions = new HashMap<>();
    final Set<Bitmap> mUnusedBitmaps = Collections.newSetFromMap(new WeakHashMap());
    private final MainThreadExecutor mMainThreadExecutor = new MainThreadExecutor();
    final Handler mWorkerHandler = new Handler(LauncherModel.getWorkerLooper());

    private static class CacheDb extends SQLiteCacheHelper {
        public CacheDb(Context context) {
            super(context, "widgetpreviews.db", 4, "shortcut_and_widget_previews");
        }

        @Override // com.voxel.launcher3.util.SQLiteCacheHelper
        public void onCreateTable(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS shortcut_and_widget_previews (componentName TEXT NOT NULL, profileId INTEGER NOT NULL, size TEXT NOT NULL, packageName TEXT NOT NULL, lastUpdated INTEGER NOT NULL DEFAULT 0, version INTEGER NOT NULL DEFAULT 0, preview_bitmap BLOB, PRIMARY KEY (componentName, profileId, size) );");
        }
    }

    public class PreviewLoadRequest {
        final PreviewLoadTask mTask;

        public PreviewLoadRequest(PreviewLoadTask previewLoadTask) {
            this.mTask = previewLoadTask;
        }

        public void lambda$cleanup$0() {
            synchronized (WidgetPreviewLoader.this.mUnusedBitmaps) {
                WidgetPreviewLoader.this.mUnusedBitmaps.add(this.mTask.mBitmapToRecycle);
            }
            this.mTask.mBitmapToRecycle = null;
        }

        public void cleanup() {
            PreviewLoadTask previewLoadTask = this.mTask;
            if (previewLoadTask != null) {
                previewLoadTask.cancel(true);
            }
            if (this.mTask.mBitmapToRecycle != null) {
                WidgetPreviewLoader.this.mWorkerHandler.post(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$WidgetPreviewLoader$PreviewLoadRequest$PFFeAx1_XFFsw2fflB5VCkrujhg
                    @Override // java.lang.Runnable
                    public final void run() {
                        PreviewLoadRequest.this.lambda$cleanup$0();
                    }
                });
            }
        }
    }

    public class PreviewLoadTask extends AsyncTask<Void, Void, Bitmap> {
        Bitmap mBitmapToRecycle;
        private final WidgetCell mCaller;
        private final Object mInfo;
        final WidgetCacheKey mKey;
        private final int mPreviewHeight;
        private final int mPreviewWidth;
        long[] mVersions;

        PreviewLoadTask(WidgetCacheKey widgetCacheKey, Object obj, int i, int i2, WidgetCell widgetCell) {
            this.mKey = widgetCacheKey;
            this.mInfo = obj;
            this.mPreviewHeight = i2;
            this.mPreviewWidth = i;
            this.mCaller = widgetCell;
        }

        public void lambda$onCancelled$1(Bitmap bitmap) {
            synchronized (WidgetPreviewLoader.this.mUnusedBitmaps) {
                WidgetPreviewLoader.this.mUnusedBitmaps.add(bitmap);
            }
        }

        public void lambda$onPostExecute$0(Bitmap bitmap) {
            if (!this.isCancelled()) {
                WidgetPreviewLoader.this.writeToDb(this.mKey, this.mVersions, bitmap);
                this.mBitmapToRecycle = bitmap;
            } else {
                synchronized (WidgetPreviewLoader.this.mUnusedBitmaps) {
                    WidgetPreviewLoader.this.mUnusedBitmaps.add(bitmap);
                }
            }
        }

        /* JADX INFO: Access modifiers changed from: protected */
        /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
        @Override // android.os.AsyncTask
        public Bitmap doInBackground(Void... voidArr) {
            Bitmap bitmap = null;
            if (isCancelled()) {
                return null;
            }
            synchronized (WidgetPreviewLoader.this.mUnusedBitmaps) {
                for (Bitmap bitmap2 : WidgetPreviewLoader.this.mUnusedBitmaps) {
                    if (bitmap2 != null && bitmap2.isMutable() && bitmap2.getWidth() == this.mPreviewWidth && bitmap2.getHeight() == this.mPreviewHeight) {
                        WidgetPreviewLoader.this.mUnusedBitmaps.remove(bitmap2);
                        bitmap = bitmap2;
                        break;
                    }
                }
            }
            Bitmap bitmapCreateBitmap = bitmap == null ? Bitmap.createBitmap(this.mPreviewWidth, this.mPreviewHeight, Bitmap.Config.ARGB_8888) : bitmap;
            if (isCancelled()) {
                return bitmapCreateBitmap;
            }
            Bitmap fromDb = WidgetPreviewLoader.this.readFromDb(this.mKey, bitmapCreateBitmap, this);
            if (isCancelled() || fromDb != null) {
                return fromDb;
            }
            this.mVersions = WidgetPreviewLoader.this.getPackageVersion(this.mKey.componentName.getPackageName());
            return WidgetPreviewLoader.this.generatePreview((Launcher) this.mCaller.getContext(), this.mInfo, bitmapCreateBitmap, this.mPreviewWidth, this.mPreviewHeight);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onCancelled(final Bitmap bitmap) {
            if (bitmap != null) {
                WidgetPreviewLoader.this.mWorkerHandler.post(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$WidgetPreviewLoader$PreviewLoadTask$vGt9AxipSBJEHvYt8SlHsRGqPkA
                    @Override // java.lang.Runnable
                    public final void run() {
                        PreviewLoadTask.this.lambda$onCancelled$1( bitmap);
                    }
                });
            }
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(final Bitmap bitmap) {
            this.mCaller.applyPreview(bitmap);
            if (this.mVersions != null) {
                WidgetPreviewLoader.this.mWorkerHandler.post(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$WidgetPreviewLoader$PreviewLoadTask$ZLXWDw4krHqCPjbcTLGgyZ1wn7g
                    @Override // java.lang.Runnable
                    public final void run() {
                        PreviewLoadTask.this.lambda$onPostExecute$0( bitmap);
                    }
                });
            } else {
                this.mBitmapToRecycle = bitmap;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    static final class WidgetCacheKey extends ComponentKey {
        final String size;

        public WidgetCacheKey(ComponentName componentName, UserHandleCompat userHandleCompat, String str) {
            super(componentName, userHandleCompat);
            this.size = str;
        }

        @Override // com.voxel.launcher3.util.ComponentKey
        public boolean equals(Object obj) {
            return super.equals(obj) && ((WidgetCacheKey) obj).size.equals(this.size);
        }

        @Override // com.voxel.launcher3.util.ComponentKey
        public int hashCode() {
            return super.hashCode() ^ this.size.hashCode();
        }
    }

    public WidgetPreviewLoader(Context context, IconCache iconCache) {
        this.mContext = context;
        this.mIconCache = iconCache;
        this.mWidgetManager = AppWidgetManagerCompat.getInstance(context);
        this.mUserManager = UserManagerCompat.getInstance(context);
        this.mDb = new CacheDb(context);
        this.mProfileBadgeMargin = context.getResources().getDimensionPixelSize(R.dimen.profile_badge_margin);
    }

    private Bitmap generateShortcutPreview(Launcher launcher, ResolveInfo resolveInfo, int i, int i2, Bitmap bitmap) {
        Canvas canvas = new Canvas();
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);
        } else {
            if (bitmap.getWidth() != i || bitmap.getHeight() != i2) {
                throw new RuntimeException("Improperly sized bitmap passed as argument");
            }
            canvas.setBitmap(bitmap);
            canvas.drawColor(0, PorterDuff.Mode.MULTIPLY);
        }
        Drawable drawableMutateOnMainThread = mutateOnMainThread(this.mIconCache.getFullResIcon(resolveInfo.activityInfo));
        drawableMutateOnMainThread.setFilterBitmap(true);
        int i3 = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getCellParamsForType(DeviceProfile.ParamType.WORKSPACE).iconSizePx;
        drawableMutateOnMainThread.setAlpha(255);
        drawableMutateOnMainThread.setColorFilter(null);
        int i4 = (i - i3) / 2;
        int i5 = (i2 - i3) / 2;
        drawableMutateOnMainThread.setBounds(i4, i5, i4 + i3, i3 + i5);
        drawableMutateOnMainThread.draw(canvas);
        canvas.setBitmap(null);
        return bitmap;
    }

    private WidgetCacheKey getObjectKey(Object obj, String str) {
        if (obj instanceof LauncherAppWidgetProviderInfo) {
            LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo = (LauncherAppWidgetProviderInfo) obj;
            return new WidgetCacheKey(launcherAppWidgetProviderInfo.provider, this.mWidgetManager.getUser(launcherAppWidgetProviderInfo), str);
        }
        ResolveInfo resolveInfo = (ResolveInfo) obj;
        return new WidgetCacheKey(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name), UserHandleCompat.myUserHandle(), str);
    }

    private Drawable mutateOnMainThread(final Drawable drawable) {
        try {
            return (Drawable) this.mMainThreadExecutor.submit(new Callable() { // from class: com.voxel.launcher3.-$$Lambda$WidgetPreviewLoader$xaUKz3V1-G_T7NEjkHcuzVVOtYo
                @Override // java.util.concurrent.Callable
                public final Object call() {
                    return drawable.mutate();
                }
            }).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e2) {
            throw new RuntimeException(e2);
        }
    }

    Bitmap generatePreview(Launcher launcher, Object obj, Bitmap bitmap, int i, int i2) {
        return obj instanceof LauncherAppWidgetProviderInfo ? generateWidgetPreview(launcher, (LauncherAppWidgetProviderInfo) obj, i, bitmap, null) : generateShortcutPreview(launcher, (ResolveInfo) obj, i, i2, bitmap);
    }

    public Bitmap generateWidgetPreview(Launcher launcher, LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo, int i, Bitmap bitmap, int[] iArr) {
        Drawable drawableLoadPreview;
        Bitmap bitmap2;
        int intrinsicWidth;
        int intrinsicHeight;
        WidgetPreviewLoader widgetPreviewLoader;
        LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo2;
        Bitmap bitmap3 = null; /* REWORK: jadx blank register */
        Bitmap bitmapCreateBitmap = bitmap;
        int i2 = i < 0 ? Integer.MAX_VALUE : i;
        if (launcherAppWidgetProviderInfo.previewImage != 0) {
            drawableLoadPreview = this.mWidgetManager.loadPreview(launcherAppWidgetProviderInfo);
            if (drawableLoadPreview != null) {
                drawableLoadPreview = mutateOnMainThread(drawableLoadPreview);
            } else {
                Log.w("WidgetPreviewLoader", "Can't load widget preview drawable 0x" + Integer.toHexString(launcherAppWidgetProviderInfo.previewImage) + " for provider: " + launcherAppWidgetProviderInfo.provider);
            }
        } else {
            drawableLoadPreview = null;
        }
        boolean z = drawableLoadPreview != null;
        int i3 = launcherAppWidgetProviderInfo.spanX;
        int i4 = launcherAppWidgetProviderInfo.spanY;
        if (z) {
            intrinsicWidth = drawableLoadPreview.getIntrinsicWidth();
            intrinsicHeight = drawableLoadPreview.getIntrinsicHeight();
            bitmap2 = null;
        } else {
            Bitmap bitmap4 = ((BitmapDrawable) this.mContext.getResources().getDrawable(R.drawable.widget_tile)).getBitmap();
            int width = bitmap4.getWidth() * i3;
            int height = bitmap4.getHeight() * i4;
            bitmap2 = bitmap4;
            intrinsicWidth = width;
            intrinsicHeight = height;
        }
        if (iArr != null) {
            iArr[0] = intrinsicWidth;
        }
        float f = intrinsicWidth > i2 ? (i2 - (this.mProfileBadgeMargin * 2)) / intrinsicWidth : 1.0f;
        if (f != 1.0f) {
            intrinsicWidth = (int) (intrinsicWidth * f);
            intrinsicHeight = (int) (intrinsicHeight * f);
        }
        Canvas canvas = new Canvas();
        if (bitmapCreateBitmap == null) {
            bitmapCreateBitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmapCreateBitmap);
        } else {
            canvas.setBitmap(bitmapCreateBitmap);
            canvas.drawColor(0, PorterDuff.Mode.MULTIPLY);
        }
        int width2 = (bitmapCreateBitmap.getWidth() - intrinsicWidth) / 2;
        int iMax = Math.max(0, (bitmapCreateBitmap.getHeight() - intrinsicHeight) / 2);
        if (z) {
            drawableLoadPreview.setBounds(width2, iMax, width2 + intrinsicWidth, iMax + intrinsicHeight);
            drawableLoadPreview.draw(canvas);
            widgetPreviewLoader = this;
            launcherAppWidgetProviderInfo2 = launcherAppWidgetProviderInfo;
        } else {
            Paint paint = new Paint();
            paint.setFilterBitmap(true);
            int i5 = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getCellParamsForType(DeviceProfile.ParamType.WORKSPACE).iconSizePx;
            Rect rect = new Rect(0, 0, bitmap2.getWidth(), bitmap2.getHeight());
            float width3 = bitmap2.getWidth() * f;
            float height2 = bitmap2.getHeight() * f;
            RectF rectF = new RectF(0.0f, 0.0f, width3, height2);
            float f2 = width2;
            int i6 = 0;
            while (i6 < i3) {
                int i7 = i3;
                int i8 = iMax;
                float f3 = iMax;
                int i9 = 0;
                while (i9 < i4) {
                    rectF.offsetTo(f2, f3);
                    canvas.drawBitmap(bitmap2, rect, rectF, paint);
                    i9++;
                    f3 += height2;
                }
                i6++;
                f2 += width3;
                iMax = i8;
                i3 = i7;
            }
            int i10 = iMax;
            float f4 = i5;
            float fMin = Math.min(Math.min(intrinsicWidth, intrinsicHeight) / (i5 + (((int) (0.25f * f4)) * 2)), f);
            widgetPreviewLoader = this;
            try {
                launcherAppWidgetProviderInfo2 = launcherAppWidgetProviderInfo;
                try {
                    Drawable drawableLoadIcon = widgetPreviewLoader.mWidgetManager.loadIcon(launcherAppWidgetProviderInfo2, widgetPreviewLoader.mIconCache);
                    if (drawableLoadIcon != null) {
                        Drawable drawableMutateOnMainThread = widgetPreviewLoader.mutateOnMainThread(drawableLoadIcon);
                        float f5 = f4 * fMin;
                        int i11 = ((int) ((width3 - f5) / 2.0f)) + width2;
                        int i12 = ((int) ((height2 - f5) / 2.0f)) + i10;
                        int i13 = (int) f5;
                        drawableMutateOnMainThread.setBounds(i11, i12, i11 + i13, i13 + i12);
                        drawableMutateOnMainThread.draw(canvas);
                    }
                    bitmap3 = null;
                } catch (Resources.NotFoundException unused) {
                    bitmap3 = null;
                }
            } catch (Resources.NotFoundException unused2) {
                launcherAppWidgetProviderInfo2 = launcherAppWidgetProviderInfo;
            }
            canvas.setBitmap(bitmap3);
        }
        return widgetPreviewLoader.mWidgetManager.getBadgeBitmap(launcherAppWidgetProviderInfo2, bitmapCreateBitmap, Math.min(bitmapCreateBitmap.getWidth(), intrinsicWidth + widgetPreviewLoader.mProfileBadgeMargin), Math.min(bitmapCreateBitmap.getHeight(), intrinsicHeight + widgetPreviewLoader.mProfileBadgeMargin));
    }

    long[] getPackageVersion(String str) {
        long[] jArr;
        synchronized (this.mPackageVersions) {
            jArr = this.mPackageVersions.get(str);
            if (jArr == null) {
                jArr = new long[2];
                try {
                    PackageInfo packageInfo = this.mContext.getPackageManager().getPackageInfo(str, 0);
                    jArr[0] = packageInfo.versionCode;
                    jArr[1] = packageInfo.lastUpdateTime;
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("WidgetPreviewLoader", "PackageInfo not found", e);
                }
                this.mPackageVersions.put(str, jArr);
            }
        }
        return jArr;
    }

    public PreviewLoadRequest getPreview(Object obj, int i, int i2, WidgetCell widgetCell) {
        PreviewLoadTask previewLoadTask = new PreviewLoadTask(getObjectKey(obj, i + "x" + i2), obj, i, i2, widgetCell);
        previewLoadTask.executeOnExecutor(Utilities.THREAD_POOL_EXECUTOR, new Void[0]);
        return new PreviewLoadRequest(previewLoadTask);
    }

    /* JADX WARN: Code duplicated, block: B:32:0x0076 A[PHI: r10
      0x0076: PHI (r10v4 android.database.Cursor) = (r10v3 android.database.Cursor), (r10v6 android.database.Cursor) binds: [B:31:0x0074, B:22:0x0064] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Code duplicated, block: B:37:0x007e  */
    Bitmap readFromDb(WidgetCacheKey widgetCacheKey, Bitmap bitmap, PreviewLoadTask previewLoadTask) { /* REWORK: the dex caught its own IO failures; the AsyncTask worker must not see them */
        /* REWORK: jadx lost an exception-flow local across catch scopes. */
        Throwable eReflow = null;
        Throwable thReflow = null;
        Cursor cursorQuery;
        Cursor cursor = null;
        try {
            cursorQuery = this.mDb.query(new String[]{"preview_bitmap"}, "componentName = ? AND profileId = ? AND size = ?", new String[]{widgetCacheKey.componentName.flattenToString(), Long.toString(this.mUserManager.getSerialNumberForUser(widgetCacheKey.user)), widgetCacheKey.size});
            try {
                try {
                    if (previewLoadTask.isCancelled()) {
                        if (cursorQuery != null) {
                            cursorQuery.close();
                        }
                        return null;
                    }
                    if (cursorQuery.moveToNext()) {
                        byte[] blob = cursorQuery.getBlob(0);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inBitmap = bitmap;
                        try {
                            if (!previewLoadTask.isCancelled()) {
                                Bitmap bitmapDecodeByteArray = BitmapFactory.decodeByteArray(blob, 0, blob.length, options);
                                if (cursorQuery != null) {
                                    cursorQuery.close();
                                }
                                return bitmapDecodeByteArray;
                            }
                        } catch (Exception unused) {
                            if (cursorQuery != null) {
                                cursorQuery.close();
                            }
                            return null;
                        }
                    }
                    if (cursorQuery != null) {
                        cursorQuery.close();
                    }
                } catch (SQLException e) {
                    e = e;
                    Log.w("WidgetPreviewLoader", "Error loading preview from DB", e);
                    if (cursorQuery != null) {
                        cursorQuery.close();
                    }
                }
            } catch (Throwable th) {
                th = th;
                cursor = cursorQuery;
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        } catch (SQLException e2) {
            eReflow = e2;
            cursorQuery = null;
        } catch (Throwable th2) {
            thReflow = th2;
            if (cursor != null) {
                cursor.close();
            }
            /* REWORK: dex swallowed DB-read failures; reparse by falling through. */
            return null;
        }
        return null;
    }

    void writeToDb(WidgetCacheKey widgetCacheKey, long[] jArr, Bitmap bitmap) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("componentName", widgetCacheKey.componentName.flattenToShortString());
        contentValues.put("profileId", Long.valueOf(this.mUserManager.getSerialNumberForUser(widgetCacheKey.user)));
        contentValues.put("size", widgetCacheKey.size);
        contentValues.put("packageName", widgetCacheKey.componentName.getPackageName());
        contentValues.put("version", Long.valueOf(jArr[0]));
        contentValues.put("lastUpdated", Long.valueOf(jArr[1]));
        contentValues.put("preview_bitmap", Utilities.flattenBitmap(bitmap));
        this.mDb.insertOrReplace(contentValues);
    }
}
