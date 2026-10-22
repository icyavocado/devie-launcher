package com.voxel.simplesearchlauncher.allapps;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.voxel.launcher3.CellParams;
import com.voxel.simplesearchlauncher.model.itemdata.LocalAppData;
import com.voxel.simplesearchlauncher.model.itemdata.SearchItemData;
import com.voxel.simplesearchlauncher.view.EmptyViewHolder;
import com.voxel.simplesearchlauncher.view.FastScrollRecyclerViewInterface;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class AllAppsGridAdapter extends AllAppsAdapter implements FastScrollRecyclerViewInterface {
    private CellParams mCellParams;
    private final int mDefaultLauncherViewHeight;
    private final GridLayoutManager mGridLayoutManager;
    private final int mIconItemPadding;
    private int mLocalAppHeight;
    private List<Integer> mRowIndexes;
    private final int mSeparatorHeight;

    public AllAppsGridAdapter(Context context, AlphabeticalAppsSectionsHelper alphabeticalAppsSectionsHelper, GridLayoutManager gridLayoutManager, CellParams cellParams, View.OnClickListener onClickListener, View.OnLongClickListener onLongClickListener) {
        super(context, alphabeticalAppsSectionsHelper, onClickListener, onLongClickListener);
        this.mRowIndexes = new ArrayList();
        this.mGridLayoutManager = gridLayoutManager;
        Resources resources = context.getResources();
        this.mDefaultLauncherViewHeight = resources.getDimensionPixelSize(R.dimen.set_default_launcher_list_item_height);
        this.mSeparatorHeight = resources.getDimensionPixelSize(R.dimen.search_list_item_separator_height);
        this.mIconItemPadding = resources.getDimensionPixelSize(R.dimen.all_apps_item_padding);
        setCellParams(cellParams, false);
    }

    private void setCellParams(CellParams cellParams, boolean z) {
        this.mCellParams = cellParams;
        this.mLocalAppHeight = cellParams.getCellContentHeightPx() + (this.mIconItemPadding * 2);
        if (z) {
            notifyDataSetChanged();
        }
    }

    @Override // com.voxel.simplesearchlauncher.allapps.AllAppsRecyclerView.AllAppsAdapter
    int getRowCount() {
        List<Integer> list = this.mRowIndexes;
        return list.get(list.size() - 1).intValue() + 1;
    }

    @Override // com.voxel.simplesearchlauncher.allapps.AllAppsRecyclerView.AllAppsAdapter
    public int getRowHeight(int i) {
        switch (getItemViewType(this.mRowIndexes.indexOf(Integer.valueOf(i)))) {
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
        return this.mRowIndexes.get(i).intValue();
    }

    public int getSpanSize(int i) {
        int itemViewType = getItemViewType(i);
        switch (itemViewType) {
            case 0:
            case 1:
            case 3:
                return this.mGridLayoutManager.getSpanCount();
            case 2:
                return 1;
            default:
                throw new IllegalArgumentException("illegal item type: " + itemViewType);
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        switch (getItemViewType(i)) {
            case 0:
                viewHolder.itemView.setTag(R.id.all_app_item_tag, getItem(i));
                viewHolder.itemView.setOnClickListener(this.mOnClickListener);
                return;
            case 1:
            case 3:
                return;
            case 2:
                viewHolder.itemView.setTag(R.id.all_app_item_tag, getItem(i));
                viewHolder.itemView.setOnClickListener(this.mOnClickListener);
                viewHolder.itemView.setOnLongClickListener(this.mOnLongClickListener);
                AllAppsFragment.AppViewHolder appViewHolder = (AllAppsFragment.AppViewHolder) viewHolder;
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) appViewHolder.mIcon.getLayoutParams();
                marginLayoutParams.width = this.mCellParams.getIconSizePx();
                marginLayoutParams.height = this.mCellParams.getIconSizePx();
                marginLayoutParams.bottomMargin = this.mCellParams.getIconDrawablePaddingPx();
                appViewHolder.mIcon.setCellParams(this.mCellParams);
                appViewHolder.mIcon.setLayoutParams(marginLayoutParams);
                appViewHolder.mIcon.setLabelVisibility(false);
                appViewHolder.mText.setTextSize(0, this.mCellParams.getIconTextSizePx());
                appViewHolder.mText.setVisibility(this.mCellParams.showLabels ? 0 : 8);
                LocalAppData localAppData = (LocalAppData) getItem(i);
                appViewHolder.mIcon.setLocalAppData(localAppData);
                appViewHolder.mIcon.loadBadgeCount();
                appViewHolder.mText.setText(localAppData.getLabel());
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
                return new EmptyViewHolder(this.mInflater.inflate(R.layout.all_apps_grid_separator_item, viewGroup, false));
            case 2:
                return new AllAppsFragment.AppViewHolder(this.mInflater.inflate(R.layout.all_apps_grid_icon_text, viewGroup, false));
            case 3:
                return new EmptyViewHolder(this.mInflater.inflate(R.layout.all_apps_grid_new_section_separator_item, viewGroup, false));
            default:
                throw new IllegalStateException("Invalid type");
        }
    }

    public void recalculateRowIndexes() {
        this.mRowIndexes.clear();
        for (int i = 0; i < getItemCount(); i++) {
            this.mRowIndexes.add(Integer.valueOf(this.mGridLayoutManager.getSpanSizeLookup().getSpanGroupIndex(i, this.mGridLayoutManager.getSpanCount())));
        }
    }

    public void setCellParams(CellParams cellParams) {
        setCellParams(cellParams, true);
    }

    @Override // com.voxel.simplesearchlauncher.allapps.AllAppsAdapter
    public boolean setResultListItems(List<? extends SearchItemData> list) {
        boolean resultListItems = super.setResultListItems(list);
        recalculateRowIndexes();
        return resultListItems;
    }
}
