package com.voxel.launcher3.fresco;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import com.evie.common.HybridNetworkFetcherCommon;
import com.evie.common.iconpack.IconPackFilter;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpNetworkFetcher;
import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.producers.BaseNetworkFetcher;
import com.facebook.imagepipeline.producers.BaseProducerContextCallbacks;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.FetchState;
import com.facebook.imagepipeline.producers.NetworkFetcher;
import com.facebook.imagepipeline.producers.ProducerContext;
import com.voxel.launcher3.IconCache;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.simplesearchlauncher.model.itemdata.LocalAppData;
import com.ovacoda.devie.R;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import timber.log.Timber;

/* JADX INFO: loaded from: classes.dex */
public class HybridNetworkFetcher extends BaseNetworkFetcher<FetchState> {
    private final IconCache mIconCache;
    private final OkHttpClient mOkHttpClient;
    private final OkHttpNetworkFetcher mOkHttpNetworkFetcher;
    private final RetryOkHttpNetworkFetcher mRetryOkHttpNetworkFetcher;
    private final UserHandleCompat mUserHandleCompat;
    private final PriorityBlockingQueue<Runnable> mQ = new PriorityBlockingQueue<>(10, new RunnableComparator());
    private final ExecutorService mExecutorService = new PriorityThreadpoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, this.mQ, HybridNetworkFetcher.class.getSimpleName());

    public HybridNetworkFetcher(Context context, OkHttpClient okHttpClient) {
        if (context.getResources().getBoolean(R.bool.widget_enabled)) {
            this.mOkHttpClient = okHttpClient.newBuilder().readTimeout(2L, TimeUnit.SECONDS).build();
            this.mOkHttpNetworkFetcher = null;
            this.mRetryOkHttpNetworkFetcher = new RetryOkHttpNetworkFetcher(this.mOkHttpClient);
        } else {
            this.mOkHttpClient = okHttpClient;
            this.mOkHttpNetworkFetcher = new OkHttpNetworkFetcher(this.mOkHttpClient);
            this.mRetryOkHttpNetworkFetcher = null;
        }
        this.mIconCache = LauncherAppState.getInstance().getIconCache();
        this.mUserHandleCompat = UserHandleCompat.myUserHandle();
    }

    private void fetchForType(FetchState fetchState, NetworkFetcher.Callback callback) {
        OkHttpNetworkFetcher okHttpNetworkFetcher = this.mOkHttpNetworkFetcher;
        if (okHttpNetworkFetcher == null) {
            this.mRetryOkHttpNetworkFetcher.fetch((RetryOkHttpNetworkFetchState) fetchState, callback);
        } else {
            okHttpNetworkFetcher.fetch((OkHttpNetworkFetcher.OkHttpNetworkFetchState) fetchState, callback);
        }
    }

    private PriorityRunnable getActivityIconRunnable(Priority priority, final NetworkFetcher.Callback callback, final String str, final String str2, final String str3) {
        return new PriorityRunnable(priority) { // from class: com.voxel.launcher3.fresco.HybridNetworkFetcher.2
            @Override // java.lang.Runnable
            public void run() {
                try {
                    Bitmap iconBitmapNoCache = HybridNetworkFetcher.this.getIconBitmapNoCache(new LocalAppData(str, str2), str3);
                    if (iconBitmapNoCache == null) {
                        callback.onFailure(new RuntimeException("Invalid icon pack"));
                        return;
                    }
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    iconBitmapNoCache.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    callback.onResponse(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), -1);
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Bitmap getIconBitmapNoCache(LocalAppData localAppData, String str) {
        Intent intent = new Intent("android.intent.action.MAIN", (Uri) null);
        intent.addCategory("android.intent.category.LAUNCHER");
        if (!TextUtils.isEmpty(localAppData.getAppActivityName())) {
            intent.setClassName(localAppData.getPackageName(), localAppData.getActivityName());
        }
        synchronized (this.mIconCache) {
            if (!isValid(this.mIconCache.getIconPackFilter(), str)) {
                return null;
            }
            return this.mIconCache.getIconNoCache(intent, this.mUserHandleCompat);
        }
    }

    public static Uri getMagicResourceUri(LocalAppData localAppData) {
        return getMagicResourceUri(localAppData.getPackageName(), localAppData.getActivityName(), localAppData.getVersionCode());
    }

    public static Uri getMagicResourceUri(String str, String str2, int i) {
        return HybridNetworkFetcherCommon.getMagicResourceUri(str, str2, i);
    }

    private boolean isValid(IconPackFilter iconPackFilter, String str) {
        return TextUtils.equals(iconPackFilter != null ? iconPackFilter.getPackageName() : null, str);
    }

    @Override // com.facebook.imagepipeline.producers.NetworkFetcher
    public FetchState createFetchState(Consumer<EncodedImage> consumer, ProducerContext producerContext) {
        OkHttpNetworkFetcher okHttpNetworkFetcher = this.mOkHttpNetworkFetcher;
        return okHttpNetworkFetcher == null ? this.mRetryOkHttpNetworkFetcher.createFetchState(consumer, producerContext) : okHttpNetworkFetcher.createFetchState(consumer, producerContext);
    }

    @Override // com.facebook.imagepipeline.producers.NetworkFetcher
    public void fetch(FetchState fetchState, final NetworkFetcher.Callback callback) {
        Uri uri = fetchState.getUri();
        if (!uri.getHost().equals("example.com")) {
            Timber.d("fetch", new Object[0]);
            fetchForType(fetchState, callback);
            return;
        }
        if (!uri.getLastPathSegment().equals("476287f4b140f81a723002230fa368847065c97a")) {
            fetchForType(fetchState, callback);
            return;
        }
        String queryParameter = uri.getQueryParameter("pn");
        String queryParameter2 = uri.getQueryParameter("an");
        String queryParameter3 = uri.getQueryParameter("ri");
        PriorityRunnable activityIconRunnable = (queryParameter3 != null ? Integer.valueOf(queryParameter3).intValue() : -1) == -1 ? getActivityIconRunnable(fetchState.getContext().getPriority(), callback, queryParameter, queryParameter2, uri.getQueryParameter("ip")) : null;
        if (activityIconRunnable == null) {
            fetchForType(fetchState, callback);
        } else {
            final Future<?> futureSubmit = this.mExecutorService.submit(activityIconRunnable);
            fetchState.getContext().addCallbacks(new BaseProducerContextCallbacks() { // from class: com.voxel.launcher3.fresco.HybridNetworkFetcher.1
                @Override // com.facebook.imagepipeline.producers.BaseProducerContextCallbacks, com.facebook.imagepipeline.producers.ProducerContextCallbacks
                public void onCancellationRequested() {
                    if (futureSubmit.cancel(false)) {
                        callback.onCancellation();
                    }
                }
            });
        }
    }
}
