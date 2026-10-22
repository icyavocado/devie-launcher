package com.voxel.launcher3;

import timber.log.Timber;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.core.view.KeyEventDispatcher;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/* JADX INFO: loaded from: classes.dex */
public class FragmentHelper {
    private static final String TAG = "FragmentHelper";
    private final FragmentActivity mActivity;
    private boolean mIsLockedSaveState;
    private Set<LauncherStackableFragment> mClosingStackableFragments = new HashSet();
    private List<PendingPopFragmentInfo> mPendingPopFragments = new ArrayList();

    private class PendingPopFragmentInfo {
        boolean animate;
        LauncherStackableFragment fragment;

        PendingPopFragmentInfo(LauncherStackableFragment launcherStackableFragment, boolean z) {
            this.fragment = launcherStackableFragment;
            this.animate = z;
        }
    }

    public FragmentHelper(FragmentActivity fragmentActivity) {
        this.mActivity = fragmentActivity;
    }

    public void closeAllFragments() {
        for (int stackFragmentCount = getStackFragmentCount() - 1; stackFragmentCount >= 0; stackFragmentCount--) {
            if (this.mIsLockedSaveState) {
                Log.d(TAG, "Saving fragment to be popped on resume.");
                this.mPendingPopFragments.add(new PendingPopFragmentInfo(getStackFragmentAt(stackFragmentCount), false));
            } else {
                popStackFragment(getStackFragmentAt(stackFragmentCount), false);
            }
        }
    }

    public Fragment getCurrentStackFragment() {
        return getStackFragmentAt(getStackFragmentCount() - 1);
    }

    public LauncherStackableFragment getStackFragmentAt(int i) {
        if (i >= 0 && getStackFragmentCount() > 0) {
            return (LauncherStackableFragment) this.mActivity.getSupportFragmentManager().findFragmentByTag(getTagForBackStackIndex(i));
        }
        return null;
    }

    public int getStackFragmentCount() {
        return this.mActivity.getSupportFragmentManager().getBackStackEntryCount();
    }

    public String getTagForBackStackIndex(int i) {
        return "backstack_" + i;
    }

    public void onResume() {
        this.mIsLockedSaveState = false;
        if (this.mPendingPopFragments.size() > 0) {
            for (PendingPopFragmentInfo pendingPopFragmentInfo : this.mPendingPopFragments) {
                popStackFragment(pendingPopFragmentInfo.fragment, pendingPopFragmentInfo.animate);
            }
            this.mPendingPopFragments.clear();
        }
    }

    public void onSaveInstanceState(Bundle bundle) {
        for (int stackFragmentCount = getStackFragmentCount() - 1; stackFragmentCount >= 0; stackFragmentCount--) {
            if (!getStackFragmentAt(stackFragmentCount).canSaveInstanceState()) {
                if (Build.VERSION.SDK_INT >= 28) {
                    this.mPendingPopFragments.add(new PendingPopFragmentInfo(getStackFragmentAt(stackFragmentCount), false));
                } else {
                    popStackFragment(getStackFragmentAt(stackFragmentCount), false);
                }
            }
        }
        Iterator it = new ArrayList(this.mClosingStackableFragments).iterator();
        while (it.hasNext()) {
            onStackableFragmentRequestClose((LauncherStackableFragment) it.next());
        }
        this.mIsLockedSaveState = true;
    }

    public void onStackableFragmentRequestClose(LauncherStackableFragment launcherStackableFragment) {
        Log.d(TAG, "onStackableFragmentRequestClose");
        if (this.mClosingStackableFragments.remove(launcherStackableFragment)) {
            if (!this.mIsLockedSaveState) {
                popStackFragment(launcherStackableFragment, true);
                return;
            }
            Log.d(TAG, "Saving fragment to be popped on resume.");
            this.mPendingPopFragments.add(new PendingPopFragmentInfo(launcherStackableFragment, true));
            Timber.e(new RuntimeException("onStackableFragmentRequestClose invoked while locked in Save State mode."));
        }
    }

    public void onStackableFragmentWillRequestClose(LauncherStackableFragment launcherStackableFragment) {
        boolean zRequiresFullScreen;
        Log.d(TAG, "onStackableFragmentWillRequestClose");
        this.mClosingStackableFragments.add(launcherStackableFragment);
        LauncherStackableFragment stackFragmentAt = getStackFragmentAt(launcherStackableFragment.getBackStackIndex() - 1);
        if (stackFragmentAt != null) {
            zRequiresFullScreen = stackFragmentAt.requiresFullScreen();
            stackFragmentAt.onStackableFragmentResult(launcherStackableFragment.getRequestCode(), launcherStackableFragment.getResultCode(), launcherStackableFragment.getResultIntent());
            stackFragmentAt.focusFragment(true);
        } else {
            launcherStackableFragment.onPopLauncherStackedAppearance(this.mActivity, true);
            zRequiresFullScreen = false;
        }
        if (zRequiresFullScreen) {
            return;
        }
        FragmentActivity fragmentActivity = this.mActivity;
        if (fragmentActivity instanceof Launcher) {
            ((Launcher) fragmentActivity).showSpecialFragments();
        }
    }

    public void onStart() {
        this.mIsLockedSaveState = false;
    }

    public void popStackFragment(LauncherStackableFragment launcherStackableFragment, boolean z) {
        Log.d(TAG, "popStackFragment");
        if (launcherStackableFragment == null || this.mIsLockedSaveState) {
            return;
        }
        int backStackIndex = launcherStackableFragment.getBackStackIndex();
        FragmentManager supportFragmentManager = this.mActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransactionBeginTransaction = supportFragmentManager.beginTransaction();
        fragmentTransactionBeginTransaction.remove(launcherStackableFragment);
        fragmentTransactionBeginTransaction.commitAllowingStateLoss();
        try {
            supportFragmentManager.popBackStackImmediate(getTagForBackStackIndex(launcherStackableFragment.getBackStackIndex()), 1);
            supportFragmentManager.executePendingTransactions();
            LauncherStackableFragment stackFragmentAt = getStackFragmentAt(backStackIndex - 1);
            if (stackFragmentAt != null) {
                try {
                    stackFragmentAt.focusFragment(z);
                    return;
                } catch (IllegalStateException unused) {
                    return;
                }
            }
            launcherStackableFragment.onPopLauncherStackedAppearance(this.mActivity, z);
            FragmentActivity fragmentActivity = this.mActivity;
            if (fragmentActivity instanceof Launcher) {
                ((Launcher) fragmentActivity).showSpecialFragments();
            }
        } catch (IllegalStateException unused2) {
        }
    }

    public void pushStackFragment(int i, LauncherStackableFragment launcherStackableFragment) {
        if (launcherStackableFragment == null || this.mIsLockedSaveState) {
            return;
        }
        LauncherStackableFragment stackFragmentAt = getStackFragmentAt(getStackFragmentCount() - 1);
        if (stackFragmentAt == null) {
            KeyEventDispatcher.Component component = this.mActivity;
            if (component instanceof LauncherStackableFragment.OnFragmentInteractionListener) {
                launcherStackableFragment.onPushLauncherStackedAppearance((LauncherStackableFragment.OnFragmentInteractionListener) component);
            }
        } else {
            stackFragmentAt.unfocusFragment(true);
        }
        launcherStackableFragment.setBackStackIndex(getStackFragmentCount());
        FragmentTransaction fragmentTransactionBeginTransaction = this.mActivity.getSupportFragmentManager().beginTransaction();
        fragmentTransactionBeginTransaction.addToBackStack(getTagForBackStackIndex(getStackFragmentCount()));
        fragmentTransactionBeginTransaction.add(i, launcherStackableFragment, getTagForBackStackIndex(getStackFragmentCount()));
        fragmentTransactionBeginTransaction.commitAllowingStateLoss();
        this.mActivity.getSupportFragmentManager().executePendingTransactions();
        if (launcherStackableFragment.requiresFullScreen()) {
            FragmentActivity fragmentActivity = this.mActivity;
            if (fragmentActivity instanceof Launcher) {
                ((Launcher) fragmentActivity).hideSpecialFragments();
            }
        }
    }
}
