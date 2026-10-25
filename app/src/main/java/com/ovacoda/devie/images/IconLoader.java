package com.ovacoda.devie.images;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;
import android.util.Base64;
import android.util.LruCache;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.evie.common.iconpack.IconPackFilter;
import com.evie.common.iconpack.IconPackFilterHolder;
import com.voxel.launcher3.IconCache;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.ovacoda.devie.R;

import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import timber.log.Timber;

/**
 * REWORK(fresco): single in-process icon loader that replaces the Fresco
 * pipeline. dEvie performs no network image loads (cloud services are
 * stripped and the app has no INTERNET permission), so this loader resolves
 * exactly three URI families:
 *
 *  1. "data:image/bmp;base64,..." — locally generated bitmaps (contact
 *     initials headers, composed badge icons) kept as in-memory data URIs
 *     (BitmapUtil.getUrlFromBitmap).
 *  2. "content://..." — contact photo URIs from the contacts provider.
 *  3. The magic "http://example.com/<hash>?pn=..&an=..&ip=.." identifiers
 *     persisted in LocalSearch.db — never real network addresses; resolved
 *     locally through IconCache (optionally through the active icon pack).
 *
 * Anything else (real http/https URLs) is refused and logged. This keeps the
 * no-network guarantee structural instead of intercepted-at-runtime.
 */
public final class IconLoader {

    /** Magic-URI host + path hash from HybridNetworkFetcherCommon. */
    private static final String MAGIC_HOST = "example.com";
    private static final String MAGIC_PATH = "476287f4b140f81a723002230fa368847065c97a";
    /** BitmapUtil.getUrlFromBitmap prefix. */
    private static final String DATA_PREFIX = "data:image/bmp;base64,";
    private static final int DATA_PREFIX_LENGTH = DATA_PREFIX.length();

    /** Completion callback, always invoked on the MAIN thread. */
    public interface OnBitmapLoaded {
        void onBitmapLoaded(@Nullable Bitmap bitmap);
    }

    private static volatile IconLoader sInstance;

    private final LruCache<String, Bitmap> mCache;
    private final Executor mDecoder = Executors.newFixedThreadPool(4, runnable -> {
        Thread thread = new Thread(runnable, "devie-icon-loader");
        thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        return thread;
    });
    private final Handler mMain = new Handler(Looper.getMainLooper());

    @Nullable
    private Context mAppContext;

    private IconLoader() {
        int maxSize = computeCacheSize(null);
        mCache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getAllocationByteCount();
            }
        };
    }

    /* Mirrors ConservativeBitmapCacheConfigSupplier: half of the per-app
     * memory class, capped at 15 MB, with a small floor. */
    private static int computeCacheSize(@Nullable Context context) {
        int memoryClass = 16;
        if (context != null) {
            ActivityManager activityManager = (ActivityManager)
                    context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager != null) {
                memoryClass = activityManager.getMemoryClass();
            }
        }
        int bytes = memoryClass * 1024 * 1024 / 2;
        return Math.max(2 * 1024 * 1024, Math.min(15 * 1024 * 1024, bytes));
    }

    /** Must be called once from BaseApplication.setup(). */
    public static IconLoader init(Context context) {
        IconLoader instance = getInstance();
        instance.mAppContext = context.getApplicationContext();
        return instance;
    }

    public static IconLoader getInstance() {
        return getInstance(null);
    }

    private static IconLoader getInstance(Context context) {
        if (sInstance == null) {
            synchronized (IconLoader.class) {
                if (sInstance == null) {
                    sInstance = new IconLoader();
                }
            }
        }
        if (context != null && getInstance().mAppContext == null) {
            getInstance().mAppContext = context.getApplicationContext();
        }
        return sInstance;
    }

    @Nullable
    private Context context() {
        return mAppContext;
    }

    /** Async bind; the cache-hit fast path applies on the caller thread. */
    public void loadInto(@NonNull ImageView view, @Nullable Uri uri) {
        loadInto(view, uri == null ? null : uri.toString(), false, null);
    }

    public void loadInto(@NonNull ImageView view, @Nullable String url) {
        loadInto(view, url, false, null);
    }

    /** Async bind with optional circle-crop and a main-thread listener. */
    public void loadInto(@NonNull final ImageView view, @Nullable final String url,
            final boolean circular, @Nullable final OnBitmapLoaded listener) {
        view.setTag(R.id.devie_icon_url, url);
        if (url == null) {
            view.setImageDrawable(null);
            if (listener != null) {
                listener.onBitmapLoaded(null);
            }
            return;
        }
        final Bitmap cached = mCache.get(url);
        if (cached != null && !cached.isRecycled()) {
            apply(view, cached, circular, listener);
            return;
        }
        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) {
            // Background bind (widget adapters, BadgedIcons callers).
            Bitmap bitmap = resolve(url, false);
            apply(view, bitmap, circular, listener);
            return;
        }
        /* REMOVED(rework): clearing the view here (was setImageDrawable(null))
         * blanked every row whose icon re-binds before a cache miss decodes —
         * the visible "pop-in" after opening the search overlay. Fresco drew
         * an equally transparent placeholder, but its producers were much
         * faster; keep the previously bound content until the new bitmap is
         * ready (the tag guard below still rejects stale results). */
        mDecoder.execute(() -> {
            final Bitmap bitmap = resolve(url, false);
            mMain.post(() -> {
                // Ignore stale results after the view was rebound.
                if (TextUtils.equals(url, (CharSequence) view.getTag(R.id.devie_icon_url))) {
                    apply(view, bitmap, circular, listener);
                }
            });
        });
    }

    private void apply(@NonNull ImageView view, @Nullable Bitmap bitmap,
            boolean circular, @Nullable OnBitmapLoaded listener) {
        if (bitmap == null) {
            view.setImageDrawable(null);
            Timber.w("icon resolve failed for a (local) URL");
        } else {
            // Cache stores raw bitmaps; circle-crop per bind so an entry can
            // serve both shapes.
            view.setImageBitmap(circular ? toCircle(bitmap) : bitmap);
        }
        if (listener != null) {
            listener.onBitmapLoaded(bitmap);
        }
    }

    /** Blocking resolve; callers run on background threads (BadgedIcons). */
    @Nullable
    public Bitmap loadSync(@Nullable String url) {
        return resolve(url, false);
    }

    /** Resolve a URL family to a raw bitmap; never performs network I/O. */
    @Nullable
    private Bitmap resolve(@Nullable String url, boolean circular) {
        if (url == null) {
            return null;
        }
        Bitmap cached = mCache.get(url);
        if (cached != null && !cached.isRecycled()) {
            return cached;
        }
        Bitmap bitmap = decode(url);
        if (bitmap != null) {
            mCache.put(url, bitmap);
        }
        return bitmap;
    }

    @Nullable
    private Bitmap decode(@NonNull String url) {
        if (url.startsWith(DATA_PREFIX)) {
            try {
                byte[] bytes = Base64.decode(url.substring(DATA_PREFIX_LENGTH), Base64.DEFAULT);
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            } catch (Exception unused) {
                return null;
            }
        }
        Uri uri = Uri.parse(url);
        if (uri == null || uri.getHost() == null) {
            return null;
        }
        String scheme = uri.getScheme();
        if ("content".equals(scheme)) {
            Context context = context();
            if (context == null) {
                return null;
            }
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                if (inputStream == null) {
                    return null;
                }
                return BitmapFactory.decodeStream(inputStream);
            } catch (Exception unused) {
                return null;
            }
        }
        if (MAGIC_HOST.equals(uri.getHost())
                && MAGIC_PATH.equals(uri.getLastPathSegment())) {
            return resolveMagic(uri);
        }
        Timber.w("refusing remote icon URL: %s", url);
        return null;
    }

    /** Port of the local branch of HybridNetworkFetcher (icon packs etc.). */
    @Nullable
    private Bitmap resolveMagic(@NonNull Uri uri) {
        final String pn = uri.getQueryParameter("pn");
        final String an = uri.getQueryParameter("an");
        final String ip = uri.getQueryParameter("ip");
        final String ri = uri.getQueryParameter("ri");
        if (ri != null && !"-1".equals(ri)) {
            /* REMOVED(rework): the ri branch used to build a remote-icon
             * network URI; refused like any other remote URL. */
            Timber.w("refusing remote icon URI (ri set): %s", uri);
            return null;
        }
        if (pn == null) {
            return null;
        }
        LauncherAppState launcherAppState = LauncherAppState.getInstanceNoCreate();
        if (launcherAppState == null) {
            return null;
        }
        IconCache iconCache = launcherAppState.getIconCache();
        return getIconBitmapNoCache(iconCache, pn, an, ip);
    }

    @Nullable
    private Bitmap getIconBitmapNoCache(@NonNull IconCache iconCache, String packageName,
            @Nullable String activityName, @Nullable String iconPack) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        if (!TextUtils.isEmpty(activityName)) {
            intent.setClassName(packageName, activityName);
        }
        synchronized (iconCache) {
            if (!isValid(iconCache.getIconPackFilter(), iconPack)) {
                return null;
            }
            return iconCache.getIconNoCache(intent, UserHandleCompat.myUserHandle());
        }
    }

    private static boolean isValid(@Nullable IconPackFilter filter, @Nullable String iconPack) {
        return TextUtils.equals(
                filter != null ? filter.getPackageName() : null, iconPack);
    }

    /** Background warm-up (replaces prefetchToBitmapCache/DiskCache). */
    public void prefetch(@Nullable final String url) {
        if (url == null) {
            return;
        }
        mDecoder.execute(() -> resolve(url, false));
    }

    public void evict(@Nullable String url) {
        if (url != null) {
            mCache.remove(url);
        }
    }

    public void evict(@Nullable Uri uri) {
        if (uri != null) {
            evict(uri.toString());
        }
    }

    public void clearMemory() {
        mCache.evictAll();
    }

    /** Circle-crop used for contact avatars (was drawee RoundingParams). */
    @NonNull
    public static Bitmap toCircle(@NonNull Bitmap bitmap) {
        int side = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap out = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Rect rect = new Rect(0, 0, side, side);
        RectF rectF = new RectF(rect);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(0xFFFF0000);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        paint.setXfermode(null);
        return out;
    }
}