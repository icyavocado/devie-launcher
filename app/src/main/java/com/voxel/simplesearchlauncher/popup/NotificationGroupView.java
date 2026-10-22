package com.voxel.simplesearchlauncher.popup;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import butterknife.ButterKnife;
import com.voxel.launcher3.anim.PillHeightRevealOutlineProvider;
import com.voxel.launcher3.graphics.IconPalette;
import com.voxel.simplesearchlauncher.notification.NotificationContent;
import com.ovacoda.devie.R;
import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
public class NotificationGroupView extends PopupGroupBaseView {
    private static final Rect sTempRect = new Rect();
    private boolean mAnimatingNextIcon;

    NotificationFooterView mFooter;

    TextView mHeaderCount;

    View mIconView;

    NotificationItemView mItemView;
    private int mNotificationHeaderTextColor;
    private SwipeHelper mSwipeHelper;

    public NotificationGroupView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NotificationGroupView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mNotificationHeaderTextColor = 0;
    }

    public void lambda$trimNotifications$0(NotificationContent notificationContent) {
        if (notificationContent != null) {
            this.mItemView.applyNotificationInfo(notificationContent, this.mIconView, true);
            this.mItemView.setVisibility(0);
        }
        this.mAnimatingNextIcon = false;
    }

    public Animator animateHeightRemoval(int i) {
        return new PillHeightRevealOutlineProvider(this.mPillRect, getBackgroundRadius(), getHeight() - i).createRevealAnimator(this, true);
    }

    public void applyNotificationContents(List<NotificationContent> list, int i) {
        if (list.isEmpty()) {
            return;
        }
        this.mItemView.applyNotificationInfo(list.get(0), this.mIconView);
        for (int i2 = 1; i2 < list.size(); i2++) {
            this.mFooter.addNotificationContent(list.get(i2));
        }
        this.mFooter.commitNotificationContents();
        updateHeader(i, null);
    }

    @Override // com.voxel.simplesearchlauncher.popup.PopupGroupBaseView
    public Animator createCloseAnimation(boolean z, boolean z2, long j) {
        return super.createZoomCloseAnimation(this.mIconView, z, z2, j);
    }

    @Override // com.voxel.simplesearchlauncher.popup.PopupGroupBaseView
    public Animator createOpenAnimation(boolean z, boolean z2) {
        return super.createZoomOpenAnimation(this.mIconView, z, z2);
    }

    @Override // com.voxel.simplesearchlauncher.popup.PopupGroupBaseView
    public int getArrowColor(boolean z) {
        return ContextCompat.getColor(getContext(), z ? R.color.popup_background_color : R.color.popup_header_background_color);
    }

    public int getHeightMinusFooter() {
        return getHeight() - (this.mFooter.getParent() == null ? 0 : this.mFooter.getHeight());
    }

    public NotificationItemView getMainItemView() {
        return this.mItemView;
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        this.mSwipeHelper = new SwipeHelper(0, this.mItemView.getSwipeHelperCallback(), getContext());
        this.mSwipeHelper.setDisableHardwareLayers(true);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (this.mItemView.getNotificationContent() == null) {
            return false;
        }
        getParent().requestDisallowInterceptTouchEvent(true);
        return this.mSwipeHelper.onInterceptTouchEvent(motionEvent);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mItemView.getNotificationContent() == null) {
            return false;
        }
        return this.mSwipeHelper.onTouchEvent(motionEvent) || super.onTouchEvent(motionEvent);
    }

    public void setFooterVisibility(int i) {
        this.mFooter.setVisibility(i);
    }

    public synchronized void trimNotifications(PopupContainerWithArrow popupContainerWithArrow, List<String> list, int i) {
        if (!(!list.contains(this.mItemView.getNotificationContent().notificationKey)) || this.mAnimatingNextIcon) {
            this.mFooter.trimNotifications(popupContainerWithArrow, list);
        } else {
            this.mAnimatingNextIcon = true;
            this.mItemView.setVisibility(4);
            this.mItemView.setTranslationX(0.0f);
            this.mIconView.getGlobalVisibleRect(sTempRect);
            updateHeader(i, null);
            this.mFooter.animateFirstNotificationTo(popupContainerWithArrow, list, sTempRect, new NotificationFooterView.IconAnimationEndListener() { // from class: com.voxel.simplesearchlauncher.popup.-$$Lambda$NotificationGroupView$YhAr-bPktD9_TR3ogcWzdkMFEfA
                @Override // com.voxel.simplesearchlauncher.popup.NotificationFooterView.IconAnimationEndListener
                public final void onIconAnimationEnd(NotificationContent notificationContent) {
                    NotificationGroupView.this.lambda$trimNotifications$0( notificationContent);
                }
            });
        }
    }

    public void updateHeader(int i, IconPalette iconPalette) {
        this.mHeaderCount.setText(i <= 1 ? "" : String.valueOf(i));
        if (iconPalette != null) {
            if (this.mNotificationHeaderTextColor == 0) {
                this.mNotificationHeaderTextColor = IconPalette.resolveContrastColor(getContext(), iconPalette.dominantColor, getResources().getColor(R.color.popup_header_background_color));
            }
            this.mHeaderCount.setTextColor(this.mNotificationHeaderTextColor);
        }
    }
}
