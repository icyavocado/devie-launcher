package com.voxel.simplesearchlauncher.popup;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public enum SystemShortcut {
    REMOVE(R.drawable.remove_app_header, R.drawable.remove_app_row, R.string.delete_target_label),
    RESIZE(R.drawable.resize_widget_row, R.drawable.resize_widget_row, R.string.resize_label),
    APP_OPTIONS(R.drawable.ic_app_settings_header, R.drawable.ic_app_settings_black_24px, R.string.app_options_label);

    private final int mHeaderIconResId;
    private final int mLabelResId;
    private final int mMainIconResId;

    SystemShortcut(int i, int i2, int i3) {
        this.mHeaderIconResId = i;
        this.mMainIconResId = i2;
        this.mLabelResId = i3;
    }

    public Drawable getHeaderIcon(Context context) {
        return context.getResources().getDrawable(this.mHeaderIconResId);
    }

    public String getLabel(Context context) {
        return context.getString(this.mLabelResId);
    }

    public Drawable getMainIcon(Context context) {
        return context.getResources().getDrawable(this.mMainIconResId);
    }
}
