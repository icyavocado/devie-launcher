package com.voxel.launcher3.compat;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInstaller;
import android.os.Handler;
import android.util.SparseArray;
import com.voxel.launcher3.IconCache;
import com.voxel.launcher3.LauncherAppState;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/* JADX INFO: loaded from: classes.dex */
@TargetApi(21)
public class PackageInstallerCompatVL extends PackageInstallerCompat implements Runnable {
    private boolean mBound;
    private final IconCache mCache;
    private final PackageInstaller mInstaller;
    private boolean mResumed;
    private final Handler mWorker;
    private final SparseArray<PackageInstaller.SessionInfo> mPendingReplays = new SparseArray<>();
    private final HashSet<String> mPendingBadgeUpdates = new HashSet<>();
    private final PackageInstaller.SessionCallback mCallback = new PackageInstaller.SessionCallback() { // from class: com.voxel.launcher3.compat.PackageInstallerCompatVL.1
        private void pushSessionBadgeToLauncher(int i) {
            PackageInstaller.SessionInfo sessionInfo = PackageInstallerCompatVL.this.mInstaller.getSessionInfo(i);
            if (sessionInfo != null) {
                PackageInstallerCompatVL.this.addSessionInfoToCahce(sessionInfo, UserHandleCompat.myUserHandle());
                if (sessionInfo.getAppPackageName() != null) {
                    PackageInstallerCompatVL.this.mPendingBadgeUpdates.add(sessionInfo.getAppPackageName());
                }
                PackageInstallerCompatVL.this.mPendingReplays.put(i, sessionInfo);
                PackageInstallerCompatVL.this.replayUpdates(null);
            }
        }

        @Override // android.content.pm.PackageInstaller.SessionCallback
        public void onActiveChanged(int i, boolean z) {
        }

        @Override // android.content.pm.PackageInstaller.SessionCallback
        public void onBadgingChanged(int i) {
            pushSessionBadgeToLauncher(i);
        }

        @Override // android.content.pm.PackageInstaller.SessionCallback
        public void onCreated(int i) {
            pushSessionBadgeToLauncher(i);
        }

        @Override // android.content.pm.PackageInstaller.SessionCallback
        public void onFinished(int i, boolean z) {
            PackageInstallerCompatVL.this.mPendingReplays.remove(i);
            PackageInstaller.SessionInfo sessionInfo = PackageInstallerCompatVL.this.mInstaller.getSessionInfo(i);
            if (sessionInfo == null || sessionInfo.getAppPackageName() == null) {
                return;
            }
            PackageInstallerCompatVL.this.mPendingBadgeUpdates.remove(sessionInfo.getAppPackageName());
            PackageInstallerCompatVL.this.replayUpdates(new PackageInstallerCompat.PackageInstallInfo(sessionInfo.getAppPackageName(), z ? 0 : 2, 0));
        }

        @Override // android.content.pm.PackageInstaller.SessionCallback
        public void onProgressChanged(int i, float f) {
            PackageInstaller.SessionInfo sessionInfo = PackageInstallerCompatVL.this.mInstaller.getSessionInfo(i);
            if (sessionInfo != null) {
                PackageInstallerCompatVL.this.mPendingReplays.put(i, sessionInfo);
                PackageInstallerCompatVL.this.replayUpdates(null);
            }
        }
    };

    PackageInstallerCompatVL(Context context) {
        this.mInstaller = context.getPackageManager().getPackageInstaller();
        LauncherAppState.setApplicationContext(context.getApplicationContext());
        this.mCache = LauncherAppState.getInstance().getIconCache();
        this.mWorker = new Handler();
        this.mResumed = false;
        this.mBound = false;
        this.mInstaller.registerSessionCallback(this.mCallback, this.mWorker);
        this.mWorker.post(new Runnable() { // from class: com.voxel.launcher3.compat.-$$Lambda$PackageInstallerCompatVL$8VcGXfKhFLT0BlcDBm94-K3O3dQ
            @Override // java.lang.Runnable
            public final void run() {
                PackageInstallerCompatVL.this.lambda$new$0();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void addSessionInfoToCahce(PackageInstaller.SessionInfo sessionInfo, UserHandleCompat userHandleCompat) {
        String appPackageName = sessionInfo.getAppPackageName();
        if (appPackageName != null) {
            this.mCache.cachePackageInstallInfo(appPackageName, userHandleCompat, sessionInfo.getAppIcon(), sessionInfo.getAppLabel());
        }
    }

    public void lambda$new$0() {
        for (PackageInstaller.SessionInfo sessionInfo : this.mInstaller.getAllSessions()) {
            this.mPendingReplays.append(sessionInfo.getSessionId(), sessionInfo);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void replayUpdates(PackageInstallerCompat.PackageInstallInfo packageInstallInfo) {
        LauncherAppState instanceNoCreate;
        if (this.mResumed && this.mBound) {
            if ((this.mPendingReplays.size() == 0 && packageInstallInfo == null) || (instanceNoCreate = LauncherAppState.getInstanceNoCreate()) == null) {
                return;
            }
            ArrayList<PackageInstallerCompat.PackageInstallInfo> arrayList = new ArrayList<>();
            if (packageInstallInfo != null && packageInstallInfo.state != 0) {
                arrayList.add(packageInstallInfo);
            }
            for (int size = this.mPendingReplays.size() - 1; size >= 0; size--) {
                PackageInstaller.SessionInfo sessionInfoValueAt = this.mPendingReplays.valueAt(size);
                if (sessionInfoValueAt.getAppPackageName() != null) {
                    arrayList.add(new PackageInstallerCompat.PackageInstallInfo(sessionInfoValueAt.getAppPackageName(), 1, (int) (sessionInfoValueAt.getProgress() * 100.0f)));
                }
            }
            this.mPendingReplays.clear();
            if (!arrayList.isEmpty()) {
                instanceNoCreate.setPackageState(arrayList);
            }
            if (this.mPendingBadgeUpdates.isEmpty()) {
                return;
            }
            Iterator<String> it = this.mPendingBadgeUpdates.iterator();
            while (it.hasNext()) {
                instanceNoCreate.updatePackageBadge(it.next());
            }
            this.mPendingBadgeUpdates.clear();
        }
    }

    @Override // com.voxel.launcher3.compat.PackageInstallerCompat
    public void onFinishBind() {
        this.mBound = true;
        this.mWorker.post(this);
    }

    @Override // com.voxel.launcher3.compat.PackageInstallerCompat
    public void onPause() {
        this.mResumed = false;
    }

    @Override // com.voxel.launcher3.compat.PackageInstallerCompat
    public void onResume() {
        this.mResumed = true;
        this.mWorker.post(this);
    }

    @Override // com.voxel.launcher3.compat.PackageInstallerCompat
    public void onStop() {
    }

    @Override // java.lang.Runnable
    public void run() {
        replayUpdates(null);
    }

    @Override // com.voxel.launcher3.compat.PackageInstallerCompat
    public HashSet<String> updateAndGetActiveSessionCache() {
        HashSet<String> hashSet = new HashSet<>();
        UserHandleCompat userHandleCompatMyUserHandle = UserHandleCompat.myUserHandle();
        for (PackageInstaller.SessionInfo sessionInfo : this.mInstaller.getAllSessions()) {
            addSessionInfoToCahce(sessionInfo, userHandleCompatMyUserHandle);
            if (sessionInfo.getAppPackageName() != null) {
                hashSet.add(sessionInfo.getAppPackageName());
            }
        }
        return hashSet;
    }
}
