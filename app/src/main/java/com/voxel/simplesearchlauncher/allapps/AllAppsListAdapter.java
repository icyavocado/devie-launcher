package com.voxel.simplesearchlauncher.allapps;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.voxel.simplesearchlauncher.model.itemdata.LocalAppData;
import com.voxel.simplesearchlauncher.view.EmptyViewHolder;
import com.voxel.simplesearchlauncher.view.FastScrollRecyclerViewInterface;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class AllAppsListAdapter extends AllAppsAdapter implements FastScrollRecyclerViewInterface {
    private final int mDefaultLauncherViewHeight;
    private boolean mLightMode;
    private final int mLocalAppHeight;
    private final int mSeparatorHeight;

    public AllAppsListAdapter(Context context, AlphabeticalAppsSectionsHelper alphabeticalAppsSectionsHelper, View.OnClickListener onClickListener, View.OnLongClickListener onLongClickListener) {
        super(context, alphabeticalAppsSectionsHelper, onClickListener, onLongClickListener);
        Resources resources = context.getResources();
        this.mDefaultLauncherViewHeight = resources.getDimensionPixelSize(R.dimen.set_default_launcher_list_item_height);
        this.mSeparatorHeight = resources.getDimensionPixelSize(R.dimen.search_list_item_separator_height);
        this.mLocalAppHeight = resources.getDimensionPixelSize(R.dimen.search_list_item_height);
    }

    @Override // com.voxel.simplesearchlauncher.allapps.AllAppsRecyclerView.AllAppsAdapter
    int getRowCount() {
        return getItemCount();
    }

    @Override // com.voxel.simplesearchlauncher.allapps.AllAppsRecyclerView.AllAppsAdapter
    public int getRowHeight(int i) {
        switch (getItemViewType(i)) {
            case 0:
                return this.mDefaultLauncherViewHeight;
            case 1:
            case 3:
                return this.mSeparatorHeight;
            case 2:
                return this.mLocalAppHeight;
            default:
                throw new IllegalStateException("Invalid type");
        }
    }

    @Override // com.voxel.simplesearchlauncher.allapps.AllAppsRecyclerView.AllAppsAdapter
    int getRowIndex(int i) {
        return i;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        switch (getItemViewType(i)) {
            case 0:
                viewHolder.itemView.setTag(R.id.all_app_item_tag, getItem(i));
                viewHolder.itemView.setOnClickListener(this.mOnClickListener);
                return;
            case 1:
                viewHolder.itemView.setVisibility(0);
                return;
            case 2:
                viewHolder.itemView.setTag(R.id.all_app_item_tag, getItem(i));
                viewHolder.itemView.setOnClickListener(this.mOnClickListener);
                viewHolder.itemView.setOnLongClickListener(this.mOnLongClickListener);
                AllAppsFragment.AppViewHolder appViewHolder = (AllAppsFragment.AppViewHolder) viewHolder;
                LocalAppData localAppData = (LocalAppData) getItem(i);
                appViewHolder.mIcon.setLocalAppData(localAppData);
                appViewHolder.mIcon.loadBadgeCount();
                appViewHolder.mText.setText(localAppData.getLabel());
                return;
            case 3:
                return;
            default:
                throw new IllegalStateException("Invalid type");
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        switch (i) {
            case 0:
                return new EmptyViewHolder(this.mInflater.inflate(R.layout.set_default_launcher_list_item, viewGroup, false));
            case 1:
                return new EmptyViewHolder(this.mInflater.inflate(R.layout.all_apps_separator_item, viewGroup, false));
            case 2:
                AllAppsFragment.AppViewHolder appViewHolder = new AllAppsFragment.AppViewHolder(this.mInflater.inflate(R.layout.all_apps_list_icon_text, viewGroup, false));
                appViewHolder.mIcon.setLabelVisibility(false);
                if (this.mLightMode) {
                    appViewHolder.mText.setTextColor(-16777216);
                }
                return appViewHolder;
            case 3:
                return new EmptyViewHolder(this.mInflater.inflate(R.layout.all_apps_list_new_section_separator_item, viewGroup, false));
            default:
                throw new IllegalStateException("Invalid type");
        }
    }

    public void setLightMode(boolean z) {
        this.mLightMode = z;
    }
}
