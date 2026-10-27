package com.voxel.simplesearchlauncher.settings;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.simplesearchlauncher.activity.SafeActivityStarter;
import com.voxel.simplesearchlauncher.model.managers.BackupRestoreManager;
import com.ovacoda.devie.R;
import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
public class SettingsActivity extends AppCompatActivity implements SafeActivityStarter {
    private static final String TAG = "SettingsActivity";

    public interface OnSettingsBackButtonPressedListener {
        void onBackPressed();
    }

    public void lambda$onCreate$0() {
        int backStackEntryCount = this.getSupportFragmentManager().getBackStackEntryCount() - 1;
        if (backStackEntryCount < 0) {
            this.getSupportActionBar().setTitle(R.string.app_settings_title);
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

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        try {
            BackupRestoreManager.getInstance().handleActivityResult(i, i2, intent, this);
        } catch (IllegalStateException e) {
            Log.e(TAG, "handleActivityResult called before BackupRestoreManager initialized", e);
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onBackPressed() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null && !fragments.isEmpty()) {
            Fragment top = fragments.get(fragments.size() - 1);
            if (top instanceof OnSettingsBackButtonPressedListener) {
                ((OnSettingsBackButtonPressedListener) top).onBackPressed();
            }
        }
        super.onBackPressed();
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
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        if (supportFragmentManager.findFragmentByTag("root") == null) {
            SettingsFragment settingsFragment = new SettingsFragment();
            FragmentTransaction fragmentTransactionBeginTransaction = supportFragmentManager.beginTransaction();
            fragmentTransactionBeginTransaction.add(R.id.container, settingsFragment, "root");
            fragmentTransactionBeginTransaction.commit();
        }
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SettingsActivity$fr4s8hBjBi1ty7LKs49sreoROeY
            @Override // androidx.fragment.app.FragmentManager.OnBackStackChangedListener
            public final void onBackStackChanged() {
                SettingsActivity.this.lambda$onCreate$0();
            }
        });
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onDestroy() {
        BackupRestoreManager.deinit();
        super.onDestroy();
    }

    @Override // androidx.appcompat.app.AppCompatActivity
    public boolean onSupportNavigateUp() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null && !fragments.isEmpty()) {
            Fragment top = fragments.get(fragments.size() - 1);
            if (top instanceof OnSettingsBackButtonPressedListener) {
                ((OnSettingsBackButtonPressedListener) top).onBackPressed();
            }
        }
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
