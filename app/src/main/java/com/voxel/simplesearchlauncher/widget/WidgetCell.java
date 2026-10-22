package com.voxel.simplesearchlauncher.widget;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.voxel.launcher3.DeviceProfile;
import com.voxel.launcher3.ItemInfo;
import com.voxel.launcher3.Launcher;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.StylusEventHelper;
import com.voxel.launcher3.WidgetPreviewLoader;
import com.voxel.launcher3.compat.AppWidgetManagerCompat;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class WidgetCell extends FrameLayout implements View.OnLayoutChangeListener {
    int cellSize;
    private WidgetPreviewLoader.PreviewLoadRequest mActiveRequest;
    private String mDimensionsFormatString;
    private Object mInfo;
    private Launcher mLauncher;
    private int mPresetPreviewHeight;
    private int mPresetPreviewWidth;
    private StylusEventHelper mStylusEventHelper;
    private TextView mWidgetDims;
    private ImageView mWidgetImage;
    private TextView mWidgetName;
    private WidgetPreviewLoader mWidgetPreviewLoader;

    public WidgetCell(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public WidgetCell(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        Resources resources = context.getResources();
        this.mLauncher = (Launcher) context;
        this.mStylusEventHelper = new StylusEventHelper(this);
        this.mDimensionsFormatString = resources.getString(R.string.widget_dims_format);
        setContainerWidth();
        setWillNotDraw(false);
        setClipToPadding(false);
    }

    private String getTagToString() {
        return ((getTag() instanceof PendingAddWidgetInfo) || (getTag() instanceof PendingAddShortcutInfo)) ? getTag().toString() : "";
    }

    private void setContainerWidth() {
        this.cellSize = (int) (LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().unscaledIconSizePx * 2.6f);
        this.mPresetPreviewWidth = getContext().getResources().getDimensionPixelSize(R.dimen.widget_preview_width);
        this.mPresetPreviewHeight = getContext().getResources().getDimensionPixelSize(R.dimen.widget_preview_height);
    }

    public void applyFromAppWidgetProviderInfo(LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo, WidgetPreviewLoader widgetPreviewLoader) {
        DeviceProfile deviceProfile = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
        this.mInfo = launcherAppWidgetProviderInfo;
        this.mWidgetName.setText(AppWidgetManagerCompat.getInstance(getContext()).loadLabel(launcherAppWidgetProviderInfo));
        this.mWidgetDims.setText(String.format(this.mDimensionsFormatString, Integer.valueOf(Math.round(Math.min(launcherAppWidgetProviderInfo.spanX, deviceProfile.getCellParamsForType(DeviceProfile.ParamType.WORKSPACE).numColumns))), Integer.valueOf(Math.round(Math.min(launcherAppWidgetProviderInfo.spanY, deviceProfile.getCellParamsForType(DeviceProfile.ParamType.WORKSPACE).numRows)))));
        this.mWidgetPreviewLoader = widgetPreviewLoader;
    }

    public void applyFromResolveInfo(PackageManager packageManager, ResolveInfo resolveInfo, WidgetPreviewLoader widgetPreviewLoader) {
        this.mInfo = resolveInfo;
        this.mWidgetName.setText(resolveInfo.loadLabel(packageManager));
        this.mWidgetDims.setText(String.format(this.mDimensionsFormatString, 1, 1));
        this.mWidgetPreviewLoader = widgetPreviewLoader;
    }

    public void applyPreview(Bitmap bitmap) {
        if (bitmap != null) {
            this.mWidgetImage.setImageBitmap(bitmap);
            this.mWidgetImage.setAlpha(0.0f);
            this.mWidgetImage.animate().alpha(1.0f).setDuration(90L);
        }
    }

    public void clear() {
        this.mWidgetImage.animate().cancel();
        this.mWidgetImage.setImageBitmap(null);
        this.mWidgetName.setText((CharSequence) null);
        this.mWidgetDims.setText((CharSequence) null);
        WidgetPreviewLoader.PreviewLoadRequest previewLoadRequest = this.mActiveRequest;
        if (previewLoadRequest != null) {
            previewLoadRequest.cleanup();
            this.mActiveRequest = null;
        }
    }

    public void ensurePreview() {
        if (this.mActiveRequest != null) {
            return;
        }
        int[] previewSize = getPreviewSize();
        this.mActiveRequest = this.mWidgetPreviewLoader.getPreview(this.mInfo, previewSize[0], previewSize[1], this);
    }

    public int getActualItemWidth() {
        ItemInfo itemInfo = (ItemInfo) getTag();
        return Math.min(getPreviewSize()[0], itemInfo.spanX * LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getCellParamsForType(DeviceProfile.ParamType.WORKSPACE).getCellWidthPx());
    }

    public int[] getPreviewSize() {
        return new int[]{this.mPresetPreviewWidth, this.mPresetPreviewHeight};
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mWidgetImage = (ImageView) findViewById(R.id.widget_preview);
        this.mWidgetName = (TextView) findViewById(R.id.widget_name);
        this.mWidgetDims = (TextView) findViewById(R.id.widget_dims);
    }

    @Override // android.view.View.OnLayoutChangeListener
    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        removeOnLayoutChangeListener(this);
        ensurePreview();
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean zOnTouchEvent = super.onTouchEvent(motionEvent);
        if (this.mStylusEventHelper.checkAndPerformStylusEvent(motionEvent)) {
            return true;
        }
        return zOnTouchEvent;
    }
}
