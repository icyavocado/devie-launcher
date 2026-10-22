package com.voxel.simplesearchlauncher.settings;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.simplesearchlauncher.activity.SafeActivityStarter;
import com.voxel.simplesearchlauncher.view.MockLauncherController;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class TransparentSettingsActivity extends AppCompatActivity implements SafeActivityStarter, MockLauncherController.MockLauncherHolder {
    private static final String TAG = "TransparentSettingsActivity";

    public void lambda$onCreate$0(int i) {
        int backStackEntryCount = this.getSupportFragmentManager().getBackStackEntryCount() - 1;
        if (backStackEntryCount < 0) {
            this.getSupportActionBar().setTitle(i);
        } else {
            this.getSupportActionBar().setTitle(this.getSupportFragmentManager().getBackStackEntryAt(backStackEntryCount).getBreadCrumbTitleRes());
        }
    }

    private boolean startActivity(View view, Intent intent, Object obj) {
        intent.addFlags(268435456);
        try {
            startActivity(intent);
            return true;
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.msg_no_generic_permission, 0).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent + ". Make sure to create a MAIN intent-filter for the corresponding activity or use the exported attribute for this activity. tag=" + obj + " intent=" + intent, e);
            return false;
        } catch (Exception unused) {
            return false;
        }
    }

    @Override // com.voxel.simplesearchlauncher.view.MockLauncherController.MockLauncherHolder
    public ViewGroup getRootView() {
        return (ViewGroup) findViewById(R.id.root);
    }

    public FrameLayout getToolbarContainer() {
        return (FrameLayout) findViewById(R.id.toolbar_container);
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        LauncherAppState.setApplicationContext(getApplicationContext());
        LauncherAppState.getInstance().initDynamicGrid(this);
        setContentView(R.layout.settings_activity);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        String stringExtra = getIntent().getStringExtra("show_fragment");
        final int intExtra = getIntent().getIntExtra("show_title", 0);
        Fragment fragmentInstantiate = Fragment.instantiate(this, stringExtra, bundle);
        FragmentTransaction fragmentTransactionBeginTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransactionBeginTransaction.replace(R.id.container, fragmentInstantiate);
        fragmentTransactionBeginTransaction.commit();
        getSupportActionBar().setTitle(intExtra);
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$TransparentSettingsActivity$LS88FYDTrxrft1L27pDUuxJw3K4
            @Override // androidx.fragment.app.FragmentManager.OnBackStackChangedListener
            public final void onBackStackChanged() {
                TransparentSettingsActivity.this.lambda$onCreate$0( intExtra);
            }
        });
    }

    @Override // androidx.appcompat.app.AppCompatActivity
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return false;
        }
        finish();
        return true;
    }

    @Override // com.voxel.simplesearchlauncher.activity.SafeActivityStarter
    public boolean startActivitySafely(Intent intent) {
        return startActivitySafely(null, intent, null);
    }

    boolean startActivitySafely(View view, Intent intent, Object obj) {
        return startActivitySafely(view, intent, obj, R.string.activity_not_found);
    }

    boolean startActivitySafely(View view, Intent intent, Object obj, int i) {
        try {
            return startActivity(view, intent, obj);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, i, 0).show();
            Log.e(TAG, "Unable to launch. tag=" + obj + " intent=" + intent, e);
            return false;
        }
    }
}
