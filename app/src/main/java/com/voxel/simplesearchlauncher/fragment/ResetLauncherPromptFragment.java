package com.voxel.simplesearchlauncher.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import com.voxel.simplesearchlauncher.activity.ResetLauncherActivity;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import dagger.android.support.AndroidSupportInjection;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class ResetLauncherPromptFragment extends PopupOptionFragment {
    AnalyticsHandler mAnalyticsHandler;
    public static final String PREFIX = ResetLauncherPromptFragment.class.getCanonicalName() + ".";
    public static final String DID_IMPORT_KEY = PREFIX + "did_import";
    private boolean mDidImport = false;
    private boolean mResetHomeOnClose = false;
    private Activity mResetHomeActivity = null;

    public static ResetLauncherPromptFragment getInstance(boolean z) {
        ResetLauncherPromptFragment resetLauncherPromptFragment = new ResetLauncherPromptFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(DID_IMPORT_KEY, z);
        resetLauncherPromptFragment.setArguments(bundle);
        return resetLauncherPromptFragment;
    }

    static /* synthetic */ void lambda$getNegativeClickListener$0(View view) {
    }

    public void lambda$getPositiveClickListener$1(View view) {
        this.mResetHomeOnClose = true;
        this.mResetHomeActivity = this.getActivity();
    }

    @Override // com.voxel.simplesearchlauncher.fragment.PopupOptionFragment
    protected Drawable getHeaderImageDrawable() {
        return getResources().getDrawable(R.drawable.launcher_icon);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.PopupOptionFragment
    protected CharSequence getMainText() {
        Resources resources;
        int i;
        if (this.mDidImport) {
            resources = getResources();
            i = R.string.reset_default_home_import_prompt;
        } else {
            resources = getResources();
            i = R.string.reset_default_home_prompt;
        }
        return resources.getText(i);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.PopupOptionFragment
    protected View.OnClickListener getNegativeClickListener() {
        return new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$ResetLauncherPromptFragment$T0t-2SQsPC5V6SXdHvYsnZP7l2o
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ResetLauncherPromptFragment.lambda$getNegativeClickListener$0(view);
            }
        };
    }

    @Override // com.voxel.simplesearchlauncher.fragment.PopupOptionFragment
    protected CharSequence getNegativeText() {
        return getResources().getText(R.string.reset_default_home_later);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.PopupOptionFragment
    protected View.OnClickListener getPositiveClickListener() {
        return new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$ResetLauncherPromptFragment$3zAOX_l-idVrXvEhBrWeqvuumMY
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                ResetLauncherPromptFragment.this.lambda$getPositiveClickListener$1( view);
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
        if (getArguments() != null) {
            this.mDidImport = getArguments().getBoolean(DID_IMPORT_KEY, false);
        }
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    protected void onFragmentDidClose() {
        super.onFragmentDidClose();
        if (this.mResetHomeOnClose) {
            ResetLauncherActivity.resetHome(this.mResetHomeActivity, true, true);
        }
    }

    @Override // com.voxel.simplesearchlauncher.fragment.SpringCardFragment
    public boolean shouldCloseOnBackgroundClick() {
        return false;
    }
}
