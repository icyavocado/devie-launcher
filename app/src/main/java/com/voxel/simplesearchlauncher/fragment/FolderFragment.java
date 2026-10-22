package com.voxel.simplesearchlauncher.fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.core.view.animation.PathInterpolatorCompat;
import androidx.fragment.app.Fragment;
import com.facebook.rebound.SpringConfig;
import com.voxel.launcher3.BubbleTextView;
import com.voxel.launcher3.CellLayout;
import com.voxel.launcher3.CellParams;
import com.voxel.launcher3.DeviceProfile;
import com.voxel.launcher3.Folder;
import com.voxel.launcher3.FolderIcon;
import com.voxel.launcher3.FolderInfo;
import com.voxel.launcher3.LauncherAnimUtils;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.LauncherApplication;
import com.voxel.launcher3.PreloadIconDrawable;
import com.voxel.launcher3.ShortcutInfo;
import com.voxel.launcher3.Utilities;
import com.voxel.simplesearchlauncher.utils.AlphaUtil;
import com.voxel.simplesearchlauncher.view.InterceptableFrameLayout;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
public class FolderFragment extends Fragment implements FolderInfo.FolderListener {
    private OnFolderFragmentInteractionListener fragmentInteractionListener;
    private Drawable mBackgroundWashDrawable;
    private InterceptableFrameLayout mContentView;
    private View mCoverClick;
    private AnimatorSet mCurrentAnimators;
    private Folder mFolder;
    private FolderIcon mFolderIcon;
    private int mFromXOffset;
    private int mFromYOffset;
    private FolderFragmentListener mListener;
    private int toXOffset;
    private int toYOffset;
    private static final SpringConfig RUBBERBANDING_CONFIG = new SpringConfig(350.0d, 50.0d);
    private static final int MIN_DURATION_MS = Math.round(450.0f);
    private static final LinearInterpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final TimeInterpolator INTERPOLATOR = PathInterpolatorCompat.create(0.35f, 0.77f, 0.19f, 1.0f);
    private static final TimeInterpolator REVERSE_INTERPOLATOR = PathInterpolatorCompat.create(0.81f, 0.0f, 0.65f, 0.23f);
    private TimeInterpolator mCurrentInterpolator = INTERPOLATOR;
    private float mMainFactor = 0.0f;
    private float mMainMaxFactor = 1.0f;
    private List<TransitionItem> mAllTransitionItems = new ArrayList();
    private List<TransitionItem> mPreviewTransitionItems = new ArrayList();
    private List<TransitionItem> mOtherTransitionItems = new ArrayList();
    private boolean mIsExiting = false;
    private Animator.AnimatorListener mCurrentAnimatorsListener = new Animator.AnimatorListener() { // from class: com.voxel.simplesearchlauncher.fragment.FolderFragment.1
        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animator) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            FolderFragment.this.mCurrentAnimators = null;
            if (FolderFragment.this.mFolder != null) {
                FolderFragment.this.mFolder.getContent().setVisibility(0);
            }
            Iterator it = FolderFragment.this.mAllTransitionItems.iterator();
            while (it.hasNext()) {
                ((TransitionItem) it.next()).mBubbleTextView.setVisibility(4);
            }
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationRepeat(Animator animator) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animator) {
            if (FolderFragment.this.mFolder != null) {
                FolderFragment.this.mFolder.getContent().setVisibility(4);
            }
            if (FolderFragment.this.mFolderIcon != null) {
                FolderFragment.this.mFolderIcon.setIconsVisible(false);
            }
            Iterator it = FolderFragment.this.mAllTransitionItems.iterator();
            while (it.hasNext()) {
                ((TransitionItem) it.next()).mBubbleTextView.setVisibility(0);
            }
        }
    };
    private ValueAnimator.AnimatorUpdateListener mMainFactorUpdateListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$FolderFragment$yKILN8e2dThFijse7KWcHsUb_DU
        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public final void onAnimationUpdate(ValueAnimator valueAnimator) {
            FolderFragment.this.lambda$new$0( valueAnimator);
        }
    };

    private static class BottomLeftRowItemSorter extends RowItemSorter {
        private BottomLeftRowItemSorter() {
        }

        @Override // com.voxel.simplesearchlauncher.fragment.FolderFragment.RowItemSorter
        float distanceSquaredToTarget(float f, float f2, float f3, float f4, int i, int i2, float f5, float f6) {
            return FolderFragment.distanceSquared(f, f2, f3, f4 + (i2 * f6));
        }

        @Override // com.voxel.simplesearchlauncher.fragment.FolderFragment.RowItemSorter
        List<TransitionItem> sort(List<TransitionItem> list, int i, int i2) {
            ArrayList arrayList = new ArrayList(list.size());
            for (int i3 = i - 1; i3 >= 0; i3--) {
                for (int i4 = i2 - 1; i4 >= 0; i4--) {
                    int i5 = (i3 * i2) + i4;
                    if (i5 < list.size()) {
                        arrayList.add(list.get(i5));
                    }
                }
            }
            return arrayList;
        }
    }

    private static class BottomRightRowItemSorter extends RowItemSorter {
        private BottomRightRowItemSorter() {
        }

        @Override // com.voxel.simplesearchlauncher.fragment.FolderFragment.RowItemSorter
        float distanceSquaredToTarget(float f, float f2, float f3, float f4, int i, int i2, float f5, float f6) {
            return FolderFragment.distanceSquared(f, f2, f3 + (i * f5), f4 + (i2 * f6));
        }

        @Override // com.voxel.simplesearchlauncher.fragment.FolderFragment.RowItemSorter
        List<TransitionItem> sort(List<TransitionItem> list, int i, int i2) {
            ArrayList arrayList = new ArrayList(list.size());
            for (int size = list.size() - 1; size >= 0; size--) {
                arrayList.add(list.get(size));
            }
            return arrayList;
        }
    }

    public interface FolderFragmentListener {
        void onAnimationStarted();
    }

    public interface OnFolderFragmentInteractionListener {
        boolean isDragging();

        void onFolderFragmentExit(FolderFragment folderFragment);

        void onFolderFragmentRequestClose(FolderFragment folderFragment);

        void onFolderFragmentWillExit(FolderFragment folderFragment);
    }

    private static abstract class RowItemSorter {
        private static final RowItemSorter[] SORTERS;

        static {
            SORTERS = new RowItemSorter[]{new TopLeftRowItemSorter(), new TopRightRowItemSorter(), new BottomLeftRowItemSorter(), new BottomRightRowItemSorter()};
        }

        private RowItemSorter() {
        }

        static RowItemSorter findBestRowItemSorter(float f, float f2, float f3, float f4, int i, int i2, float f5, float f6) {
            ArrayList<Pair> arrayList = new ArrayList(SORTERS.length);
            for (RowItemSorter rowItemSorter : SORTERS) {
                arrayList.add(new Pair(rowItemSorter, Float.valueOf(rowItemSorter.distanceSquaredToTarget(f, f2, f3, f4, i, i2, f5, f6))));
            }
            Pair pair = null;
            for (Pair pair2 : arrayList) {
                if (pair == null) {
                    pair = pair2;
                } else if (((Float) pair2.second).floatValue() < ((Float) pair.second).floatValue()) {
                    pair = pair2;
                }
            }
            return (RowItemSorter) pair.first;
        }

        abstract float distanceSquaredToTarget(float f, float f2, float f3, float f4, int i, int i2, float f5, float f6);

        abstract List<TransitionItem> sort(List<TransitionItem> list, int i, int i2);
    }

    private static class TopLeftRowItemSorter extends RowItemSorter {
        private TopLeftRowItemSorter() {
        }

        @Override // com.voxel.simplesearchlauncher.fragment.FolderFragment.RowItemSorter
        float distanceSquaredToTarget(float f, float f2, float f3, float f4, int i, int i2, float f5, float f6) {
            return FolderFragment.distanceSquared(f, f2, f3, f4);
        }

        @Override // com.voxel.simplesearchlauncher.fragment.FolderFragment.RowItemSorter
        List<TransitionItem> sort(List<TransitionItem> list, int i, int i2) {
            return new ArrayList(list);
        }
    }

    private static class TopRightRowItemSorter extends RowItemSorter {
        private TopRightRowItemSorter() {
        }

        @Override // com.voxel.simplesearchlauncher.fragment.FolderFragment.RowItemSorter
        float distanceSquaredToTarget(float f, float f2, float f3, float f4, int i, int i2, float f5, float f6) {
            return FolderFragment.distanceSquared(f, f2, f3 + (i * f5), f4);
        }

        @Override // com.voxel.simplesearchlauncher.fragment.FolderFragment.RowItemSorter
        List<TransitionItem> sort(List<TransitionItem> list, int i, int i2) {
            ArrayList arrayList = new ArrayList(list.size());
            for (int i3 = 0; i3 < i; i3++) {
                for (int i4 = i2 - 1; i4 >= 0; i4--) {
                    int i5 = (i3 * i2) + i4;
                    if (i5 < list.size()) {
                        arrayList.add(list.get(i5));
                    }
                }
            }
            return arrayList;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    class TransitionItem {
        BubbleTextView mBubbleTextView;
        float mFactor;
        private ValueAnimator.AnimatorUpdateListener mFactorUpdateListener;
        TransitionItemDrawingParams mFromParams;
        float mMaxFactor = 1.0f;
        TransitionItemDrawingParams mToParams;

        TransitionItem(LayoutInflater layoutInflater, ViewGroup viewGroup) {
            BubbleTextView bubbleTextView = (BubbleTextView) layoutInflater.inflate(R.layout.folder_application, viewGroup, false);
            bubbleTextView.setCellParams(FolderFragment.this.mFolder.getContent().getCellParams());
            bubbleTextView.setTextColor(Color.argb(0, 0, 0, 0));
            bubbleTextView.setIgnorePressedStateChange(true);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) bubbleTextView.getLayoutParams();
            CellLayout pageAt = FolderFragment.this.mFolder.getContent().getPageAt(0);
            layoutParams.width = pageAt.getCellWidth();
            layoutParams.height = pageAt.getCellHeight();
            layoutParams.gravity = 51;
            bubbleTextView.setLayoutParams(layoutParams);
            bubbleTextView.setPivotX(0.0f);
            bubbleTextView.setPivotY(0.0f);
            bubbleTextView.setVisibility(4);
            bubbleTextView.setLayerType(2, null);
            this.mBubbleTextView = bubbleTextView;
            this.mFactorUpdateListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$FolderFragment$TransitionItem$KKunX63pXZ1dK2mUrN164srvjSM
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    FolderFragment.TransitionItem.lambda$new$0(TransitionItem.this, FolderFragment.this, valueAnimator); /* REWORK: jadx dropped the captured outer receiver */
                }
            };
        }

        public static /* synthetic */ void lambda$new$0(TransitionItem transitionItem, FolderFragment folderFragment, ValueAnimator valueAnimator) {
            transitionItem.mFactor = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            folderFragment.applyFactorToTransitionItem(transitionItem, folderFragment.mCurrentInterpolator.getInterpolation(transitionItem.mFactor));
        }
    }

    private static class TransitionItemDrawingParams {
        float alpha;
        int baseShadowAlpha;
        int baseShadowColor;
        int baseShadowSmallAlpha;
        int baseShadowSmallColor;
        int baseTextAlpha;
        int baseTextColor;
        int paddingBottom;
        int paddingLeft;
        int paddingRight;
        int paddingTop;
        float scale;
        float transX;
        float transY;

        private TransitionItemDrawingParams() {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void applyFactorToTransitionItem(TransitionItem transitionItem, float f) {
        float f2 = transitionItem.mMaxFactor * f;
        BubbleTextView bubbleTextView = transitionItem.mBubbleTextView;
        TransitionItemDrawingParams transitionItemDrawingParams = transitionItem.mFromParams;
        TransitionItemDrawingParams transitionItemDrawingParams2 = transitionItem.mToParams;
        float f3 = transitionItemDrawingParams2.transX - transitionItemDrawingParams.transX;
        float f4 = transitionItemDrawingParams2.transY - transitionItemDrawingParams.transY;
        float f5 = transitionItemDrawingParams2.scale - transitionItemDrawingParams.scale;
        float f6 = transitionItemDrawingParams2.alpha - transitionItemDrawingParams.alpha;
        int i = transitionItemDrawingParams2.paddingLeft - transitionItemDrawingParams.paddingLeft;
        int i2 = transitionItemDrawingParams2.paddingTop - transitionItemDrawingParams.paddingTop;
        int i3 = transitionItemDrawingParams2.paddingRight - transitionItemDrawingParams.paddingRight;
        int i4 = transitionItemDrawingParams2.paddingBottom - transitionItemDrawingParams.paddingBottom;
        float f7 = transitionItemDrawingParams.transX + (f3 * f2);
        float f8 = transitionItemDrawingParams.transY + (f4 * f2);
        float f9 = transitionItemDrawingParams.scale + (f5 * f2);
        float fLimitAlpha = AlphaUtil.limitAlpha(transitionItemDrawingParams.alpha + (f6 * f2));
        int iRound = Math.round(transitionItemDrawingParams.paddingLeft + (i * f2));
        int iRound2 = Math.round(transitionItemDrawingParams.paddingTop + (i2 * f2));
        int iRound3 = Math.round(transitionItemDrawingParams.paddingRight + (i3 * f2));
        int iRound4 = Math.round(transitionItemDrawingParams.paddingBottom + (i4 * f2));
        int iArgb = Color.argb(Math.round(transitionItemDrawingParams2.baseTextAlpha * fLimitAlpha * f2), Color.red(transitionItemDrawingParams2.baseTextColor), Color.green(transitionItemDrawingParams2.baseTextColor), Color.blue(transitionItemDrawingParams2.baseTextColor));
        int iArgb2 = Color.argb(Math.round(transitionItemDrawingParams2.baseShadowAlpha * fLimitAlpha * f2), Color.red(transitionItemDrawingParams2.baseShadowColor), Color.green(transitionItemDrawingParams2.baseShadowColor), Color.blue(transitionItemDrawingParams2.baseShadowColor));
        int iArgb3 = bubbleTextView.isCustomShadowsEnabled() ? Color.argb(Math.round(f2 * transitionItemDrawingParams2.baseShadowSmallAlpha * fLimitAlpha), Color.red(transitionItemDrawingParams2.baseShadowColor), Color.green(transitionItemDrawingParams2.baseShadowColor), Color.blue(transitionItemDrawingParams2.baseShadowColor)) : 0;
        bubbleTextView.setTranslationX(f7);
        bubbleTextView.setTranslationY(f8);
        bubbleTextView.setScaleX(f9);
        bubbleTextView.setScaleY(f9);
        bubbleTextView.setPadding(iRound, iRound2, iRound3, iRound4);
        bubbleTextView.setTextColor(iArgb);
        bubbleTextView.setDrawableAlpha(fLimitAlpha);
        if (!bubbleTextView.isCustomShadowsEnabled()) {
            bubbleTextView.setShadowLayer(bubbleTextView.getShadowRadius(), bubbleTextView.getShadowDx(), bubbleTextView.getShadowDy(), iArgb2);
        } else {
            bubbleTextView.setCustomShadowLargeColor(iArgb2);
            bubbleTextView.setCustomShadowSmallColor(iArgb3);
        }
    }

    private void createNeededTransitionParams() {
        int childCount = this.mFolder.getContent().getNextPage() == 0 ? 0 : this.mFolder.getContent().getPageAt(0).getShortcutsAndWidgets().getChildCount();
        int size = (this.mFolder.getContent().getItemsForPage(this.mFolder.getContent().getCurrentPage()).size() + childCount) - this.mAllTransitionItems.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                TransitionItem transitionItem = new TransitionItem(getActivity().getLayoutInflater(), this.mContentView);
                transitionItem.mFactor = this.mMainFactor;
                this.mContentView.addView(transitionItem.mBubbleTextView);
                this.mAllTransitionItems.add(transitionItem);
            }
        } else if (size < 0) {
            int iAbs = Math.abs(size);
            for (int i2 = 0; i2 < iAbs; i2++) {
                List<TransitionItem> list = this.mAllTransitionItems;
                list.remove(list.size() - 1);
            }
        }
        this.mPreviewTransitionItems.clear();
        this.mOtherTransitionItems.clear();
        for (int i3 = 0; i3 < childCount; i3++) {
            this.mPreviewTransitionItems.add(this.mAllTransitionItems.get(i3));
        }
        while (childCount < this.mAllTransitionItems.size()) {
            this.mOtherTransitionItems.add(this.mAllTransitionItems.get(childCount));
            childCount++;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static float distanceSquared(float f, float f2, float f3, float f4) {
        float f5 = f - f3;
        float f6 = f2 - f4;
        return (f5 * f5) + (f6 * f6);
    }

    private Drawable getTopDrawable(TextView textView) {
        Drawable drawable = textView.getCompoundDrawables()[1];
        return drawable instanceof PreloadIconDrawable ? ((PreloadIconDrawable) drawable).mIcon : drawable;
    }

    public void lambda$new$0(ValueAnimator valueAnimator) {
        this.mMainFactor = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        float interpolation = this.mCurrentInterpolator.getInterpolation(this.mMainFactor) * this.mMainMaxFactor;
        Drawable drawable = this.mBackgroundWashDrawable;
        if (drawable != null) {
            drawable.setAlpha(AlphaUtil.floatToIntAlpha(0.4f * interpolation));
        }
        Folder folder = this.mFolder;
        if (folder != null) {
            folder.setAlpha(interpolation);
        }
    }

    public boolean lambda$onCreateView$1(View view, MotionEvent motionEvent) {
        Folder folder;
        if (motionEvent.getActionMasked() != 0 || (folder = this.mFolder) == null || !folder.isEditingName() || this.mFolder.getParent() != this.mContentView) {
            return false;
        }
        int[] iArr = {0, 0};
        View editTextRegion = this.mFolder.getEditTextRegion();
        Utilities.getDescendantCoordRelativeToParent(editTextRegion, this.mContentView, iArr, false);
        return !new Rect(iArr[0], iArr[1], iArr[0] + editTextRegion.getMeasuredWidth(), iArr[1] + editTextRegion.getMeasuredHeight()).contains((int) motionEvent.getX(), (int) motionEvent.getY());
    }

    public void lambda$onCreateView$2(View view) {
        Folder folder = this.mFolder;
        if (folder != null && folder.isEditingName()) {
            this.mFolder.dismissEditingName();
        } else if (this.fragmentInteractionListener != null) {
            this.mContentView.setOnClickListener(null);
            this.fragmentInteractionListener.onFolderFragmentRequestClose(this);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void runAnimatorsOnTransitionItems(List<TransitionItem> list, float f, float f2, long j, AnimatorSet animatorSet) {
        if (list.size() == 0) {
            return;
        }
        TransitionItem transitionItem = list.get(0);
        CellLayout pageAt = this.mFolder.getContent().getPageAt(0);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        List<TransitionItem> listSort = RowItemSorter.findBestRowItemSorter(displayMetrics.widthPixels / 2.0f, displayMetrics.heightPixels / 2.0f, transitionItem.mFromParams.transX, transitionItem.mFromParams.transY, pageAt.getCountY(), pageAt.getCountX(), pageAt.getCellWidth() * transitionItem.mFromParams.scale, pageAt.getCellHeight() * transitionItem.mFromParams.scale).sort(list, pageAt.getCountY(), pageAt.getCountX());
        for (int i = 0; i < listSort.size(); i++) {
            TransitionItem transitionItem2 = listSort.get(i);
            ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(f, f2);
            valueAnimatorOfFloat.setInterpolator(LINEAR_INTERPOLATOR);
            valueAnimatorOfFloat.addUpdateListener(transitionItem2.mFactorUpdateListener);
            valueAnimatorOfFloat.setDuration(j);
            valueAnimatorOfFloat.setStartDelay(((long) i) * 0);
            animatorSet.play(valueAnimatorOfFloat);
        }
    }

    private void updateTransitionItem(TransitionItem transitionItem, BubbleTextView bubbleTextView, int i, int i2, boolean z) {
        float f;
        CellLayout currentCellLayout = this.mFolder.getContent().getCurrentCellLayout();
        getTopDrawable(bubbleTextView);
        DeviceProfile deviceProfile = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
        deviceProfile.getCellParamsForType(DeviceProfile.ParamType.WORKSPACE);
        CellParams cellParamsForType = deviceProfile.getCellParamsForType(DeviceProfile.ParamType.FOLDER);
        boolean z2 = i >= 4;
        if (z2) {
            i = 0;
        }
        FolderIcon.PreviewItemDrawingParams previewItemDrawingParamsComputePreviewItemDrawingParams = this.mFolderIcon.computePreviewItemDrawingParams(i, null);
        previewItemDrawingParamsComputePreviewItemDrawingParams.transX += this.mFromXOffset;
        previewItemDrawingParamsComputePreviewItemDrawingParams.transY += this.mFromYOffset;
        previewItemDrawingParamsComputePreviewItemDrawingParams.transX -= ((currentCellLayout.getCellWidth() - cellParamsForType.getIconSizePx()) * previewItemDrawingParamsComputePreviewItemDrawingParams.scale) / 2.0f;
        TransitionItemDrawingParams transitionItemDrawingParams = new TransitionItemDrawingParams();
        transitionItemDrawingParams.transX = previewItemDrawingParamsComputePreviewItemDrawingParams.transX;
        transitionItemDrawingParams.transY = previewItemDrawingParamsComputePreviewItemDrawingParams.transY;
        transitionItemDrawingParams.scale = previewItemDrawingParamsComputePreviewItemDrawingParams.scale;
        if (z) {
            f = 1.5f;
        } else {
            f = z2 ? -0.5f : 1.0f;
        }
        transitionItemDrawingParams.alpha = f;
        transitionItemDrawingParams.paddingLeft = 0;
        transitionItemDrawingParams.paddingTop = 0;
        transitionItemDrawingParams.paddingBottom = 0;
        transitionItemDrawingParams.paddingRight = 0;
        transitionItemDrawingParams.baseTextColor = bubbleTextView.getCurrentTextColor();
        transitionItemDrawingParams.baseTextAlpha = Color.alpha(transitionItemDrawingParams.baseTextColor);
        transitionItemDrawingParams.baseShadowColor = bubbleTextView.isCustomShadowsEnabled() ? bubbleTextView.getCustomShadowLargeColor() : bubbleTextView.getShadowColor();
        transitionItemDrawingParams.baseShadowAlpha = Color.alpha(transitionItemDrawingParams.baseShadowColor);
        transitionItemDrawingParams.baseShadowSmallColor = bubbleTextView.isCustomShadowsEnabled() ? bubbleTextView.getCustomShadowSmallColor() : 0;
        transitionItemDrawingParams.baseShadowSmallAlpha = Color.alpha(transitionItemDrawingParams.baseShadowSmallColor);
        TransitionItemDrawingParams transitionItemDrawingParams2 = new TransitionItemDrawingParams();
        transitionItemDrawingParams2.transX = (currentCellLayout.getCellWidth() * (i2 % currentCellLayout.getCountX())) + this.toXOffset + this.mFolder.getContent().getScrollX();
        transitionItemDrawingParams2.transY = (currentCellLayout.getCellHeight() * (i2 / currentCellLayout.getCountX())) + this.toYOffset;
        transitionItemDrawingParams2.scale = 1.0f;
        transitionItemDrawingParams2.alpha = z ? -0.5f : 1.0f;
        transitionItemDrawingParams2.paddingLeft = bubbleTextView.getPaddingLeft();
        transitionItemDrawingParams2.paddingTop = bubbleTextView.getPaddingTop();
        transitionItemDrawingParams2.paddingBottom = bubbleTextView.getPaddingBottom();
        transitionItemDrawingParams2.paddingRight = bubbleTextView.getPaddingRight();
        transitionItemDrawingParams2.baseTextColor = bubbleTextView.getCurrentTextColor();
        transitionItemDrawingParams2.baseTextAlpha = Color.alpha(transitionItemDrawingParams.baseTextColor);
        transitionItemDrawingParams2.baseShadowColor = bubbleTextView.isCustomShadowsEnabled() ? bubbleTextView.getCustomShadowLargeColor() : bubbleTextView.getShadowColor();
        transitionItemDrawingParams2.baseShadowAlpha = Color.alpha(transitionItemDrawingParams.baseShadowColor);
        transitionItemDrawingParams2.baseShadowSmallColor = bubbleTextView.isCustomShadowsEnabled() ? bubbleTextView.getCustomShadowSmallColor() : 0;
        transitionItemDrawingParams2.baseShadowSmallAlpha = Color.alpha(transitionItemDrawingParams.baseShadowSmallColor);
        transitionItem.mBubbleTextView.applyFromShortcutInfo((ShortcutInfo) bubbleTextView.getTag(), LauncherAppState.getInstance().getIconCache(), false, false);
        transitionItem.mBubbleTextView.setTranslationX(previewItemDrawingParamsComputePreviewItemDrawingParams.transX);
        transitionItem.mBubbleTextView.setTranslationY(previewItemDrawingParamsComputePreviewItemDrawingParams.transY);
        transitionItem.mBubbleTextView.setScaleX(previewItemDrawingParamsComputePreviewItemDrawingParams.scale);
        transitionItem.mBubbleTextView.setScaleY(previewItemDrawingParamsComputePreviewItemDrawingParams.scale);
        transitionItem.mFromParams = transitionItemDrawingParams;
        transitionItem.mToParams = transitionItemDrawingParams2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateTransitionParams() {
        Folder folder = this.mFolder;
        if (folder == null || this.mFolderIcon == null) {
            return;
        }
        int[] iArr = {0, 0};
        ArrayList<View> itemsInReadingOrder = folder.getItemsInReadingOrder();
        List<View> itemsForPage = this.mFolder.getContent().getItemsForPage(this.mFolder.getContent().getCurrentPage());
        if (itemsInReadingOrder.size() > 0) {
            Utilities.getDescendantCoordRelativeToParent(itemsInReadingOrder.get(0), this.mContentView, iArr, false);
            this.toXOffset = iArr[0];
            this.toYOffset = iArr[1];
        }
        createNeededTransitionParams();
        if (this.mAllTransitionItems.size() > 0) {
            int iconSizePx = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getCellParamsForType(DeviceProfile.ParamType.FOLDER).getIconSizePx();
            FolderIcon folderIcon = this.mFolderIcon;
            folderIcon.computePreviewDrawingParams(iconSizePx, folderIcon.getMeasuredWidth());
        }
        int nextPage = this.mFolder.getContent().getNextPage() * this.mFolder.getContent().itemsPerPage();
        for (int i = 0; i < this.mPreviewTransitionItems.size(); i++) {
            updateTransitionItem(this.mAllTransitionItems.get(i), (BubbleTextView) itemsInReadingOrder.get(i), i, i, true);
        }
        for (int i2 = 0; i2 < this.mOtherTransitionItems.size(); i2++) {
            updateTransitionItem(this.mOtherTransitionItems.get(i2), (BubbleTextView) itemsForPage.get(i2), i2 + nextPage, i2, false);
        }
    }

    public Folder getFolder() {
        return this.mFolder;
    }

    public FolderIcon getFolderIcon() {
        return this.mFolderIcon;
    }

    protected final void notifyFragmentExit() {
        Folder folder = this.mFolder;
        if (folder != null) {
            folder.onCloseComplete();
        }
        OnFolderFragmentInteractionListener onFolderFragmentInteractionListener = this.fragmentInteractionListener;
        if (onFolderFragmentInteractionListener != null) {
            onFolderFragmentInteractionListener.onFolderFragmentExit(this);
        }
    }

    protected final void notifyFragmentWillExit() {
        OnFolderFragmentInteractionListener onFolderFragmentInteractionListener = this.fragmentInteractionListener;
        if (onFolderFragmentInteractionListener != null) {
            onFolderFragmentInteractionListener.onFolderFragmentWillExit(this);
        }
    }

    @Override // com.voxel.launcher3.FolderInfo.FolderListener
    public void onAdd(ShortcutInfo shortcutInfo) {
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // androidx.fragment.app.Fragment
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.fragmentInteractionListener = (OnFolderFragmentInteractionListener) activity;
        } catch (ClassCastException unused) {
            throw new ClassCastException(activity.toString() + " must implement " + OnFolderFragmentInteractionListener.class.getCanonicalName());
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.mContentView = (InterceptableFrameLayout) layoutInflater.inflate(R.layout.fragment_folder, viewGroup, false);
        View.OnTouchListener onTouchListener = new View.OnTouchListener() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$FolderFragment$XuMkb5P6jzcuzVe9r_CzJxttaTU
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return FolderFragment.this.lambda$onCreateView$1( view, motionEvent);
            }
        };
        View.OnClickListener onClickListener = new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$FolderFragment$i7gc-USRAa5QC7JW43GMr11g2DU
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                FolderFragment.this.lambda$onCreateView$2( view);
            }
        };
        this.mContentView.setOnInterceptTouchListener(onTouchListener);
        this.mContentView.setOnClickListener(onClickListener);
        this.mCoverClick = this.mContentView.findViewById(R.id.cover_click);
        this.mCoverClick.setOnTouchListener(new View.OnTouchListener() { // from class: com.voxel.simplesearchlauncher.fragment.FolderFragment.2
            boolean mHandleTouch;

            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() == 0) {
                    if (FolderFragment.this.mCurrentAnimators != null && FolderFragment.this.mCurrentAnimators.isRunning()) {
                        this.mHandleTouch = true;
                    }
                } else if (motionEvent.getActionMasked() == 1 || motionEvent.getActionMasked() == 3) {
                    this.mHandleTouch = false;
                }
                return this.mHandleTouch;
            }
        });
        this.mBackgroundWashDrawable = new ColorDrawable(Color.argb(255, 0, 0, 0));
        this.mBackgroundWashDrawable.setAlpha(0);
        this.mContentView.setBackground(this.mBackgroundWashDrawable);
        Folder folder = this.mFolder;
        if (folder == null || folder.getParent() != null || !this.mFolder.getInfo().opened) {
            notifyFragmentExit();
            return this.mContentView;
        }
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2);
        layoutParams.gravity = 17;
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.folder_minheight);
        int contentAreaHeight = this.mFolder.getContentAreaHeight();
        if (contentAreaHeight < dimensionPixelSize) {
            layoutParams.bottomMargin = (dimensionPixelSize - contentAreaHeight) / 3;
        }
        this.mFolder.setLayoutParams(layoutParams);
        if (this.mFolder.getContent().getPageCount() >= 1) {
            this.mFolder.getContent().setCurrentPage(0);
        }
        this.mContentView.addView(this.mFolder);
        createNeededTransitionParams();
        this.mContentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() { // from class: com.voxel.simplesearchlauncher.fragment.FolderFragment.3
            @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
            public void onGlobalLayout() {
                FolderFragment.this.mContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (FolderFragment.this.getActivity() == null || FolderFragment.this.mFolder == null || FolderFragment.this.mFolder.getParent() == null || FolderFragment.this.mFolder.getParent() != FolderFragment.this.mContentView) {
                    return;
                }
                FolderFragment.this.updateTransitionParams();
                Iterator it = FolderFragment.this.mAllTransitionItems.iterator();
                while (it.hasNext()) {
                    FolderFragment.this.applyFactorToTransitionItem((TransitionItem) it.next(), 0.0f);
                }
                FolderFragment.this.mCurrentAnimators = LauncherAnimUtils.createAnimatorSet();
                FolderFragment.this.mCurrentAnimators.addListener(FolderFragment.this.mCurrentAnimatorsListener);
                ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
                valueAnimatorOfFloat.setInterpolator(FolderFragment.LINEAR_INTERPOLATOR);
                valueAnimatorOfFloat.addUpdateListener(FolderFragment.this.mMainFactorUpdateListener);
                valueAnimatorOfFloat.setDuration(450L);
                FolderFragment.this.mCurrentAnimators.play(valueAnimatorOfFloat);
                FolderFragment folderFragment = FolderFragment.this;
                folderFragment.runAnimatorsOnTransitionItems(folderFragment.mPreviewTransitionItems, 0.0f, 1.0f, 450L, FolderFragment.this.mCurrentAnimators);
                FolderFragment folderFragment2 = FolderFragment.this;
                folderFragment2.runAnimatorsOnTransitionItems(folderFragment2.mOtherTransitionItems, 0.0f, 1.0f, 450L, FolderFragment.this.mCurrentAnimators);
                FolderFragment.this.mCurrentAnimators.start();
                if (FolderFragment.this.mListener != null) {
                    FolderFragment.this.mListener.onAnimationStarted();
                }
            }
        });
        this.mCoverClick.bringToFront();
        return this.mContentView;
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroy() {
        super.onDestroy();
    }

    @Override // com.voxel.launcher3.FolderInfo.FolderListener
    public void onItemsChanged() {
        updateTransitionParams();
    }

    @Override // com.voxel.launcher3.FolderInfo.FolderListener
    public void onRemove(ShortcutInfo shortcutInfo) {
        OnFolderFragmentInteractionListener onFolderFragmentInteractionListener = this.fragmentInteractionListener;
        if (onFolderFragmentInteractionListener == null || onFolderFragmentInteractionListener.isDragging() || this.mFolder.getItemCount() > 1) {
            return;
        }
        runExitAnimation();
    }

    @Override // androidx.fragment.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    @Override // com.voxel.launcher3.FolderInfo.FolderListener
    public void onTitleChanged(CharSequence charSequence) {
    }

    public void runExitAnimation() {
        synchronized (this) {
            if (this.mIsExiting) {
                return;
            }
            this.mIsExiting = true;
            notifyFragmentWillExit();
            updateTransitionParams();
            this.mCoverClick.bringToFront();
            AnimatorSet animatorSet = this.mCurrentAnimators;
            if (animatorSet != null && animatorSet.isRunning()) {
                this.mCurrentAnimators.cancel();
            }
            this.mCurrentInterpolator = REVERSE_INTERPOLATOR;
            if (this.mMainFactor == 0.0f) {
                notifyFragmentExit();
                return;
            }
            this.mCurrentAnimators = LauncherAnimUtils.createAnimatorSet();
            this.mCurrentAnimators.addListener(this.mCurrentAnimatorsListener);
            this.mCurrentAnimators.addListener(new Animator.AnimatorListener() { // from class: com.voxel.simplesearchlauncher.fragment.FolderFragment.4
                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animator) {
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    FolderFragment.this.notifyFragmentExit();
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationRepeat(Animator animator) {
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                }
            });
            this.mMainMaxFactor = this.mMainFactor;
            ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
            valueAnimatorOfFloat.setInterpolator(LINEAR_INTERPOLATOR);
            valueAnimatorOfFloat.addUpdateListener(this.mMainFactorUpdateListener);
            valueAnimatorOfFloat.setDuration(Math.max(MIN_DURATION_MS, Math.round(this.mMainFactor * 450.0f)));
            this.mCurrentAnimators.play(valueAnimatorOfFloat);
            if (this.mAllTransitionItems.size() > 0) {
                runAnimatorsOnTransitionItems(this.mPreviewTransitionItems, 1.0f, 0.0f, valueAnimatorOfFloat.getDuration(), this.mCurrentAnimators);
                runAnimatorsOnTransitionItems(this.mOtherTransitionItems, 1.0f, 0.0f, valueAnimatorOfFloat.getDuration(), this.mCurrentAnimators);
            }
            this.mCurrentAnimators.start();
        }
    }

    public void setFolder(Folder folder) {
        InterceptableFrameLayout interceptableFrameLayout;
        Folder folder2 = this.mFolder;
        if (folder2 != null) {
            folder2.getInfo().removeListener(this);
        }
        Folder folder3 = this.mFolder;
        if (folder3 != null && this.mContentView != null) {
            ViewParent parent = folder3.getParent();
            InterceptableFrameLayout interceptableFrameLayout2 = this.mContentView;
            if (parent == interceptableFrameLayout2) {
                interceptableFrameLayout2.removeView(this.mFolder);
            }
        }
        this.mFolder = folder;
        Folder folder4 = this.mFolder;
        if (folder4 != null && folder4.getParent() == null && (interceptableFrameLayout = this.mContentView) != null) {
            interceptableFrameLayout.addView(this.mFolder);
        }
        Folder folder5 = this.mFolder;
        if (folder5 != null) {
            folder5.getInfo().addListener(this);
        }
    }

    public void setFolderIcon(FolderIcon folderIcon) {
        this.mFolderIcon = folderIcon;
        FolderIcon folderIcon2 = this.mFolderIcon;
        if (folderIcon2 == null) {
            this.mFromXOffset = 0;
            this.mFromYOffset = 0;
        } else {
            int[] iArr = {0, 0};
            Utilities.getDescendantCoordRelativeToParent(folderIcon2, (View) folderIcon2.getParent().getParent().getParent().getParent(), iArr, false);
            this.mFromXOffset = iArr[0] + this.mFolderIcon.getPreviewOffsetX();
            this.mFromYOffset = iArr[1] + this.mFolderIcon.getPreviewOffsetY();
        }
    }

    public void setListener(FolderFragmentListener folderFragmentListener) {
        this.mListener = folderFragmentListener;
    }

    public void setVisible(boolean z) {
        if (getView() != null) {
            float f = z ? 1.0f : 0.0f;
            View view = getView();
            view.animate().cancel();
            view.animate().alpha(f);
        }
    }
}
