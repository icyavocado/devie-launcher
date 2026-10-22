package com.voxel.launcher3;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import com.evie.common.dragndrop.ItemInfoCommon;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.compat.UserManagerCompat;
import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
public class ItemInfo extends ItemInfoCommon {
    public int cellX;
    public int cellY;
    public long container;
    CharSequence contentDescription;
    int[] dropPos;
    public long id;
    public int itemType;
    public int minSpanX;
    public int minSpanY;
    public int rank;
    public long screenId;
    public int spanX;
    public int spanY;
    public UserHandleCompat user;

    public ItemInfo() {
        this.id = -1L;
        this.container = -1L;
        this.screenId = -1L;
        this.cellX = -1;
        this.cellY = -1;
        this.spanX = 1;
        this.spanY = 1;
        this.minSpanX = 1;
        this.minSpanY = 1;
        this.rank = 0;
        this.dropPos = null;
        this.user = UserHandleCompat.myUserHandle();
    }

    public ItemInfo(ItemInfo itemInfo) {
        this.id = -1L;
        this.container = -1L;
        this.screenId = -1L;
        this.cellX = -1;
        this.cellY = -1;
        this.spanX = 1;
        this.spanY = 1;
        this.minSpanX = 1;
        this.minSpanY = 1;
        this.rank = 0;
        this.dropPos = null;
        copyFrom(itemInfo);
    }

    static void writeBitmap(ContentValues contentValues, Bitmap bitmap) {
        if (bitmap != null) {
            contentValues.put("icon", Utilities.flattenBitmap(bitmap));
        }
    }

    public void copyFrom(ItemInfo itemInfo) {
        this.id = itemInfo.id;
        this.cellX = itemInfo.cellX;
        this.cellY = itemInfo.cellY;
        this.spanX = itemInfo.spanX;
        this.spanY = itemInfo.spanY;
        this.rank = itemInfo.rank;
        this.screenId = itemInfo.screenId;
        this.itemType = itemInfo.itemType;
        this.container = itemInfo.container;
        this.user = itemInfo.user;
        this.contentDescription = itemInfo.contentDescription;
    }

    public Intent getIntent() {
        return null;
    }

    public ComponentName getTargetComponent() {
        if (getIntent() == null) {
            return null;
        }
        return getIntent().getComponent();
    }

    void onAddToDatabase(Context context, ContentValues contentValues) {
        contentValues.put("itemType", Integer.valueOf(this.itemType));
        contentValues.put("container", Long.valueOf(this.container));
        contentValues.put("screen", Long.valueOf(this.screenId));
        contentValues.put("cellX", Integer.valueOf(this.cellX));
        contentValues.put("cellY", Integer.valueOf(this.cellY));
        contentValues.put("spanX", Integer.valueOf(this.spanX));
        contentValues.put("spanY", Integer.valueOf(this.spanY));
        contentValues.put("rank", Integer.valueOf(this.rank));
        contentValues.put("profileId", Long.valueOf(UserManagerCompat.getInstance(context).getSerialNumberForUser(this.user)));
        if (this.screenId == -201) {
            throw new RuntimeException("Screen id should not be EXTRA_EMPTY_SCREEN_ID");
        }
    }

    public String toString() {
        return "Item(id=" + this.id + " type=" + this.itemType + " container=" + this.container + " screen=" + this.screenId + " cellX=" + this.cellX + " cellY=" + this.cellY + " spanX=" + this.spanX + " spanY=" + this.spanY + " dropPos=" + Arrays.toString(this.dropPos) + " user=" + this.user + ")";
    }

    void unbind() {
    }
}
