package com.voxel.simplesearchlauncher.settings;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class CustomGestureSettingsFragment_ViewBinding implements Unbinder {
    private CustomGestureSettingsFragment target;

    public CustomGestureSettingsFragment_ViewBinding(CustomGestureSettingsFragment customGestureSettingsFragment, View view) {
        this.target = customGestureSettingsFragment;
        customGestureSettingsFragment.mRecyclerView = (RecyclerView) Utils.findRequiredViewAsType(view, R.id.recycler_view, "field 'mRecyclerView'", RecyclerView.class);
    }

    @Override // butterknife.Unbinder
    public void unbind() {
        CustomGestureSettingsFragment customGestureSettingsFragment = this.target;
        if (customGestureSettingsFragment == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.target = null;
        customGestureSettingsFragment.mRecyclerView = null;
    }
}
