package com.voxel.launcher3.fresco;

import android.os.Looper;
import android.os.SystemClock;
import com.facebook.common.logging.FLog;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.producers.BaseNetworkFetcher;
import com.facebook.imagepipeline.producers.BaseProducerContextCallbacks;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.FetchState;
import com.facebook.imagepipeline.producers.NetworkFetcher;
import com.facebook.imagepipeline.producers.ProducerContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

/* JADX INFO: loaded from: classes.dex */
public class RetryOkHttpNetworkFetcher extends BaseNetworkFetcher<RetryOkHttpNetworkFetchState> {
    private final Call.Factory mCallFactory;
    private Executor mCancellationExecutor;

    /* JADX INFO: renamed from: com.voxel.launcher3.fresco.RetryOkHttpNetworkFetcher$1, reason: invalid class name */
    class AnonymousClass1 extends BaseProducerContextCallbacks {
        final /* synthetic */ Call val$call;

        AnonymousClass1(Call call) {
            this.val$call = call;
        }

        @Override // com.facebook.imagepipeline.producers.BaseProducerContextCallbacks, com.facebook.imagepipeline.producers.ProducerContextCallbacks
        public void onCancellationRequested() {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                this.val$call.cancel();
                return;
            }
            Executor executor = RetryOkHttpNetworkFetcher.this.mCancellationExecutor;
            final Call call = this.val$call;
            executor.execute(new Runnable() { // from class: com.voxel.launcher3.fresco.-$$Lambda$RetryOkHttpNetworkFetcher$1$7t5SjLTubmKVHe9dUFjcpfkWXkw
                @Override // java.lang.Runnable
                public final void run() {
                    call.cancel();
                }
            });
        }
    }

    public RetryOkHttpNetworkFetcher(Call.Factory factory, Executor executor) {
        this.mCallFactory = factory;
        this.mCancellationExecutor = executor;
    }

    public RetryOkHttpNetworkFetcher(OkHttpClient okHttpClient) {
        this(okHttpClient, okHttpClient.dispatcher().executorService());
    }

    private void fetchWithRequest(final RetryOkHttpNetworkFetchState retryOkHttpNetworkFetchState, final NetworkFetcher.Callback callback, Request request, final int i) {
        Timber.d("fetchWithRequest: %d, %s", Integer.valueOf(i), request.url());
        Call callNewCall = this.mCallFactory.newCall(request);
        retryOkHttpNetworkFetchState.getContext().addCallbacks(new AnonymousClass1(callNewCall));
        callNewCall.enqueue(new okhttp3.Callback() { // from class: com.voxel.launcher3.fresco.RetryOkHttpNetworkFetcher.2
            @Override // okhttp3.Callback
            public void onFailure(Call call, IOException iOException) {
                RetryOkHttpNetworkFetcher.this.handleException(retryOkHttpNetworkFetchState, call, iOException, callback, i);
            }

            @Override // okhttp3.Callback
            public void onResponse(Call call, Response response) {
                Timber.d("onResponse: %s", call.request().url());
                retryOkHttpNetworkFetchState.responseTime = SystemClock.elapsedRealtime();
                ResponseBody responseBodyBody = response.body();
                try {
                    try {
                        try {
                            if (response.isSuccessful()) {
                                long jContentLength = responseBodyBody.contentLength();
                                if (jContentLength < 0) {
                                    jContentLength = 0;
                                }
                                callback.onResponse(responseBodyBody.byteStream(), (int) jContentLength);
                                responseBodyBody.close();
                                return;
                            }
                            RetryOkHttpNetworkFetcher.this.handleException(retryOkHttpNetworkFetchState, call, new IOException("Unexpected HTTP code " + response), callback, i);
                            try {
                                responseBodyBody.close();
                            } catch (Exception e) {
                                FLog.w("OkHttpNetworkFetchProducer", "Exception when closing response body", e);
                            }
                        } catch (Exception e2) {
                            RetryOkHttpNetworkFetcher.this.handleException(retryOkHttpNetworkFetchState, call, e2, callback, i);
                            responseBodyBody.close();
                        }
                    } catch (Exception e3) {
                        FLog.w("OkHttpNetworkFetchProducer", "Exception when closing response body", e3);
                    }
                } catch (Throwable th) {
                    try {
                        responseBodyBody.close();
                    } catch (Exception e4) {
                        FLog.w("OkHttpNetworkFetchProducer", "Exception when closing response body", e4);
                    }
                    throw th;
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleException(RetryOkHttpNetworkFetchState retryOkHttpNetworkFetchState, Call call, Exception exc, NetworkFetcher.Callback callback, int i) {
        Timber.e(exc, "handleException: %d, %s", Integer.valueOf(i), call.request().url());
        if (call.isCanceled()) {
            callback.onCancellation();
        } else if (i > 2) {
            callback.onFailure(exc);
        } else {
            fetchWithRequest(retryOkHttpNetworkFetchState, callback, call.request(), i + 1);
        }
    }

    @Override // com.facebook.imagepipeline.producers.NetworkFetcher
    public RetryOkHttpNetworkFetchState createFetchState(Consumer<EncodedImage> consumer, ProducerContext producerContext) {
        Timber.d("createFetchState", new Object[0]);
        return new RetryOkHttpNetworkFetchState(consumer, producerContext);
    }

    @Override // com.facebook.imagepipeline.producers.NetworkFetcher
    public void fetch(RetryOkHttpNetworkFetchState retryOkHttpNetworkFetchState, NetworkFetcher.Callback callback) {
        Timber.d("fetch", new Object[0]);
        retryOkHttpNetworkFetchState.submitTime = SystemClock.elapsedRealtime();
        try {
            fetchWithRequest(retryOkHttpNetworkFetchState, callback, new Request.Builder().cacheControl(new CacheControl.Builder().noStore().build()).url(retryOkHttpNetworkFetchState.getUri().toString()).get().build(), 0);
        } catch (Exception e) {
            Timber.d(e, "fetch", new Object[0]);
            callback.onFailure(e);
        }
    }

    @Override // com.facebook.imagepipeline.producers.BaseNetworkFetcher, com.facebook.imagepipeline.producers.NetworkFetcher
    public Map<String, String> getExtraMap(RetryOkHttpNetworkFetchState retryOkHttpNetworkFetchState, int i) {
        HashMap map = new HashMap(4);
        map.put("queue_time", Long.toString(retryOkHttpNetworkFetchState.responseTime - retryOkHttpNetworkFetchState.submitTime));
        map.put("fetch_time", Long.toString(retryOkHttpNetworkFetchState.fetchCompleteTime - retryOkHttpNetworkFetchState.responseTime));
        map.put("total_time", Long.toString(retryOkHttpNetworkFetchState.fetchCompleteTime - retryOkHttpNetworkFetchState.submitTime));
        map.put("image_size", Integer.toString(i));
        return map;
    }

    @Override // com.facebook.imagepipeline.producers.BaseNetworkFetcher, com.facebook.imagepipeline.producers.NetworkFetcher
    public void onFetchCompletion(RetryOkHttpNetworkFetchState retryOkHttpNetworkFetchState, int i) {
        retryOkHttpNetworkFetchState.fetchCompleteTime = SystemClock.elapsedRealtime();
    }
}
