package com.voxel.simplesearchlauncher.popup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.voxel.launcher3.LauncherAnimUtils;
import com.voxel.launcher3.Utilities;
import com.voxel.launcher3.anim.PropertyListBuilder;
import com.voxel.launcher3.anim.PropertyResetListener;
import com.voxel.simplesearchlauncher.notification.NotificationContent;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
public class NotificationFooterView extends FrameLayout {
    private static final Rect sTempRect = new Rect();
    private int mBackgroundColor;
    FrameLayout.LayoutParams mIconLayoutParams;
    private LinearLayout mIconRow;
    private final List<NotificationContent> mNotifications;
    private View mOverflowEllipsis;
    private final List<NotificationContent> mOverflowNotifications;
    private final boolean mRtl;

    public interface IconAnimationEndListener {
        void onIconAnimationEnd(NotificationContent notificationContent);
    }

    public NotificationFooterView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NotificationFooterView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mNotifications = new ArrayList();
        this.mOverflowNotifications = new ArrayList();
        Resources resources = getResources();
        this.mRtl = Utilities.isRtl(resources);
        int dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.notification_footer_icon_size);
        this.mIconLayoutParams = new FrameLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize);
        this.mIconLayoutParams.gravity = 16;
        int dimensionPixelSize2 = resources.getDimensionPixelSize(R.dimen.notification_footer_icon_row_padding);
        this.mIconLayoutParams.setMarginStart((((resources.getDimensionPixelSize(R.dimen.bg_popup_item_width) - dimensionPixelSize2) - (resources.getDimensionPixelSize(R.dimen.horizontal_ellipsis_offset) + resources.getDimensionPixelSize(R.dimen.horizontal_ellipsis_size))) - (dimensionPixelSize * 5)) / 5);
    }

    private View addNotificationIconForContent(NotificationContent notificationContent) {
        View view = new View(getContext());
        view.setBackground(notificationContent.getIconForBackground(getContext(), this.mBackgroundColor));
        view.setTag(notificationContent);
        view.setImportantForAccessibility(2);
        this.mIconRow.addView(view, 0, this.mIconLayoutParams);
        return view;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeViewFromIconRow(PopupContainerWithArrow popupContainerWithArrow, View view) {
        this.mIconRow.removeView(view);
        this.mNotifications.remove(view.getTag());
        updateOverflowEllipsisVisibility();
        if (this.mIconRow.getChildCount() != 0 || popupContainerWithArrow == null) {
            return;
        }
        Animator animatorReduceNotificationViewHeight = popupContainerWithArrow.reduceNotificationViewHeight(getHeight(), getResources().getInteger(R.integer.config_removeNotificationViewDuration));
        animatorReduceNotificationViewHeight.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.simplesearchlauncher.popup.NotificationFooterView.2
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                ((ViewGroup) NotificationFooterView.this.getParent()).removeView(NotificationFooterView.this);
            }
        });
        animatorReduceNotificationViewHeight.start();
    }

    private void updateOverflowEllipsisVisibility() {
        this.mOverflowEllipsis.setVisibility(this.mOverflowNotifications.isEmpty() ? 8 : 0);
    }

    public void addNotificationContent(NotificationContent notificationContent) {
        if (this.mNotifications.size() < 5) {
            this.mNotifications.add(notificationContent);
        } else {
            this.mOverflowNotifications.add(notificationContent);
        }
    }

    public void animateFirstNotificationTo(final PopupContainerWithArrow popupContainerWithArrow, List<String> list, Rect rect, final IconAnimationEndListener iconAnimationEndListener) {
        AnimatorSet animatorSetCreateAnimatorSet = LauncherAnimUtils.createAnimatorSet();
        LinearLayout linearLayout = this.mIconRow;
        final View childAt = linearLayout.getChildAt(linearLayout.getChildCount() - 1);
        if (childAt == null) {
            trimNotifications(popupContainerWithArrow, list);
            return;
        }
        Rect rect2 = sTempRect;
        childAt.getGlobalVisibleRect(rect2);
        float fHeight = rect.height() / rect2.height();
        ObjectAnimator objectAnimatorOfPropertyValuesHolder = LauncherAnimUtils.ofPropertyValuesHolder(childAt, new PropertyListBuilder().scale(fHeight).translationY((rect.top - rect2.top) + (((rect2.height() * fHeight) - rect2.height()) / 2.0f)).build());
        objectAnimatorOfPropertyValuesHolder.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.simplesearchlauncher.popup.NotificationFooterView.1
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                iconAnimationEndListener.onIconAnimationEnd((NotificationContent) childAt.getTag());
                NotificationFooterView.this.removeViewFromIconRow(popupContainerWithArrow, childAt);
            }
        });
        animatorSetCreateAnimatorSet.play(objectAnimatorOfPropertyValuesHolder);
        int marginStart = this.mIconLayoutParams.width + this.mIconLayoutParams.getMarginStart();
        if (this.mRtl) {
            marginStart = -marginStart;
        }
        if (!this.mOverflowNotifications.isEmpty()) {
            NotificationContent notificationContentRemove = this.mOverflowNotifications.remove(0);
            this.mNotifications.add(notificationContentRemove);
            animatorSetCreateAnimatorSet.play(ObjectAnimator.ofFloat(addNotificationIconForContent(notificationContentRemove), (Property<View, Float>) ALPHA, 0.0f, 1.0f));
        }
        int childCount = this.mIconRow.getChildCount() - 1;
        PropertyResetListener propertyResetListener = new PropertyResetListener(TRANSLATION_X, Float.valueOf(0.0f));
        for (int i = 0; i < childCount; i++) {
            ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(this.mIconRow.getChildAt(i), (Property<View, Float>) TRANSLATION_X, marginStart);
            objectAnimatorOfFloat.addListener(propertyResetListener);
            animatorSetCreateAnimatorSet.play(objectAnimatorOfFloat);
        }
        animatorSetCreateAnimatorSet.start();
    }

    public void commitNotificationContents() {
        this.mIconRow.removeAllViews();
        for (int i = 0; i < this.mNotifications.size(); i++) {
            addNotificationIconForContent(this.mNotifications.get(i));
        }
        updateOverflowEllipsisVisibility();
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mOverflowEllipsis = findViewById(R.id.overflow);
        this.mIconRow = (LinearLayout) findViewById(R.id.icon_row);
        this.mBackgroundColor = ((ColorDrawable) getBackground()).getColor();
    }

    public void trimNotifications(PopupContainerWithArrow popupContainerWithArrow, List<String> list) {
        if (!isAttachedToWindow() || this.mIconRow.getChildCount() == 0) {
            return;
        }
        Iterator<NotificationContent> it = this.mOverflowNotifications.iterator();
        while (it.hasNext()) {
            if (!list.contains(it.next().notificationKey)) {
                it.remove();
            }
        }
        for (int childCount = this.mIconRow.getChildCount() - 1; childCount >= 0; childCount--) {
            View childAt = this.mIconRow.getChildAt(childCount);
            if (!list.contains(((NotificationContent) childAt.getTag()).notificationKey)) {
                removeViewFromIconRow(popupContainerWithArrow, childAt);
            }
        }
    }
}
