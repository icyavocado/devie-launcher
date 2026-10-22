package com.evie.screen;

import okhttp3.OkHttpClient;

/* JADX INFO: loaded from: classes.dex */
public final class TermsAndConditionsActivity_MembersInjector {
    public static void injectMClient(TermsAndConditionsActivity termsAndConditionsActivity, OkHttpClient okHttpClient) {
        termsAndConditionsActivity.mClient = okHttpClient;
    }
}
