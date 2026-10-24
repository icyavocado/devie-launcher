package com.voxel.simplesearchlauncher.search;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.evie.search.SearchPresenter;
import com.evie.search.recyclerview.viewholder.BaseViewHolder;
import com.evie.search.recyclerview.viewholder.CustomViewHolderFactory;
import com.voxel.simplesearchlauncher.search.view.SetDefaultLauncherSlide;
import com.voxel.simplesearchlauncher.view.SlideLayout;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
/* REWORK: the rate-us / feedback survey view types were cut with the Play
 * Store (345780345 + the orphaned 345780346 AppFlash variant); only the
 * set-default-launcher prompt remains. */
class SearchViewHolderFactory implements CustomViewHolderFactory {
    private Activity mActivity;
    private SearchPresenter mSearch;
    private SlideViewHolder mSetDefaultLauncherViewHolder;

    private class SlideViewHolder extends BaseViewHolder {
        ViewGroup mContent;
        SlideLayout mSlideLayout;
        ViewGroup mViewGroup;

        private SlideViewHolder(View view) {
            super(view);
            this.mViewGroup = (ViewGroup) view;
            this.mContent = (ViewGroup) view.findViewById(R.id.content);
            this.mSlideLayout = (SlideLayout) view.findViewById(R.id.feedback_slides);
        }

        @Override // com.evie.search.recyclerview.viewholder.BaseViewHolder
        public void clear() {
        }
    }

    public SearchViewHolderFactory(Activity activity, SearchPresenter searchPresenter) {
        this.mActivity = activity;
        this.mSearch = searchPresenter;
    }

    @Override // com.evie.search.recyclerview.viewholder.CustomViewHolderFactory
    public boolean handlesViewType(int i) {
        return i == 796234692;
    }

    @Override // com.evie.search.recyclerview.viewholder.CustomViewHolderFactory
    public BaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == 796234692) {
            if (this.mSetDefaultLauncherViewHolder == null) {
                this.mSetDefaultLauncherViewHolder = new SlideViewHolder((ViewGroup) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.search_default_launcher_prompt_item, viewGroup, false));
                this.mSetDefaultLauncherViewHolder.mSlideLayout.addSlide(new SetDefaultLauncherSlide(viewGroup.getContext(), this.mSetDefaultLauncherViewHolder.mSlideLayout, this.mSearch, this.mActivity), 0);
            }
            return this.mSetDefaultLauncherViewHolder;
        }
        return null;
    }
}