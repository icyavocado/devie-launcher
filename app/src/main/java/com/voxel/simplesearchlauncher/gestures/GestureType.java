package com.voxel.simplesearchlauncher.gestures;

import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public enum GestureType {
    HOME(R.string.gesture_type_home_button),
    DOUBLE_TAP(R.string.gesture_type_double_tap),
    SWIPE_UP(R.string.gesture_type_swipe_up),
    SWIPE_DOWN(R.string.gesture_type_swipe_down),
    PINCH(R.string.gesture_type_pinch),
    EXPAND(R.string.gesture_type_expand),
    TWO_FINGER_SWIPE_UP(R.string.gesture_type_two_finger_swipe_up),
    TWO_FINGER_SWIPE_DOWN(R.string.gesture_type_two_finger_swipe_down);

    public int titleResId;

    GestureType(int i) {
        this.titleResId = i;
    }
}
