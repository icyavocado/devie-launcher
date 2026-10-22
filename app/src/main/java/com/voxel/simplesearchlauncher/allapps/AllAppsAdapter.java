package com.voxel.simplesearchlauncher.allapps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import com.voxel.simplesearchlauncher.adapter.ListSeparator;
import com.voxel.simplesearchlauncher.model.itemdata.LocalAppData;
import com.voxel.simplesearchlauncher.model.itemdata.SearchItemData;
import com.voxel.simplesearchlauncher.view.FastScrollRecyclerViewInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public abstract class AllAppsAdapter extends AllAppsRecyclerView.AllAppsAdapter implements FastScrollRecyclerViewInterface {
    protected final LayoutInflater mInflater;
    protected final View.OnClickListener mOnClickListener;
    protected final View.OnLongClickListener mOnLongClickListener;
    protected final AlphabeticalAppsSectionsHelper mSectionsHelper;
    private List<SearchItemData> mSearchItemDataList = new ArrayList();
    private HashMap<String, Integer> mMapIndex = new HashMap<>();

    public AllAppsAdapter(Context context, AlphabeticalAppsSectionsHelper alphabeticalAppsSectionsHelper, View.OnClickListener onClickListener, View.OnLongClickListener onLongClickListener) {
        this.mSectionsHelper = alphabeticalAppsSectionsHelper;
        this.mInflater = LayoutInflater.from(context);
        this.mOnClickListener = onClickListener;
        this.mOnLongClickListener = onLongClickListener;
    }

    public SearchItemData getItem(int i) {
        return this.mSearchItemDataList.get(i);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.mSearchItemDataList.size();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemViewType(int i) {
        SearchItemData searchItemData = this.mSearchItemDataList.get(i);
        if (searchItemData instanceof LocalAppData) {
            return 2;
        }
        if (searchItemData instanceof ListSeparator) {
            return 1;
        }
        return searchItemData instanceof AllAppsFragment.NewSectionSeparator ? 3 : 0;
    }

    @Override // com.voxel.simplesearchlauncher.view.FastScrollRecyclerViewInterface
    public HashMap<String, Integer> getMapIndex() {
        return this.mMapIndex;
    }

    @Override // com.voxel.simplesearchlauncher.allapps.AllAppsRecyclerView.AllAppsAdapter
    String getSectionTitle(int i) {
        switch (getItemViewType(i)) {
            case 0:
            case 1:
            case 3:
                return "";
            case 2:
                return this.mSectionsHelper.getSectionName(this.mSearchItemDataList.get(i).getLabel());
            default:
                throw new IllegalStateException("Invalid type");
        }
    }

    public void setMapIndex(HashMap<String, Integer> map) {
        this.mMapIndex.clear();
        this.mMapIndex.putAll(map);
    }

    public boolean setResultListItems(List<? extends SearchItemData> list) {
        boolean z = !this.mSearchItemDataList.equals(list);
        this.mSearchItemDataList.clear();
        this.mSearchItemDataList.addAll(list);
        return z;
    }
}
