package com.evie.common.dragndrop;

import android.view.View;

/* JADX INFO: loaded from: classes.dex */
public interface DragSource {
    float getIntrinsicIconScaleFactor();

    void onDropCompleted(View view, DragObjectAbstraction dragObjectAbstraction, boolean z, boolean z2);

    void onFlingToDeleteCompleted();

    boolean supportsFlingToDelete();

    boolean supportsRemoveDropTarget();
}
