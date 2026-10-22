package com.voxel.launcher3.accessibility;

import android.view.View;
import android.view.accessibility.AccessibilityManager;

/* JADX INFO: loaded from: classes.dex */
public class DragViewStateAnnouncer implements Runnable {
    private final View mTargetView;

    private DragViewStateAnnouncer(View view) {
        this.mTargetView = view;
    }

    public static DragViewStateAnnouncer createFor(View view) {
        if (((AccessibilityManager) view.getContext().getSystemService("accessibility")).isEnabled()) {
            return new DragViewStateAnnouncer(view);
        }
        return null;
    }

    @Override // java.lang.Runnable
    public void run() {
        this.mTargetView.sendAccessibilityEvent(4);
    }
}
