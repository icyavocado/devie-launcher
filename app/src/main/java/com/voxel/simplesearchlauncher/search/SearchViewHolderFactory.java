package com.voxel.simplesearchlauncher.search;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.evie.search.SearchPresenter;
import com.evie.search.recyclerview.viewholder.BaseViewHolder;
import com.evie.search.recyclerview.viewholder.CustomViewHolderFactory;
import com.voxel.simplesearchlauncher.search.view.AppFlashFeedbackSlide;
import com.voxel.simplesearchlauncher.search.view.EnjoySlide;
import com.voxel.simplesearchlauncher.search.view.FeedbackSlide;
import com.voxel.simplesearchlauncher.search.view.GenericEnjoySlide;
import com.voxel.simplesearchlauncher.search.view.GenericNoFeedbackSlide;
import com.voxel.simplesearchlauncher.search.view.NoFeedbackSlide;
import com.voxel.simplesearchlauncher.search.view.NoRatingSlide;
import com.voxel.simplesearchlauncher.search.view.RatingSlide;
import com.voxel.simplesearchlauncher.search.view.SetDefaultLauncherSlide;
import com.voxel.simplesearchlauncher.view.SlideLayout;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
class SearchViewHolderFactory implements CustomViewHolderFactory {
    private Activity mActivity;
    private SlideViewHolder mAppflashFeedbackViewHolder;
    private SlideViewHolder mFeedbackViewHolder;
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
        return i == 345780345 || i == 796234692 || i == 345780346;
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
        switch (i) {
            case 345780345:
                if (this.mFeedbackViewHolder == null) {
                    this.mFeedbackViewHolder = new SlideViewHolder((ViewGroup) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.search_feedback_item, viewGroup, false));
                    this.mFeedbackViewHolder.mSlideLayout.addSlide(new EnjoySlide(viewGroup.getContext(), this.mFeedbackViewHolder.mSlideLayout, 1, 3), 0);
                    this.mFeedbackViewHolder.mSlideLayout.addSlide(new RatingSlide(viewGroup.getContext(), this.mFeedbackViewHolder.mSlideLayout, this.mSearch, this.mActivity, 2), 1);
                    this.mFeedbackViewHolder.mSlideLayout.addSlide(new NoRatingSlide(viewGroup.getContext(), this.mFeedbackViewHolder.mSlideLayout, this.mSearch), 2);
                    this.mFeedbackViewHolder.mSlideLayout.addSlide(new FeedbackSlide(viewGroup.getContext(), this.mFeedbackViewHolder.mSlideLayout, this.mSearch, this.mActivity, 4), 3);
                    this.mFeedbackViewHolder.mSlideLayout.addSlide(new NoFeedbackSlide(viewGroup.getContext(), this.mFeedbackViewHolder.mSlideLayout, this.mSearch), 4);
                }
                return this.mFeedbackViewHolder;
            case 345780346:
                if (this.mAppflashFeedbackViewHolder == null) {
                    this.mAppflashFeedbackViewHolder = new SlideViewHolder((ViewGroup) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.search_feedback_item, viewGroup, false));
                    this.mAppflashFeedbackViewHolder.mSlideLayout.addSlide(new GenericEnjoySlide(viewGroup.getContext(), this.mAppflashFeedbackViewHolder.mSlideLayout, 1, 3), 0);
                    this.mAppflashFeedbackViewHolder.mSlideLayout.addSlide(new RatingSlide(viewGroup.getContext(), this.mAppflashFeedbackViewHolder.mSlideLayout, this.mSearch, this.mActivity, 2), 1);
                    this.mAppflashFeedbackViewHolder.mSlideLayout.addSlide(new NoRatingSlide(viewGroup.getContext(), this.mAppflashFeedbackViewHolder.mSlideLayout, this.mSearch), 2);
                    this.mAppflashFeedbackViewHolder.mSlideLayout.addSlide(new AppFlashFeedbackSlide(viewGroup.getContext(), this.mAppflashFeedbackViewHolder.mSlideLayout, this.mSearch, this.mActivity, 4), 3);
                    this.mAppflashFeedbackViewHolder.mSlideLayout.addSlide(new GenericNoFeedbackSlide(viewGroup.getContext(), this.mAppflashFeedbackViewHolder.mSlideLayout, this.mSearch), 4);
                }
                return this.mAppflashFeedbackViewHolder;
            default:
                return null;
        }
    }
}
