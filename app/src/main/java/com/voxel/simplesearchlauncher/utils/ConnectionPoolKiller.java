package com.voxel.simplesearchlauncher.utils;

/* REWORK: minSdk-safe receiver registration (API 34+ flag rule) */
import androidx.core.content.ContextCompat;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import okhttp3.OkHttpClient;
import timber.log.Timber;

/* JADX INFO: loaded from: classes2.dex */
public class ConnectionPoolKiller {
    private BroadcastReceiver mBroadcastReceiver;
    private final Context mContext;
    private final OkHttpClient mOkHttpClient;

    public ConnectionPoolKiller(Context context, OkHttpClient okHttpClient) {
        this.mContext = context;
        this.mOkHttpClient = okHttpClient;
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.voxel.simplesearchlauncher.utils.ConnectionPoolKiller$2] */
    public void clearConnectionPool() {
        new AsyncTask<Void, Void, Void>() { // from class: com.voxel.simplesearchlauncher.utils.ConnectionPoolKiller.2
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Void doInBackground(Void... voidArr) {
                Timber.d("clearConnectionPool", new Object[0]);
                ConnectionPoolKiller.this.mOkHttpClient.connectionPool().evictAll();
                return null;
            }
        }.execute(new Void[0]);
    }

    public synchronized void registerReceiver() {
        if (this.mBroadcastReceiver != null) {
            this.mBroadcastReceiver = new BroadcastReceiver() { // from class: com.voxel.simplesearchlauncher.utils.ConnectionPoolKiller.1
                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    Timber.d("Connectivity changed", new Object[0]);
                    ConnectionPoolKiller.this.clearConnectionPool();
                }
            };
        }
        ContextCompat.registerReceiver(this.mContext, this.mBroadcastReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"), ContextCompat.RECEIVER_NOT_EXPORTED);
    }
}
