package com.voxel.launcher3;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.StackView;
import com.voxel.launcher3.dragndrop.DragLayer;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class LauncherAppWidgetHostView extends AppWidgetHostView implements DragLayer.TouchCompleteListener {
    private Context mContext;
    private DragLayer mDragLayer;
    LayoutInflater mInflater;
    private CheckLongPressHelper mLongPressHelper;
    private int mPreviousOrientation;
    private float mSlop;

    public LauncherAppWidgetHostView(Context context) {
        super(context);
        this.mContext = context;
        this.mLongPressHelper = new CheckLongPressHelper(this);
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.mDragLayer = ((Launcher) context).getDragLayer();
    }

    private boolean checkForAdapterView(View view) {
        if ((view instanceof AdapterView) && ((view instanceof StackView) || view.canScrollVertically(1) || view.canScrollVertically(-1) || view.canScrollHorizontally(-1) || view.canScrollHorizontally(1))) {
            return true;
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (checkForAdapterView(viewGroup.getChildAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override // android.view.View
    public void cancelLongPress() {
        super.cancelLongPress();
        this.mLongPressHelper.cancelLongPress();
    }

    boolean checkForAdapterView() {
        return checkForAdapterView(this);
    }

    @Override // android.view.ViewGroup
    public int getDescendantFocusability() {
        return 393216;
    }

    @Override // android.appwidget.AppWidgetHostView
    protected View getErrorView() {
        return this.mInflater.inflate(R.layout.appwidget_error, (ViewGroup) this, false);
    }

    public boolean isReinflateRequired() {
        return this.mPreviousOrientation != this.mContext.getResources().getConfiguration().orientation;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            this.mLongPressHelper.cancelLongPress();
        }
        if (this.mLongPressHelper.hasPerformedLongPress()) {
            this.mLongPressHelper.cancelLongPress();
            return true;
        }
        switch (motionEvent.getAction()) {
            case 0:
                this.mLongPressHelper.postCheckForLongPress();
                this.mDragLayer.setTouchCompleteListener(this);
                ((Launcher) this.mContext).onTouchDownScrollingWidget(this);
                return false;
            case 1:
            case 3:
                this.mLongPressHelper.cancelLongPress();
                return false;
            case 2:
                if (Utilities.pointInView(this, motionEvent.getX(), motionEvent.getY(), this.mSlop)) {
                    return false;
                }
                this.mLongPressHelper.cancelLongPress();
                return false;
            default:
                return false;
        }
    }

    @Override // com.voxel.launcher3.dragndrop.DragLayer.TouchCompleteListener
    public void onTouchComplete() {
        if (this.mLongPressHelper.hasPerformedLongPress()) {
            return;
        }
        this.mLongPressHelper.cancelLongPress();
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case 1:
            case 3:
                this.mLongPressHelper.cancelLongPress();
                break;
            case 2:
                if (!Utilities.pointInView(this, motionEvent.getX(), motionEvent.getY(), this.mSlop)) {
                    this.mLongPressHelper.cancelLongPress();
                }
                break;
        }
        return true;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestDisallowInterceptTouchEvent(boolean z) {
        if (z) {
            cancelLongPress();
        }
        super.requestDisallowInterceptTouchEvent(z);
    }

    @Override // android.appwidget.AppWidgetHostView
    public void updateAppWidget(RemoteViews remoteViews) {
        this.mPreviousOrientation = this.mContext.getResources().getConfiguration().orientation;
        super.updateAppWidget(remoteViews);
    }
}
