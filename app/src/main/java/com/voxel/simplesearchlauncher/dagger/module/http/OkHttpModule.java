package com.voxel.simplesearchlauncher.dagger.module.http;

import okhttp3.OkHttpClient;

/* JADX INFO: loaded from: classes2.dex */
public class OkHttpModule {
    public OkHttpClient providesOkHttpClient() {
        return new OkHttpClient.Builder().build();
    }
}
