package com.voxel.launcher3.dragndrop;

import android.graphics.Point;

/* JADX INFO: loaded from: classes.dex */
public class DragOptions {
    public boolean isAccessibleDrag = false;
    public Point systemDndStartPoint = null;
    public DeferDragCondition deferDragCondition = new DeferDragCondition();
    public boolean isDesktopLocked = false;

    public static class DeferDragCondition {
        public void onDeferredDragStart() {
        }

        public void onDragStart() {
        }

        public void onDropBeforeDeferredDrag() {
        }

        public boolean shouldStartDeferredDrag(double d) {
            return true;
        }
    }
}
