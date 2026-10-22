package com.voxel.simplesearchlauncher.popup;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class ShortcutItemView_ViewBinding implements Unbinder {
    private ShortcutItemView target;

    public ShortcutItemView_ViewBinding(ShortcutItemView shortcutItemView, View view) {
        this.target = shortcutItemView;
        shortcutItemView.mIconView = (ImageView) Utils.findRequiredViewAsType(view, R.id.icon, "field 'mIconView'", ImageView.class);
        shortcutItemView.mTextView = (TextView) Utils.findRequiredViewAsType(view, R.id.text, "field 'mTextView'", TextView.class);
        shortcutItemView.mHandleView = Utils.findRequiredView(view, R.id.handle, "field 'mHandleView'");
        shortcutItemView.mDividerView = Utils.findRequiredView(view, R.id.divider, "field 'mDividerView'");
    }

    @Override // butterknife.Unbinder
    public void unbind() {
        ShortcutItemView shortcutItemView = this.target;
        if (shortcutItemView == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.target = null;
        shortcutItemView.mIconView = null;
        shortcutItemView.mTextView = null;
        shortcutItemView.mHandleView = null;
        shortcutItemView.mDividerView = null;
    }
}
