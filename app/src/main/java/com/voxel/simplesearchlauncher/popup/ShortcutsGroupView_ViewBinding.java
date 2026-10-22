package com.voxel.simplesearchlauncher.popup;

import android.view.View;
import android.widget.LinearLayout;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class ShortcutsGroupView_ViewBinding implements Unbinder {
    private ShortcutsGroupView target;

    public ShortcutsGroupView_ViewBinding(ShortcutsGroupView shortcutsGroupView, View view) {
        this.target = shortcutsGroupView;
        shortcutsGroupView.mShortcutsLayout = (LinearLayout) Utils.findRequiredViewAsType(view, R.id.deep_shortcuts, "field 'mShortcutsLayout'", LinearLayout.class);
    }

    @Override // butterknife.Unbinder
    public void unbind() {
        ShortcutsGroupView shortcutsGroupView = this.target;
        if (shortcutsGroupView == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.target = null;
        shortcutsGroupView.mShortcutsLayout = null;
    }
}
