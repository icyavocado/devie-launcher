package com.voxel.simplesearchlauncher.model.itemdata;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.voxel.simplesearchlauncher.utils.HashUtil;
import java.io.Serializable;
import java.util.Objects;

/* JADX INFO: loaded from: classes2.dex */
public abstract class SearchItemData implements Serializable {
    protected String label;

    public enum ActionType {
        CLICK
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SearchItemData searchItemData = (SearchItemData) obj;
        return Objects.equals(getLabel(), searchItemData.getLabel()) && Objects.equals(getItemId(), searchItemData.getItemId());
    }

    public Intent getIntent(Context context) {
        return null;
    }

    public abstract String getItemId();

    public String getLabel() {
        return this.label;
    }

    public abstract void handleAction(ActionType actionType, Activity activity);

    public int hashCode() {
        return HashUtil.hash(HashUtil.hash(23, this.label), getItemId());
    }

    public synchronized void setLabel(String str) {
        if (str != null) {
            str = str.trim();
        }
        this.label = str;
    }
}
