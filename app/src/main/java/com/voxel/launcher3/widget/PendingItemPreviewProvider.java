package com.voxel.launcher3.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import com.voxel.launcher3.HolographicOutlineHelper;
import com.voxel.launcher3.Launcher;
import com.voxel.launcher3.PendingAddItemInfo;
import com.voxel.launcher3.graphics.DragPreviewProvider;
import com.voxel.simplesearchlauncher.widget.PendingAddWidgetInfo;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class PendingItemPreviewProvider extends DragPreviewProvider {
    private final PendingAddItemInfo mAddInfo;
    private final int mOutlineColor;
    private final Bitmap mPreviewBitmap;

    public PendingItemPreviewProvider(View view, PendingAddItemInfo pendingAddItemInfo, Bitmap bitmap) {
        super(view);
        this.mAddInfo = pendingAddItemInfo;
        this.mPreviewBitmap = bitmap;
        this.mOutlineColor = view.getResources().getColor(R.color.outline_color);
    }

    @Override // com.voxel.launcher3.graphics.DragPreviewProvider
    public Bitmap createDragOutline(Canvas canvas) {
        int[] iArrEstimateItemSize = Launcher.getLauncher(this.mView.getContext()).getWorkspace().estimateItemSize(this.mAddInfo.spanX, this.mAddInfo.spanY, false);
        int i = iArrEstimateItemSize[0];
        boolean z = true;
        int i2 = iArrEstimateItemSize[1];
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(i, i2, Bitmap.Config.ALPHA_8);
        canvas.setBitmap(bitmapCreateBitmap);
        Rect rect = new Rect(0, 0, this.mPreviewBitmap.getWidth(), this.mPreviewBitmap.getHeight());
        float fMin = Math.min((i - 2) / (float) this.mPreviewBitmap.getWidth(), (i2 - 2) / (float) this.mPreviewBitmap.getHeight());
        int width = (int) (this.mPreviewBitmap.getWidth() * fMin);
        int height = (int) (fMin * this.mPreviewBitmap.getHeight());
        Rect rect2 = new Rect(0, 0, width, height);
        rect2.offset((i - width) / 2, (i2 - height) / 2);
        canvas.drawBitmap(this.mPreviewBitmap, rect, rect2, (Paint) null);
        PendingAddItemInfo pendingAddItemInfo = this.mAddInfo;
        if ((pendingAddItemInfo instanceof PendingAddWidgetInfo) && ((PendingAddWidgetInfo) pendingAddItemInfo).previewImage == 0) {
            z = false;
        }
        HolographicOutlineHelper.obtain(this.mView.getContext()).applyExpensiveOutlineWithBlur(bitmapCreateBitmap, canvas, z);
        canvas.setBitmap(null);
        return bitmapCreateBitmap;
    }
}
