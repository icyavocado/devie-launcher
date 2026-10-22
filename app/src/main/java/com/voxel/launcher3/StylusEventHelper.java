package com.voxel.launcher3;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/* JADX INFO: loaded from: classes.dex */
public class StylusEventHelper {
    private boolean mIsButtonPressed;
    private View mView;

    public StylusEventHelper(View view) {
        this.mView = view;
    }

    private static boolean isStylusButtonPressed(MotionEvent motionEvent) {
        return motionEvent.getToolType(0) == 2 && (motionEvent.getButtonState() & 2) == 2;
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean checkAndPerformStylusEvent(MotionEvent motionEvent) {
        float scaledTouchSlop = ViewConfiguration.get(this.mView.getContext()).getScaledTouchSlop();
        if (!this.mView.isLongClickable()) {
            return false;
        }
        boolean zIsStylusButtonPressed = isStylusButtonPressed(motionEvent);
        switch (motionEvent.getAction()) {
            case 0:
                this.mIsButtonPressed = false;
                if (zIsStylusButtonPressed && this.mView.performLongClick()) {
                    this.mIsButtonPressed = true;
                    return true;
                }
                return false;
            case 1:
            case 3:
                this.mIsButtonPressed = false;
                return false;
            case 2:
                if (Utilities.pointInView(this.mView, motionEvent.getX(), motionEvent.getY(), scaledTouchSlop)) {
                    if (!this.mIsButtonPressed && zIsStylusButtonPressed && this.mView.performLongClick()) {
                        this.mIsButtonPressed = true;
                        return true;
                    }
                    if (this.mIsButtonPressed && !zIsStylusButtonPressed) {
                        this.mIsButtonPressed = false;
                    }
                }
                return false;
            default:
                return false;
        }
    }
}
