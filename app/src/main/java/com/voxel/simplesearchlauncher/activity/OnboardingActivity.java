package com.voxel.simplesearchlauncher.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.simplesearchlauncher.activity.onboarding.PermissionsSlide;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.voxel.simplesearchlauncher.view.SlideLayout;
import dagger.android.AndroidInjection;
import com.ovacoda.devie.BuildConfig;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class OnboardingActivity extends FragmentActivity {
    private View mContentView;
    private ActivityCompat.OnRequestPermissionsResultCallback mOnRequestPermissionsResultCallback;
    boolean mPermissionMode;
    private SlideLayout mSlideLayout;

    @Override // android.app.Activity
    public void finish() {
        super.finish();
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onBackPressed() {
        if (this.mPermissionMode) {
            finish();
            return;
        }
        SlideLayout slideLayout = this.mSlideLayout;
        if (slideLayout == null || slideLayout.isAnimating()) {
            return;
        }
        /* REWORK: per-launcher import slides removed. */
    }

    @Override // androidx.fragment.app.FragmentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        AndroidInjection.inject(this);
        super.onCreate(null);
        this.mContentView = getLayoutInflater().inflate(R.layout.activity_onboarding, (ViewGroup) null);
        setContentView(this.mContentView);
        getWindow().clearFlags(201326592);
        this.mSlideLayout = (SlideLayout) this.mContentView.findViewById(R.id.slides);
        if (getIntent().getBooleanExtra("permission", false)) {
            SlideLayout slideLayout = this.mSlideLayout;
            slideLayout.addSlide(new PermissionsSlide(this, slideLayout), 0);
            this.mPermissionMode = true;
            return;
        }
        SlideLayout slideLayout2 = this.mSlideLayout;
        /* REWORK: intro promo-video slide removed — onboarding starts on permissions. */
        slideLayout2.addSlide(new PermissionsSlide(this, slideLayout2), 0);
        SharedPreferences.Editor editorEdit = getSharedPreferences(LauncherAppState.getSharedPreferencesKey(), 0).edit();
        editorEdit.putBoolean("launcher.first_run_activity_displayed", true);
        editorEdit.putBoolean("launcher.show_evie_two_intro", false);
        editorEdit.apply();
        SettingsStorage.getInstance().setFirstVersion(BuildConfig.VERSION_CODE);
        SettingsStorage.getInstance().setCurrentVersion(BuildConfig.VERSION_CODE);
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        this.mSlideLayout.removeAllViews();
        this.mSlideLayout = null;
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity, androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        ActivityCompat.OnRequestPermissionsResultCallback onRequestPermissionsResultCallback = this.mOnRequestPermissionsResultCallback;
        if (onRequestPermissionsResultCallback != null) {
            onRequestPermissionsResultCallback.onRequestPermissionsResult(i, strArr, iArr);
        }
    }

    public void setOnRequestPermissionsResultCallback(ActivityCompat.OnRequestPermissionsResultCallback onRequestPermissionsResultCallback) {
        this.mOnRequestPermissionsResultCallback = onRequestPermissionsResultCallback;
    }
}
