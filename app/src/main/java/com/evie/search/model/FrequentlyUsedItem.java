package com.evie.search.model;

import android.content.Intent;

/* JADX INFO: loaded from: classes.dex */
public class FrequentlyUsedItem {
    private String imageUrl;
    private Intent intent;

    public FrequentlyUsedItem(Intent intent) {
        this.intent = intent;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public Intent getIntent() {
        return this.intent;
    }

    public void setImageUrl(String str) {
        this.imageUrl = str;
    }
}
