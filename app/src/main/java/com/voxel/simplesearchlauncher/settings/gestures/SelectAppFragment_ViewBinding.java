package com.voxel.simplesearchlauncher.settings.gestures;

import android.view.View;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.voxel.simplesearchlauncher.allapps.AllAppsRecyclerView;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class SelectAppFragment_ViewBinding implements Unbinder {
    private SelectAppFragment target;

    public SelectAppFragment_ViewBinding(SelectAppFragment selectAppFragment, View view) {
        this.target = selectAppFragment;
        selectAppFragment.mAllAppsRecyclerView = (AllAppsRecyclerView) Utils.findRequiredViewAsType(view, R.id.recycler_view, "field 'mAllAppsRecyclerView'", AllAppsRecyclerView.class);
    }

    @Override // butterknife.Unbinder
    public void unbind() {
        SelectAppFragment selectAppFragment = this.target;
        if (selectAppFragment == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.target = null;
        selectAppFragment.mAllAppsRecyclerView = null;
    }
}
