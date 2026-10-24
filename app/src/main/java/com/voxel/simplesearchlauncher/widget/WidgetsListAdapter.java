package com.voxel.simplesearchlauncher.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.voxel.launcher3.DeviceProfile;
import com.voxel.launcher3.DynamicGrid;
import com.voxel.launcher3.Launcher;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.Utilities;
import com.voxel.launcher3.WidgetPreviewLoader;
import com.voxel.simplesearchlauncher.widget.WidgetsRowViewHolder;
import com.ovacoda.devie.R;
import com.ovacoda.devie.images.IconLoader;
import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
public class WidgetsListAdapter extends RecyclerView.Adapter<WidgetsRowViewHolder> {
    private View.OnClickListener mIconClickListener;
    private View.OnLongClickListener mIconLongClickListener;
    private int mIndent = 0;
    private Launcher mLauncher;
    private LayoutInflater mLayoutInflater;
    private WidgetPreviewLoader mWidgetPreviewLoader;
    private WidgetsModel mWidgetsModel;

    public WidgetsListAdapter(Context context, View.OnClickListener onClickListener, View.OnLongClickListener onLongClickListener, Launcher launcher) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mIconClickListener = onClickListener;
        this.mIconLongClickListener = onLongClickListener;
        this.mLauncher = launcher;
        setContainerHeight();
    }

    private WidgetPreviewLoader getWidgetPreviewLoader() {
        if (this.mWidgetPreviewLoader == null) {
            this.mWidgetPreviewLoader = new WidgetPreviewLoader(this.mLauncher, LauncherAppState.getInstance().getIconCache());
        }
        return this.mWidgetPreviewLoader;
    }

    private void setContainerHeight() {
        Resources resources = this.mLauncher.getResources();
        DeviceProfile deviceProfile = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
        if (deviceProfile.isLargeTablet || deviceProfile.isTablet) {
            this.mIndent = DynamicGrid.pxFromDp(56.0f, resources.getDisplayMetrics());
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        WidgetsModel widgetsModel = this.mWidgetsModel;
        if (widgetsModel == null) {
            return 0;
        }
        return widgetsModel.getPackageSize();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public long getItemId(int i) {
        return i;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(WidgetsRowViewHolder widgetsRowViewHolder, int i) {
        List<Object> sortedWidgets = this.mWidgetsModel.getSortedWidgets(i);
        ViewGroup viewGroup = widgetsRowViewHolder.mRow;
        int size = sortedWidgets.size() - viewGroup.getChildCount();
        if (size > 0) {
            for (int i2 = 0; i2 < size; i2++) {
                WidgetCell widgetCell = (WidgetCell) this.mLayoutInflater.inflate(R.layout.widget_cell, viewGroup, false);
                widgetCell.setOnClickListener(this.mIconClickListener);
                widgetCell.setOnLongClickListener(this.mIconLongClickListener);
                viewGroup.addView(widgetCell);
            }
        } else if (size < 0) {
            for (int size2 = sortedWidgets.size(); size2 < viewGroup.getChildCount(); size2++) {
                viewGroup.getChildAt(size2).setVisibility(8);
            }
        }
        PackageItemInfo packageItemInfo = this.mWidgetsModel.getPackageItemInfo(i);
        if (packageItemInfo.iconUri == null) {
            widgetsRowViewHolder.mSectionIcon.setImageDrawable(null);
        } else {
            IconLoader.getInstance().loadInto(widgetsRowViewHolder.mSectionIcon, packageItemInfo.iconUri);
        }
        TextView textView = widgetsRowViewHolder.mSection;
        textView.setText(packageItemInfo.title);
        textView.setTag(packageItemInfo);
        if (getWidgetPreviewLoader() == null) {
            return;
        }
        for (int i3 = 0; i3 < sortedWidgets.size(); i3++) {
            WidgetCell widgetCell2 = (WidgetCell) viewGroup.getChildAt(i3);
            if (sortedWidgets.get(i3) instanceof LauncherAppWidgetProviderInfo) {
                LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo = (LauncherAppWidgetProviderInfo) sortedWidgets.get(i3);
                widgetCell2.setTag(new PendingAddWidgetInfo(this.mLauncher, launcherAppWidgetProviderInfo, null));
                widgetCell2.applyFromAppWidgetProviderInfo(launcherAppWidgetProviderInfo, this.mWidgetPreviewLoader);
            } else if (sortedWidgets.get(i3) instanceof ResolveInfo) {
                ResolveInfo resolveInfo = (ResolveInfo) sortedWidgets.get(i3);
                widgetCell2.setTag(new PendingAddShortcutInfo(resolveInfo.activityInfo));
                widgetCell2.applyFromResolveInfo(this.mLauncher.getPackageManager(), resolveInfo, this.mWidgetPreviewLoader);
            }
            widgetCell2.ensurePreview();
            widgetCell2.setVisibility(0);
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    @TargetApi(17)
    public WidgetsRowViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        ViewGroup viewGroup2 = (ViewGroup) this.mLayoutInflater.inflate(R.layout.widgets_list_row_view, viewGroup, false);
        LinearLayout linearLayout = (LinearLayout) viewGroup2.findViewById(R.id.widgets_cell_list);
        if (Utilities.ATLEAST_JB_MR1) {
            linearLayout.setPaddingRelative(this.mIndent, 0, 1, 0);
        } else {
            linearLayout.setPadding(this.mIndent, 0, 1, 0);
        }
        return new WidgetsRowViewHolder(viewGroup2);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public boolean onFailedToRecycleView(WidgetsRowViewHolder widgetsRowViewHolder) {
        return true;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onViewRecycled(WidgetsRowViewHolder widgetsRowViewHolder) {
        ViewGroup viewGroup = (ViewGroup) widgetsRowViewHolder.getContent().findViewById(R.id.widgets_cell_list);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            ((WidgetCell) viewGroup.getChildAt(i)).clear();
        }
    }

    public void setWidgetsModel(WidgetsModel widgetsModel) {
        this.mWidgetsModel = widgetsModel;
    }
}
