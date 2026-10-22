package com.voxel.launcher3;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class PendingAppWidgetHostView extends LauncherAppWidgetHostView implements View.OnClickListener {
    private static Resources.Theme sPreloaderTheme;
    private Drawable mCenterDrawable;
    private View.OnClickListener mClickListener;
    private View mDefaultView;
    private PreloadIconDrawable mDrawable;
    private boolean mDrawableSizeChanged;
    private Bitmap mIcon;
    private final Intent mIconLookupIntent;
    private final LauncherAppWidgetInfo mInfo;
    private final TextPaint mPaint;
    private final Rect mRect;
    private Layout mSetupTextLayout;
    private final int mStartState;
    private Drawable mTopCornerDrawable;

    public PendingAppWidgetHostView(Context context, LauncherAppWidgetInfo launcherAppWidgetInfo) {
        super(context);
        this.mRect = new Rect();
        this.mInfo = launcherAppWidgetInfo;
        this.mStartState = launcherAppWidgetInfo.restoreStatus;
        this.mIconLookupIntent = new Intent().setComponent(launcherAppWidgetInfo.providerName);
        this.mPaint = new TextPaint();
        this.mPaint.setColor(-1);
        this.mPaint.setTextSize(TypedValue.applyDimension(0, getDeviceProfile().getCellParamsForType(DeviceProfile.ParamType.WORKSPACE).iconTextSizePx, getResources().getDisplayMetrics()));
        setBackgroundResource(R.drawable.quantum_panel_dark);
        setWillNotDraw(false);
    }

    private DeviceProfile getDeviceProfile() {
        return LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
    }

    public void applyState() {
        PreloadIconDrawable preloadIconDrawable = this.mDrawable;
        if (preloadIconDrawable != null) {
            preloadIconDrawable.setLevel(Math.max(this.mInfo.installProgress, 0));
        }
    }

    @Override // android.appwidget.AppWidgetHostView
    protected View getDefaultView() {
        if (this.mDefaultView == null) {
            this.mDefaultView = this.mInflater.inflate(R.layout.appwidget_not_ready, (ViewGroup) this, false);
            this.mDefaultView.setOnClickListener(this);
            applyState();
        }
        return this.mDefaultView;
    }

    public boolean isReadyForClickSetup() {
        return (this.mInfo.restoreStatus & 2) == 0 && (this.mInfo.restoreStatus & 4) != 0;
    }

    @Override // com.voxel.launcher3.LauncherAppWidgetHostView
    public boolean isReinflateRequired() {
        return this.mStartState != this.mInfo.restoreStatus;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        View.OnClickListener onClickListener = this.mClickListener;
        if (onClickListener != null) {
            onClickListener.onClick(this);
        }
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        boolean z;
        CellParams cellParamsForType = getDeviceProfile().getCellParamsForType(DeviceProfile.ParamType.WORKSPACE);
        if (this.mDrawable != null) {
            if (this.mDrawableSizeChanged) {
                int iMin = Math.min(cellParamsForType.iconSizePx + (this.mDrawable.getOutset() * 2), Math.min((getWidth() - getPaddingLeft()) - getPaddingRight(), (getHeight() - getPaddingTop()) - getPaddingBottom()));
                this.mRect.set(0, 0, iMin, iMin);
                this.mRect.inset(this.mDrawable.getOutset(), this.mDrawable.getOutset());
                this.mRect.offsetTo((getWidth() - this.mRect.width()) / 2, (getHeight() - this.mRect.height()) / 2);
                this.mDrawable.setBounds(this.mRect);
                this.mDrawableSizeChanged = false;
            }
            this.mDrawable.draw(canvas);
            return;
        }
        if (this.mCenterDrawable == null || this.mTopCornerDrawable == null) {
            return;
        }
        if (this.mDrawableSizeChanged) {
            getDeviceProfile();
            int i = cellParamsForType.iconSizePx;
            int paddingTop = getPaddingTop();
            int paddingBottom = getPaddingBottom();
            int paddingLeft = getPaddingLeft();
            int paddingRight = getPaddingRight();
            int iMax = Math.max(0, (getWidth() - paddingLeft) - paddingRight);
            int iMax2 = Math.max(0, (getHeight() - paddingTop) - paddingBottom);
            this.mSetupTextLayout = new StaticLayout(getResources().getText(R.string.gadget_setup_text), this.mPaint, iMax, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, true);
            if (this.mSetupTextLayout.getLineCount() == 1) {
                int iMin2 = Math.min(i, Math.min(iMax, iMax2 - this.mSetupTextLayout.getHeight()));
                this.mRect.set(0, 0, iMin2, iMin2);
                this.mRect.offsetTo((getWidth() - this.mRect.width()) / 2, (((getHeight() - this.mRect.height()) - this.mSetupTextLayout.getHeight()) - cellParamsForType.iconDrawablePaddingPx) / 2);
                this.mTopCornerDrawable.setBounds(this.mRect);
                Rect rect = this.mRect;
                rect.left = paddingLeft;
                rect.top = rect.bottom + cellParamsForType.iconDrawablePaddingPx;
                z = false;
            } else {
                this.mSetupTextLayout = null;
                int iMin3 = Math.min(i, Math.min((getWidth() - paddingLeft) - paddingRight, (getHeight() - paddingTop) - paddingBottom));
                this.mRect.set(0, 0, iMin3, iMin3);
                this.mRect.offsetTo((getWidth() - this.mRect.width()) / 2, (getHeight() - this.mRect.height()) / 2);
                this.mCenterDrawable.setBounds(this.mRect);
                int iMin4 = Math.min(iMin3 / 2, Math.max(this.mRect.top - paddingTop, this.mRect.left - paddingLeft));
                this.mTopCornerDrawable.setBounds(paddingLeft, paddingTop, paddingLeft + iMin4, iMin4 + paddingTop);
                z = false;
            }
            this.mDrawableSizeChanged = z;
        }
        if (this.mSetupTextLayout == null) {
            this.mCenterDrawable.draw(canvas);
            this.mTopCornerDrawable.draw(canvas);
            return;
        }
        canvas.save();
        canvas.translate(this.mRect.left, this.mRect.top);
        this.mSetupTextLayout.draw(canvas);
        canvas.restore();
        this.mTopCornerDrawable.draw(canvas);
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        this.mDrawableSizeChanged = true;
    }

    @Override // android.view.View
    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.mClickListener = onClickListener;
    }

    @Override // android.appwidget.AppWidgetHostView
    public void updateAppWidgetSize(Bundle bundle, int i, int i2, int i3, int i4) {
    }

    public void updateIcon(IconCache iconCache) {
        Bitmap icon = iconCache.getIcon(this.mIconLookupIntent, this.mInfo.user);
        if (this.mIcon == icon) {
            return;
        }
        this.mIcon = icon;
        PreloadIconDrawable preloadIconDrawable = this.mDrawable;
        if (preloadIconDrawable != null) {
            preloadIconDrawable.setCallback(null);
            this.mDrawable = null;
        }
        if (this.mIcon != null) {
            if (isReadyForClickSetup()) {
                this.mCenterDrawable = getResources().getDrawable(R.drawable.ic_setting);
                this.mTopCornerDrawable = new FastBitmapDrawable(this.mIcon);
            } else {
                if (sPreloaderTheme == null) {
                    sPreloaderTheme = getResources().newTheme();
                    sPreloaderTheme.applyStyle(R.style.PreloadIcon, true);
                }
                this.mDrawable = new PreloadIconDrawable(Utilities.createIconDrawable(this.mIcon), sPreloaderTheme);
                this.mDrawable.setCallback(this);
                applyState();
            }
            this.mDrawableSizeChanged = true;
        }
    }

    @Override // android.view.View
    protected boolean verifyDrawable(Drawable drawable) {
        return drawable == this.mDrawable || super.verifyDrawable(drawable);
    }
}
