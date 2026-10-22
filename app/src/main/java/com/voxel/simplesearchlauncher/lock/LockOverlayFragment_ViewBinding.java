package com.voxel.simplesearchlauncher.lock;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class LockOverlayFragment_ViewBinding implements Unbinder {
    private LockOverlayFragment target;

    public LockOverlayFragment_ViewBinding(LockOverlayFragment lockOverlayFragment, View view) {
        this.target = lockOverlayFragment;
        lockOverlayFragment.mBackground = Utils.findRequiredView(view, R.id.background, "field 'mBackground'");
        lockOverlayFragment.mGroupContainer = Utils.findRequiredView(view, R.id.container, "field 'mGroupContainer'");
        lockOverlayFragment.mControlGroup = Utils.findRequiredView(view, R.id.control_group, "field 'mControlGroup'");
        lockOverlayFragment.mFingerprintIcon = (ImageView) Utils.findRequiredViewAsType(view, R.id.fingerprint_icon, "field 'mFingerprintIcon'", ImageView.class);
        lockOverlayFragment.mUnlockIcon = (ImageView) Utils.findRequiredViewAsType(view, R.id.unlock_icon, "field 'mUnlockIcon'", ImageView.class);
        lockOverlayFragment.mText = (TextView) Utils.findRequiredViewAsType(view, R.id.text, "field 'mText'", TextView.class);
    }

    @Override // butterknife.Unbinder
    public void unbind() {
        LockOverlayFragment lockOverlayFragment = this.target;
        if (lockOverlayFragment == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.target = null;
        lockOverlayFragment.mBackground = null;
        lockOverlayFragment.mGroupContainer = null;
        lockOverlayFragment.mControlGroup = null;
        lockOverlayFragment.mFingerprintIcon = null;
        lockOverlayFragment.mUnlockIcon = null;
        lockOverlayFragment.mText = null;
    }
}
