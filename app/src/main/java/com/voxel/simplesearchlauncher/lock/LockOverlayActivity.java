package com.voxel.simplesearchlauncher.lock;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import com.voxel.launcher3.FragmentHelper;
import com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class LockOverlayActivity extends FragmentActivity implements LauncherStackableFragment.OnFragmentInteractionListener {
    private FragmentHelper mFragmentHelper;

    @Override // androidx.fragment.app.FragmentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.lock_overlay_activity);
        this.mFragmentHelper = new FragmentHelper(this);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment.OnFragmentInteractionListener
    public void onStackableFragmentRequestClose(LauncherStackableFragment launcherStackableFragment) {
        this.mFragmentHelper.onStackableFragmentRequestClose(launcherStackableFragment);
        finish();
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment.OnFragmentInteractionListener
    public void onStackableFragmentWillRequestClose(LauncherStackableFragment launcherStackableFragment) {
        this.mFragmentHelper.onStackableFragmentWillRequestClose(launcherStackableFragment);
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onStart() {
        super.onStart();
        pushStackFragment(new LockOverlayFragment());
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment.OnFragmentInteractionListener
    public void popStackFragment(LauncherStackableFragment launcherStackableFragment, boolean z) {
        this.mFragmentHelper.popStackFragment(launcherStackableFragment, z);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment.OnFragmentInteractionListener
    public boolean providesTransitionAnimation() {
        return false;
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment.OnFragmentInteractionListener
    public void pushStackFragment(LauncherStackableFragment launcherStackableFragment) {
        this.mFragmentHelper.pushStackFragment(R.id.container, launcherStackableFragment);
    }
}
