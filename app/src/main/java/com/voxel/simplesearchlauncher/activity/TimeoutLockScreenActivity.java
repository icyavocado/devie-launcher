package com.voxel.simplesearchlauncher.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.WindowManager;

/* JADX INFO: loaded from: classes.dex */
public class TimeoutLockScreenActivity extends Activity {
    private int mExistingScreenTimeout;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        float floatExtra = getIntent().getFloatExtra("brightness value", 0.0f);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.screenBrightness = floatExtra;
        getWindow().setAttributes(attributes);
        if (Build.VERSION.SDK_INT < 19) {
            getWindow().getDecorView().setSystemUiVisibility(8);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(4102);
        }
        /* Guard WRITE_SETTINGS: without it Settings.System.putInt throws a
           SecurityException on every gesture invocation (revoked permission). */
        if (Build.VERSION.SDK_INT < 23 || Settings.System.canWrite(this)) {
            this.mExistingScreenTimeout = Settings.System.getInt(getContentResolver(), "screen_off_timeout", -1);
            Settings.System.putInt(getContentResolver(), "screen_off_timeout", 10);
        } else {
            this.mExistingScreenTimeout = -1;
        }
    }

    @Override // android.app.Activity
    protected void onStop() {
        super.onStop();
        if (this.mExistingScreenTimeout >= 0 && (Build.VERSION.SDK_INT < 23 || Settings.System.canWrite(this))) {
            Settings.System.putInt(getContentResolver(), "screen_off_timeout", this.mExistingScreenTimeout);
        }
        finish();
    }
}
