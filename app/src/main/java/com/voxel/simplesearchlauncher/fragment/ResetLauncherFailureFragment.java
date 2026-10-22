package com.voxel.simplesearchlauncher.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import com.voxel.simplesearchlauncher.activity.ResetLauncherActivity;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import dagger.android.support.AndroidSupportInjection;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class ResetLauncherFailureFragment extends PopupOptionFragment {
    AnalyticsHandler mAnalyticsHandler;
    private boolean mResetHomeOnClose = false;
    private Activity mResetHomeActivity = null;

    static /* synthetic */ void lambda$getNegativeClickListener$0(View view) {
    }

    public void lambda$getPositiveClickListener$1(View view) {
        this.mResetHomeOnClose = true;
        this.mResetHomeActivity = this.getActivity();
    }

    @Override // com.voxel.simplesearchlauncher.fragment.PopupOptionFragment
    protected Drawable getHeaderImageDrawable() {
        return getResources().getDrawable(R.drawable.sad_emoji);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.PopupOptionFragment
    protected CharSequence getMainText() {
        return getResources().getText(R.string.reset_default_home_failure);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.PopupOptionFragment
    protected View.OnClickListener getNegativeClickListener() {
        return new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$ResetLauncherFailureFragment$0m0o1-pZGrbrDde8JgpBaTnG_wc
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ResetLauncherFailureFragment.lambda$getNegativeClickListener$0(view);
            }
        };
    }

    @Override // com.voxel.simplesearchlauncher.fragment.PopupOptionFragment
    protected CharSequence getNegativeText() {
        return getResources().getText(R.string.reset_default_home_later);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.PopupOptionFragment
    protected View.OnClickListener getPositiveClickListener() {
        return new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$ResetLauncherFailureFragment$rHL55wcfmpeUSbm9cPxhK8YHiNs
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ResetLauncherFailureFragment.this.lambda$getPositiveClickListener$1( view);
            }
        };
    }

    @Override // com.voxel.simplesearchlauncher.fragment.PopupOptionFragment
    protected CharSequence getPositiveText() {
        return getResources().getText(android.R.string.ok);
    }

    @Override // androidx.fragment.app.Fragment
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    protected void onFragmentDidClose() {
        super.onFragmentDidClose();
        if (this.mResetHomeOnClose) {
            ResetLauncherActivity.resetHome(this.mResetHomeActivity, true, true);
        }
    }
}
