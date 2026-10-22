package com.voxel.simplesearchlauncher.adapter;

import android.app.Activity;
import com.voxel.simplesearchlauncher.model.itemdata.SearchItemData;

/* JADX INFO: loaded from: classes.dex */
public class ListSeparator extends SearchItemData {
    @Override // com.voxel.simplesearchlauncher.model.itemdata.SearchItemData
    public String getItemId() {
        return "__list_separator__";
    }

    @Override // com.voxel.simplesearchlauncher.model.itemdata.SearchItemData
    public void handleAction(SearchItemData.ActionType actionType, Activity activity) {
    }
}
