package com.voxel.simplesearchlauncher.utils;

import timber.log.Timber;
import android.content.SharedPreferences;

/* JADX INFO: loaded from: classes2.dex */
public class UpgradeUtil {

    /* JADX INFO: loaded from: classes.dex */
    public interface UpgradableComponent {
        void onUpgrade(int i, int i2);
    }

    public static synchronized boolean processUpgrade(SharedPreferences sharedPreferences, UpgradableComponent upgradableComponent, int i) {
        if (sharedPreferences == null) {
            throw new IllegalArgumentException("processUpgrade requires a non-null SharedPreferences");
        }
        if (i < 0) {
            throw new IllegalArgumentException("currentVersion must be non-negative");
        }
        int i2 = sharedPreferences.getInt("_last_version", -1);
        if (i <= i2) {
            return false;
        }
        if (upgradableComponent != null) {
            try {
                upgradableComponent.onUpgrade(i2, i);
            } catch (Exception e) {
                Timber.e(e);
                return false;
            }
        }
        SharedPreferences.Editor editorEdit = sharedPreferences.edit();
        editorEdit.putInt("_last_version", i);
        editorEdit.apply();
        return true;
    }
}
