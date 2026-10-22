package com.voxel.launcher3;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import com.voxel.launcher3.dragndrop.DragLayer;
import com.voxel.launcher3.dragndrop.DragOptions;
import com.voxel.launcher3.dragndrop.DragView;
import com.ovacoda.devie.R;
import timber.log.Timber;

/* JADX INFO: loaded from: classes.dex */
public class RemoveDropTarget extends ButtonDropTarget {
    private static int DELETE_ANIMATION_DURATION = 285;
    private static int FLING_DELETE_ANIMATION_DURATION = 350;
    private static float FLING_TO_DELETE_FRICTION = 0.035f;
    private static int MODE_FLING_DELETE_ALONG_VECTOR = 1;
    private static int MODE_FLING_DELETE_TO_TRASH;
    private TransitionDrawable mCurrentDrawable;
    private final int mFlingDeleteMode;
    private ColorStateList mOriginalTextColor;
    private TransitionDrawable mRemoveDrawable;

    private static class FlingAlongVectorAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        private final TimeInterpolator mAlphaInterpolator = new DecelerateInterpolator(0.75f);
        private DragLayer mDragLayer;
        private float mFriction;
        private Rect mFrom;
        private boolean mHasOffsetForScale;
        private long mPrevTime;
        private PointF mVelocity;

        public FlingAlongVectorAnimatorUpdateListener(DragLayer dragLayer, PointF pointF, Rect rect, long j, float f) {
            this.mDragLayer = dragLayer;
            this.mVelocity = pointF;
            this.mFrom = rect;
            this.mPrevTime = j;
            this.mFriction = 1.0f - (dragLayer.getResources().getDisplayMetrics().density * f);
        }

        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            DragView dragView = (DragView) this.mDragLayer.getAnimatedView();
            float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            long jCurrentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis();
            if (!this.mHasOffsetForScale) {
                this.mHasOffsetForScale = true;
                float scaleX = dragView.getScaleX() - 1.0f;
                float measuredWidth = (dragView.getMeasuredWidth() * scaleX) / 2.0f;
                float measuredHeight = (scaleX * dragView.getMeasuredHeight()) / 2.0f;
                Rect rect = this.mFrom;
                rect.left = (int) (rect.left + measuredWidth);
                Rect rect2 = this.mFrom;
                rect2.top = (int) (rect2.top + measuredHeight);
            }
            Rect rect3 = this.mFrom;
            rect3.left = (int) (rect3.left + ((this.mVelocity.x * (jCurrentAnimationTimeMillis - this.mPrevTime)) / 1000.0f));
            Rect rect4 = this.mFrom;
            rect4.top = (int) (rect4.top + ((this.mVelocity.y * (jCurrentAnimationTimeMillis - this.mPrevTime)) / 1000.0f));
            dragView.setTranslationX(this.mFrom.left);
            dragView.setTranslationY(this.mFrom.top);
            dragView.setAlpha(1.0f - this.mAlphaInterpolator.getInterpolation(fFloatValue));
            this.mVelocity.x *= this.mFriction;
            this.mVelocity.y *= this.mFriction;
            this.mPrevTime = jCurrentAnimationTimeMillis;
        }
    }

    public RemoveDropTarget(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public RemoveDropTarget(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mFlingDeleteMode = MODE_FLING_DELETE_ALONG_VECTOR;
    }

    private void animateToTrashAndCompleteDrop(final DropTarget.DragObject dragObject) {
        DragLayer dragLayer = this.mLauncher.getDragLayer();
        Rect rect = new Rect();
        dragLayer.getViewRectRelativeToSelf(dragObject.dragView, rect);
        TransitionDrawable transitionDrawable = this.mCurrentDrawable;
        int intrinsicWidth = transitionDrawable == null ? 0 : transitionDrawable.getIntrinsicWidth();
        TransitionDrawable transitionDrawable2 = this.mCurrentDrawable;
        Rect iconRect = getIconRect(dragObject.dragView.getMeasuredWidth(), dragObject.dragView.getMeasuredHeight(), intrinsicWidth, transitionDrawable2 != null ? transitionDrawable2.getIntrinsicHeight() : 0);
        float fWidth = iconRect.width() / rect.width();
        this.mSearchDropTargetBar.deferOnDragEnd();
        dragLayer.animateView(dragObject.dragView, rect, iconRect, fWidth, 1.0f, 1.0f, 0.1f, 0.1f, DELETE_ANIMATION_DURATION, new DecelerateInterpolator(2.0f), new LinearInterpolator(), new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$RemoveDropTarget$lw1zz3g1qcX0F699k7-6dk8tc6g
            @Override // java.lang.Runnable
            public final void run() {
                RemoveDropTarget.this.lambda$animateToTrashAndCompleteDrop$0( dragObject);
            }
        }, 0, null);
    }

    /* JADX WARN: Type inference failed for: r0v2, types: [com.voxel.launcher3.RemoveDropTarget$1] */
    private void completeDrop(DropTarget.DragObject dragObject) {
        ItemInfo itemInfo = dragObject.dragInfo;
        if (isWorkspaceOrFolderApplication(dragObject)) {
            LauncherModel.deleteItemFromDatabase(this.mLauncher, itemInfo);
            return;
        }
        if (isWorkspaceFolder(dragObject)) {
            FolderInfo folderInfo = (FolderInfo) itemInfo;
            this.mLauncher.removeFolder(folderInfo);
            LauncherModel.deleteFolderContentsFromDatabase(this.mLauncher, folderInfo);
        } else if (isWorkspaceOrFolderWidget(dragObject)) {
            final LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) itemInfo;
            this.mLauncher.removeAppWidget(launcherAppWidgetInfo);
            LauncherModel.deleteItemFromDatabase(this.mLauncher, itemInfo);
            final LauncherAppWidgetHost appWidgetHost = this.mLauncher.getAppWidgetHost();
            if (appWidgetHost == null || !launcherAppWidgetInfo.isWidgetIdValid()) {
                return;
            }
            new AsyncTask<Void, Void, Void>() { // from class: com.voxel.launcher3.RemoveDropTarget.1
                @Override // android.os.AsyncTask
                public Void doInBackground(Void... voidArr) {
                    Timber.d("================= Widget deleted " + launcherAppWidgetInfo.appWidgetId, new Object[0]);
                    appWidgetHost.deleteAppWidgetId(launcherAppWidgetInfo.appWidgetId);
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        }
    }

    private ValueAnimator.AnimatorUpdateListener createFlingAlongVectorAnimatorListener(DragLayer dragLayer, DropTarget.DragObject dragObject, PointF pointF, long j, int i, ViewConfiguration viewConfiguration) {
        Rect rect = new Rect();
        dragLayer.getViewRectRelativeToSelf(dragObject.dragView, rect);
        return new FlingAlongVectorAnimatorUpdateListener(dragLayer, pointF, rect, j, FLING_TO_DELETE_FRICTION);
    }

    private ValueAnimator.AnimatorUpdateListener createFlingToTrashAnimatorListener(final DragLayer dragLayer, DropTarget.DragObject dragObject, PointF pointF, ViewConfiguration viewConfiguration) {
        TransitionDrawable transitionDrawable = this.mCurrentDrawable;
        int intrinsicWidth = transitionDrawable == null ? 0 : transitionDrawable.getIntrinsicWidth();
        TransitionDrawable transitionDrawable2 = this.mCurrentDrawable;
        Rect iconRect = getIconRect(dragObject.dragView.getMeasuredWidth(), dragObject.dragView.getMeasuredHeight(), intrinsicWidth, transitionDrawable2 != null ? transitionDrawable2.getIntrinsicHeight() : 0);
        Rect rect = new Rect();
        dragLayer.getViewRectRelativeToSelf(dragObject.dragView, rect);
        final float scaleX = dragObject.dragView.getScaleX();
        int iMin = (int) ((-rect.top) * Math.min(1.0f, Math.abs(pointF.length()) / (viewConfiguration.getScaledMaximumFlingVelocity() / 2.0f)));
        int i = (int) (iMin / (pointF.y / pointF.x));
        final float f = rect.top + iMin;
        final float f2 = rect.left + i;
        final float f3 = rect.left;
        final float f4 = rect.top;
        final float f5 = iconRect.left;
        final float f6 = iconRect.top;
        /* REWORK: lambda-holder class used as a type; use its functional interface */
        final TimeInterpolator __lambda_removedroptarget_5gdyfcdvbcbkdlgdb_scnvymcrq = new TimeInterpolator() { // from class: com.voxel.launcher3.-$$Lambda$RemoveDropTarget$5gDyFCDVbcBKDLgDb_sCnVYmCrQ
            @Override // android.animation.TimeInterpolator
            public final float getInterpolation(float f7) {
                return RemoveDropTarget.lambda$createFlingToTrashAnimatorListener$1(f7);
            }
        };
        return new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.launcher3.-$$Lambda$RemoveDropTarget$_brESJ0et1_lDIdT4lVj_uJOxTY
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                RemoveDropTarget.lambda$createFlingToTrashAnimatorListener$2(dragLayer, __lambda_removedroptarget_5gdyfcdvbcbkdlgdb_scnvymcrq, f3, f2, f5, f4, f, f6, scaleX, valueAnimator);
            }
        };
    }

    private boolean isDragSourceWorkspaceOrFolder(DropTarget.DragObject dragObject) {
        return (dragObject.dragSource instanceof Workspace) || (dragObject.dragSource instanceof Folder);
    }

    private boolean isWorkspaceFolder(DropTarget.DragObject dragObject) {
        return (dragObject.dragSource instanceof Workspace) && (dragObject.dragInfo instanceof FolderInfo);
    }

    private boolean isWorkspaceOrFolderApplication(DropTarget.DragObject dragObject) {
        return isDragSourceWorkspaceOrFolder(dragObject) && (dragObject.dragInfo instanceof ShortcutInfo);
    }

    private boolean isWorkspaceOrFolderWidget(DropTarget.DragObject dragObject) {
        return isDragSourceWorkspaceOrFolder(dragObject) && (dragObject.dragInfo instanceof LauncherAppWidgetInfo);
    }

    public void lambda$animateToTrashAndCompleteDrop$0(DropTarget.DragObject dragObject) {
        this.completeDrop(dragObject);
        this.mSearchDropTargetBar.onDragEnd();
        this.mLauncher.exitSpringLoadedDragMode();
    }

    static /* synthetic */ float lambda$createFlingToTrashAnimatorListener$1(float f) {
        return f * f * f * f * f * f * f * f;
    }

    static /* synthetic */ void lambda$createFlingToTrashAnimatorListener$2(DragLayer dragLayer, TimeInterpolator timeInterpolator, float f, float f2, float f3, float f4, float f5, float f6, float f7, ValueAnimator valueAnimator) {
        DragView dragView = (DragView) dragLayer.getAnimatedView();
        float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        float interpolation = timeInterpolator.getInterpolation(fFloatValue);
        float scaleX = 1.0f - dragView.getScaleX();
        float measuredWidth = (dragView.getMeasuredWidth() * scaleX) / 2.0f;
        float measuredHeight = (scaleX * dragView.getMeasuredHeight()) / 2.0f;
        float f8 = 1.0f - fFloatValue;
        float f9 = f8 * f8;
        float f10 = f8 * 2.0f * fFloatValue;
        float f11 = fFloatValue * fFloatValue;
        dragView.setTranslationX(((f - measuredWidth) * f9) + ((f2 - measuredWidth) * f10) + (f3 * f11));
        dragView.setTranslationY((f9 * (f4 - measuredHeight)) + (f10 * (f5 - measuredWidth)) + (f11 * f6));
        float f12 = 1.0f - interpolation;
        float f13 = f7 * f12;
        dragView.setScaleX(f13);
        dragView.setScaleY(f13);
        dragView.setAlpha((f12 * 0.5f) + 0.5f);
    }

    public void lambda$onFlingToDelete$3(DropTarget.DragObject dragObject) {
        this.mLauncher.exitSpringLoadedDragMode();
        this.completeDrop(dragObject);
        this.mSearchDropTargetBar.onDragEnd();
        this.mLauncher.getDragController().onDeferredEndFling(dragObject);
    }

    public static boolean willAcceptDrop(Object obj) {
        if (obj instanceof ItemInfo) {
            ItemInfo itemInfo = (ItemInfo) obj;
            if (itemInfo.itemType == 4 || itemInfo.itemType == 1 || itemInfo.itemType == 6) {
                return true;
            }
            if (!LauncherAppState.isDisableAllApps() && itemInfo.itemType == 2) {
                return true;
            }
            if (!LauncherAppState.isDisableAllApps() && itemInfo.itemType == 0 && (itemInfo instanceof AppInfo)) {
                return (((AppInfo) obj).flags & 1) != 0;
            }
            if (itemInfo.itemType == 0 && (itemInfo instanceof ShortcutInfo)) {
                return (LauncherAppState.isDisableAllApps() && (((ShortcutInfo) obj).flags & 1) == 0) ? false : true;
            }
        }
        return false;
    }

    @Override // com.voxel.launcher3.ButtonDropTarget, com.voxel.launcher3.DropTarget
    public boolean acceptDrop(DropTarget.DragObject dragObject) {
        if (dragObject.isDesktopLocked) {
            return false;
        }
        return willAcceptDrop(dragObject.dragInfo);
    }

    @Override // com.voxel.launcher3.ButtonDropTarget, com.voxel.launcher3.dragndrop.DragController.DragListener
    public void onDragEnd() {
        super.onDragEnd();
        this.mActive = false;
    }

    @Override // com.voxel.launcher3.ButtonDropTarget, com.voxel.launcher3.DropTarget
    public void onDragEnter(DropTarget.DragObject dragObject) {
        if (dragObject.isDesktopLocked) {
            return;
        }
        super.onDragEnter(dragObject);
        setHoverColor();
    }

    @Override // com.voxel.launcher3.ButtonDropTarget, com.voxel.launcher3.DropTarget
    public void onDragExit(DropTarget.DragObject dragObject) {
        if (dragObject.isDesktopLocked) {
            return;
        }
        super.onDragExit(dragObject);
        if (dragObject.dragComplete) {
            dragObject.dragView.setColor(this.mHoverColor);
        } else {
            resetHoverColor();
        }
    }

    @Override // com.voxel.launcher3.ButtonDropTarget, com.voxel.launcher3.dragndrop.DragController.DragListener
    public void onDragStart(DropTarget.DragObject dragObject, DragOptions dragOptions) {
        this.mCurrentDrawable = (TransitionDrawable) getCurrentDrawable();
        this.mActive = true;
        resetHoverColor();
        ((ViewGroup) getParent()).setVisibility(0);
        if ((dragObject.dragSource instanceof Workspace) || (dragObject.dragSource instanceof Folder)) {
            setText(R.string.delete_target_label);
        } else {
            setText(R.string.delete_target_cancel_label);
        }
    }

    @Override // com.voxel.launcher3.ButtonDropTarget, com.voxel.launcher3.DropTarget
    public void onDrop(DropTarget.DragObject dragObject) {
        animateToTrashAndCompleteDrop(dragObject);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mOriginalTextColor = getTextColors();
        Resources resources = getResources();
        this.mHoverColor = resources.getColor(R.color.delete_target_hover_tint);
        this.mRemoveDrawable = (TransitionDrawable) resources.getDrawable(R.drawable.remove_target_selector);
        this.mRemoveDrawable.setCrossFadeEnabled(true);
        this.mCurrentDrawable = (TransitionDrawable) getCurrentDrawable();
        if (getResources().getConfiguration().orientation != 2 || LauncherAppState.getInstance().isScreenLarge()) {
            return;
        }
        setText("");
    }

    @Override // com.voxel.launcher3.ButtonDropTarget, com.voxel.launcher3.DropTarget
    public void onFlingToDelete(final DropTarget.DragObject dragObject, PointF pointF) {
        ValueAnimator.AnimatorUpdateListener animatorUpdateListenerCreateFlingAlongVectorAnimatorListener;
        dragObject.dragView.setColor(0);
        if (this.mFlingDeleteMode == MODE_FLING_DELETE_TO_TRASH) {
            this.mSearchDropTargetBar.deferOnDragEnd();
        }
        ViewConfiguration viewConfiguration = ViewConfiguration.get(this.mLauncher);
        DragLayer dragLayer = this.mLauncher.getDragLayer();
        final int i = FLING_DELETE_ANIMATION_DURATION;
        final long jCurrentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis();
        TimeInterpolator timeInterpolator = new TimeInterpolator() { // from class: com.voxel.launcher3.RemoveDropTarget.2
            private int mCount = -1;
            private float mOffset = 0.0f;

            @Override // android.animation.TimeInterpolator
            public float getInterpolation(float f) {
                int i2 = this.mCount;
                if (i2 < 0) {
                    this.mCount = i2 + 1;
                } else if (i2 == 0) {
                    this.mOffset = Math.min(0.5f, (AnimationUtils.currentAnimationTimeMillis() - jCurrentAnimationTimeMillis) / i);
                    this.mCount++;
                }
                return Math.min(1.0f, this.mOffset + f);
            }
        };
        int i2 = this.mFlingDeleteMode;
        if (i2 == MODE_FLING_DELETE_TO_TRASH) {
            animatorUpdateListenerCreateFlingAlongVectorAnimatorListener = createFlingToTrashAnimatorListener(dragLayer, dragObject, pointF, viewConfiguration);
        } else {
            animatorUpdateListenerCreateFlingAlongVectorAnimatorListener = i2 == MODE_FLING_DELETE_ALONG_VECTOR ? createFlingAlongVectorAnimatorListener(dragLayer, dragObject, pointF, jCurrentAnimationTimeMillis, i, viewConfiguration) : null;
        }
        dragLayer.animateView(dragObject.dragView, animatorUpdateListenerCreateFlingAlongVectorAnimatorListener, i, timeInterpolator, new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$RemoveDropTarget$XblgOfvxNl34ZCj5WB90i_Fl7pE
            @Override // java.lang.Runnable
            public final void run() {
                RemoveDropTarget.this.lambda$onFlingToDelete$3( dragObject);
            }
        }, 0, null);
    }

    void resetHoverColor() {
        TransitionDrawable transitionDrawable = this.mCurrentDrawable;
        if (transitionDrawable != null) {
            transitionDrawable.resetTransition();
        }
        setTextColor(this.mOriginalTextColor);
    }

    void setHoverColor() {
        TransitionDrawable transitionDrawable = this.mCurrentDrawable;
        if (transitionDrawable != null) {
            transitionDrawable.startTransition(this.mTransitionDuration);
        }
        setTextColor(this.mHoverColor);
    }
}
