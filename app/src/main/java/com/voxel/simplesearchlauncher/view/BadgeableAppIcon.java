package com.voxel.simplesearchlauncher.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.evie.common.HybridNetworkFetcherCommon;
import com.voxel.launcher3.CellParams;
import com.voxel.launcher3.Utilities;
import com.voxel.launcher3.graphics.IconPalette;
import com.ovacoda.devie.images.IconLoader;
import com.voxel.simplesearchlauncher.model.AppsInfoDataHelper;
import com.voxel.simplesearchlauncher.model.itemdata.LocalAppData;
import com.voxel.simplesearchlauncher.notification.Notifier;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
/* REWORK(fresco): icon view = ImageView; the badge-tint bitmap callback now
 * arrives through IconLoader's main-thread listener (no drawee pipeline). */
public class BadgeableAppIcon extends LinearLayout {
    private String mActivityName;
    private BadgeDrawable mBadgeDrawable;
    private IconLoader.OnBitmapLoaded mDataSubscriber;
    private ImageView mIcon;
    private TextView mLabel;
    private String mPackageName;
    private int mTextColor;

    public BadgeableAppIcon(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BadgeableAppIcon(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mDataSubscriber = new IconLoader.OnBitmapLoaded() {
            public void onBitmapLoaded(Bitmap bitmap) {
                if (bitmap != null && BadgeableAppIcon.this.mBadgeDrawable != null) {
                    BadgeableAppIcon.this.mBadgeDrawable.setIconPalette(IconPalette.fromDominantColor(Utilities.findDominantColorByHue(bitmap, 20), true));
                }
                if (BadgeableAppIcon.this.mBadgeDrawable != null) {
                    BadgeableAppIcon.this.mBadgeDrawable.setBadgeEnabled(true);
                }
                BadgeableAppIcon.this.postInvalidate();
            }
        };
        setGravity(1);
        setOrientation(1);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
        layoutParams.gravity = 1;
        this.mIcon = new ImageView(getContext());
        this.mIcon.setLayoutParams(layoutParams);
        addView(this.mIcon);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, -2);
        layoutParams2.gravity = 1;
        this.mTextColor = getResources().getColor(R.color.workspace_icon_text_color);
        this.mLabel = new TextView(getContext());
        this.mLabel.setLayoutParams(layoutParams2);
        this.mLabel.setTextAlignment(4);
        this.mLabel.setTextColor(this.mTextColor);
        this.mLabel.setShadowLayer(4.0f, 0.0f, 2.0f, -587202560);
        addView(this.mLabel);
        this.mBadgeDrawable = new BadgeDrawable(getContext(), null);
        this.mBadgeDrawable.setCallback(this);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        BadgeDrawable badgeDrawable = this.mBadgeDrawable;
        if (badgeDrawable != null) {
            badgeDrawable.draw(canvas);
        }
    }

    public void loadBadgeCount() {
        if (this.mBadgeDrawable == null) {
            return;
        }
        this.mBadgeDrawable.setBadge(Notifier.getInstance(getContext()).getCount(this.mPackageName, this.mActivityName));
    }

    public void loadImage(Uri uri) {
        this.mBadgeDrawable.setBadge(0);
        this.mBadgeDrawable.setBadgeEnabled(false);
        this.mBadgeDrawable.setIconPalette(null);
        this.mIcon.setImageDrawable(null);
        IconLoader.getInstance().loadInto(this.mIcon, uri == null ? null : uri.toString(), false, this.mDataSubscriber);
    }

    @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (this.mBadgeDrawable == null) {
            return;
        }
        Rect rect = new Rect();
        rect.left = this.mIcon.getLeft() + this.mIcon.getPaddingLeft();
        rect.right = this.mIcon.getRight() - this.mIcon.getPaddingRight();
        rect.top = this.mIcon.getTop() - this.mIcon.getPaddingTop();
        rect.bottom = this.mIcon.getBottom() - this.mIcon.getPaddingBottom();
        this.mBadgeDrawable.setBaseBounds(rect);
        Rect rect2 = new Rect();
        rect2.right = getWidth();
        rect2.bottom = getHeight();
        this.mBadgeDrawable.setMaxBounds(rect2);
    }

    public void setAppInfo(AppsInfoDataHelper.AppInfo appInfo) {
        this.mPackageName = appInfo.packageName;
        this.mActivityName = appInfo.activityName;
        loadImage(HybridNetworkFetcherCommon.getMagicResourceUri(appInfo.packageName, appInfo.activityName, appInfo.appVersion));
        this.mLabel.setText(appInfo.appLabel);
        invalidate();
        requestLayout();
    }

    public void setBadgeCount(int i) {
        BadgeDrawable badgeDrawable = this.mBadgeDrawable;
        if (badgeDrawable == null) {
            return;
        }
        badgeDrawable.setBadge(i);
        postInvalidate();
    }

    public void setCellParams(CellParams cellParams) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(cellParams.getIconSizePx(), cellParams.getIconSizePx());
        layoutParams.gravity = 1;
        this.mIcon.setLayoutParams(layoutParams);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, -2);
        layoutParams2.gravity = 1;
        layoutParams2.topMargin = cellParams.getIconDrawablePaddingPx();
        this.mLabel.setLayoutParams(layoutParams2);
        this.mLabel.setTextSize(0, cellParams.getIconTextSizePx());
        this.mLabel.setVisibility(cellParams.showLabels ? 0 : 8);
        invalidate();
        requestLayout();
    }

    public void setLabelVisibility(boolean z) {
        this.mLabel.setVisibility(z ? 0 : 8);
    }

    public void setLocalAppData(LocalAppData localAppData) {
        this.mPackageName = localAppData.getPackageName();
        this.mActivityName = localAppData.getActivityName();
        loadImage(HybridNetworkFetcherCommon.getMagicResourceUri(this.mPackageName, this.mActivityName, localAppData.getAppVersionCode()));
        this.mLabel.setText(localAppData.getLabel());
        invalidate();
        requestLayout();
    }
}
