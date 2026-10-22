package com.voxel.simplesearchlauncher.popup;

import android.view.View;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class PopupFragment_ViewBinding implements Unbinder {
    private PopupFragment target;

    public PopupFragment_ViewBinding(PopupFragment popupFragment, View view) {
        this.target = popupFragment;
        popupFragment.mPopupContainer = (PopupContainerWithArrow) Utils.findRequiredViewAsType(view, R.id.popup_container, "field 'mPopupContainer'", PopupContainerWithArrow.class);
        popupFragment.mBackgroundWash = Utils.findRequiredView(view, R.id.background_wash, "field 'mBackgroundWash'");
    }

    @Override // butterknife.Unbinder
    public void unbind() {
        PopupFragment popupFragment = this.target;
        if (popupFragment == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.target = null;
        popupFragment.mPopupContainer = null;
        popupFragment.mBackgroundWash = null;
    }
}
