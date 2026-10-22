package com.voxel.simplesearchlauncher.settings.gestures;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class SelectGestureActionFragment_ViewBinding implements Unbinder {
    private SelectGestureActionFragment target;

    public SelectGestureActionFragment_ViewBinding(SelectGestureActionFragment selectGestureActionFragment, View view) {
        this.target = selectGestureActionFragment;
        selectGestureActionFragment.mRecyclerView = (RecyclerView) Utils.findRequiredViewAsType(view, R.id.recycler_view, "field 'mRecyclerView'", RecyclerView.class);
    }

    @Override // butterknife.Unbinder
    public void unbind() {
        SelectGestureActionFragment selectGestureActionFragment = this.target;
        if (selectGestureActionFragment == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.target = null;
        selectGestureActionFragment.mRecyclerView = null;
    }
}
