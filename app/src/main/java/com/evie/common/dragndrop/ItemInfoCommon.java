package com.evie.common.dragndrop;

import android.content.Intent;
import android.graphics.Bitmap;

/* JADX INFO: loaded from: classes.dex */
public class ItemInfoCommon {
    public Bitmap iconBitmap;
    public Intent intent;
    public String itemInfoType;
    public CharSequence title;

    public ItemInfoCommon() {
    }

    public ItemInfoCommon(CharSequence charSequence, Intent intent, Bitmap bitmap, String str) {
        this.title = charSequence;
        this.intent = intent;
        this.iconBitmap = bitmap;
        this.itemInfoType = str;
    }
}
