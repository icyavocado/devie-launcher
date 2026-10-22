package com.voxel.launcher3.compat;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import com.voxel.launcher3.LauncherAppState;
import java.util.ArrayList;
import java.util.HashSet;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/* JADX INFO: loaded from: classes.dex */
public class PackageInstallerCompatV16 extends PackageInstallerCompat {
    boolean mFinishedBind;
    protected final SharedPreferences mPrefs;
    boolean mReplayPending;
    boolean mUseQueue;

    PackageInstallerCompatV16(Context context) {
        this.mPrefs = context.getSharedPreferences("com.voxel.launcher3.compat.PackageInstallerCompatV16.queue", 0);
    }

    private static PackageInstallerCompat.PackageInstallInfo infoFromJson(String str, String str2) {
        PackageInstallerCompat.PackageInstallInfo packageInstallInfo = new PackageInstallerCompat.PackageInstallInfo(str);
        try {
            JSONObject jSONObject = (JSONObject) new JSONTokener(str2).nextValue();
            packageInstallInfo.state = jSONObject.getInt("state");
            packageInstallInfo.progress = jSONObject.getInt("progress");
        } catch (JSONException e) {
            Log.e("PackageInstallerCompatV16", "failed to deserialize app state update", e);
        }
        return packageInstallInfo;
    }

    private void replayUpdates() {
        LauncherAppState instanceNoCreate = LauncherAppState.getInstanceNoCreate();
        if (instanceNoCreate == null) {
            this.mReplayPending = true;
            return;
        }
        this.mReplayPending = false;
        ArrayList<PackageInstallerCompat.PackageInstallInfo> arrayList = new ArrayList<>();
        for (String str : this.mPrefs.getAll().keySet()) {
            String string = this.mPrefs.getString(str, null);
            if (!TextUtils.isEmpty(string)) {
                arrayList.add(infoFromJson(str, string));
            }
        }
        if (arrayList.isEmpty()) {
            return;
        }
        sendUpdate(instanceNoCreate, arrayList);
    }

    private void sendUpdate(LauncherAppState launcherAppState, ArrayList<PackageInstallerCompat.PackageInstallInfo> arrayList) {
        if (launcherAppState == null) {
            this.mReplayPending = true;
        } else {
            launcherAppState.setPackageState(arrayList);
        }
    }

    @Override // com.voxel.launcher3.compat.PackageInstallerCompat
    public void onFinishBind() {
        this.mFinishedBind = true;
        if (this.mUseQueue) {
            return;
        }
        replayUpdates();
    }

    @Override // com.voxel.launcher3.compat.PackageInstallerCompat
    public void onPause() {
        this.mUseQueue = true;
    }

    @Override // com.voxel.launcher3.compat.PackageInstallerCompat
    public void onResume() {
        this.mUseQueue = false;
        if (this.mFinishedBind) {
            replayUpdates();
        }
    }

    @Override // com.voxel.launcher3.compat.PackageInstallerCompat
    public void onStop() {
    }

    @Override // com.voxel.launcher3.compat.PackageInstallerCompat
    public HashSet<String> updateAndGetActiveSessionCache() {
        return new HashSet<>();
    }
}
