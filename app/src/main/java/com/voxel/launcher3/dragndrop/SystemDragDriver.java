package com.voxel.launcher3.dragndrop;

import android.content.Context;
import android.view.MotionEvent;
import com.voxel.launcher3.DropTarget;

/* JADX INFO: compiled from: DragDriver.java */
/* JADX INFO: loaded from: classes.dex */
class SystemDragDriver extends DragDriver {
    private final Context mContext;
    private final DropTarget.DragObject mDragObject;
    float mLastX;
    float mLastY;
    boolean mReceivedDropEvent;

    public SystemDragDriver(DragController dragController, Context context, DropTarget.DragObject dragObject) {
        super(dragController);
        this.mReceivedDropEvent = false;
        this.mLastX = 0.0f;
        this.mLastY = 0.0f;
        this.mDragObject = dragObject;
        this.mContext = context;
    }

    @Override // com.voxel.launcher3.dragndrop.DragDriver
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override // com.voxel.launcher3.dragndrop.DragDriver
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return false;
    }
}
