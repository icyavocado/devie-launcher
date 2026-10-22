package com.voxel.launcher3;

import android.view.KeyEvent;
import android.view.View;

/* JADX INFO: compiled from: FocusHelper.java */
/* JADX INFO: loaded from: classes.dex */
class IconKeyEventListener implements View.OnKeyListener {
    IconKeyEventListener() {
    }

    @Override // android.view.View.OnKeyListener
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        return FocusHelper.handleIconKeyEvent(view, i, keyEvent);
    }
}
