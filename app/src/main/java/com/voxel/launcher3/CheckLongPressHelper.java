package com.voxel.launcher3;

import android.view.View;

/* JADX INFO: loaded from: classes.dex */
public class CheckLongPressHelper {
    private boolean mHasPerformedLongPress;
    private CheckForLongPress mPendingCheckForLongPress;
    private View mView;

    class CheckForLongPress implements Runnable {
        CheckForLongPress() {
        }

        @Override // java.lang.Runnable
        public void run() {
            if (CheckLongPressHelper.this.mView.getParent() == null || !CheckLongPressHelper.this.mView.hasWindowFocus() || CheckLongPressHelper.this.mHasPerformedLongPress || !CheckLongPressHelper.this.mView.performLongClick()) {
                return;
            }
            CheckLongPressHelper.this.mView.setPressed(false);
            CheckLongPressHelper.this.mHasPerformedLongPress = true;
        }
    }

    public CheckLongPressHelper(View view) {
        this.mView = view;
    }

    public void cancelLongPress() {
        this.mHasPerformedLongPress = false;
        CheckForLongPress checkForLongPress = this.mPendingCheckForLongPress;
        if (checkForLongPress != null) {
            this.mView.removeCallbacks(checkForLongPress);
            this.mPendingCheckForLongPress = null;
        }
    }

    public boolean hasPerformedLongPress() {
        return this.mHasPerformedLongPress;
    }

    public void postCheckForLongPress() {
        this.mHasPerformedLongPress = false;
        if (this.mPendingCheckForLongPress == null) {
            this.mPendingCheckForLongPress = new CheckForLongPress();
        }
        this.mView.postDelayed(this.mPendingCheckForLongPress, LauncherAppState.getInstance().getLongPressTimeout());
    }
}
