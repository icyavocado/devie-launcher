package com.voxel.launcher3.shortcuts;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.voxel.launcher3.HolographicOutlineHelper;
import com.voxel.launcher3.Launcher;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.Utilities;
import com.voxel.launcher3.graphics.DragPreviewProvider;

/* JADX INFO: loaded from: classes.dex */
public class ShortcutDragPreviewProvider extends DragPreviewProvider {
    private final Point mPositionShift;

    public ShortcutDragPreviewProvider(View view, Point point) {
        super(view);
        this.mPositionShift = point;
    }

    private Bitmap drawScaledPreview(Canvas canvas, Bitmap.Config config) {
        Drawable background = this.mView.getBackground();
        Rect drawableBounds = getDrawableBounds(background);
        int i = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().unscaledIconSizePx;
        int i2 = i + 2;
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(i2, i2, config);
        canvas.setBitmap(bitmapCreateBitmap);
        canvas.save();
        canvas.translate(1.0f, 1.0f);
        float f = i;
        canvas.scale(f / drawableBounds.width(), f / drawableBounds.height(), 0.0f, 0.0f);
        canvas.translate(drawableBounds.left, drawableBounds.top);
        background.draw(canvas);
        canvas.restore();
        return bitmapCreateBitmap;
    }

    @Override // com.voxel.launcher3.graphics.DragPreviewProvider
    public Bitmap createDragBitmap(Canvas canvas) {
        Bitmap bitmapDrawScaledPreview = drawScaledPreview(canvas, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(null);
        return bitmapDrawScaledPreview;
    }

    @Override // com.voxel.launcher3.graphics.DragPreviewProvider
    public Bitmap createDragOutline(Canvas canvas) {
        Bitmap bitmapDrawScaledPreview = drawScaledPreview(canvas, Bitmap.Config.ALPHA_8);
        HolographicOutlineHelper.obtain(this.mView.getContext()).applyExpensiveOutlineWithBlur(bitmapDrawScaledPreview, canvas);
        canvas.setBitmap(null);
        return bitmapDrawScaledPreview;
    }

    @Override // com.voxel.launcher3.graphics.DragPreviewProvider
    public float getScaleAndPosition(Bitmap bitmap, int[] iArr) {
        Launcher launcher = Launcher.getLauncher(this.mView.getContext());
        int iWidth = getDrawableBounds(this.mView.getBackground()).width();
        float locationInDragLayer = launcher.getDragLayer().getLocationInDragLayer(this.mView, iArr);
        int paddingStart = this.mView.getPaddingStart();
        if (Utilities.isRtl(this.mView.getResources())) {
            paddingStart = (this.mView.getWidth() - iWidth) - paddingStart;
        }
        float f = iWidth * locationInDragLayer;
        iArr[0] = iArr[0] + Math.round((paddingStart * locationInDragLayer) + ((f - bitmap.getWidth()) / 2.0f) + this.mPositionShift.x);
        iArr[1] = iArr[1] + Math.round((((locationInDragLayer * this.mView.getHeight()) - bitmap.getHeight()) / 2.0f) + this.mPositionShift.y);
        return f / LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().unscaledIconSizePx;
    }
}
