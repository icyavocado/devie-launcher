package com.voxel.launcher3;

import android.content.ContentValues;
import android.content.Context;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/* JADX INFO: loaded from: classes.dex */
public class FolderInfo extends ItemInfo {
    public ArrayList<ShortcutInfo> contents = new ArrayList<>();
    ArrayList<FolderListener> listeners = new ArrayList<>();
    public boolean opened;
    public int options;

    public interface FolderListener {
        void onAdd(ShortcutInfo shortcutInfo);

        void onItemsChanged();

        void onRemove(ShortcutInfo shortcutInfo);

        void onTitleChanged(CharSequence charSequence);
    }

    public FolderInfo() {
        this.itemType = 2;
        this.user = UserHandleCompat.myUserHandle();
    }

    public void add(ShortcutInfo shortcutInfo) {
        this.contents.add(shortcutInfo);
        if (SettingsStorage.getInstance().getFolderAlphabetSortEnabled()) {
            Collections.sort(this.contents, Folder.ALPHA_COMPARATOR);
        }
        for (int i = 0; i < this.listeners.size(); i++) {
            this.listeners.get(i).onAdd(shortcutInfo);
        }
        itemsChanged();
    }

    public void addListener(FolderListener folderListener) {
        this.listeners.add(folderListener);
    }

    void itemsChanged() {
        for (int i = 0; i < this.listeners.size(); i++) {
            this.listeners.get(i).onItemsChanged();
        }
    }

    @Override // com.voxel.launcher3.ItemInfo
    void onAddToDatabase(Context context, ContentValues contentValues) {
        super.onAddToDatabase(context, contentValues);
        contentValues.put("title", this.title != null ? this.title.toString() : null);
        contentValues.put("options", Integer.valueOf(this.options));
    }

    public void remove(ShortcutInfo shortcutInfo) {
        this.contents.remove(shortcutInfo);
        for (int i = 0; i < this.listeners.size(); i++) {
            this.listeners.get(i).onRemove(shortcutInfo);
        }
        itemsChanged();
    }

    public void removeListener(FolderListener folderListener) {
        this.listeners.remove(folderListener);
    }

    public void setOption(int i, boolean z, Context context) {
        int i2 = this.options;
        if (z) {
            this.options = i | i2;
        } else {
            this.options = (i ^ (-1)) & i2;
        }
        if (context == null || i2 == this.options) {
            return;
        }
        LauncherModel.updateItemInDatabase(context, this);
    }

    public void setTitle(CharSequence charSequence) {
        this.title = charSequence;
        for (int i = 0; i < this.listeners.size(); i++) {
            this.listeners.get(i).onTitleChanged(charSequence);
        }
    }

    @Override // com.voxel.launcher3.ItemInfo
    public String toString() {
        return "FolderInfo(id=" + this.id + " type=" + this.itemType + " container=" + this.container + " screen=" + this.screenId + " cellX=" + this.cellX + " cellY=" + this.cellY + " spanX=" + this.spanX + " spanY=" + this.spanY + " dropPos=" + Arrays.toString(this.dropPos) + ")";
    }

    @Override // com.voxel.launcher3.ItemInfo
    void unbind() {
        super.unbind();
        this.listeners.clear();
    }
}
