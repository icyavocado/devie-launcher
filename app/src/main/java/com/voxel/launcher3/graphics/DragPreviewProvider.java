package com.voxel.launcher3.graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;
import com.voxel.launcher3.FolderIcon;
import com.voxel.launcher3.HolographicOutlineHelper;
import com.voxel.launcher3.Launcher;
import com.voxel.launcher3.PreloadIconDrawable;
import com.voxel.launcher3.Workspace;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class DragPreviewProvider {
    public Bitmap generatedDragOutline;
    private final Rect mTempRect = new Rect();
    protected final View mView;
    private final int outlineColor;
    public final int previewPadding;

    public DragPreviewProvider(View view) {
        this.mView = view;
        View view2 = this.mView;
        if (view2 instanceof TextView) {
            Rect drawableBounds = getDrawableBounds(Workspace.getTextViewIcon((TextView) view2));
            this.previewPadding = (2 - drawableBounds.left) - drawableBounds.top;
        } else {
            this.previewPadding = 2;
        }
        this.outlineColor = view.getContext().getResources().getColor(R.color.outline_color);
    }

    private void drawDragView(Canvas canvas) {
        canvas.save();
        View view = this.mView;
        if (view instanceof TextView) {
            Drawable textViewIcon = Workspace.getTextViewIcon((TextView) view);
            Rect drawableBounds = getDrawableBounds(textViewIcon);
            canvas.translate(1 - drawableBounds.left, 1 - drawableBounds.top);
            textViewIcon.draw(canvas);
        } else {
            Rect rect = this.mTempRect;
            view.getDrawingRect(rect);
            int visibility = this.mView.getVisibility();
            boolean z = false;
            this.mView.setVisibility(0);
            View view2 = this.mView;
            if ((view2 instanceof FolderIcon) && ((FolderIcon) view2).getTextVisible()) {
                ((FolderIcon) this.mView).setTextVisible(false);
                z = true;
            }
            canvas.translate((-this.mView.getScrollX()) + 1, (-this.mView.getScrollY()) + 1);
            canvas.clipRect(rect, Region.Op.INTERSECT);
            this.mView.draw(canvas);
            this.mView.setVisibility(visibility);
            if (z) {
                ((FolderIcon) this.mView).setTextVisible(true);
            }
        }
        canvas.restore();
    }

    protected static Rect getDrawableBounds(Drawable drawable) {
        Rect rect = new Rect();
        drawable.copyBounds(rect);
        if (rect.width() == 0 || rect.height() == 0) {
            rect.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        } else {
            rect.offsetTo(0, 0);
        }
        if (drawable instanceof PreloadIconDrawable) {
            int i = -((PreloadIconDrawable) drawable).getOutset();
            rect.inset(i, i);
        }
        return rect;
    }

    public Bitmap createDragBitmap(Canvas canvas) {
        Bitmap bitmapCreateBitmap;
        View view = this.mView;
        if (view instanceof TextView) {
            Rect drawableBounds = getDrawableBounds(Workspace.getTextViewIcon((TextView) view));
            bitmapCreateBitmap = Bitmap.createBitmap(drawableBounds.width() + 2, drawableBounds.height() + 2, Bitmap.Config.ARGB_8888);
        } else {
            bitmapCreateBitmap = Bitmap.createBitmap(view.getWidth() + 2, this.mView.getHeight() + 2, Bitmap.Config.ARGB_8888);
        }
        canvas.setBitmap(bitmapCreateBitmap);
        drawDragView(canvas);
        canvas.setBitmap(null);
        return bitmapCreateBitmap;
    }

    public Bitmap createDragOutline(Canvas canvas) {
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(this.mView.getWidth() + 2, this.mView.getHeight() + 2, Bitmap.Config.ALPHA_8);
        canvas.setBitmap(bitmapCreateBitmap);
        drawDragView(canvas);
        HolographicOutlineHelper.obtain(this.mView.getContext()).applyExpensiveOutlineWithBlur(bitmapCreateBitmap, canvas);
        canvas.setBitmap(null);
        return bitmapCreateBitmap;
    }

    public final void generateDragOutline(Canvas canvas) {
        this.generatedDragOutline = createDragOutline(canvas);
    }

    public float getScaleAndPosition(Bitmap bitmap, int[] iArr) {
        float locationInDragLayer = Launcher.getLauncher(this.mView.getContext()).getDragLayer().getLocationInDragLayer(this.mView, iArr);
        iArr[0] = Math.round(iArr[0] - ((bitmap.getWidth() - (this.mView.getWidth() * locationInDragLayer)) / 2.0f));
        iArr[1] = Math.round((iArr[1] - (((1.0f - locationInDragLayer) * bitmap.getHeight()) / 2.0f)) - (this.previewPadding / 2));
        return locationInDragLayer;
    }
}
