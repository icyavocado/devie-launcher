package com.voxel.simplesearchlauncher.appoptions;

import android.view.View;
import android.widget.ImageView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.voxel.launcher3.ExtendedEditText;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class AppOptionsFragment_ViewBinding implements Unbinder {
    private AppOptionsFragment target;
    private View view7f0a009f;

    public AppOptionsFragment_ViewBinding(final AppOptionsFragment appOptionsFragment, View view) {
        this.target = appOptionsFragment;
        appOptionsFragment.mIconImageView = (ImageView) Utils.findRequiredViewAsType(view, R.id.icon, "field 'mIconImageView'", ImageView.class);
        appOptionsFragment.mIconLabel = (ExtendedEditText) Utils.findRequiredViewAsType(view, R.id.text, "field 'mIconLabel'", ExtendedEditText.class);
        View viewFindRequiredView = Utils.findRequiredView(view, R.id.close, "field 'mRevertButton' and method 'onCloseClick'");
        appOptionsFragment.mRevertButton = viewFindRequiredView;
        this.view7f0a009f = viewFindRequiredView;
        viewFindRequiredView.setOnClickListener(new DebouncingOnClickListener() { // from class: com.voxel.simplesearchlauncher.appoptions.AppOptionsFragment_ViewBinding.1
            @Override // butterknife.internal.DebouncingOnClickListener
            public void doClick(View view2) {
                appOptionsFragment.onCloseClick();
            }
        });
    }

    @Override // butterknife.Unbinder
    public void unbind() {
        AppOptionsFragment appOptionsFragment = this.target;
        if (appOptionsFragment == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.target = null;
        appOptionsFragment.mIconImageView = null;
        appOptionsFragment.mIconLabel = null;
        appOptionsFragment.mRevertButton = null;
        this.view7f0a009f.setOnClickListener(null);
        this.view7f0a009f = null;
    }
}
