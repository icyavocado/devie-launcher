package com.voxel.launcher3;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.voxel.launcher3.dragndrop.DragLayer;
import com.voxel.simplesearchlauncher.view.CenterCropOffsetImageView;
import com.voxel.simplesearchlauncher.view.HotseatImageView;
import com.voxel.simplesearchlauncher.view.PullDownRecyclerView;
import com.voxel.simplesearchlauncher.view.SpringySearchView;
import com.voxel.simplesearchlauncher.view.WindowInsetPolicyFrameLayout;
import com.voxel.simplesearchlauncher.view.launcher.IndicatorContainer;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class Launcher_ViewBinding implements Unbinder {
    private Launcher target;

    public Launcher_ViewBinding(Launcher launcher, View view) {
        this.target = launcher;
        launcher.mLauncherView = (CoordinatorLayout) Utils.findRequiredViewAsType(view, R.id.launcher, "field 'mLauncherView'", CoordinatorLayout.class);
        launcher.mWorkspace = (Workspace) Utils.findRequiredViewAsType(view, R.id.workspace, "field 'mWorkspace'", Workspace.class);
        launcher.mMainLayer = (WindowInsetPolicyFrameLayout) Utils.findRequiredViewAsType(view, R.id.main_layer, "field 'mMainLayer'", WindowInsetPolicyFrameLayout.class);
        launcher.mMainLayerBackground = (CenterCropOffsetImageView) Utils.findRequiredViewAsType(view, R.id.main_layer_background, "field 'mMainLayerBackground'", CenterCropOffsetImageView.class);
        launcher.mMainLayerDarken = (ImageView) Utils.findRequiredViewAsType(view, R.id.main_layer_darken, "field 'mMainLayerDarken'", ImageView.class);
        launcher.mIndicatorContainer = (IndicatorContainer) Utils.findRequiredViewAsType(view, R.id.indicator_container, "field 'mIndicatorContainer'", IndicatorContainer.class);
        launcher.mAllAppsUpChevron = (ImageView) Utils.findRequiredViewAsType(view, R.id.all_apps_chevron, "field 'mAllAppsUpChevron'", ImageView.class);
        launcher.mPageIndicators = Utils.findRequiredView(view, R.id.page_indicator, "field 'mPageIndicators'");
        launcher.mDragLayer = (DragLayer) Utils.findRequiredViewAsType(view, R.id.drag_layer, "field 'mDragLayer'", DragLayer.class);
        launcher.mHotseat = (Hotseat) Utils.findRequiredViewAsType(view, R.id.hotseat, "field 'mHotseat'", Hotseat.class);
        launcher.mHotseatBg = (HotseatImageView) Utils.findRequiredViewAsType(view, R.id.hotseat_bg, "field 'mHotseatBg'", HotseatImageView.class);
        launcher.mOverviewPanel = (ViewGroup) Utils.findRequiredViewAsType(view, R.id.overview_panel, "field 'mOverviewPanel'", ViewGroup.class);
        launcher.mSearchDropTargetBar = (SearchDropTargetBar) Utils.findRequiredViewAsType(view, R.id.search_drop_target_bar, "field 'mSearchDropTargetBar'", SearchDropTargetBar.class);
        launcher.mSearchLayout = Utils.findRequiredView(view, R.id.search_layout, "field 'mSearchLayout'");
        launcher.mQsb = Utils.findRequiredView(view, R.id.qsb, "field 'mQsb'");
        launcher.mQsbSearchView = (SpringySearchView) Utils.findRequiredViewAsType(view, R.id.qsb_search, "field 'mQsbSearchView'", SpringySearchView.class);
        launcher.mQsbSearchViewEditText = (EditText) Utils.findRequiredViewAsType(view, R.id.search_src_text, "field 'mQsbSearchViewEditText'", EditText.class);
        launcher.mPullDownRecyclerView = (PullDownRecyclerView) Utils.findRequiredViewAsType(view, R.id.pull_down_list, "field 'mPullDownRecyclerView'", PullDownRecyclerView.class);
        launcher.mBlurLayoutView = (FrameLayout) Utils.findRequiredViewAsType(view, R.id.home_blur_layout, "field 'mBlurLayoutView'", FrameLayout.class);
        launcher.mBlurImageView = (CenterCropOffsetImageView) Utils.findRequiredViewAsType(view, R.id.home_blur_image_view, "field 'mBlurImageView'", CenterCropOffsetImageView.class);
        launcher.mAllAppsBackground = (CenterCropOffsetImageView) Utils.findRequiredViewAsType(view, R.id.all_apps_background, "field 'mAllAppsBackground'", CenterCropOffsetImageView.class);
        launcher.mAllAppsContainer = Utils.findRequiredView(view, R.id.all_apps_container, "field 'mAllAppsContainer'");
        launcher.mGoogleNowAnimBackground = (FrameLayout) Utils.findRequiredViewAsType(view, R.id.google_now_launch_background, "field 'mGoogleNowAnimBackground'", FrameLayout.class);
        launcher.mGoogleNowAnimIcon = (ImageView) Utils.findRequiredViewAsType(view, R.id.google_now_launch_background_image, "field 'mGoogleNowAnimIcon'", ImageView.class);
        launcher.mFocusHandler = (FocusIndicatorView) Utils.findRequiredViewAsType(view, R.id.focus_indicator, "field 'mFocusHandler'", FocusIndicatorView.class);
        launcher.mWorkspaceBackgroundDrawable = ContextCompat.getDrawable(view.getContext(), R.drawable.workspace_bg);
    }

    @Override // butterknife.Unbinder
    public void unbind() {
        Launcher launcher = this.target;
        if (launcher == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.target = null;
        launcher.mLauncherView = null;
        launcher.mWorkspace = null;
        launcher.mMainLayer = null;
        launcher.mMainLayerBackground = null;
        launcher.mMainLayerDarken = null;
        launcher.mIndicatorContainer = null;
        launcher.mAllAppsUpChevron = null;
        launcher.mPageIndicators = null;
        launcher.mDragLayer = null;
        launcher.mHotseat = null;
        launcher.mHotseatBg = null;
        launcher.mOverviewPanel = null;
        launcher.mSearchDropTargetBar = null;
        launcher.mSearchLayout = null;
        launcher.mQsb = null;
        launcher.mQsbSearchView = null;
        launcher.mQsbSearchViewEditText = null;
        launcher.mPullDownRecyclerView = null;
        launcher.mBlurLayoutView = null;
        launcher.mBlurImageView = null;
        launcher.mAllAppsBackground = null;
        launcher.mAllAppsContainer = null;
        launcher.mGoogleNowAnimBackground = null;
        launcher.mGoogleNowAnimIcon = null;
        launcher.mFocusHandler = null;
    }
}
